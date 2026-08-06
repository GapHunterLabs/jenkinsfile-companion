package dev.gaphunter.jenkinsfilecompanion.validate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateStageNameValidatorTest {

    @Test
    fun `no finding when all stage names are unique`() {
        val doc = JenkinsfileDocument(listOf(stage("Build"), stage("Test"), stage("Deploy")))
        assertTrue(DuplicateStageNameValidator.validate(doc).isEmpty())
    }

    @Test
    fun `finding for the second occurrence of a duplicated stage name, not the first`() {
        val first = stage("Build")
        val second = stage("Build")
        val doc = JenkinsfileDocument(listOf(first, second))
        val findings = DuplicateStageNameValidator.validate(doc)
        assertEquals(1, findings.size)
        assertEquals(second.location, findings.first().location)
    }

    @Test
    fun `three occurrences of the same name produce two findings`() {
        val doc = JenkinsfileDocument(listOf(stage("Build"), stage("Build"), stage("Build")))
        assertEquals(2, DuplicateStageNameValidator.validate(doc).size)
    }
}
