package dev.gaphunter.jenkinsfilecompanion.validate

/**
 * Pure data, deliberately decoupled from Groovy PSI -- every validator
 * in this package is unit-testable against these classes directly,
 * with no platform bootstrap. The PSI-walking that BUILDS this model
 * lives in highlighting/JenkinsfileAnnotator.kt, kept as thin as
 * possible. Same shape as gitlab-ci-companion's validate/ package.
 *
 * v1 scope is Declarative Pipeline syntax only (`pipeline { stages {
 * stage('x') { steps { ... } } } }`) -- the dominant, modern Jenkins
 * syntax and what the cited competitor evidence is about. Scripted
 * Pipeline (`node { ... }`) is a documented, deliberate v1 cut.
 */

data class Finding(val message: String, val location: SourceRef)

/** Which PSI element (identified by an opaque key the annotator assigns) a finding should be reported against. */
data class SourceRef(val elementKey: String)

data class StageDef(
    val name: String,
    val hasSteps: Boolean,
    val hasNestedStagesOrParallel: Boolean,
    val location: SourceRef,
)

data class JenkinsfileDocument(
    val stages: List<StageDef>,
)
