package dev.gaphunter.jenkinsfilecompanion.detection

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.groovy.GroovyFileType

/**
 * A file named exactly "Jenkinsfile" has no extension, so the bundled
 * Groovy plugin never associates it by extension on its own -- this
 * override is what makes Groovy syntax highlighting/PSI/completion
 * apply to it at all, the same real gap the cited paid competitor
 * fills (and the actual connection point for every complaint this
 * plugin answers: without Groovy PSI, there is no highlighting, no
 * navigation, nothing to build structural checks on top of).
 *
 * Filename-only check (no content read), so this never touches the
 * `contentsToByteArray()` re-entrancy trap documented in
 * `ansible-companion/KNOWN_ISSUES.md` -- there is nothing to sniff.
 */
class JenkinsfileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? =
        if (JenkinsfileDetector.isJenkinsfile(file.name)) GroovyFileType.GROOVY_FILE_TYPE else null
}
