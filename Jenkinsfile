#!groovy

node {
    def dockerImage = docker.image('android')
    dockerImage.inside {
        stage('Build') {
            sh './gradlew -Duser.home=. -Pandroid.enableBuildCache=false lintDebug testDebug'
        }
    }
}