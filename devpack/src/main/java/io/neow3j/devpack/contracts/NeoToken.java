package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.annotations.Struct;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.annotations.CallFlags;

import static io.neow3j.devpack.constants.CallFlags.States;
import static io.neow3j.devpack.constants.CallFlags.ReadStates;

/**
 * Represents an interface to the native NeoToken contract.
 */
public class NeoToken extends FungibleToken {

    /**
     * Initializes an interface to the native NeoToken contract.
     */
    public NeoToken() {
        super(NativeContract.NeoTokenScriptHash);
    }

    /**
     * Gets the amount of unclaimed GAS for the given account up the the given block number.
     *
     * @param scriptHash  the script hash of the account to get the unclaimed GAS amount for.
     * @param blockHeight the block height up to which the GAS amount will be fetched.
     * @return the amount of unclaimed GAS.
     */
    @CallFlags(ReadStates)
    public native int unclaimedGas(Hash160 scriptHash, int blockHeight);

    /**
     * Registers the given public key as a validator candidate.
     *
     * @param publicKey the public key of the candidate.
     * @return true if registering was successful. False, otherwise.
     */
    @CallFlags(States)
    public native boolean registerCandidate(ECPoint publicKey);

    /**
     * Unregisters the given public key from the list of validator candidates.
     *
     * @param publicKey the public key of the candidate.
     * @return true if deregistering was successful. False, otherwise.
     */
    @CallFlags(States)
    public native boolean unregisterCandidate(ECPoint publicKey);

    /**
     * Casts a vote for the candidate with the given public key.
     * <p>
     * Note, that a witness (signature) of the account corresponding to the public key has to be available for this
     * to work.
     *
     * @param scriptHash      the script hash of the account that is used to cast the vote.
     * @param candidatePubKey the public key of the candidate to vote for.
     * @return true if voting was successful. False, otherwise.
     */
    @CallFlags(States)
    public native boolean vote(Hash160 scriptHash, ECPoint candidatePubKey);

    /**
     * @return the first 256 registered candidates.
     */
    @CallFlags(ReadStates)
    public native Candidate[] getCandidates();

    /**
     * @return an iterator of all registered candidates.
     */
    @CallFlags(ReadStates)
    public native Iterator<Iterator.Struct<ECPoint, Integer>> getAllCandidates();

    /**
     * Gets the votes for a specific candidate.
     *
     * @param pubKey the candidate's public key.
     * @return the candidate's votes, or -1 if it was not found.
     */
    @CallFlags(ReadStates)
    public native int getCandidateVote(ECPoint pubKey);

    /**
     * @return the committee members' public keys.
     */
    @CallFlags(ReadStates)
    public native ECPoint[] getCommittee();

    /**
     * @return the next block validators' public keys.
     */
    @CallFlags(ReadStates)
    public native ECPoint[] getNextBlockValidators();

    /**
     * @return the amount of GAS that is minted per newly generated block.
     */
    @CallFlags(ReadStates)
    public native int getGasPerBlock();

    /**
     * Sets the amount of GAS that should be minted per newly generated block.
     * <p>
     * Only the committee can successfully invoke this method.
     *
     * @param gasPerBlock the desired amount of GAS per block.
     */
    @CallFlags(States)
    public native void setGasPerBlock(int gasPerBlock);

    /**
     * @return the GAS price for registering a new candidate.
     */
    @CallFlags(ReadStates)
    public native int getRegisterPrice();

    /**
     * Sets the GAS price for registering a new candidate.
     * <p>
     * Only the committee can successfully invoke this method.
     *
     * @param registerPrice the new price for registering a candidate.
     */
    @CallFlags(States)
    public native void setRegisterPrice(int registerPrice);

    /**
     * Gets the account state.
     *
     * @param scriptHash the script hash of the account.
     * @return the state of the account or null if the account does not hold any NEO.
     */
    @CallFlags(ReadStates)
    public native AccountState getAccountState(Hash160 scriptHash);

    /**
     * The state of an account regarding its NEO balance and voting target.
     */
    @Struct
    public static class AccountState {

        /**
         * The NEO balance of the account.
         */
        public final int balance;

        /**
         * The block height when the balance changed last.
         * <p>
         * This field is {@code 0} by default if the account does not hold any NEO.
         */
        public final int balanceHeight;

        /**
         * The voting target of the account.
         * <p>
         * This field may be {@code null} if the account has no voting target.
         */
        public final ECPoint voteTo;

        /**
         * The last GAS per NEO for the committee member this account last voted for.
         * <p>
         * This field is {@code 0} by default if the account does not hold any NEO.
         */
        public final int lastGasPerVote;

        private AccountState() {
            balance = 0;
            balanceHeight = 0;
            voteTo = null;
            lastGasPerVote = 0;
        }

    }

    /**
     * Represents a validator candidate.
     */
    @Struct
    public static class Candidate {

        /**
         * This candidate's public key.
         */
        public final ECPoint publicKey;

        /**
         * This candidate's votes.
         */
        public final int votes;

        private Candidate() {
            publicKey = null;
            votes = 0;
        }

    }

}
