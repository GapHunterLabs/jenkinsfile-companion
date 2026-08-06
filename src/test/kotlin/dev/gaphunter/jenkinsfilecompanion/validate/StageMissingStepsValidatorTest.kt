package dev.gaphunter.jenkinsfilecompanion.validate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StageMissingStepsValidatorTest {

    @Test
    fun `no finding for a stage with a steps block`() {
        val doc = JenkinsfileDocument(listOf(stage("Build", hasSteps = true)))
        assertTrue(StageMissingStepsValidator.validate(doc).isEmpty())
    }

    @Test
    fun `no finding for a stage that only nests stages or parallel`() {
        val doc = JenkinsfileDocument(listOf(stage("Build", hasSteps = false, hasNestedStagesOrParallel = true)))
        assertTrue(StageMissingStepsValidator.validate(doc).isEmpty())
    }

    @Test
    fun `finding for a stage with neither steps nor nested stages or parallel`() {
        val target = stage("Build", hasSteps = false, hasNestedStagesOrParallel = false)
        val doc = JenkinsfileDocument(listOf(target))
        val findings = StageMissingStepsValidator.validate(doc)
        assertEquals(1, findings.size)
        assertEquals(target.location, findings.first().location)
    }
}
