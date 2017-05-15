#!/usr/bin/env amm

import ammonite.ops._
import ammonite.ops.ImplicitWd._

/**
 * Report success of diff build back to Phabricator.
 *
 * @param diffId The differential ID of the build.
 * @param revisionId The identifier for the Phabricator revision that was build.
 * @param buildUrl A link back to the build on Jenkins.
 */
@main
def reportSuccess(diffId: String, revisionId: String, buildUrl: String): Unit = {
    //phabricator("differential.revision.edit", """ transactions: [{type: "accept", value: true}, {type: "comment", value: "\u2714 Build of $DIFF_ID completed at $BUILD_URL"}], objectIdentifier: "D$REVISION_ID" """)
}
