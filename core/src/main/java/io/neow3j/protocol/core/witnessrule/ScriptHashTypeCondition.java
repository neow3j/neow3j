package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.transaction.witnessrule.WitnessConditionType;
import io.neow3j.types.Hash160;

public class ScriptHashTypeCondition extends WitnessCondition {

    @JsonProperty("hash")
    protected Hash160 scriptHash;

    protected ScriptHashTypeCondition(WitnessConditionType type) {
        super(type);
    }

    @Override
    public Hash160 getScriptHash() {
        return scriptHash;
    }

}
