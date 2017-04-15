pipeline {
  agent {
    label 'java'
  }

  stages {
    stage('Build') {
      environment {
        ANDROID_HOME = '/android-sdk'
      }
      steps {
        sh './gradlew lintDebug testDebug'
      }
    }
  }
}
