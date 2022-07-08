package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoAccountState;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.StackItemType.ANY;
import static io.neow3j.types.StackItemType.ARRAY;
import static io.neow3j.types.StackItemType.BYTE_STRING;
import static java.util.Arrays.asList;

/**
 * Represents the NeoToken native contract and provides methods to invoke its functions.
 */
@SuppressWarnings("unchecked")
public class NeoToken extends FungibleToken {

    public static final String NAME = "NeoToken";
    public static final Hash160 SCRIPT_HASH = calcNativeContractHash(NAME);

    public static final int DECIMALS = 0;
    public static final String SYMBOL = "NEO";
    public static final BigInteger TOTAL_SUPPLY = new BigInteger("100000000");

    private static final String UNCLAIMED_GAS = "unclaimedGas";
    private static final String REGISTER_CANDIDATE = "registerCandidate";
    private static final String UNREGISTER_CANDIDATE = "unregisterCandidate";
    private static final String VOTE = "vote";
    private static final String GET_CANDIDATES = "getCandidates";
    static final String GET_ALL_CANDIDATES = "getAllCandidates";
    private static final String GET_CANDIDATE_VOTES = "getCandidateVote";
    private static final String GET_COMMITTEE = "getCommittee";
    private static final String GET_NEXT_BLOCK_VALIDATORS = "getNextBlockValidators";
    private static final String SET_GAS_PER_BLOCK = "setGasPerBlock";
    private static final String GET_GAS_PER_BLOCK = "getGasPerBlock";
    private static final String SET_REGISTER_PRICE = "setRegisterPrice";
    private static final String GET_REGISTER_PRICE = "getRegisterPrice";
    private static final String GET_ACCOUNT_STATE = "getAccountState";

    /**
     * Constructs a new {@code NeoToken} that uses the given {@link Neow3j} instance for invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public NeoToken(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the name of the NEO token.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the symbol of the NEO token.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the symbol.
     */
    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    /**
     * Returns the total supply of the NEO token.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the total supply.
     */
    @Override
    public BigInteger getTotalSupply() {
        return TOTAL_SUPPLY;
    }

    /**
     * Returns the number of decimals of the NEO token.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the number of decimals.
     */
    @Override
    public int getDecimals() {
        return DECIMALS;
    }

    // region unclaimed gas

    /**
     * Gets the amount of unclaimed GAS at the given height for the given account.
     *
     * @param account     the account.
     * @param blockHeight the block height.
     * @return the amount of unclaimed GAS.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger unclaimedGas(Account account, long blockHeight) throws IOException {
        return unclaimedGas(account.getScriptHash(), blockHeight);
    }

    /**
     * Gets the amount of unclaimed GAS at the given height for the given account.
     *
     * @param scriptHash  the account's script hash.
     * @param blockHeight the block height.
     * @return the amount of unclaimed GAS.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger unclaimedGas(Hash160 scriptHash, long blockHeight) throws IOException {
        ContractParameter accParam = hash160(scriptHash);
        ContractParameter heightParam = integer(BigInteger.valueOf(blockHeight));
        return callFunctionReturningInt(UNCLAIMED_GAS, accParam, heightParam);
    }

    // endregion unclaimed gas
    // region candidate registration

    /**
     * Creates a transaction script for registering a candidate with the given public key and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Note, that the transaction has to be signed with the account corresponding to the public key.
     *
     * @param candidateKey the public key to register as a candidate.
     * @return a transaction builder.
     */
    public TransactionBuilder registerCandidate(ECPublicKey candidateKey) {
        return invokeFunction(REGISTER_CANDIDATE, publicKey(candidateKey.getEncoded(true)));
    }

    /**
     * Creates a transaction script for registering a candidate and initializes a {@link TransactionBuilder} based on
     * this script.
     *
     * @param candidateKey the public key to register as a candidate.
     * @return a transaction builder.
     */
    public TransactionBuilder unregisterCandidate(ECPublicKey candidateKey) {
        return invokeFunction(UNREGISTER_CANDIDATE, publicKey(candidateKey.getEncoded(true)));
    }

    // endregion candidate registration
    // region committee and candidates information

    /**
     * Gets the public keys of the current committee members.
     *
     * @return the committee members' public keys.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the return type is not an array or the returned array's elements are
     *                                       not public keys.
     */
    public List<ECPublicKey> getCommittee() throws IOException {
        return callFunctionReturningListOfPublicKeys(GET_COMMITTEE);
    }

