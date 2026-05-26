/**
 * Jenkinsfile – Pipeline CI complète (Windows)
 * Projet : Boutique en ligne – ICDE848
 */

pipeline {

    agent any

    tools {
        maven 'Maven3'
        jdk   'JDK17'
    }

    parameters {
        string(
            name:         'BRANCH',
            defaultValue: 'master',
            description:  'Branche Git à builder'
        )
        choice(
            name:    'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Environnement de déploiement cible'
        )
        booleanParam(
            name:         'SKIP_TESTS',
            defaultValue: false,
            description:  'Ignorer les tests (urgence uniquement !)'
        )
    }

    stages {

        // Stage 1 : Récupérer le code
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch  : ${env.GIT_BRANCH}"
                echo "Commit  : ${env.GIT_COMMIT}"
            }
        }

        // Stage 2 : Compiler
        stage('Build') {
            steps {
                bat 'mvn clean compile -B'
            }
        }

        // Stage 3 : Tests unitaires
        stage('Tests unitaires') {
            when {
                not { expression { return params.SKIP_TESTS } }
            }
            steps {
                bat 'mvn test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo 'Tests unitaires en ECHEC — vérifier les logs ci-dessus'
                }
            }
        }

        // Stage 4 : Tests d'intégration
        stage('Tests integration') {
            when {
                not { expression { return params.SKIP_TESTS } }
            }
            steps {
                bat 'mvn verify -Dsurefire.skip=true -B'
            }
            post {
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }

        // Stage 5 : Couverture de code
        stage('Couverture JaCoCo') {
            steps {
                bat 'mvn jacoco:report -B'
            }
            post {
                always {
                    jacoco(
                        execPattern:   '**/target/jacoco.exec',
                        classPattern:  '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        minimumLineCoverage: '70'
                    )
                }
            }
        }

        // Stage 6 : Analyse qualité
        stage('Qualite') {
            steps {
                bat 'mvn checkstyle:checkstyle pmd:pmd pmd:cpd spotbugs:spotbugs -B'
            }
            post {
                always {
                    recordIssues(
                        enabledForFailure: true,
                        tools: [
                            checkStyle(pattern: '**/checkstyle-result.xml'),
                            pmdParser(pattern:  '**/pmd.xml'),
                            cpd(pattern:        '**/cpd.xml'),
                            spotBugs(pattern:   '**/spotbugsXml.xml')
                        ],
                        qualityGates: [[
                            threshold: 10,
                            type: 'TOTAL',
                            unstable: true
                        ]]
                    )
                }
            }
        }

        // Stage 7 : Archiver le JAR
        stage('Archive') {
            steps {
                archiveArtifacts(
                    artifacts:   '**/target/*.jar',
                    fingerprint: true,
                    allowEmptyArchive: true
                )
                echo "Artefact archivé avec succès"
            }
        }

    }

    post {
        always {
            echo "Pipeline terminée — statut : ${currentBuild.currentResult}"
        }
    }

}
