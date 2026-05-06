package io.github.jimisola.gradle.plugins.gitdynsemver

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitDynSemVerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("gitDynSemVer", GitDynSemVerExtension::class.java).apply {
            includeBuildNumber.convention(true)
            snapshotSuffix.convention("SNAPSHOT")
        }

        val versionProvider = project.providers.gradleProperty("version.force")
            .orElse(
                project.providers.of(GitVersionValueSource::class.java) {
                    parameters.projectDir.set(project.rootDir)
                    parameters.includeBuildNumber.set(extension.includeBuildNumber)
                    parameters.snapshotSuffix.set(extension.snapshotSuffix)
                }
            )

        project.afterEvaluate {
            project.version = versionProvider.get()
        }

        project.tasks.register("printVersion") {
            doLast { println(versionProvider.get()) }
        }
    }
}
