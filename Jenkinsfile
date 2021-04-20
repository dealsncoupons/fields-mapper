pipeline {
    agent any

    tools {
        gradle "gradle 6.8.3"
        git "git"
    }

    stages {
        stage('init') {
            steps{
                script {
                    env.JAVA_HOME="${tool 'openjdk-11.28'}"
                    env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
                }
            }
        }

        stage('Build') {
            steps {
                sh "echo ${env.JAVA_HOME}"
                sh "gradle clean build"
            }

            post {
                success {
//                     junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}
