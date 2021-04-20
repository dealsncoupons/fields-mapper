pipeline {
    agent any

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven "gradle 6.8.3"
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
