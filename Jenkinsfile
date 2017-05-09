#!/usr/bin/env groovy

ansiColor('gnome-terminal') {
  node('JenkinsMarathonCI-Debian8-2017-04-27') {
    stage("Checkout") {
      checkout scm
      sh "/usr/local/bin/amm scripts/kill_stale_test_processes.sc"
      sh "/usr/local/bin/amm scripts/install_mesos.sc"
    }
    stage("Build and Test") {
     sh "sudo -E sbt ci"
    }
    stage("Package and Publish") {
     sh """sudo rm -f target/packages/* && sudo sbt clean packageAll"""
    }
  }
}
