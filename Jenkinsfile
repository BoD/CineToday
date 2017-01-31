#!groovy

node {
    stage('Checkout'){
        git url: '/Users/bod/gitrepo/CineToday', branch: 'jenkins'
    }

    stage('Build') {
        sh './gradlew lintDebug testDebug'
    }
}