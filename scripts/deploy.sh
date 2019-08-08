#!/usr/bin/env bash

SNAPSHOT_TAG_PATTERN="^(\d+(\.\d+){1,2}(\.\*)?)-SNAPSHOT$"
RELEASE_TAG_PATTERN="^(\d+(\.\d+){1,2}(\.\*)?)$"

GPG_ENC_FILE="${TRAVIS_BUILD_DIR}/scripts/neow3j.gpg.enc"
GPG_FILE="${TRAVIS_BUILD_DIR}/scripts/neow3j.gpg"

echo "Tag detected: ${TRAVIS_TAG}"
echo "Travis build dir: ${TRAVIS_BUILD_DIR}"

SNAPSHOT=`echo "${TRAVIS_TAG}" | grep -P ${SNAPSHOT_TAG_PATTERN}`
RELEASE=`echo "${TRAVIS_TAG}" | grep -P ${RELEASE_TAG_PATTERN}`

if [ "${SNAPSHOT}" == "${TRAVIS_TAG}" ]; then
    PARAMS="-Psnapshot"
    echo "Setting the build as SNAPSHOT."
fi
if [ "${RELEASE}" == "${TRAVIS_TAG}" ]; then
    echo "Setting the build as RELEASE."
fi

if [ "${SNAPSHOT}" == "${TRAVIS_TAG}" ] || [ "${RELEASE}" == "${TRAVIS_TAG}" ]; then
    echo "It's a valid tag pattern."
    if [ "${TRAVIS_PULL_REQUEST}" == "false" ]; then

        # re-creating the .gpg file
        openssl aes-256-cbc -K $encrypted_f2caa0a2639d_key -iv $encrypted_f2caa0a2639d_iv -in ${GPG_ENC_FILE} -out ${GPG_FILE} -d

        # publish all modules
        ./gradlew publish -PnexusUsername=${SONATYPE_USERNAME} -PnexusPassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=${GPG_FILE} ${PARAMS}

        if [ $? -eq 0 ] && [ "${RELEASE}" == "${TRAVIS_TAG}" ]; then
            # After releasing (uploadArchives) to nexus,
            # it's a good practice to wait 1 minute or so
            # due to issues with nexus servers.
            sleep 60

            # close and release repo!
            ./gradlew closeAndReleaseRepository -PnexusUsername=${SONATYPE_USERNAME} -PnexusPassword=${SONATYPE_PASSWORD} -Psigning.keyId=${GPG_KEY_ID} -Psigning.password=${GPG_KEY_PASSPHRASE} -Psigning.secretKeyRingFile=${GPG_FILE} ${PARAMS}
        fi
    fi
fi