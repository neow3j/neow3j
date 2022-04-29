package io.neow3j.transaction.witnessrule;

import io.neow3j.transaction.Signer;
import io.neow3j.types.Hash160;

/**
 * This condition allows including or excluding a specific contract (with the defined script hash) from using the
 * witness. This is the same as adding the contract to the scope of a {@link Signer} with
 * {@link Signer#setAllowedContracts(Hash160...)}.
 */
public class ScriptHashCondition extends ScriptHashTypeCondition {

    public ScriptHashCondition() {
        type = WitnessConditionType.SCRIPT_HASH;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#SCRIPT_HASH} with the given contract script
     * hash.
     *
     * @param scriptHash the contract script hash.
     */
    public ScriptHashCondition(Hash160 scriptHash) {
        this();
        this.scriptHash = scriptHash;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.ScriptHashCondition(getScriptHash());
    }

}
