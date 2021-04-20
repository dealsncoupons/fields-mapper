pipeline {
    agent any

    stages {
        stage('init'){
            steps {
                gradle init
            }
        }

        stage('clean') {
            steps {
                gradle clean
            }
        }

        stage('build') {
            steps {
               gradle build
            }
        }

        stage('deploy') {
            steps {
                echo 'deploying step'
            }
        }
    }
}