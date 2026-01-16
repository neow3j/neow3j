# Development

## Publishing and Using a Snapshot to/from Sonatype

- Either run the "_Release SDK, devpack, Gradle plugin_" workflow on Github with the "Snapshot" flag set to true.
- Or run `./gradlew --info publish -Psnapshot` locally. Requires the same properties as for a normal
  release (check the `RELEASE.md` file).

Note that both approaches will generate snapshot artifacts for all modules including the `gradle-plugin` but excluding the `int-tests` module. 

### Using the SDK and Devpack Snapshot 

To use the SDK and Devpack artifacts in your project, add the following to the `repositories` section of your 
project's build file. It tells Gradle to use the Sonatype Snapshot repository as a dependency repository.

```groovy
repositories {
    maven { url "https://central.sonatype.com/repository/maven-snapshots/" }
    mavenCentral()
}
```

The dependencies can be used as usual. Of course, with the snapshot version. For example:

```groovy
dependencies {
    implementation 'io.neow3j:contract:x.y.z-SNAPSHOT'
    implementation 'io.neow3j:devpack:x.y.z-SNAPSHOT'
}
```

> Make sure to change `x.y.z` to an actual neow3j version for which a snapshot release exists.

### Using the Gradle Plugin Snapshot

To use the snapshot version of the `gradle-plugin` in your project, add the following to the `settings.gradle` file. It
tells Gradle to use the Sonatype Snapshot repository as a plugin repository.

```groovy
pluginManagement {
    repositories {
        maven { url "https://central.sonatype.com/repository/maven-snapshots/" }
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == 'io.neow3j.gradle-plugin') {
                useModule("io.neow3j:gradle-plugin:${requested.version}")
            }
        }
    }
}
```

Then use the plugin as usual in the `plugins` section of your project's build file but with the snapshot version
sepcified.

```groovy
plugins {
    id 'java'
    id 'io.neow3j.gradle-plugin' version "x.y.z-SNAPSHOT"
}
```

> Make sure to change `x.y.z` to an actual neow3j version for which a snapshot release exists.

## Publishing and Using a Local Release

Run the following command to publish the SDK, Devpack and Gradle plugin to your local Maven repository.

```bash
rm -rf ~/.m2/repository/io/neow3j && ./gradlew clean publishToMavenLocal
```

### Using the Local SDK and Devpack Release

To use the SDK and Devpack artifacts in your project, add the following to the `repositories` section of your
project's build file. It tells Gradle to use your local Maven repository as a dependency repository first.

```groovy
repositories {
    mavenLocal()
    mavenCentral() 
}
```

The dependencies can be used as usual. Of course, with the current version set in neow3j. For example:

```groovy
dependencies {
    implementation 'io.neow3j:contract:x.y.z'
    implementation 'io.neow3j:devpack:x.y.z'
}
```

> Make sure to change `x.y.z` to an actual neow3j version for which a snapshot release exists.

### Using Local Gradle Plugin Release 

To use local release of the `gradle-plugin` in your project, add the following to the `settings.gradle` file. It
tells Gradle to use your local Maven repository as a plugin repository first.

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == 'io.neow3j.gradle-plugin') {
                useModule("io.neow3j:gradle-plugin:${requested.version}")
            }
        }
    }
}
```

Then use the plugin as usual in the `plugins` section of your project's build file 

```groovy
plugins {
    id 'java'
    id 'io.neow3j.gradle-plugin'
}
```

## Locally Testing GitHub Action Workflows

If you would like to locally test GitHub Actions workflows, it's not required to make
hundreds of (useless) commits. It's better to first use [act](https://github.com/nektos/act) to
debug things.

1. Follow the [installation steps](https://github.com/nektos/act#installation).

2. [Create a PAT](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token) for your
   GitHub user, for reading container registries (i.e., ghcr.io).

3. Edit your local `~/.actrc` file and add the following lines in the end:

```
-s CR_PAT_USERNAME=<YOUR_GITHUB_USERNAME>
-s CR_PAT=<YOUR_GITHUB_PAT_TOKEN>
```

4. If you would like to run the `.github/workflows/integration.yml`, run the following command:

```
act --detect-event -W .github/workflows/integration.yml
```

That's it. :rocket:

## Generate armored PGP file for GitHub Action

```
gpg --list-secret-keys info@neow3j.io
gpg --export-secret-keys 7008418AEC2D69578BA07551DCED5430E76D91F5 | base64 > neow3j.key
```

Go to GitHub, create a new secret named `GPG_KEY_ARMOR` and paste the base64 content of the `neow3j.key` file.