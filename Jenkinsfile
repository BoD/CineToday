#!groovy

node {
    def dockerImage = docker.image('android')
    dockerImage.inside {
        stage('Build') {
            sh './gradlew -Duser.home=. -Dorg.gradle.parallel=true -Dorg.gradle.daemon=false -Pandroid.enableBuildCache=false lintDebug testDebug'
        }
    }
}