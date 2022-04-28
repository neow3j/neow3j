package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;

/**
 * This condition defines that the calling contract must be part of the specified contract group. I.e., a contract
 * can only use the witness when it is invoked by a contract that is part of the here specified contract group.
 */
public class CalledByGroupCondition extends GroupTypeCondition {

    public CalledByGroupCondition() {
        type = WitnessConditionType.CALLED_BY_GROUP;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#CALLED_BY_GROUP} with the given group's
     * public key.
     *
     * @param group the group's public key.
     */
    public CalledByGroupCondition(ECKeyPair.ECPublicKey group) {
        this();
        this.group = group;
    }
    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.CalledByGroupCondition(getGroup());
    }

}
