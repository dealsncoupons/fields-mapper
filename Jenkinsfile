pipeline {
    agent any

    stages {
        stage('clean') {
            steps {
                ./gradlew clean
            }
        }

        stage('build') {
            steps {
               ./gradlew build
            }
        }

        stage('deploy') {
            steps {
                echo 'deploying step'
            }
        }
    }
}