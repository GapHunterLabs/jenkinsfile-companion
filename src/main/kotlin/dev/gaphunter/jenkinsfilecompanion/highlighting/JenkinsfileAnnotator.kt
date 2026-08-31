package dev.gaphunter.jenkinsfilecompanion.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.jenkinsfilecompanion.detection.JenkinsfileDetector
import dev.gaphunter.jenkinsfilecompanion.settings.JenkinsfileCompanionSettings
import dev.gaphunter.jenkinsfilecompanion.settings.JenkinsfileRule
import dev.gaphunter.jenkinsfilecompanion.validate.DuplicateStageNameValidator
import dev.gaphunter.jenkinsfilecompanion.validate.Finding
import dev.gaphunter.jenkinsfilecompanion.validate.JenkinsfileDocument
import dev.gaphunter.jenkinsfilecompanion.validate.SourceRef
import dev.gaphunter.jenkinsfilecompanion.validate.StageDef
import dev.gaphunter.jenkinsfilecompanion.validate.StageMissingStepsValidator
import dev.gaphunter.jenkinsfilecompanion.review.ReviewPrompt
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral

private const val STAGE_KEYWORD = "stage"
private const val STEPS_KEYWORD = "steps"
private const val STAGES_KEYWORD = "stages"
private const val PARALLEL_KEYWORD = "parallel"

// Declarative Pipeline's matrix directive -- a stage containing only a
// `matrix { axes {...} stages {...} }` block does real work (runs its
// nested stages once per axis combination) with no direct steps/stages/
// parallel block of its own, so it must count the same way those do.
private const val MATRIX_KEYWORD = "matrix"

/**
 * Fires once per file (on the file's root PSI element), gated by
 * [JenkinsfileDetector] -- never by FileType identity alone (same
 * discipline as gitlab-ci-companion's own annotator: a Jenkinsfile IS
 * a Groovy file after [dev.gaphunter.jenkinsfilecompanion.detection.JenkinsfileTypeOverrider],
 * but not every Groovy file is a Jenkinsfile). Deliberately thin: this
 * class only walks PSI to build a [JenkinsfileDocument] and remembers
 * which PsiElement each [SourceRef] points to; every actual rule lives
 * in validate/, unit-testable without PSI.
 *
 * Finds every `stage(...)` call anywhere in the file rather than
 * assuming one fixed nesting path (`pipeline > stages > stage`) --
 * real Declarative Pipelines can nest stages inside `parallel {}`
 * blocks, and assuming a single rigid shape would miss those or
 * misreport them.
 */
class JenkinsfileAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val file = element.containingFile as? GroovyFile ?: return
        if (element !== file) return // fire exactly once, on the file root
        if (!JenkinsfileDetector.isJenkinsfile(file.virtualFile?.name ?: return)) return

        val settings = JenkinsfileCompanionSettings.getInstance()
        val elementsByKey = mutableMapOf<String, PsiElement>()
        val doc = buildDocument(file, elementsByKey)

        val findings = mutableListOf<Finding>()
        if (settings.isEnabled(JenkinsfileRule.DUPLICATE_STAGE_NAME)) findings += DuplicateStageNameValidator.validate(doc)
        if (settings.isEnabled(JenkinsfileRule.STAGE_MISSING_STEPS)) findings += StageMissingStepsValidator.validate(doc)

        for (finding in findings) {
            val target = elementsByKey[finding.location.elementKey] ?: continue
            holder.newAnnotation(HighlightSeverity.WARNING, finding.message).range(target.textRange).create()
            val lineNumber = file.viewProvider.document?.getLineNumber(target.textRange.startOffset)?.plus(1) ?: 0
            ReviewPrompt.recordHit(file.project, "${file.virtualFile?.path}:$lineNumber:${finding.message}")
        }
    }

    private fun buildDocument(file: GroovyFile, elementsByKey: MutableMap<String, PsiElement>): JenkinsfileDocument {
        val stageCalls = PsiTreeUtil.findChildrenOfType(file, GrMethodCall::class.java)
            .filter { methodName(it) == STAGE_KEYWORD }

        val stages = stageCalls.mapNotNull { call -> buildStage(call, elementsByKey) }
        return JenkinsfileDocument(stages)
    }

    private fun buildStage(call: GrMethodCall, elementsByKey: MutableMap<String, PsiElement>): StageDef? {
        val nameLiteral = call.expressionArguments.filterIsInstance<GrLiteral>().firstOrNull { it.isString() }
            ?: return null
        val name = nameLiteral.value as? String ?: return null

        val body = call.closureArguments.firstOrNull() ?: return null

        // Only direct/shallow children matter here -- a steps{} block nested
        // inside a DIFFERENT stage further down must never count towards
        // THIS stage. bodyOwnerOf walks up from each candidate call to the
        // nearest enclosing stage(...) call and only keeps it if that's us.
        val ownStatements = PsiTreeUtil.findChildrenOfType(body, GrMethodCall::class.java)
            .filter { nearestEnclosingStage(it) === call }
        val hasSteps = ownStatements.any { methodName(it) == STEPS_KEYWORD }
        val hasNestedStagesOrParallel = ownStatements.any { methodName(it) in setOf(STAGES_KEYWORD, PARALLEL_KEYWORD, MATRIX_KEYWORD) }

        val key = remember(elementsByKey, nameLiteral)
        return StageDef(name, hasSteps, hasNestedStagesOrParallel, SourceRef(key))
    }

    /** Walks up from [call] to the nearest enclosing `stage(...)` call, or null if there isn't one. */
    private fun nearestEnclosingStage(call: GrMethodCall): GrMethodCall? {
        var current: PsiElement? = call.parent
        while (current != null) {
            if (current is GrMethodCall && methodName(current) == STAGE_KEYWORD) return current
            current = current.parent
        }
        return null
    }

    private fun methodName(call: GrCall): String? =
        (call as? GrMethodCall)?.invokedExpression?.let { it as? GrReferenceExpression }?.referenceName

    private var keyCounter = 0
    private fun remember(map: MutableMap<String, PsiElement>, element: PsiElement): String {
        val key = "k${keyCounter++}"
        map[key] = element
        return key
    }
}
