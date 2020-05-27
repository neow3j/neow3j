package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Cosigner;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents the NeoToken native contract and provides methods to invoke all its functions.
 */
public class NeoToken extends Nep5Token {

    public final static int DECIMALS = 0;
    public final static String NAME = "NEO";
    public final static String SYMBOL = "neo";
    public final static BigInteger TOTAL_SUPPLY = new BigInteger("100000000");
    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_NEO).toArray());

    public static final String UNCLAIMED_GAS = "unclaimedGas";
    public static final String REGISTER_CANDIDATE = "registerCandidate";
    public static final String GET_VALIDATORS = "getValidators";
    public static final String GET_CANDIDATES = "getCandidates";
    public static final String GET_NEXT_BLOCK_VALIDATORS = "getNextBlockValidators";
    public static final String VOTE = "vote";

    /**
     * Constructs a new <tt>NeoToken</tt> which will use the given {@link Neow3j} for all
     * interactions with the contract.
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public NeoToken(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

    @Override
    public BigInteger getTotalSupply() {
        return TOTAL_SUPPLY;
    }

    @Override
    public int getDecimals() {
        return DECIMALS;
    }

    /**
     * Gets the amount of unclaimed GAS at the given height for the given account.
     *
     * @param scriptHash  The account's script hash.
     * @param blockHeight The block height.
     * @return the amount of unclaimed GAS
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getUnclaimedGas(ScriptHash scriptHash, long blockHeight) throws IOException {
        ContractParameter accParam = ContractParameter.hash160(scriptHash);
        ContractParameter heightParam = ContractParameter.integer(BigInteger.valueOf(blockHeight));
        return callFuncReturningInt(UNCLAIMED_GAS, accParam, heightParam);
    }

    /**
     * Creates and sends a transaction registering a validator candidate.
     *
     * @param candidate    The script hash of the candidate's account.
     * @param wallet       The wallet containing the candidate's account.
     * @param candidateKey The public key to register as a candidate.
     * @return the hash of the created transaction.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NeoSendRawTransaction registerCandidate(ScriptHash candidate, Wallet wallet,
            ECPublicKey candidateKey)
            throws IOException {

        return buildRegisterInvocation(candidate, wallet, candidateKey).send();
    }

    // Method extracted for testability.
    Invocation buildRegisterInvocation(ScriptHash candidate, Wallet wallet,
            ECPublicKey candidateKey)
            throws IOException {

        return invoke(REGISTER_CANDIDATE)
                .withSender(candidate)
                .withWallet(wallet)
                .withParameters(ContractParameter.publicKey(candidateKey.getEncoded(true)))
                .withAttributes(Cosigner.global(candidate))
                .build()
                .sign();
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
     * Gets the public keys of currently registered validator candidates and their NEO
     * balances.
     *
     * @return the candidate public keys and their NEO balances.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException If the return type is not an array or the array
     *                                       elements are not public keys and node counts.
     */
    public Map<ECPublicKey, Integer> getCandidates() throws IOException {
        StackItem arrayItem = callFunction(GET_CANDIDATES);
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

        StackItem arrayItem = callFunction(function);
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
     * Creates and sends a transaction that votes for the given validators.
     *
     * @param voter      The account that casts the vote.
     * @param wallet     The wallet that contains the vote-casting account.
     * @param validators The validators for which to vote for.
     * @return the response from the neo-node.
     * @throws IOException if something goes wrong when communicating with the neo-node.
     */
    public NeoSendRawTransaction vote(ScriptHash voter, Wallet wallet, ECPublicKey... validators)
            throws IOException {

        return buildVoteInvocation(voter, wallet, validators).send();
    }

    // Method extracted for testability.
    Invocation buildVoteInvocation(ScriptHash voter, Wallet wallet, ECPublicKey... validators)
            throws IOException {

        ContractParameter[] validatorParams = Stream.of(validators)
                .map(v -> ContractParameter.publicKey(v.getEncoded(true)))
                .toArray(ContractParameter[]::new);

        return invoke(VOTE)
                .withSender(voter)
                .withWallet(wallet)
                .withParameters(ContractParameter.hash160(voter))
                .withParameters(validatorParams)
                .withAttributes(Cosigner.global(voter))
                .build()
                .sign();
    }
}

