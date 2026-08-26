import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2025.2.6.2")

        // A Jenkinsfile is Groovy code with a specific DSL (pipeline {}/
        // stage(){}/steps{} trailing-closure calls) -- built entirely on
        // top of the bundled Groovy plugin's real PSI
        // (org.jetbrains.plugins.groovy.lang.psi.api.*), confirmed real via
        // javap against the actual bundled Groovy.jar, not assumed. Real
        // plugin id confirmed the same way (extracted its own plugin.xml):
        // "org.intellij.groovy", not "com.intellij.groovy" or similar guess.
        bundledPlugin("org.intellij.groovy")

        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // 243 = 2024.3, so as not to exclude the real installed base.
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }

    // Same tooling bug as every other Gap Hunter Labs plugin (Gradle 9.5 +
    // IntelliJ Platform Gradle Plugin 2.16 + IDE 2025.2.6.2): the
    // bytecode instrumenter fails with "instrumentIdeaExtensions
    // doesn't support the nested element". Not required for
    // build/test/verifyPlugin.
    instrumentCode = false

    // Catch experimental/internal API usage locally, before Marketplace's
    // own verifier flags it post-upload. Never relax this list without a
    // documented exception (see AUTOMATION_PLAYBOOK.md SS1.5).
    //
    // EXPERIMENTAL_API_USAGES is deliberately NOT in this list, same
    // documented exception as ansible-companion: JenkinsfileTypeOverrider
    // implements com.intellij.openapi.fileTypes.impl.FileTypeOverrider,
    // which the platform itself marks @ApiStatus.Experimental. That is
    // the only real hook that lets a plugin change a file's FileType by
    // filename alone, ahead of content-sniffing detectors -- required
    // here because "Jenkinsfile" carries no extension for FileType
    // association to key off of otherwise. (Contrast with cmake-companion/
    // nginx-companion's own "*FileTypeOverrider"-named classes: despite
    // the name, those implement the stable FileTypeRegistry.FileTypeDetector
    // instead, so they don't need this exception.)
    pluginVerification {
        failureLevel = listOf(
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS,
            VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES,
            VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES,
            VerifyPluginTask.FailureLevel.SCHEDULED_FOR_REMOVAL_API_USAGES,
        )
    }

    // Publish token read from a LOCAL, non-repo Gradle property
    // (~/.gradle/gradle.properties, never committed) -- never hardcoded
    // here. Falls back to null (task fails loudly asking for the token)
    // if that file doesn't define it, rather than silently no-op-ing.
    publishing {
        token.set(providers.gradleProperty("gapHunterLabs.marketplace.token"))
    }

    // Same pattern: signing material lives only in the local, non-repo
    // gradle.properties (self-signed cert generated once for the whole
    // catalog, 10-year validity).
    signing {
        certificateChain.set(providers.gradleProperty("gapHunterLabs.marketplace.certificateChain"))
        privateKey.set(providers.gradleProperty("gapHunterLabs.marketplace.privateKey"))
        password.set(providers.gradleProperty("gapHunterLabs.marketplace.privateKeyPassword"))
    }
}
