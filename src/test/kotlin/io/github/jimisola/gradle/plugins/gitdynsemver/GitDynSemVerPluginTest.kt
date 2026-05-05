package io.github.jimisola.gradle.plugins.gitdynsemver

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue

class GitDynSemVerPluginTest {

    @TempDir
    lateinit var projectDir: File

    @Test
    fun `plugin applies and printVersion task is available`() {
        gitInit()
        gitTag("1.0.0")
        writeFile("settings.gradle.kts", """rootProject.name = "test"""")
        writeFile(
            "build.gradle.kts",
            """
            plugins {
                id("io.github.jimisola.git-dyn-semver")
            }
            """.trimIndent()
        )

        val result = runner().withArguments("printVersion").build()

        assertTrue(result.output.contains("1.0.0"), result.output)
    }

    @Test
    fun `includeBuildNumber false omits distance from snapshot`() {
        gitInit()
        gitTag("1.0.0")
        gitCommit("feat: new thing")
        writeFile("settings.gradle.kts", """rootProject.name = "test"""")
        writeFile(
            "build.gradle.kts",
            """
            plugins {
                id("io.github.jimisola.git-dyn-semver")
            }
            gitDynSemVer {
                includeBuildNumber.set(false)
            }
            """.trimIndent()
        )

        val result = runner().withArguments("printVersion").build()

        assertTrue(result.output.contains("1.1.0-SNAPSHOT"), result.output)
    }

    @Test
    fun `version force property overrides computed version`() {
        gitInit()
        gitTag("1.0.0")
        gitCommit("feat: new thing")
        writeFile("settings.gradle.kts", """rootProject.name = "test"""")
        writeFile(
            "build.gradle.kts",
            """
            plugins {
                id("io.github.jimisola.git-dyn-semver")
            }
            """.trimIndent()
        )

        val result = runner().withArguments("printVersion", "-Pversion.force=9.9.9").build()

        assertTrue(result.output.contains("9.9.9"), result.output)
    }

    private fun runner() = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withDebug(true)

    private fun writeFile(name: String, content: String) =
        projectDir.resolve(name).writeText(content)

    private fun gitInit() {
        exec("git", "init")
        exec("git", "config", "user.email", "test@example.com")
        exec("git", "config", "user.name", "Test")
        exec("git", "commit", "--allow-empty", "-m", "chore: init")
    }

    private fun gitTag(tag: String) = exec("git", "tag", tag)

    private fun gitCommit(message: String) =
        exec("git", "commit", "--allow-empty", "-m", message)

    private fun exec(vararg cmd: String) {
        ProcessBuilder(*cmd).directory(projectDir).inheritIO().start().waitFor()
    }
}
