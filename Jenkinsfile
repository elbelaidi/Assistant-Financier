pipeline {
    agent any
    tools {
        maven 'Maven' 
    }
    stages {
        stage('Checkout') {
    steps {
        git branch: 'main',
            credentialsId: 'github',
            url: 'https://github.com/elbelaidi/Assistant-Financier.git'
    }
}


        stage('Build') {
            steps {
                dir('backend') {
                    bat 'mvn clean compile'
                }
            }
        }
        stage('Test') {
            steps {
                dir('backend') {
                    bat 'mvn test'
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        bat 'mvn sonar:sonar -Dsonar.host.url=http://localhost:9000'
                    }
                }
            }
        }
    }
    post {
        always {
            echo 'Pipeline completed.'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}