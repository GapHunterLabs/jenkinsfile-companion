package dev.gaphunter.jenkinsfilecompanion.validate

/** A duplicate stage name is confusing at best (which run in the Jenkins UI is "Build"?) and a real, easy-to-miss copy-paste mistake in a long pipeline. */
object DuplicateStageNameValidator {
    fun validate(doc: JenkinsfileDocument): List<Finding> =
        doc.stages.groupBy { it.name }
            .filterValues { it.size > 1 }
            .flatMap { (name, stages) ->
                // Report on every occurrence after the first, at each occurrence's own location.
                stages.drop(1).map { stage ->
                    Finding("Stage '$name' is defined more than once.", stage.location)
                }
            }
}
