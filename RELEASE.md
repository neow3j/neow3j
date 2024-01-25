# Release Instructions/Checklist

This file describes the required steps to release neow3j. Before releasing make sure that the external documentation
at [neow3j.io](https://neow3j.io) is up-to-date.

> If this release contains changes for a new Neo release, make sure that the `test-tools` module is equipped 
> with an updated neo-express version. This requires an update of the `neow3j-test-docker` Docker image. 
> See section [Updating neow3j-test-docker](#updating-neow3j-test-docker) for more details. It is possible that 
> at the time of release there is no new `neo-express` version available that supports the new Neo version. 

> For instructions on how to produce snapshot versions or release locally see `DEVELOPMENT.md`.

## Releasing with GitHub Actions

1. Create a branch `release` from `main`.
2. Set the release version number in:
   - build.gradle, in the `version` property
   - Compiler.java, in the `COMPILER_NAME` constant
   - README.md, in the sections that explain how to import neow3j
3. Tag the `main` branch at the **top** commit. Tag format, e.g., `3.14.0`. Note, that the release workflow currently 
   doesn't support releasing anything else than the top commit of a branch.
4. Run the "_Release SDK, devpack, Gradle plugin_" GitHub workflow on the `main` branch. It will publish the packages
   `compiler`, `contract`, `core`, `devpack`, `devpack-test`, `test-tools` to Maven Central via Sonatype and the `gradle-plugin` to
   the Gradle Plugin Repository.


## Releasing from a Local Machine

The following properties are required to create a release:
- `signingKey`, the PGP key used to sign neow3j artifacts, ASCII armored and encrypted
- `signingPassword`, the password for the PGP key
- `sonatypeUsername`, the username for the Sonatype account, should be a token and not the actual username
- `sonatypePassword`, the password for the Sonatype account, should be a token and not the actual password 
- `gradle.publish.key`, the API key for the Gradle Plugin Repository
- `gradle.publish.secret`, the API secret for the Gradle Plugin Repository

You can set those properties in the `gradle.properties` file in the project root. Or via in the command line with a 
`-P` prefix, e.g., `-PsigningKey=...`.

Putting the armored PGP key in the `gradle.properties` file is a bit ugly but doable. You have to add `\n\` at the 
end of every line - also on empty lines - to make it a valid property value. It will look something like this:
```
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n\
\n\
xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxa0W8BM\n\
...
xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx9e\n\
xxx9E\n\
-----END PGP PRIVATE KEY BLOCK-----

```

To publish the SDK and devpack artifacts from the currently checked-out code base, run:
```bash
./gradlew --info -x :gradle-plugin:publishToSonatype -x :neofs:publishToSonatype  publishToSonatype closeAndReleaseSonatypeStagingRepository
```
The task `publishToSonatype` compiles, packages, signs and uploads the artifacts to Sonatype into a "Staging
Repository". The task `closeAndReleaseSonatypeStagingRepository` closes and releases that Staging Repository to 
Maven Central. We don't want to release all subprojects to Maven Central. Thus, you can exclude them from the 
command with the `-x` flag[^1].

To publish the Gradle plugin from your currently checked out code base run:
```bash
./gradlew :gradle-plugin:publishPlugin 
```

## After Releasing the Artifacts

- Create Release Notes on GitHub
  - Title format: "neow3j: 3.x.x"
  - The body should contain the sections "Breaking Changes", "Changes", "New Features", and "Fixes" in that order. If
     one section doesn't have content it can be omitted.
  - It's important that users know what actions they need to take if they want to move to the new version.
 
- Update the dependencies in the following repositories and test whether the build still works. Depending on the 
  changes in the new release, update these repositories, e.g., by adding new examples or updating existing ones.
  - `neow3j-examples`
  - `neow3j-boilerplate-sdk`
  - `neow3j-boilerplate-contracts`

- Update `neow3j-docs` according to the changes in the new release. At the very least, update the version number.

- Update the `neo-dev-portal` repository by first updating our [fork](https://github.com/AxLabs/neo-dev-portal) and then
  opening a PR to the origin repo. The PR should target the `dev` branch. Usually the changes are only about the new
  version number.

## Updating neow3j-test-docker

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

[^1]: It is not easy to exclude the `gradle-plugin` subproject from the `publishToSonatype` task in the build script.
The `nexus.publish-plugin` automatically includes all projects that use the `maven-publish` plugin and the 
`gradl-plugin` subproject does implicitly use that plugin. Thus, it is always included when executing `./gradlew
publishToSonatype` and has to be explicitly excluded with the `-x` flag.
