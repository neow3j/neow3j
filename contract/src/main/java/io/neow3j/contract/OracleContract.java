package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.ByteStringStackItem;

/**
 * Represents an Oracle contract and provides methods to invoke it.
 */
public class OracleContract extends SmartContract {

    private static final String NAME = "OracleContract";
    public static final ScriptHash SCRIPT_HASH = getScriptHashOfNativeContract(NAME);

    private static final String FINISH = "finish";
    private static final String REQUEST = "request";
    private static final String VERIFY = "verify";

    /**
     * Constructs a new <tt>Oracle</tt> that uses the given {@link Neow3j} instance for
     * invocations.
     *
     * @param neow The {@link Neow3j} instance to use for invocations.
     */
    public OracleContract(Neow3j neow) {
        super(SCRIPT_HASH, neow);
    }

    /**
     * Creates a transaction script for invoking the method {@code finish} of the Oracle contract
     * and initializes a {@link TransactionBuilder} based on this script.
     *
     * @return A transaction builder.
     */
    public TransactionBuilder finish() {
        return invokeFunction(FINISH);
    }

    /**
     * Creates a transaction script for invoking the method {@code request} of the Oracle contract
     * and initializes a {@link TransactionBuilder} based on this script.
     *
     * @param url The URL.
     * @param filter The filter.
     * @param callback The callback.
     * @param userData The user data.
     * @param gasForResponse The amount of gas used for the response.
     * @return A transaction builder.
     */
    public TransactionBuilder request(String url, String filter, String callback, ByteStringStackItem userData,
                                      int gasForResponse) {
        // TODO: 14.12.20 Michael: check correct handling of needed parameters for this invocation.
        return invokeFunction(REQUEST, ContractParameter.string(url), ContractParameter.string(filter),
                ContractParameter.byteArray(userData.getValue()), ContractParameter.integer(gasForResponse));
    }

    /**
     * Creates a transaction script for invoking the method {@code verify} of the Oracle contract
     * and intitializes a {@link TransactionBuilder} based on this script.
     *
     * @return A transaction builder.
     */
    public TransactionBuilder verify() {
        return invokeFunction(VERIFY);
    }
}
