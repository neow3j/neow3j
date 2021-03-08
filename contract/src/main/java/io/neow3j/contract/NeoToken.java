package io.neow3j.contract;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractParameter.publicKey;
import static io.neow3j.model.types.StackItemType.ARRAY;
import static io.neow3j.model.types.StackItemType.BYTE_STRING;
import static io.neow3j.model.types.StackItemType.INTEGER;
import static io.neow3j.model.types.StackItemType.STRUCT;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the NeoToken native contract and provides methods to invoke its functions.
 */
public class NeoToken extends FungibleToken {

    public final static String NAME = "NeoToken";
    public final static long NEF_CHECKSUM = 3921333105L;
    public static final Hash160 SCRIPT_HASH = getScriptHashOfNativeContract(NEF_CHECKSUM, NAME);

    public final static int DECIMALS = 0;
    public final static String SYMBOL = "NEO";
    public final static BigInteger TOTAL_SUPPLY = new BigInteger("100000000");

    public static final String UNCLAIMED_GAS = "unclaimedGas";
    public static final String REGISTER_CANDIDATE = "registerCandidate";
    public static final String UNREGISTER_CANDIDATE = "unregisterCandidate";
    public static final String VOTE = "vote";
    public static final String GET_CANDIDATES = "getCandidates";
    public static final String GET_COMMITTEE = "getCommittee";
    public static final String GET_NEXT_BLOCK_VALIDATORS = "getNextBlockValidators";
    public static final String SET_GAS_PER_BLOCK = "setGasPerBlock";
    public static final String GET_GAS_PER_BLOCK = "getGasPerBlock";

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
     * Creates a {@code TransactionBuilder} for registering a committee candidate with the given
     * public key.
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
     * Gets the public keys of currently registered validator candidates and their NEO balances.
     *
     * @return the candidate public keys and their NEO balances.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the return type is not an array or the array
     *                                       elements are not public keys and node counts.
     */
    public Map<ECPublicKey, Integer> getCandidates() throws IOException {
        StackItem arrayItem = callInvokeFunction(GET_CANDIDATES)
                .getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), ARRAY);
        }
        Map<ECPublicKey, Integer> validators = new HashMap<>();
        for (StackItem valItem : arrayItem.getList()) {
            if (!valItem.getType().equals(STRUCT)) {
                throw new UnexpectedReturnTypeException(valItem.getType(), STRUCT);
            }
            ECPublicKey key = extractPublicKey(valItem.getList().get(0));
            StackItem nrItem = valItem.getList().get(1);
            if (!nrItem.getType().equals(INTEGER)) {
                throw new UnexpectedReturnTypeException(nrItem.getType(), INTEGER);
            }
            validators.put(key, nrItem.getInteger().intValue());
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
            throw new UnexpectedReturnTypeException(
                    "Byte array return type did not contain public key in expected format.", e);
        }
    }

    /**
     * Creates a transaction script to vote for the given validators and
     * initializes a {@link TransactionBuilder} based on this script.
     *
     * @param voter     the account that casts the vote.
     * @param candidate the candidate to vote for.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder vote(Account voter, ECPublicKey candidate) throws IOException {
        return vote(voter.getScriptHash(), candidate);
    }

    /**
     * Creates a transaction script to vote for the given validators and
     * initializes a {@link TransactionBuilder} based on this script.
     *
     * @param voter     the account that casts the vote.
     * @param candidate the candidate to vote for.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder vote(Hash160 voter, ECPublicKey candidate) throws IOException {
        if (!isCandidate(candidate)) {
            throw new IllegalArgumentException("The provided public key is not a candidate. Only " +
                    "candidates can be voted for.");
        }
        return invokeFunction(VOTE, hash160(voter),
                publicKey(candidate.getEncoded(true)));
    }

    private boolean isCandidate(ECPublicKey publicKey) throws IOException {
        Map<ECPublicKey, Integer> candidates = getCandidates();
        return candidates.containsKey(publicKey);
    }

    /**
     * Gets the max GAS amount per block. This sets a cap on the accumulated GAS cost of all
     * transactions in a block.
     *
     * @return the max GAS amount per block.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getGasPerBlock() throws IOException {
        return callFuncReturningInt(GET_GAS_PER_BLOCK);
    }

    /**
     * Creates a transaction build for setting the GAS amount per block.
     * <p>
     * This contract invocation can only be successful if it is signed by the network committee.
     *
     * @param gasPerBlock the desired maximum amount of GAS in one block.
     * @return the transaction builder.
     */
    public TransactionBuilder setGasPerBlock(int gasPerBlock) {
        return invokeFunction(SET_GAS_PER_BLOCK, integer(gasPerBlock));
    }

}
