[![Commit Activity](https://img.shields.io/github/commit-activity/m/jimisola/git-dyn-semver-gradle-plugin?label=commits&style=for-the-badge)](https://github.com/jimisola/git-dyn-semver-gradle-plugin/pulse)
[![GitHub Issues](https://img.shields.io/github/issues/jimisola/git-dyn-semver-gradle-plugin?style=for-the-badge&logo=github)](https://github.com/jimisola/git-dyn-semver-gradle-plugin/issues)
[![License](https://img.shields.io/github/license/jimisola/git-dyn-semver-gradle-plugin?style=for-the-badge&logo=opensourceinitiative)](https://opensource.org/license/mit/)
[![Build](https://img.shields.io/github/actions/workflow/status/jimisola/git-dyn-semver-gradle-plugin/build.yml?style=for-the-badge&logo=github)](https://github.com/jimisola/git-dyn-semver-gradle-plugin/actions/workflows/build.yml)

# Git Dynamic Semantic Versioning Gradle Plugin

Gradle plugin that automatically sets the project version from git tags using [Conventional Commits](https://www.conventionalcommits.org/) bump logic. Pure JGit — no git CLI required.

## Overview

On every build the plugin walks back to the nearest semver tag and computes the next version based on the commit messages since that tag:

| Commits since tag | Highest bump | Example result |
|---|---|---|
| None | — | `1.2.3` (exact tag, release build) |
| `fix:` only | patch | `1.2.4-3-SNAPSHOT` |
| `feat:` present | minor | `1.3.0-3-SNAPSHOT` |
| `feat!:` or `BREAKING CHANGE:` present | major | `2.0.0-3-SNAPSHOT` |
| No reachable tag | — | `0.1.0-5-SNAPSHOT` |

The snapshot version format is `{next-version}-{distance}-SNAPSHOT` where `distance` is the number of commits since the tag.

## Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.jimisola.git-dyn-semver") version "<version>"
}
```

Or `build.gradle`:

```groovy
plugins {
    id 'io.github.jimisola.git-dyn-semver' version '<version>'
}
```

That's it — the project version is set automatically. No further configuration required.

## Configuration

All settings are optional. The `gitDynSemVer` extension is available to customise behaviour:

```kotlin
gitDynSemVer {
    includeBuildNumber.set(true)   // default: true
    snapshotSuffix.set("SNAPSHOT") // default: "SNAPSHOT"
}
```

| Property | Type | Default | Description |
|---|---|---|---|
| `includeBuildNumber` | `Property<Boolean>` | `true` | Include commit distance in snapshot versions, e.g. `1.3.0-4-SNAPSHOT` vs `1.3.0-SNAPSHOT` |
| `snapshotSuffix` | `Property<String>` | `"SNAPSHOT"` | Suffix appended to pre-release versions |

### Forcing a specific version

Pass `-Pversion.force=X.Y.Z` on the command line to override the computed version. Useful in CI release pipelines:

```bash
./gradlew publish -Pversion.force=1.5.0
```

## Task reference

### `printVersion`

Prints the resolved project version to stdout. Useful for inspecting the computed version before publishing:

```bash
./gradlew printVersion
```

## Tagging conventions

The plugin matches tags of the form `{major}.{minor}.{patch}` (no prefix). Create a release tag with:

```bash
git tag 1.2.3
git push origin 1.2.3
```

Both lightweight and annotated tags are supported.

## Contributing

Contributions are welcome. Please open an issue or pull request on [GitHub](https://github.com/jimisola/git-dyn-semver-gradle-plugin).

## License

MIT License.
