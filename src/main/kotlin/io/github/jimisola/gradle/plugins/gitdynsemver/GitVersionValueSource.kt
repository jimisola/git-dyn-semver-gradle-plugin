package io.github.jimisola.gradle.plugins.gitdynsemver

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.File

abstract class GitVersionValueSource : ValueSource<String, GitVersionValueSource.Params> {
    interface Params : ValueSourceParameters {
        val projectDir: Property<File>
        val includeBuildNumber: Property<Boolean>
        val snapshotSuffix: Property<String>
    }

    override fun obtain(): String = GitDynSemVer.resolveVersion(
        parameters.projectDir.get(),
        GitDynSemVer.Options(
            includeBuildNumber = parameters.includeBuildNumber.get(),
            snapshotSuffix = parameters.snapshotSuffix.get(),
        )
    )
}
