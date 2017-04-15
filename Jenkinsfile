#!groovy

node {
    def dockerImage = docker.image('android')
    dockerImage.inside {
        stage('Checkout') {
            git url: '/Users/bod/gitrepo/CineToday', branch: 'jenkins'
        }

        stage('Build') {
            sh './gradlew -Dgradle.user.home=.gradle lintDebug testDebug'
        }
    }
}