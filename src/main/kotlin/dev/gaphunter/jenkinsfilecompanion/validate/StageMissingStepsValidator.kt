package dev.gaphunter.jenkinsfilecompanion.validate

/**
 * A stage with neither a `steps {}` block nor a nested `stages {}`/
 * `parallel {}` block does nothing at all when the pipeline runs --
 * a real, silent no-op that's easy to miss by eye (Jenkins itself
 * won't warn about it either).
 */
object StageMissingStepsValidator {
    fun validate(doc: JenkinsfileDocument): List<Finding> =
        doc.stages
            .filterNot { it.hasSteps || it.hasNestedStagesOrParallel }
            .map { stage ->
                Finding("Stage '${stage.name}' has no steps/parallel/stages block -- it will do nothing when this pipeline runs.", stage.location)
            }
}
