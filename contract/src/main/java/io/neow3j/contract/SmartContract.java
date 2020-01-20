package io.neow3j.contract;

import io.neow3j.contract.Invocation.InvocationBuilder;
import io.neow3j.protocol.Neow3j;

public class SmartContract {

    protected ScriptHash scriptHash;
    protected Neow3j neow;

    /**
     * @param scriptHash
     * @param neow
     */
    public SmartContract(ScriptHash scriptHash, Neow3j neow) {
        if (scriptHash == null)  {
            throw new IllegalArgumentException("The contract script hash must not be null.");
        }
        if (neow == null)  {
            throw new IllegalArgumentException("The Neow3j object must not be null.");
        }
        this.scriptHash = scriptHash;
        this.neow = neow;
    }

    /**
     * @param function
     * @return
     */
    public InvocationBuilder invoke(String function) {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException(
                "The invocation function must not be null or empty.");
        }
        return new InvocationBuilder(neow, scriptHash, function);
    }

}
