package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the NeoToken native contract and provides methods to invoke all its functions.
 */
public class NeoToken extends Nep5Token {

    public final static String NAME = "NEO";
    public final static int DECIMALS = 0;
    public final static String SYMBOL = "neo";
    public final static BigInteger TOTAL_SUPPLY = new BigInteger("100000000");

    private static final byte[] SCRIPT = new ScriptBuilder()
            .pushData(NAME)
            .sysCall(InteropServiceCode.SYSTEM_CONTRACT_CALLNATIVE)
            .toArray();

    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder()
                    .opCode(OpCode.ABORT)
                    .pushData(ScriptHash.ZERO.toArray())
                    .pushData(SCRIPT)
                    .toArray());

    public static final String UNCLAIMED_GAS = "unclaimedGas";
    public static final String REGISTER_CANDIDATE = "registerCandidate";
    public static final String UNREGISTER_CANDIDATE = "unregisterCandidate";
    public static final String VOTE = "vote";
    public static final String GET_CANDIDATES = "getCandidates";
    public static final String GET_VALIDATORS = "getValidators";
    public static final String GET_COMMITTEE = "getCommittee";
    public static final String GET_NEXT_BLOCK_VALIDATORS = "getNextBlockValidators";

    /**
     * Constructs a new {@code NeoToken} that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public NeoToken(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Returns the name of the NeoToken contract. Doesn't require a call to the neo-node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns the symbol of the NeoToken contract. Doesn't require a call to the neo-node.
     *
     * @return the symbol.
     */
    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    /**
     * Returns the total supply of the NeoToken contract. Doesn't require a call to the neo-node.
     *
     * @return the total supply.
     */
    @Override
    public BigInteger getTotalSupply() {
        return TOTAL_SUPPLY;
    }

    /**
     * Returns the number of decimals of the NEO token. Doesn't require a call to the neo-node.
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
     * @param account  the account.
     * @param blockHeight the block height.
     * @return the amount of unclaimed GAS
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
    public BigInteger unclaimedGas(ScriptHash scriptHash, long blockHeight) throws IOException {
        ContractParameter accParam = ContractParameter.hash160(scriptHash);
        ContractParameter heightParam = ContractParameter.integer(BigInteger.valueOf(blockHeight));
        return callFuncReturningInt(UNCLAIMED_GAS, accParam, heightParam);
    }

    /**
     * Creates a transaction script for registering a validator candidate and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param candidateKey The public key to register as a candidate.
     * @return A transaction builder.
     */
    public TransactionBuilder registerCandidate(ECPublicKey candidateKey) {

        return invokeFunction(REGISTER_CANDIDATE,
                ContractParameter.publicKey(candidateKey.getEncoded(true)));
    }

    /**
     * Creates a transaction script for registering a validator candidate and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param candidateKey The public key to register as a candidate.
     * @return A transaction builder.
     */
    public TransactionBuilder unregisterCandidate(ECPublicKey candidateKey) {

        return invokeFunction(UNREGISTER_CANDIDATE,
                ContractParameter.publicKey(candidateKey.getEncoded(true)));
    }

    /**
     * Gets the public keys of all current validators.
     *
     * @return the validators' public keys.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException If the return type is not an array or the returned
     *                                       array's elements are not public keys.
     */
    public List<ECPublicKey> getValidators() throws IOException {
        return callFunctionReturningListOfPublicKeys(GET_VALIDATORS);
    }

    /**
     * Gets the public keys of the current committee members.
     *
     * @return the committee members' public keys.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException If the return type is not an array or the returned
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
     * @throws UnexpectedReturnTypeException If the return type is not an array or the array
     *                                       elements are not public keys and node counts.
     */
    public Map<ECPublicKey, Integer> getCandidates() throws IOException {
        StackItem arrayItem = callInvokeFunction(GET_CANDIDATES).getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), StackItemType.ARRAY);
        }
        Map<ECPublicKey, Integer> validators = new HashMap<>();
        for (StackItem valItem : arrayItem.asArray().getValue()) {
            if (!valItem.getType().equals(StackItemType.ARRAY)) {
                throw new UnexpectedReturnTypeException(valItem.getType(), StackItemType.ARRAY);
            }
            ECPublicKey key = extractPublicKey(valItem.asArray().getValue().get(0));
            StackItem nrItem = valItem.asArray().getValue().get(1);
            if (!nrItem.getType().equals(StackItemType.INTEGER)) {
                throw new UnexpectedReturnTypeException(nrItem.getType(), StackItemType.INTEGER);
            }
            validators.put(key, nrItem.asInteger().getValue().intValue());
        }
        return validators;
    }

    /**
     * Gets the public keys of the next block's validators.
     *
     * @return the validators' public keys.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException If the return type is not an array or the returned
     *                                       array's elements are not public keys.
     */
    public List<ECPublicKey> getNextBlockValidators() throws IOException {
        return callFunctionReturningListOfPublicKeys(GET_NEXT_BLOCK_VALIDATORS);
    }

    private List<ECPublicKey> callFunctionReturningListOfPublicKeys(String function)
            throws IOException {

        StackItem arrayItem = callInvokeFunction(function).getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), StackItemType.ARRAY);
        }
        List<ECPublicKey> valKeys = new ArrayList<>();
        for (StackItem keyItem : arrayItem.asArray().getValue()) {
            valKeys.add(extractPublicKey(keyItem));
        }
        return valKeys;
    }

    private ECPublicKey extractPublicKey(StackItem keyItem) {
        if (!keyItem.getType().equals(StackItemType.BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(keyItem.getType(),
                    StackItemType.BYTE_STRING);
        }
        try {
            return new ECPublicKey(keyItem.asByteString().getValue());
        } catch (IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Byte array return type did not contain "
                    + "public key in expected format.", e);
        }
    }

    /**
     * Creates a transaction script to vote for the given validators and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param voter      The account that casts the vote.
     * @param validators The validators for which to vote for.
     * @return A transaction builder.
     */
    public TransactionBuilder vote(Account voter, ECPublicKey... validators) {
        return vote(voter.getScriptHash(), validators);
    }

    /**
     * Creates a transaction script to vote for the given validators and initializes
     * a {@link TransactionBuilder} based on this script.
     *
     * @param voter      The account that casts the vote.
     * @param validators The validators for which to vote for.
     * @return A transaction builder.
     */
    public TransactionBuilder vote(ScriptHash voter, ECPublicKey... validators) {
        List<ContractParameter> validatorParams = Stream.of(validators)
                .map(v -> ContractParameter.publicKey(v.getEncoded(true)))
                .collect(Collectors.toList());

        ArrayList<ContractParameter> params = new ArrayList<>();
        params.add(ContractParameter.hash160(voter));
        params.addAll(validatorParams);

        return invokeFunction(VOTE, params.toArray(new ContractParameter[]{}));
    }
}
