plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.1"
    id("pl.allegro.tech.build.axion-release") version "1.21.1"
}

scmVersion {
    tag {
        prefix.set("")
    }
    versionCreator("simple")
}

group = "io.github.jimisola"
version = scmVersion.version

kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    website = "https://github.com/jimisola/git-dyn-semver-gradle-plugin"
    vcsUrl = "https://github.com/jimisola/git-dyn-semver-gradle-plugin"
    plugins {
        create("gitDynSemVer") {
            id = "io.github.jimisola.git-dyn-semver"
            displayName = "Git Dynamic Semantic Versioning"
            description = "Sets project version automatically from git tags using Conventional Commits bump logic (MAJOR/MINOR/PATCH). Pure JGit — no git CLI dependency."
            tags = listOf("semver", "semantic-versioning", "git", "git-tag", "conventional-commits", "jgit", "versioning", "automation")
            implementationClass = "io.github.jimisola.gradle.plugins.gitdynsemver.GitDynSemVerPlugin"
        }
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r")

    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.2")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
