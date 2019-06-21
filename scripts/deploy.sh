#!/usr/bin/env bash

SNAPSHOT_TAG_PATTERN="^(\d+(\.\d+){1,2}(\.\*)?)-SNAPSHOT$"
RELEASE_TAG_PATTERN="^(\d+(\.\d+){1,2}(\.\*)?)$"

GPG_ENC_FILE="./scripts/neow3j.gpg.enc"
GPG_FILE="./scripts/neow3j.gpg"

echo "Tag detected: ${TRAVIS_TAG}"

if echo "${TRAVIS_TAG}" | grep -P -q ${SNAPSHOT_TAG_PATTERN}; then
    PARAMS="-Psnapshot"
    echo "Setting the build as SNAPSHOT."
fi

if (echo "${TRAVIS_TAG}" | grep -P -q ${SNAPSHOT_TAG_PATTERN}) || (echo "${TRAVIS_TAG}" | grep -P -q ${RELEASE_TAG_PATTERN}); then
    echo "It's a valid tag pattern."
    if [ "${TRAVIS_PULL_REQUEST}" == "false" ]; then
        openssl aes-256-cbc -K $encrypted_f2caa0a2639d_key -iv $encrypted_f2caa0a2639d_iv -in ${GPG_ENC_FILE} -out ${GPG_FILE} -d
        # release (uploadArchives)
        ./gradlew release -PnexusUsername=${SONATYPE_USERNAME} -PnexusPassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=${GPG_FILE} ${PARAMS}

        # After releasing (uploadArchives) to nexus,
        # it's a good practice to wait 1 minute or so
        # due to issues with nexus servers.
        sleep 60

        # close and release repo!
        ./gradlew closeAndReleaseRepository -PnexusUsername=${SONATYPE_USERNAME} -PnexusPassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=${GPG_FILE} ${PARAMS}
    fi
fi