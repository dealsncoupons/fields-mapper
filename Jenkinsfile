pipeline {
    agent any

    tools {
        java "openjdk-11.28"
        gradle "gradle 6.8.3"
        git "git"
    }

    stages {
        stage('Build') {
            steps {
                // Run Maven on a Unix agent.
                sh "gradle clean build"
            }

            post {
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}
