package io.github.jimisola.gradle.plugins.gitdynsemver

import org.gradle.api.provider.Property

abstract class GitDynSemVerExtension {
    abstract val includeBuildNumber: Property<Boolean>
    abstract val snapshotSuffix: Property<String>

    internal fun toOptions() = GitDynSemVer.Options(
        includeBuildNumber = includeBuildNumber.getOrElse(true),
        snapshotSuffix = snapshotSuffix.getOrElse("SNAPSHOT"),
    )
}
