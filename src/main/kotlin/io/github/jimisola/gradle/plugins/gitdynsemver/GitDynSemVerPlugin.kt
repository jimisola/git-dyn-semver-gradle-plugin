package io.github.jimisola.gradle.plugins.gitdynsemver

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitDynSemVerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("gitDynSemVer", GitDynSemVerExtension::class.java).apply {
            includeBuildNumber.convention(true)
            snapshotSuffix.convention("SNAPSHOT")
        }

        project.afterEvaluate {
            project.version = project.findProperty("version.force") as String?
                ?: project.providers.of(GitVersionValueSource::class.java) {
                    parameters.projectDir.set(project.rootDir)
                    parameters.includeBuildNumber.set(extension.includeBuildNumber)
                    parameters.snapshotSuffix.set(extension.snapshotSuffix)
                }.get()
        }

        project.tasks.register("printVersion") {
            doLast { println(project.version) }
        }
    }
}
