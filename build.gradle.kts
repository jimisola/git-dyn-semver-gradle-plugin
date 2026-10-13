plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.2.1"
    // Bootstrap: versions this build with the last published release of itself.
    id("io.github.jimisola.git-dyn-semver") version "0.1.2"
}

group = "io.github.jimisola"

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

repositories {
    maven("https://repo.eclipse.org/content/repositories/jgit-releases/")
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.8.0.202609011348-r")

    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.3")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
