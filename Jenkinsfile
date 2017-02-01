#!groovy

node {
    def dockerImage = docker.image('jenkins-1')
    dockerImage.inside {
        stage('Checkout') {
            git url: '/Users/bod/gitrepo/CineToday', branch: 'jenkins'
        }

        stage('Build') {
            withEnv(['ANDROID_HOME=/Users/bod/Dev/android-sdk-macosx']) {
                sh './gradlew lintDebug testDebug'
            }
        }
    }
}