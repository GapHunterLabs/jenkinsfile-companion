package dev.gaphunter.jenkinsfilecompanion.detection

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JenkinsfileDetectorTest {

    @Test
    fun `recognizes a file literally named Jenkinsfile`() {
        assertTrue(JenkinsfileDetector.isJenkinsfile("Jenkinsfile"))
        assertTrue(JenkinsfileDetector.isJenkinsfile("some/repo/path/Jenkinsfile"))
    }

    @Test
    fun `recognizes the dot-jenkinsfile extension convention`() {
        assertTrue(JenkinsfileDetector.isJenkinsfile("release.jenkinsfile"))
    }

    @Test
    fun `is case sensitive, matching Jenkins own default convention`() {
        assertFalse(JenkinsfileDetector.isJenkinsfile("jenkinsfile"))
        assertFalse(JenkinsfileDetector.isJenkinsfile("JENKINSFILE"))
    }

    @Test
    fun `does not recognize an unrelated file`() {
        assertFalse(JenkinsfileDetector.isJenkinsfile("build.gradle"))
        assertFalse(JenkinsfileDetector.isJenkinsfile("Dockerfile"))
    }
}
