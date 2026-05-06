package io.github.jimisola.gradle.plugins.gitdynsemver

import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals

class GitDynSemVerTest {

    @TempDir
    lateinit var projectDir: File

    private lateinit var git: Git

    @BeforeEach
    fun setUp() {
        git = Git.init().setDirectory(projectDir).call()
        git.repository.config.apply {
            setString("user", null, "email", "test@example.com")
            setString("user", null, "name", "Test")
            save()
        }
        gitCommit(git, "chore: init")
    }

    data class Scenario(
        val name: String,
        val setup: (Git) -> Unit,
        val expectedVersion: String,
        val options: GitDynSemVer.Options = GitDynSemVer.Options(),
    ) {
        override fun toString() = name
    }

    companion object {
        @JvmStatic
        fun scenarios(): Stream<Scenario> = Stream.of(
            Scenario(
                name = "no-tags",
                setup = {},
                expectedVersion = "0.1.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "no-tags-multiple-commits",
                setup = { git ->
                    gitCommit(git, "chore: second")
                    gitCommit(git, "chore: third")
                },
                expectedVersion = "0.1.0-3-SNAPSHOT",
            ),
            Scenario(
                name = "at-lightweight-tag",
                setup = { git -> gitTag(git, "1.2.3") },
                expectedVersion = "1.2.3",
            ),
            Scenario(
                name = "at-annotated-tag",
                setup = { git -> gitAnnotatedTag(git, "2.0.0", "Release 2.0.0") },
                expectedVersion = "2.0.0",
            ),
            Scenario(
                name = "patch-bump-fix-commit",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "fix: correct null check")
                },
                expectedVersion = "1.2.4-1-SNAPSHOT",
            ),
            Scenario(
                name = "minor-bump-feat-commit",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "feat: add search")
                },
                expectedVersion = "1.3.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "minor-bump-highest-wins-over-patch",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "fix: small fix")
                    gitCommit(git, "feat: add search")
                    gitCommit(git, "fix: another fix")
                },
                expectedVersion = "1.3.0-3-SNAPSHOT",
            ),
            Scenario(
                name = "major-bump-breaking-exclamation",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "feat!: remove legacy endpoint")
                },
                expectedVersion = "2.0.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "major-bump-breaking-change-footer",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "refactor: restructure API\n\nBREAKING CHANGE: changed response format")
                },
                expectedVersion = "2.0.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "major-bump-highest-wins-over-minor",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "feat: add search")
                    gitCommit(git, "feat!: remove legacy endpoint")
                },
                expectedVersion = "2.0.0-2-SNAPSHOT",
            ),
            Scenario(
                name = "major-zero-feat-breaking-bumps-minor",
                setup = { git ->
                    gitTag(git, "0.1.0")
                    gitCommit(git, "feat!: remove legacy endpoint")
                },
                expectedVersion = "0.2.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "major-zero-breaking-footer-stays-patch",
                setup = { git ->
                    gitTag(git, "0.1.0")
                    gitCommit(git, "fix: bug\n\nBREAKING CHANGE: changed response format")
                },
                expectedVersion = "0.1.1-1-SNAPSHOT",
            ),
            Scenario(
                name = "commits-past-annotated-tag",
                setup = { git ->
                    gitAnnotatedTag(git, "2.0.0", "Release 2.0.0")
                    gitCommit(git, "fix: patch after annotated tag")
                },
                expectedVersion = "2.0.1-1-SNAPSHOT",
            ),
            Scenario(
                name = "no-build-number",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "feat: add search")
                },
                options = GitDynSemVer.Options(includeBuildNumber = false),
                expectedVersion = "1.3.0-SNAPSHOT",
            ),
            Scenario(
                name = "custom-snapshot-suffix",
                setup = { git ->
                    gitTag(git, "1.2.3")
                    gitCommit(git, "fix: patch")
                },
                options = GitDynSemVer.Options(snapshotSuffix = "dev"),
                expectedVersion = "1.2.4-1-dev",
            ),
        )

        private fun gitCommit(git: Git, message: String) {
            git.commit().setMessage(message).setAllowEmpty(true).call()
        }

        private fun gitTag(git: Git, name: String) {
            git.tag().setName(name).setAnnotated(false).call()
        }

        private fun gitAnnotatedTag(git: Git, name: String, message: String) {
            git.tag().setName(name).setAnnotated(true).setMessage(message).call()
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    fun version(scenario: Scenario) {
        scenario.setup(git)
        assertEquals(scenario.expectedVersion, GitDynSemVer.resolveVersion(projectDir, scenario.options))
    }
}
