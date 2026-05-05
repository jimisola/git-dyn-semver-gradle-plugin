package io.github.jimisola.gitdynsemver

import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream
import kotlin.test.assertEquals

class GitTagDynamicSemVerTest {

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
        commit("chore: init")
    }

    data class Scenario(
        val name: String,
        val setup: Git.() -> Unit,
        val expectedVersion: String,
        val options: GitTagDynamicSemVer.Options = GitTagDynamicSemVer.Options(),
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
                setup = {
                    commit("chore: second")
                    commit("chore: third")
                },
                expectedVersion = "0.1.0-3-SNAPSHOT",
            ),
            Scenario(
                name = "at-tag-exact",
                setup = { tag("1.2.3") },
                expectedVersion = "1.2.3",
            ),
            Scenario(
                name = "patch-bump-fix-commit",
                setup = {
                    tag("1.2.3")
                    commit("fix: correct null check")
                },
                expectedVersion = "1.2.4-1-SNAPSHOT",
            ),
            Scenario(
                name = "minor-bump-feat-commit",
                setup = {
                    tag("1.2.3")
                    commit("feat: add search")
                },
                expectedVersion = "1.3.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "minor-bump-highest-wins-over-patch",
                setup = {
                    tag("1.2.3")
                    commit("fix: small fix")
                    commit("feat: add search")
                    commit("fix: another fix")
                },
                expectedVersion = "1.3.0-3-SNAPSHOT",
            ),
            Scenario(
                name = "major-bump-breaking-exclamation",
                setup = {
                    tag("1.2.3")
                    commit("feat!: remove legacy endpoint")
                },
                expectedVersion = "2.0.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "major-bump-breaking-change-footer",
                setup = {
                    tag("1.2.3")
                    commit("refactor: restructure API\n\nBREAKING CHANGE: changed response format")
                },
                expectedVersion = "2.0.0-1-SNAPSHOT",
            ),
            Scenario(
                name = "major-bump-highest-wins-over-minor",
                setup = {
                    tag("1.2.3")
                    commit("feat: add search")
                    commit("feat!: remove legacy endpoint")
                },
                expectedVersion = "2.0.0-2-SNAPSHOT",
            ),
            Scenario(
                name = "no-build-number",
                setup = {
                    tag("1.2.3")
                    commit("feat: add search")
                },
                options = GitTagDynamicSemVer.Options(includeBuildNumber = false),
                expectedVersion = "1.3.0-SNAPSHOT",
            ),
            Scenario(
                name = "custom-snapshot-suffix",
                setup = {
                    tag("1.2.3")
                    commit("fix: patch")
                },
                options = GitTagDynamicSemVer.Options(snapshotSuffix = "dev"),
                expectedVersion = "1.2.4-1-dev",
            ),
            Scenario(
                name = "at-annotated-tag",
                setup = { annotatedTag("2.0.0", "Release 2.0.0") },
                expectedVersion = "2.0.0",
            ),
            Scenario(
                name = "commits-past-annotated-tag",
                setup = {
                    annotatedTag("2.0.0", "Release 2.0.0")
                    commit("fix: patch after annotated tag")
                },
                expectedVersion = "2.0.1-1-SNAPSHOT",
            ),
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    fun version(scenario: Scenario) {
        scenario.setup(git)
        assertEquals(scenario.expectedVersion, GitTagDynamicSemVer.resolveVersion(projectDir, scenario.options))
    }

    private fun Git.commit(message: String) {
        repository.index.read()
        commit().setMessage(message).setAllowEmpty(true).call()
    }

    private fun Git.tag(name: String) {
        tag().setName(name).setAnnotated(false).call()
    }

    private fun Git.annotatedTag(name: String, message: String) {
        tag().setName(name).setAnnotated(true).setMessage(message).call()
    }
}
