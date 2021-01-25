package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0x0a46e2e37c9987f570b4af253fb77e7eef0f72b6")
public class NeoToken extends Nep17Token {

    /**
     * Gets the amount of unclaimed GAS for the given account up the the given block number.
     *
     * @param scriptHash  The script hash of the account to get the unclaimed GAS amount for.
     * @param blockHeight The block height up to which the GAS amount will be fetched.
     * @return the amount of unclaimed GAS.
     */
    public static native int unclaimedGas(Hash160 scriptHash, int blockHeight);

    /**
     * Registers the given public key as a validator candidate.
     *
     * @param publicKey The public key of the candidate.
     * @return true, if registering was successful. False, otherwise.
     */
    public static native boolean registerCandidate(ECPoint publicKey);

    /**
     * Unregisters the given public key from the list of validator candidates.
     *
     * @param publicKey The public key of the candidate.
     * @return true, if deregistering was successful. False, otherwise.
     */
    public static native boolean unregisterCandidate(ECPoint publicKey);

    /**
     * Casts a vote for the candidate with the given public key.
     * <p>
     * Note, that a witness (signature) of the account corresponding to the public key has to be
     * available for this to work.
     *
     * @param scriptHash      The script hash of the account that is used to cast the vote.
     * @param candidatePubKey The public key of the candidate to vote for.
     * @return true, if voting was successful. False, otherwise.
     */
    public static native boolean vote(Hash160 scriptHash, ECPoint candidatePubKey);

    /**
     * Gets the registered candidates.
     *
     * @return the list of registered candidates.
     */
    public static native Candidate[] getCandidates();

    /**
     * Gets the public keys of the current committee members.
     *
     * @return the committee members' public keys as strings.
     */
    public static native ECPoint[] getCommittee();

    /**
     * Gets the public keys of the validators that will validate the upcoming block.
     *
     * @return the next validators' public keys as strings.
     */
    public static native ECPoint[] getNextBlockValidators();

    /**
     * Gets the amount of GAS that is minted per newly generated block.
     *
     * @return the amount of minted GAS per block.
     */
    public static native int getGasPerBlock();

    /**
     * Sets the amount of GAS that should be minted per newly generated block.
     *
     * @param gasPerBlock The desired amount of GAS per block.
     * @return true, if setting the amount was successful. False, otherwise.
     */
    public static native boolean setGasPerBlock(int gasPerBlock);

    /**
     * Represents a validator candidate.
     */
    public static class Candidate {

        /**
         * This candidates public key.
         */
        public final ECPoint publicKey;

        /**
         * This candidates votes.
         */
        public final int votes;

        private Candidate() {
            publicKey = new ECPoint("");
            votes = 0;
        }
    }
}
