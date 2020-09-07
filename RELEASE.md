# Release Instructions/Checklist

This file describes the required steps to release neow3j.

## Release the Source Code

1. Bump the version on the develop branch (e.g., `develop-3.x`) -- in the `build.gradle` file, search for `group`
and `version` clauses
2. Create a Pull Request from develop to master (e.g., from `develop-3.x` to `master-3.x`) -- this
is called a "Release Pull Request"
  - Set the name as, e.g., "Release 3.2.1"
  - Set the correct milestone
  - Set the label (e.g., neo3 or related ones)
  - Assign a reviewer (yourself if no one of the team is available)
3. Review the Release Pull Request, mainly checking for critical changes **or**
changes that shouldn't be in the release
4. Get approval for the Release Pull Request (i.e., click on approve)
5. Merge the Release Pull Request
6. Tag the Pull Request with the version to be release (e.g., `3.2.1`)

## Credentials

Make sure you have a `gradle.properties` file with the following:

```
jfrogUsername=XXXXXXXXX
jfrogPassword=XXXXXXXXX

signing.keyId=E76D91F5
signing.password=XXXXXXXXX
signing.secretKeyRingFile=/path/to/neow3j-info_at_neow3j.io.gpg

gradle.publish.key=XXXXXXXXX
gradle.publish.secret=XXXXXXXXX
```

Where:

- `jfrogUsername`: is the Bintray username
- `jfrogPassword`: is the Bintray API key
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
6. Run `./gradlew signMavenJavaPublication` to sign the artifacts
7. Run `./gradlew bintrayUpload` to upload the files to Bintray
8. Run `./gradlew :gradle-plugin:publishPlugin`
9. Go to [https://bintray.com/neow3j/maven/neow3j](https://bintray.com/neow3j/maven/neow3j), click
on the Maven Central tab and Sync the repositories (providing the necessary SonaType API tokens)
10. Go to [https://oss.sonatype.org/](https://oss.sonatype.org/) and search for `io.neow3j` to make
sure that the synchronization with Maven Central worked

## Smoke Tests

TBD.
