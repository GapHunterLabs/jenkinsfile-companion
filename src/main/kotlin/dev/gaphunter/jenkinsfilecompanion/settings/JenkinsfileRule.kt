package dev.gaphunter.jenkinsfilecompanion.settings

enum class JenkinsfileRule(val id: String, val displayName: String) {
    DUPLICATE_STAGE_NAME("duplicateStageName", "Stage name defined more than once"),
    STAGE_MISSING_STEPS("stageMissingSteps", "Stage has no steps/parallel/stages block"),
}
