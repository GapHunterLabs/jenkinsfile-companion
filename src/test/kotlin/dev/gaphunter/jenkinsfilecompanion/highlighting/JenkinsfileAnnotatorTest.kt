package dev.gaphunter.jenkinsfilecompanion.highlighting

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Real PSI, through the full extension pipeline (file named exactly
 * "Jenkinsfile", resolved to Groovy via the real, registered
 * [dev.gaphunter.jenkinsfilecompanion.detection.JenkinsfileTypeOverrider],
 * then annotated by the real [JenkinsfileAnnotator]) -- not a
 * simplified unit test of the validators alone. Uses
 * `myFixture.doHighlighting()` + filters for this plugin's own WARNING
 * annotations, same discipline as gitlab-ci-companion's own annotator
 * test.
 */
class JenkinsfileAnnotatorTest : BasePlatformTestCase() {

    private fun warnings(): List<String> =
        myFixture.doHighlighting()
            .filter { it.description != null }
            .map { it.description }

    fun testNoWarningsForAWellFormedDeclarativePipeline() {
        myFixture.configureByText(
            "Jenkinsfile",
            """
            pipeline {
                agent any
                stages {
                    stage('Build') {
                        steps {
                            sh 'make build'
                        }
                    }
                    stage('Test') {
                        steps {
                            sh 'make test'
                        }
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(
            "expected no warnings for a well-formed pipeline, got: ${warnings()}",
            warnings().none { it.contains("defined more than once") || it.contains("will do nothing") },
        )
    }

    fun testFlagsADuplicateStageName() {
        myFixture.configureByText(
            "Jenkinsfile",
            """
            pipeline {
                agent any
                stages {
                    stage('Build') {
                        steps {
                            sh 'make build'
                        }
                    }
                    stage('Build') {
                        steps {
                            sh 'make build again'
                        }
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings().any { it.contains("Stage 'Build' is defined more than once") })
    }

    fun testFlagsAStageWithNoStepsOrNestedBlock() {
        myFixture.configureByText(
            "Jenkinsfile",
            """
            pipeline {
                agent any
                stages {
                    stage('Placeholder') {
                        echo "not a steps block, this stage does nothing"
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings().any { it.contains("Stage 'Placeholder' has no steps") })
    }

    fun testDoesNotFlagAStageThatOnlyNestsParallelBranches() {
        myFixture.configureByText(
            "Jenkinsfile",
            """
            pipeline {
                agent any
                stages {
                    stage('Parallel Tests') {
                        parallel {
                            stage('Unit') {
                                steps {
                                    sh 'make unit-test'
                                }
                            }
                            stage('Integration') {
                                steps {
                                    sh 'make integration-test'
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(
            "the outer stage nests parallel{} so it must not be flagged as empty, got: ${warnings()}",
            warnings().none { it.contains("Parallel Tests") },
        )
    }

    fun testDoesNotFlagAStageThatOnlyNestsAMatrixBlock() {
        myFixture.configureByText(
            "Jenkinsfile",
            """
            pipeline {
                agent any
                stages {
                    stage('Build Matrix') {
                        matrix {
                            axes {
                                axis {
                                    name 'PLATFORM'
                                    values 'linux', 'windows'
                                }
                            }
                            stages {
                                stage('Build') {
                                    steps {
                                        sh 'make build'
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(
            "the outer stage nests matrix{} so it must not be flagged as empty, got: ${warnings()}",
            warnings().none { it.contains("Build Matrix") },
        )
    }

    fun testDoesNotAnnotateAGroovyFileThatIsNotNamedJenkinsfile() {
        myFixture.configureByText(
            "Utils.groovy",
            """
            pipeline {
                agent any
                stages {
                    stage('Build') {
                        steps {
                            sh 'make build'
                        }
                    }
                    stage('Build') {
                        steps {
                            sh 'make build again'
                        }
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(
            "a plain .groovy file, even with identical DSL-shaped content, must never be annotated by this plugin",
            warnings().none { it.contains("defined more than once") },
        )
    }
}
