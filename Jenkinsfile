#!groovy

node {
    stage('Checkout'){
        checkout(
            [$class: 'GitSCM', branches: [[name: 'jenkins']],
            doGenerateSubmoduleConfigurations: false,
            extensions: [],
            submoduleCfg: [],
            userRemoteConfigs: [[url: '/Users/bod/gitrepo/CineToday']]]
        )
    }

    stage('Build') {
        sh '/bin/ls -al'
    }
}