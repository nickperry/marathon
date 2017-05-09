#!/usr/bin/env amm

import ammonite.ops._
import ammonite.ops.ImplicitWd._

/**
 * Build clean Marathon packages.
 */
@main
def main(): Unit = {
  rm! pwd/'target/'packages

  %('sbt, 'clean, 'packageAll)
}
