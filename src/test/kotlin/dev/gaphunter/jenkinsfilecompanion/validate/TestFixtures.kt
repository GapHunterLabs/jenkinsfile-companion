package dev.gaphunter.jenkinsfilecompanion.validate

fun stage(
    name: String,
    hasSteps: Boolean = true,
    hasNestedStagesOrParallel: Boolean = false,
) = StageDef(name, hasSteps, hasNestedStagesOrParallel, SourceRef(name))
