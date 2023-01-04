# Release Instructions/Checklist

This file describes the required steps to release neow3j. Before releasing make sure that the external documentation
at [neow3j.io](https://neow3j.io) is up-to-date.

## Prepare for Source Code Release

> If this release considers a major Neo version update (e.g., `3.4.x` &rarr; `3.5.x`): Check whether the [Neo-Express
> version](https://github.com/neo-project/neo-express/releases) used in
> the [resources](test-tools/src/main/resources/test.properties) of neow3j's `test-tools` module (`neoExpressDockerImage`
> property) supports that new Neo version. If yes, start the release process. Otherwise, wait with this release until an
> according `neo-express` version is released, update its version in the `test-tools` resources and only then start the
> release process.

1. Create a branch `release` from `main`.
2. Change the neow3j version on that branch to the release version. Do a global search with the previous version number
   and replace it with the new version number. There should be two affected files, i.e., `README.md`,
   and `DEVELOPMENT.md`.
3. Verify that the release version is also set correctly in the files `build.gradle` and `Compiler.java`.
4. Create a Pull Request from `release` to `main` -- this is called a "Release Pull Request".
    - Set the name as "Release x.x.x".
    - Set the correct milestone, project and a reviewer.
5. Review the Release Pull Request, mainly checking for critical changes **or** changes that shouldn't be in the
   release.
6. Merge the Pull Request, and then, delete the `release` branch.
7. Tag the `main` branch at the commit to be release (e.g., `3.14.0`).

## Publish the Release Artifacts

### Credentials

Make sure you have a `gradle.properties` file with the following:

```
signing.keyId=E76D91F5
signing.password=XXXXXXXXX
signing.secretKeyRingFile=/path/to/neow3j-info_at_neow3j.io.gpg

gradle.publish.key=XXXXXXXXX
gradle.publish.secret=XXXXXXXXX
```

Where:

- `signing.keyId`: is the ID of the neow3j public key
- `signing.password`: is the password for the encrypted neow3j private key file
- `signing.secretKeyRingFile`: is the path where the GPG private key file is located
- `gradle.publish.key`: is the API key for the [Gradle Plugin](https://plugins.gradle.org) repository
- `gradle.publish.secret`: is the API secret for the [Gradle Plugin](https://plugins.gradle.org) repository

### Publishing Steps

1. Check if you're in the main branch (i.e., `main` or `master-2.x`).
2. Is the current commit tagged with the release version, i.e., the one to be released?
3. Run `./gradlew clean build` making sure no old artifacts are present and the project is build.
4. Run `./gradlew bundleJar` to sign the artifacts. For each module, this will produce a bundle Jar (e.g.,
   ./core/build/libs/core-3.8.0-all.jar) of all the artifacts that need to be released for that module.
5. Go to [https://oss.sonatype.org/](https://oss.sonatype.org/), log in and go to the *Staging Upload* section.
6. Choose *Artifact Bundle* as the *Upload Mode* and upload the bundle jars (i.e., the files ending with `-all.jar`) for
   each module except for the `gradle-plugin` and `int-tests` modules. A confirmation for a successful upload should be
   displayed in the GUI.
7. All uploaded bundles should show up in the *Staging Repositories* section as separate repositories. Once they reach
   the Status **closed** the **Release** button becomes available. Select all repositories and click **Release**.
8. Search for `io.neow3j` in the *Artifact Search* section to make sure that the process worked.
9. Finally, run `./gradlew :gradle-plugin:publishPlugin` to publish the compiler Gradle plugin to the Gradle Plugins
   Repository.

## Finish Source Code Release Process

1. Go to the `main` branch and bump the version in the `build.gradle` and `Compiler.java` file on the `main` branch.

## Update `neow3j-examples` Repositories and Run Smoke Tests

1. Update the neow3j-examples repository to use the new neow3j version.
2. Run some examples as smoke tests. Compile example smart contracts with the Gradle plugin and programmatically.
3. Correct broken examples according to the changes in neow3j.
4. Add examples that cover new features.

## Update `neow3j-docs`

1. Update the version number in the documentation where necessary.
2. Update the documentation to reflect the changes and features of the release.
3. Merge the changes into the master branch of the neow3j-docs repository. That will automatically update the neow3j.io
   website.

## Update `neo-dev-portal` Repository

Update the forked [neo-dev-portal](https://github.com/AxLabs/neo-dev-portal) repository and open a Pull Request to its
origin [repository](https://github.com/neo-project/neo-dev-portal). Make sure to target the `dev` branch on both
repositories.

## Update `neow3j-boilerplate` Repositories

Update the boilerplate repos (i.e., [sdk template](https://github.com/neow3j/neow3j-boilerplate-sdk) and
[contracts template](https://github.com/neow3j/neow3j-boilerplate-sdk)) gradle.build file and possibly the example
contract if major changes happened.

## Update neow3j-test-docker

1. Clone repo: `git clone git@github.com:neow3j/neow3j-test-docker.git`
2. Open the `Dockerfile` and modify the version in the neo-express installation line:
   ```dockerfile
   RUN dotnet tool install Neo.Express -g --version 3.4.18
   ```
   If you need to depend on a preview release use the following command and adapt the version:
   ```dockerfile
   RUN dotnet tool install Neo.Express -g \
     --add-source https://pkgs.dev.azure.com/ngdenterprise/Build/_packaging/public/nuget/v3/index.json \
     --version 3.5.11-preview
   ```
   Instructions can also be found [here](https://github.com/neo-project/neo-express#installing-preview-releases).
3. You can build the docker image locally to run tests: 
   ```dockerfile
   docker build -t ghcr.io/neow3j/neow3j-test-docker:latest .
   docker build -t ghcr.io/neow3j/neow3j-test-docker:neoxp-3.4.18 . 
   ```
   Adapt `neoExpressDockerImage` in the `test-tools` application properties file to the new tag.
4. To publish the new docker image run the GitHub workflow called "Build and Publish container" on the 
   neow3j-test-docker repository. Use the `main` branch and set the version to the tag used in the last step, e.g., 
   `neoxp-3.4.18`.

## Publish GitHub Release

1. Update the README.md in all neow3j repositories if necessary.
2. Create a release draft on GitHub
    - Title format: "neow3j: 3.x.x"
    - The body should contain the sections "Breaking Changes", "Changes", "New Features", and "Fixes" in that order. If
      one section doesn't have content it can be omitted.
3. Let the team review the draft.
4. Publish the release.