    /**
     * Gets the public keys of the registered candidates and their corresponding vote count.
     * <p>
     * Note that this method returns at max 256 candidates. Use {@link NeoToken#getAllCandidatesIterator()} to
     * traverse through all candidates if there are more than 256.
     *
     * @return the candidates.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<Candidate> getCandidates() throws IOException {
        StackItem arrayItem = callInvokeFunction(GET_CANDIDATES).getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), ARRAY);
        }
        return arrayItem.getList().stream().map(candidateMapper()).collect(Collectors.toList());
    }

    /**
     * Checks if there is a candidate with the provided public key.
     * <p>
     * Note that this only checks the first 256 candidates. Use {@link NeoToken#getAllCandidatesIterator()} to
     * traverse through all candidates if there are more than 256.
     *
     * @param publicKey the candidate's public key.
     * @return true if the public key belongs to a candidate. False otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isCandidate(ECPublicKey publicKey) throws IOException {
        return getCandidates().stream().anyMatch(c -> c.getPublicKey().equals(publicKey));
    }

    /**
     * Gets an iterator of all registered candidates.
     * <p>
     * Use the method {@link Iterator#traverse(int)} to traverse the iterator and retrieve all candidates.
     *
     * @return an iterator of all registered candidates.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<Candidate> getAllCandidatesIterator() throws IOException {
        return callFunctionReturningIterator(candidateMapper(), GET_ALL_CANDIDATES);
    }

    static Function<StackItem, Candidate> candidateMapper() {
        return stackItem -> {
            List<StackItem> list = stackItem.getList();
            return new Candidate(new ECPublicKey(list.get(0).getByteArray()), list.get(1).getInteger());
        };
    }

    /**
     * Gets the votes for a specific candidate.
     *
     * @param pubKey the candidate's public key.
     * @return the candidate's votes, or -1 if it was not found.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getCandidateVotes(ECPublicKey pubKey) throws IOException {
        return callFunctionReturningInt(GET_CANDIDATE_VOTES, publicKey(pubKey));
    }

    /**
     * Gets the public keys of the next block's validators.
     *
     * @return the validators' public keys.
     * @throws IOException                   if there was a problem fetching information from the Neo node.
     * @throws UnexpectedReturnTypeException if the return type is not an array or the returned array's elements are
     *                                       not public keys.
     */
    public List<ECPublicKey> getNextBlockValidators() throws IOException {
        return callFunctionReturningListOfPublicKeys(GET_NEXT_BLOCK_VALIDATORS);
    }

