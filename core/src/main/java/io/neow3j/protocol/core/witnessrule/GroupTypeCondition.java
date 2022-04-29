package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.transaction.witnessrule.WitnessConditionType;

public class GroupTypeCondition extends WitnessCondition {

    @JsonProperty("group")
    protected ECKeyPair.ECPublicKey pubKey;

    protected GroupTypeCondition(WitnessConditionType type) {
        super(type);
    }

    @Override
    public ECKeyPair.ECPublicKey getGroup() {
        return pubKey;
    }

}
