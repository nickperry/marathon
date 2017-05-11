#!/usr/bin/env groovy


ansiColor('gnome-terminal') {
  node('JenkinsMarathonCI-Debian8-2017-04-27') {
    stage("Checkout") {
      checkout scm
    }
    stage("Run Pipeline") {
      withCredentials([file(credentialsId: 'DOT_M2_SETTINGS', variable: 'DOT_M2_SETTINGS')]) {
      withEnv(['RUN_DOCKER_INTEGRATION_TESTS=true', 'RUN_MESOS_INTEGRATION_TESTS=true']) {
        sh "sudo -E /usr/local/bin/amm ci/pipeline.sc jenkins"
      }}
    }
  }
}
