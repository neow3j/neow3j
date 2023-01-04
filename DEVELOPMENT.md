# Development

This file describes configurations and nuances for developing in the neow3j project.

## Using the SNAPSHOT versions

If you would like to use the "latest and greatest" version of newo3j (for development *only*), then
add the Sonatype Snapshot repository to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}
```

Then you would be able to use the snapshot versions:

```groovy
implementation 'io.neow3j:contract:3.19.2-SNAPSHOT'
implementation 'io.neow3j:devpack:3.19.2-SNAPSHOT'
```

## Locally Testing GitHub Action Workflows

If you would like to locally test GitHub Actions workflows, it's not required to make
hundreds of (useless) commits. It's better to first use [act](https://github.com/nektos/act) to
debug things.

1. Follow the [installation steps](https://github.com/nektos/act#installation).

2. [Create a PAT](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token) for your GitHub user, for reading container registries (i.e., ghcr.io).

3. Edit your local `~/.actrc` file and add the following lines in the end:

```
-s CR_PAT_USERNAME=<YOUR_GITHUB_USERNAME>
-s CR_PAT=<YOUR_GITHUB_PAT_TOKEN>
```

4. If you would like to run the `.github/workflows/intergation.yml`, run the following command:

```
act --detect-event -W .github/workflows/integration.yml
```

That's it. :rocket:


## Generate armored PGP file for GitHub Action

```
gpg --list-secret-keys info@neow3j.io
gpg --export-secret-keys 7008418AEC2D69578BA07551DCED5430E76D91F5 | base64 > neow3j.key
```

Go to GitHub, create a new secret named `GPG_KEY_ARMOR` and paste
the base64 content of the `neow3j.key` file.