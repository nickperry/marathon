#!/usr/bin/env groovy

ansiColor('gnome-terminal') {
  node('JenkinsMarathonCI-Debian8-2017-04-27') {
    stage("Checkout") {
      checkout scm
      sh "/usr/local/bin/amm scripts/provision.sc all"
    }
    stage("Build and Test") {
     sh "sudo -E sbt ci"
    }
    stage("Package and Publish") {
      sh "sudo /usr/local/bin/amm scripts/package.sc"
    }
  }
}
