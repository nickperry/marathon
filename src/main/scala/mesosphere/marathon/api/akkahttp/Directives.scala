package mesosphere.marathon
package api.akkahttp
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.PathMatcher.Matching
import akka.http.scaladsl.server.PathMatcher1
import akka.http.scaladsl.server.{ Directive1, Directives => AkkaDirectives, Rejection }
import com.wix.accord.{ Success, Failure, Validator }
import mesosphere.marathon.state.{ Group, PathId, RootGroup }
import scala.annotation.tailrec

/**
  * All Marathon Directives and Akka Directives
  *
  * These should be imported by the respective controllers
  */
object Directives extends AuthDirectives with LeaderDirectives with AkkaDirectives {

  /**
    * Matches the rest of the path segment as a PathId; ignores trailing slash, consumes everything.
    */
  object RemainingPathId extends PathMatcher1[PathId] {
    import akka.http.scaladsl.server.PathMatcher._

    @tailrec final def iter(reversePieces: List[String], remaining: Path): Matching[Tuple1[PathId]] = remaining match {
      case Path.Slash(rest) =>
        iter(reversePieces, rest)
      case Path.Empty =>
        if (reversePieces.nonEmpty)
          Matched(Path.Empty, Tuple1(PathId.sanitized(reversePieces.reverse, true)))
        else
          Unmatched
      case Path.Segment(segment, rest) =>
        iter(segment :: reversePieces, rest)
    }

    override def apply(path: Path) = iter(Nil, path)
  }

  /**
    * Given the current root group, only match and consume an existing appId
    *
    * This is useful because our v2 API has an unfortunate design decision which leads to ambiguity in our URLs, such as:
    *
    *   POST /v2/apps/my-group/restart/restart
    *
    * The intention here is to restart the app named "my-group/restart"
    *
    * This matcher will only consume "my-group/restart" from the path, leaving the rest of the matcher to match the rest
    */
  case class ExistingAppPathId(rootGroup: RootGroup) extends PathMatcher1[PathId] {
    import akka.http.scaladsl.server.PathMatcher._

    @tailrec final def iter(reversePieces: List[String], remaining: Path, group: Group): Matching[Tuple1[PathId]] = remaining match {
      case Path.Slash(rest) =>
        iter(reversePieces, rest, group)
      case Path.Segment(segment, rest) =>
        val appended = (segment :: reversePieces)
        val pathId = PathId.sanitized(appended.reverse, true)
        if (group.groupsById.contains(pathId)) {
          iter(appended, rest, group.groupsById(pathId))
        } else if (group.apps.contains(pathId)) {
          Matched(rest, Tuple1(pathId))
        } else {
          Unmatched
        }
      case _ =>
        Unmatched
    }

    override def apply(path: Path) = iter(Nil, path, rootGroup)
  }

  /**
    * Validate the given resource using the implicit validator in scope; reject if invalid
    *
    * Ideally, we validate while unmarshalling; however, in the case of app updates, we need to apply validation after
    * applying some update operation.
    */
  def validated[T](resource: T)(implicit validator: Validator[T]): Directive1[T] = {
    validator(resource) match {
      case Success => provide(resource)
      case failure: Failure =>
        reject(EntityMarshallers.ValidationFailed(failure))
    }
  }

  def rejectingLeft[T](result: Directive1[Either[Rejection, T]]): Directive1[T] =
    result.flatMap {
      case Left(rej) => reject(rej)
      case Right(t) => provide(t)
    }
}
