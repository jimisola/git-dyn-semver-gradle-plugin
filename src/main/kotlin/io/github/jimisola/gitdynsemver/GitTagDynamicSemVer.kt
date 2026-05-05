package io.github.jimisola.gitdynsemver

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

object GitTagDynamicSemVer {

    data class Options(
        val includeBuildNumber: Boolean = true,
        val snapshotSuffix: String = "SNAPSHOT",
    )

    private val MAJOR_REGEX = Regex("""(^\w+(\([^()]+\))?!:|^BREAKING[ -]CHANGE:)""", RegexOption.MULTILINE)
    private val MINOR_REGEX = Regex("""^feat!?(\([^()]+\))?:""")
    private val SEMVER_TAG_REGEX = Regex("""^(\d+)\.(\d+)\.(\d+)$""")

    fun resolveVersion(projectDir: File, options: Options = Options()): String {
        val gitDir = generateSequence(projectDir) { it.parentFile }
            .map { File(it, ".git") }
            .first { it.exists() }

        val repo = FileRepositoryBuilder()
            .setGitDir(gitDir)
            .build()

        return repo.use { calculateVersion(it, options) }
    }

    private fun calculateVersion(repo: Repository, options: Options): String {
        val described = Git(repo).describe()
            .setLong(true)
            .setTags(true)
            .setMatch("[0-9]*.[0-9]*.[0-9]*")
            .call()
            ?: return snapshot("0.1.0", countAllCommits(repo), options)

        // format: "1.2.3-5-gabcdef"
        val parts = described.split("-")
        val distance = parts[parts.size - 2].toIntOrNull() ?: 0
        val tag = parts.dropLast(2).joinToString("-")

        if (distance == 0) return tag

        val match = SEMVER_TAG_REGEX.matchEntire(tag) ?: return snapshot("0.1.0", distance, options)
        val major = match.groupValues[1].toInt()
        val minor = match.groupValues[2].toInt()
        val patch = match.groupValues[3].toInt()

        val tagObjectId = repo.resolve(tag) ?: return snapshot("$major.$minor.${patch + 1}", distance, options)
        val commits = commitsSinceTag(repo, tagObjectId)
        val bump = highestBump(commits, major)

        val base = when (bump) {
            Bump.MAJOR -> "${major + 1}.0.0"
            Bump.MINOR -> "$major.${minor + 1}.0"
            Bump.PATCH -> "$major.$minor.${patch + 1}"
        }
        return snapshot(base, distance, options)
    }

    private fun snapshot(base: String, distance: Int, options: Options): String {
        val build = if (options.includeBuildNumber) "-$distance" else ""
        return "$base$build-${options.snapshotSuffix}"
    }

    private enum class Bump { MAJOR, MINOR, PATCH }

    private fun highestBump(commits: List<String>, major: Int): Bump {
        var bump = Bump.PATCH
        for (message in commits) {
            when {
                major > 0 && MAJOR_REGEX.containsMatchIn(message) -> return Bump.MAJOR
                MINOR_REGEX.containsMatchIn(message) -> bump = Bump.MINOR
            }
        }
        return bump
    }

    private fun countAllCommits(repo: Repository): Int {
        val walk = RevWalk(repo)
        try {
            walk.markStart(walk.parseCommit(repo.resolve("HEAD")))
            return walk.count()
        } finally {
            walk.dispose()
        }
    }

    private fun commitsSinceTag(repo: Repository, tagObjectId: ObjectId): List<String> {
        val walk = RevWalk(repo)
        walk.revFilter = RevFilter.NO_MERGES
        try {
            walk.markStart(walk.parseCommit(repo.resolve("HEAD")))
            val tagCommit = walk.parseCommit(tagObjectId)
            val messages = mutableListOf<String>()
            for (commit: RevCommit in walk) {
                if (commit.id == tagCommit.id) break
                messages.add(commit.fullMessage)
            }
            return messages
        } finally {
            walk.dispose()
        }
    }
}
