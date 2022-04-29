package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.transaction.witnessrule.WitnessConditionType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupCondition extends GroupTypeCondition {

    public GroupCondition() {
        super(WitnessConditionType.GROUP);
    }

    public GroupCondition(ECPublicKey pubKey) {
        this();
        this.pubKey = pubKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupCondition)) {
            return false;
        }
        GroupCondition other = (GroupCondition) o;
        return getType() == other.getType() &&
                Objects.equals(getGroup(), other.getGroup());
    }

    @Override
    public String toString() {
        return "GroupCondition{" +
                "group=" + getGroup() +
                "}";
    }

}
