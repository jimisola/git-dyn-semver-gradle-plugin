package io.github.jimisola.gradle.plugins.gitdynsemver

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitDynSemVerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("gitDynSemVer", GitDynSemVerExtension::class.java)

        project.afterEvaluate {
            project.version = project.findProperty("version.force") as String?
                ?: GitDynSemVer.resolveVersion(project.rootDir, extension.toOptions())
        }

        project.tasks.register("printVersion") {
            doLast { println(project.version) }
        }
    }
}
