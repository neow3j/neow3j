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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.neow3j.types.ContractParameter.any;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.publicKey;
import static io.neow3j.types.StackItemType.ANY;
import static io.neow3j.types.StackItemType.ARRAY;
import static io.neow3j.types.StackItemType.BYTE_STRING;
import static io.neow3j.types.StackItemType.INTEGER;
import static io.neow3j.types.StackItemType.STRUCT;
import static java.util.Arrays.asList;

/**
 * Represents the NeoToken native contract and provides methods to invoke its functions.
 */
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
    private static final String GET_COMMITTEE = "getCommittee";
    private static final String GET_NEXT_BLOCK_VALIDATORS = "getNextBlockValidators";
    private static final String SET_GAS_PER_BLOCK = "setGasPerBlock";
    private static final String GET_GAS_PER_BLOCK = "getGasPerBlock";
    private static final String SET_REGISTER_PRICE = "setRegisterPrice";
    private static final String GET_REGISTER_PRICE = "getRegisterPrice";
    private static final String GET_ACCOUNT_STATE = "getAccountState";

    /**
     * Constructs a new {@code NeoToken} that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public NeoToken(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the name of the NeoToken contract.
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
     * Returns the symbol of the NeoToken contract.
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
     * Returns the total supply of the NeoToken contract.
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
     * @return the amount of unclaimed GAS
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger unclaimedGas(Hash160 scriptHash, long blockHeight) throws IOException {
        ContractParameter accParam = hash160(scriptHash);
        ContractParameter heightParam = integer(BigInteger.valueOf(blockHeight));
        return callFuncReturningInt(UNCLAIMED_GAS, accParam, heightParam);
    }

    /**
     * Creates a transaction script for registering a committee candidate with the given
     * public key and initializes a {@link TransactionBuilder} based on this script.
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
     * Creates a transaction script for registering a validator candidate and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param candidateKey the public key to register as a candidate.
     * @return a transaction builder.
     */
    public TransactionBuilder unregisterCandidate(ECPublicKey candidateKey) {
        return invokeFunction(UNREGISTER_CANDIDATE, publicKey(candidateKey.getEncoded(true)));
    }

    /**
     * Gets the public keys of the current committee members.
     *
     * @return the committee members' public keys.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the return type is not an array or the returned
     *                                       array's elements are not public keys.
     */
    public List<ECPublicKey> getCommittee() throws IOException {
        return callFunctionReturningListOfPublicKeys(GET_COMMITTEE);
    }

    /**
     * Gets the public keys of the currently registered validator candidates and their
     * corresponding vote count.
     * <p>
     * The vote count is based on the summed up NEO balances of the respective candidate's voters.
     *
     * @return the candidate public keys and their corresponding vote count.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the return type is not an array or the array
     *                                       elements are not public keys and node counts.
     */
    public Map<ECPublicKey, BigInteger> getCandidates() throws IOException {
        StackItem arrayItem = callInvokeFunction(GET_CANDIDATES)
                .getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), ARRAY);
        }
        Map<ECPublicKey, BigInteger> validators = new HashMap<>();
        for (StackItem valItem : arrayItem.getList()) {
            if (!valItem.getType().equals(STRUCT)) {
                throw new UnexpectedReturnTypeException(valItem.getType(), STRUCT);
            }
            ECPublicKey key = extractPublicKey(valItem.getList().get(0));
            StackItem nrItem = valItem.getList().get(1);
            if (!nrItem.getType().equals(INTEGER)) {
                throw new UnexpectedReturnTypeException(nrItem.getType(), INTEGER);
            }
            validators.put(key, nrItem.getInteger());
        }
        return validators;
    }

    /**
     * Gets the public keys of the next block's validators.
     *
     * @return the validators' public keys.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the return type is not an array or the returned
     *                                       array's elements are not public keys.
     */
    public List<ECPublicKey> getNextBlockValidators() throws IOException {
        return callFunctionReturningListOfPublicKeys(GET_NEXT_BLOCK_VALIDATORS);
    }

    private List<ECPublicKey> callFunctionReturningListOfPublicKeys(String function)
            throws IOException {

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
            throw new UnexpectedReturnTypeException("Byte array return type did not contain " +
                    "public key in expected format.", e);
        }
    }

    /**
     * Creates a transaction script to vote for the given validators and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param voter     the account that casts the vote.
     * @param candidate the candidate to vote for. If null, then the current vote of the voter is
     *                  withdrawn (see {@link NeoToken#cancelVote(Account)}).
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder vote(Account voter, ECPublicKey candidate) throws IOException {
        return vote(voter.getScriptHash(), candidate);
    }

    /**
     * Creates a transaction script to vote for the given validators and initializes a
     * {@link TransactionBuilder} based on this script.
     *
     * @param voter     the account that casts the vote.
     * @param candidate the candidate to vote for. If null, then the current vote of the voter is
     *                  withdrawn (see {@link NeoToken#cancelVote(Hash160)}).
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder vote(Hash160 voter, ECPublicKey candidate) throws IOException {
        if (candidate == null) {
            return invokeFunction(VOTE, hash160(voter), any(null));
        }
        return invokeFunction(VOTE, hash160(voter), publicKey(candidate.getEncoded(true)));
    }

    /**
     * Creates a transaction script to cancel the vote of {@code voter} and initializes a
     * transaction Builder based on the script.
     *
     * @param voter the account for which to cancel the vote.
     * @return a transaction builder
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder cancelVote(Hash160 voter) throws IOException {
        return vote(voter, null);
    }

    /**
     * Creates a transaction script to cancel the vote of {@code voter} and initializes a
     * transaction Builder based on the script.
     *
     * @param voter the account for which to cancel the vote.
     * @return a transaction builder
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder cancelVote(Account voter) throws IOException {
        return cancelVote(voter.getScriptHash());
    }

    /**
     * Checks if there is a committee candidate or member with {@code publicKey}.
     *
     * @param publicKey The candidates public key.
     * @return true if the public key belongs to a candidate. False otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isCandidate(ECPublicKey publicKey) throws IOException {
        return getCandidates().containsKey(publicKey);
    }

    /**
     * Gets the number of GAS generated in each block.
     *
     * @return the max GAS amount per block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getGasPerBlock() throws IOException {
        return callFuncReturningInt(GET_GAS_PER_BLOCK);
    }

    /**
     * Creates a transaction script to set the number of GAS generated in each block and
     * initializes a {@link TransactionBuilder} based on this script.
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
        return callFuncReturningInt(GET_REGISTER_PRICE);
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
        return new NeoAccountState(balance, updateHeight,
                new ECPublicKey(publicKeyItem.getHexString()));
    }

}
