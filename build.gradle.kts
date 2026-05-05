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
            displayName = "Git Dynamic SemVer"
            description = "Automatically sets project version from git tags using Conventional Commits bump logic. Pure JGit — no git CLI required."
            tags = listOf("semver", "versioning", "git", "conventional-commits")
            implementationClass = "io.github.jimisola.gitdynsemver.GitDynSemVerPlugin"
        }
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")

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
