package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoToken extends Nep5Token {

    public final static int DECIMALS = 0;
    public final static String NAME = "NEO";
    public final static String SYMBOL = "neo";
    public final static BigInteger TOTAL_SUPPLY = new BigInteger("100000000");
    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_NEO).toArray());

    public static final String UNCLAIMED_GAS = "unclaimedGas";
    public static final String REGISTER_VALIDATOR = "registerValidator";
    public static final String GET_VALIDATORS = "getValidators";
    public static final String GET_REGISTERED_VALIDATORS = "getRegisteredValidators";
    public static final String GET_NEXT_BLOCK_VALIDATOR = "getNextBlockValidators";
    public static final String VOTE = "vote";

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
     * Creates and sends a transaction registering a validator candidate
     *
     * @param wallet       The wallet paying the transaction fees.
     * @param validatorKey The public key to register as a candidate.
     * @return the hash of the created transaction.
     * @throws IOException            if there was a problem fetching information from the Neo
     *                                node.
     * @throws ErrorResponseException if the registration was unsuccessful.
     */
    public String registerValidator(Wallet wallet, ECPublicKey validatorKey)
            throws IOException, ErrorResponseException {
        NeoSendRawTransaction response = invoke(REGISTER_VALIDATOR)
                .withWallet(wallet)
                .withParameters(ContractParameter.publicKey(validatorKey.getEncoded(true)))
                .failOnFalse()
                .build()
                .sign()
                .send();

        response.throwOnError();

        return response.getResult();
    }

    /**
     * Gets the public keys of all current validators.
     *
     * @return the validators' public keys.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException If the return type is not an array or the array
     *                                       elements are not public keys.
     */
    public List<ECPublicKey> getValidators() throws IOException {
        StackItem arrayItem = callFunction(GET_VALIDATORS);
        if (!arrayItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), StackItemType.ARRAY);
        }
        List<ECPublicKey> valKeys = new ArrayList<>();
        for (StackItem keyItem : arrayItem.asArray().getValue()) {
            valKeys.add(extractPublicKey(keyItem));
        }
        return valKeys;
    }

    /**
     * Gets the public keys of currently registered validators and the number of their backup nodes.
     *
     * @return the registered validators public keys and the number of their backup nodes.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException If the return type is not an array or the array
     *                                       elements are not public keys and node counts.
     */
    public Map<ECPublicKey, Integer> getRegisteredValidators() throws IOException {
        StackItem arrayItem = callFunction(GET_REGISTERED_VALIDATORS);
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
            BigInteger nr = nrItem.asInteger().getValue();

            validators.put(key, nr.intValue());
        }
        return validators;
    }

    private ECPublicKey extractPublicKey(StackItem keyItem) {
        if (!keyItem.getType().equals(StackItemType.BYTE_ARRAY)) {
            throw new UnexpectedReturnTypeException(keyItem.getType(),
                    StackItemType.BYTE_ARRAY);
        }
        ECPublicKey key;
        try {
            key = new ECPublicKey(keyItem.asByteArray().getValue());
        } catch (IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Byte array return type did not contain "
                    + "public key in expected format.", e);
        }
        return key;
    }

    // TODO: Implement method for GET_NEXT_BLOCK_VALIDATOR

    // TODO: Implement method for VOTE
}

