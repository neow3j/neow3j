# Release Instructions/Checklist

This file describes the required steps to release neow3j.
Before releasing make sure that the external documentation at [neow3j.io](https://neow3j.io) is up 
to date.

## Release the Source Code

- Bump the version on the develop branch (e.g., `develop-3.x`). Do a global search with the current 
  version number and replace it with the new version number. There should be four affected 
  locations in the `README.md`, one in the `build.gradle`, and one in `Compiler.java`.
- Create a Pull Request from develop to master (e.g., from `develop-3.x` to `master-3.x`) -- this
is called a "Release Pull Request"
  - Set the name as, e.g., "Release 3.2.1"
  - Set the correct milestone
  - Set the label (e.g., neo3 or related ones)
  - Assign a reviewer (yourself if no one of the team is available)
- Review the Release Pull Request, mainly checking for critical changes **or**
changes that shouldn't be in the release
- Get approval for the Release Pull Request (i.e., click on approve)
- Merge the Release Pull Request
- Tag the Pull Request with the version to be release (e.g., `3.2.1`)
- Right after merging, bump the version in the `build.gradle` file (`develop-3.x` branch)

## Credentials

Make sure you have a `gradle.properties` file with the following:

```
signing.keyId=E76D91F5
signing.password=XXXXXXXXX
signing.secretKeyRingFile=/path/to/neow3j-info_at_neow3j.io.gpg

gradle.publish.key=XXXXXXXXX
gradle.publish.secret=XXXXXXXXX
```

Where:

- `signing.keyId`: is the key ID for the GPG file
- `signing.password`: is the password for the GPG file
- `signing.secretKeyRingFile`: is the path where the GPG file is located
- `gradle.publish.key`: is the API key for the [Gradle Plugin](https://plugins.gradle.org) repository
- `gradle.publish.secret`: is the API secret for the [Gradle Plugin](https://plugins.gradle.org) repository

## Publishing the Release Artifacts

1. Check if you're in the `master` branch (i.e., `master-2.x` or `master-3.x`)
2. Correct version in `build.gradle` file?
3. Is the current commit tagged with the release version? (i.e., the one to be released)
4. Run `./gradlew clean` to make sure no old artifacts are present in the file structure
5. Run `./gradlew build` to build the whole project
6. Run `./gradlew bundleJar` to sign the artifacts. For each module, this will produce a bundle 
   Jar (e.g., ./core/build/libs/core-3.8.0-all.jar) of all the artifacts that need to be released 
   for that module.
7. Go to [https://oss.sonatype.org/](https://oss.sonatype.org/), log in and go to the *Staging 
   Upload* section.
8. Choose *Artifact Bundle* as the *Upload Mode* and upload the bundle jars for each module. A 
   confirmation for a successful upload should be displayed in the GUI. 
9. All uploaded bunldes should show up in the *Staging Repositories* section as separate 
   repositories. Once they reach the Status *closed* the *Release* button becomes available. Press 
   it for all the repositories.
9. Search for `io.neow3j` in the *Artifact Search* section to make sure that the process worked.
10. Finally, run `./gradlew :gradle-plugin:publishPlugin` to publish the compiler Gradle plugin 
    to the Gradle Plugins Repository.

## neow3j-examples and Smoke Tests

1. Update the neow3j-examples repository to use the new neow3j version. 
2. Run some of the examples as smoke tests.
    - Compile example smart contracts with the Gradle plugin and programmatically.
3. Correct broken examples according to the changes in neow3j.
4. Add examples that cover new features.

## neow3j-docs

1. Update the version number in the documentation where necessary.
2. Update the documentation to reflect the changes and features of the release.
3. Merge the changes into the master branch of the neow3j-docs repository. That will automatically 
   update the neow3j.io website.

## neow3j-boilerplate

Update the boilerplate [repo's](https://github.com/neow3j/neow3j-boilerplate) gradle.build file 
and possibly the example contract if major changes happened.

## Github release

1. Update the README.md in all neow3j repositories if necessary.
2. Create a release draft on Github
    - Title format: "neow3j: 3.x.x"
    - The body should contain the sections "Changes", "New Features", and "Fixes" in that order. 
    If one section doesn't have content it can be omitted.
3. Let the team review the draft.
4. Publish the release.
