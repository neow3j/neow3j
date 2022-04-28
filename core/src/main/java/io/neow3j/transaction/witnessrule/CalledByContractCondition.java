package io.neow3j.transaction.witnessrule;

import io.neow3j.types.Hash160;

/**
 * This condition defines that the calling contract must match the specified script hash. I.e., a contract can only
 * use the witness when it is invoked by the here specified contract.
 */
public class CalledByContractCondition extends ScriptHashTypeCondition {

    public CalledByContractCondition() {
        type = WitnessConditionType.CALLED_BY_CONTRACT;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#CALLED_BY_CONTRACT} with the given contract
     * script hash.
     *
     * @param scriptHash the contract script hash.
     */
    public CalledByContractCondition(Hash160 scriptHash) {
        this();
        this.scriptHash = scriptHash;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.CalledByContractCondition(getScriptHash());
    }

}
