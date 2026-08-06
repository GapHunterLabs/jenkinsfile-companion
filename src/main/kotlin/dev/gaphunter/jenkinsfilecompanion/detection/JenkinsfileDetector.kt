package dev.gaphunter.jenkinsfilecompanion.detection

/**
 * Filename/path only, never content-sniffed -- avoids the whole
 * `FileTypeOverrider` content-reading recursion class of bug entirely
 * (see `ansible-companion/KNOWN_ISSUES.md` for the real
 * `StackOverflowError` that content-sniffing caused there via
 * `contentsToByteArray()`). Jenkins itself resolves the pipeline
 * definition file the same way: by exact filename ("Jenkinsfile" by
 * default, case-sensitive, no extension), so this is not a heuristic --
 * it's the real, documented convention.
 */
object JenkinsfileDetector {
    fun isJenkinsfile(fileName: String): Boolean {
        val normalized = fileName.substringAfterLast('/').substringAfterLast('\\')
        return normalized == "Jenkinsfile" || normalized.endsWith(".jenkinsfile")
    }
}
