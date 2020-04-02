package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.protocol.Neow3j;
import java.io.IOException;
import java.math.BigInteger;

public class NeoToken extends Nep5Token {

    public final static int DECIMALS = 0;
    public final static String NAME = "NEO";
    public final static String SYMBOL = "neo";
    public final static BigInteger TOTAL_SUPPLY = new BigInteger("100000000");
    public static final ScriptHash SCRIPT_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_NEO).toArray());

    public static final String UNCLAIMED_GAS = "unClaimGas";
    // TODO: Add methods for the remaining function calls.
    //  See https://github.com/neo-ngd/NEO3-Development-Guide/tree/master/en/SmartContract#neotoken
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
     * @param scriptHash The account's script hash.
     * @param blockHeight The block height.
     * @return the amount of unclaimed GAS
     * @throws IOException            if there was a problem fetching information from the Neo
     *                                node.
     */
    public BigInteger getUnclaimedGas(ScriptHash scriptHash, long blockHeight) throws IOException {
        ContractParameter heightParam = ContractParameter.integer(BigInteger.valueOf(blockHeight));
        ContractParameter accParam = ContractParameter.byteArrayFromAddress(scriptHash.toAddress());
        return callFuncReturningInt(UNCLAIMED_GAS, heightParam, accParam);
    }
}