    private List<ECPublicKey> callFunctionReturningListOfPublicKeys(String function) throws IOException {
        StackItem arrayItem = callInvokeFunction(function).getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), ARRAY);
        }
        List<ECPublicKey> valKeys = new ArrayList<>();
        for (StackItem keyItem : arrayItem.getList()) {
            valKeys.add(extractPublicKey(keyItem));
        }
        return valKeys;
    }

    private ECPublicKey extractPublicKey(StackItem keyItem) {
        if (!keyItem.getType().equals(BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(keyItem.getType(), BYTE_STRING);
        }
        try {
            return new ECPublicKey(keyItem.getByteArray());
        } catch (IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Byte array return type did not contain public key in expected " +
                    "format.", e);
        }
    }

    // endregion committee and candidates information
    // region voting

    /**
     * Creates a transaction script to vote for the given candidate and initializes a {@link TransactionBuilder}
     * based on this script.
     *
     * @param voter     the account that casts the vote.
     * @param candidate the candidate to vote for. If null, then the current vote of the voter is withdrawn (see
     *                  {@link NeoToken#cancelVote(Account)}).
     * @return a transaction builder.
     */
    public TransactionBuilder vote(Account voter, ECPublicKey candidate) {
        return vote(voter.getScriptHash(), candidate);
    }

    /**
     * Creates a transaction script to vote for the given candidate and initializes a {@link TransactionBuilder}
     * based on this script.
     *
     * @param voter     the account that casts the vote.
     * @param candidate the candidate to vote for. If null, then the current vote of the voter is withdrawn (see
     *                  {@link NeoToken#cancelVote(Hash160)}).
     * @return a transaction builder.
     */
    public TransactionBuilder vote(Hash160 voter, ECPublicKey candidate) {
        if (candidate == null) {
            return invokeFunction(VOTE, hash160(voter), any(null));
        }
        return invokeFunction(VOTE, hash160(voter), publicKey(candidate.getEncoded(true)));
    }

    /**
     * Creates a transaction script to cancel the vote of {@code voter} and initializes a {@link TransactionBuilder}
     * based on the script.
     *
     * @param voter the account for which to cancel the vote.
     * @return a transaction builder.
     */
    public TransactionBuilder cancelVote(Hash160 voter) {
        return vote(voter, null);
    }

    /**
     * Creates a transaction script to cancel the vote of {@code voter} and initializes a {@link TransactionBuilder}
     * based on the script.
     *
     * @param voter the account for which to cancel the vote.
     * @return a transaction builder.
     */
    public TransactionBuilder cancelVote(Account voter) {
        return cancelVote(voter.getScriptHash());
    }

    /**
     * Builds a script to vote for a candidate.
     *
     * @param voter     the account that casts the vote.
     * @param candidate the candidate to vote for. If null, then the current vote of the voter is withdrawn (see
     *                  {@link NeoToken#cancelVote(Hash160)}).
     * @return the script.
     */
    public byte[] buildVoteScript(Hash160 voter, ECPublicKey candidate) {
        if (candidate == null) {
            return buildInvokeFunctionScript(VOTE, hash160(voter), any(null));
        }
        return buildInvokeFunctionScript(VOTE, hash160(voter), publicKey(candidate.getEncoded(true)));
    }

    // endregion voting
    // region network settings

    /**
     * Gets the number of GAS generated in each block.
     *
     * @return the max GAS amount per block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getGasPerBlock() throws IOException {
        return callFunctionReturningInt(GET_GAS_PER_BLOCK);
    }

    /**
     * Creates a transaction script to set the number of GAS generated in each block and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * This contract invocation can only be successful if it is signed by the network committee.
     *
     * @param gasPerBlock the maximum amount of GAS in one block.
     * @return the transaction builder.
     */
    public TransactionBuilder setGasPerBlock(BigInteger gasPerBlock) {
        return invokeFunction(SET_GAS_PER_BLOCK, integer(gasPerBlock));
    }

    /**
     * Gets the price to register as a candidate.
     *
     * @return the price to register as a candidate.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getRegisterPrice() throws IOException {
        return callFunctionReturningInt(GET_REGISTER_PRICE);
    }

    /**
     * Creates a transaction script to set the price for candidate registration and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * This contract invocation can only be successful if it is signed by the network committee.
     *
     * @param registerPrice the price to register as a candidate.
     * @return the transaction builder.
     */
    public TransactionBuilder setRegisterPrice(BigInteger registerPrice) {
        return invokeFunction(SET_REGISTER_PRICE, integer(registerPrice));
    }

    // endregion network settings

    /**
     * Gets the state of an account.
     *
     * @param account the account to get the state from.
     * @return the account state.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NeoAccountState getAccountState(Hash160 account) throws IOException {
        StackItem result = callInvokeFunction(GET_ACCOUNT_STATE, asList(hash160(account)))
                .getInvocationResult().getStack().get(0);
        if (result.getType().equals(ANY)) {
            return NeoAccountState.withNoBalance();
        }

        List<StackItem> state = result.getList();
        BigInteger balance = state.get(0).getInteger();
        BigInteger updateHeight = state.get(1).getInteger();
        StackItem publicKeyItem = state.get(2);
        if (publicKeyItem.getType().equals(ANY)) {
            return NeoAccountState.withNoVote(balance, updateHeight);
        }
        return new NeoAccountState(balance, updateHeight, new ECPublicKey(publicKeyItem.getHexString()));
    }

    /**
     * This class represents the state of a candidate.
     */
    public static class Candidate {

        /**
         * The candidate's public key;
         */
        private final ECPublicKey publicKey;

        /**
         * The candidate's votes. It is based on the summed up NEO balances of this candidate's voters.
         */
        private final BigInteger votes;

        public Candidate(ECPublicKey publicKey, BigInteger votes) {
            this.publicKey = publicKey;
            this.votes = votes;
        }

        public ECPublicKey getPublicKey() {
            return publicKey;
        }

        public BigInteger getVotes() {
            return votes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Candidate)) {
                return false;
            }
            Candidate that = (Candidate) o;
            return Objects.equals(getPublicKey(), that.getPublicKey()) &&
                    Objects.equals(getVotes(), that.getVotes());
        }

        @Override
        public String toString() {
            return "Candidate{" +
                    "publicKey=" + publicKey +
                    ", votes=" + votes +
                    '}';
        }
    }

}
