package io.github.jimisola.gitdynsemver

import org.gradle.api.provider.Property

abstract class GitDynSemVerExtension {
    abstract val includeBuildNumber: Property<Boolean>
    abstract val snapshotSuffix: Property<String>

    internal fun toOptions() = GitTagDynamicSemVer.Options(
        includeBuildNumber = includeBuildNumber.getOrElse(true),
        snapshotSuffix = snapshotSuffix.getOrElse("SNAPSHOT"),
    )
}
