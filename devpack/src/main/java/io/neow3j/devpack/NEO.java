package io.neow3j.devpack;

import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xde5f57d430d3dece511cf975a8d37848cb9e0525")
public class NEO extends Nep17Token {

    /**
     * Gets the amount of unclaimed GAS for the given account up the the given block number.
     *
     * @param scriptHash  The script hash of the account to get the unclaimed GAS amount for.
     * @param blockHeight The block height up to which the GAS amount will be fetched.
     * @return the amount of unclaimed GAS.
     */
    public static native int unclaimedGas(byte[] scriptHash, int blockHeight);

    /**
     * Registers the given public key as a validator candidate.
     *
     * @param publicKey The public key of the candidate.
     * @return true, if registering was successful. False, otherwise.
     */
    public static native boolean registerCandidate(byte[] publicKey);

    /**
     * Unregisters the given public key from the list of validator candidates.
     *
     * @param publicKey The public key of the candidate.
     * @return true, if deregistering was successful. False, otherwise.
     */
    public static native boolean unRegisterCandidate(byte[] publicKey);

    /**
     * Casts a vote for the candidate with the given public key.
     *
     * @param scriptHash      The script hash of the account that is used to cast the vote.
     * @param candidatePubKey The public key of the candidate to vote for.
     * @return true, if voting was successful. False, otherwise.
     */
    public static native boolean vote(byte[] scriptHash, byte[] candidatePubKey);

    /**
     * Gets the registered candidates.
     *
     * @return the list of registered candidates.
     */
    public static native Candidate[] getCandidates();

    /**
     * Gets the public keys of current validators.
     *
     * @return the validators' public keys as strings.
     */
    public static native String[] getValidators();

    /**
     * Gets the public keys of the current committee members.
     *
     * @return the committee members' public keys as strings.
     */
    public static native String[] getCommittee();

    /**
     * Gets the public keys of the validators that will validate the upcoming block.
     *
     * @return the next validators' public keys as strings.
     */
    public static native String[] getNextBlockValidators();

    /**
     * Represents a validator candidate.
     */
    public static class Candidate {

        /**
         * This candidates public key.
         */
        public final String publicKey;

        /**
         * This candidates NEO balance.
         */
        public final int neoBalance;

        private Candidate() {
            publicKey = "";
            neoBalance = 0;
        }
    }
}
