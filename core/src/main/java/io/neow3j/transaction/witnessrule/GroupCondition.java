package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.transaction.Signer;

/**
 * This condition allows including or excluding a contract group (with the defined public key) from using the witness.
 * This is the same as adding the group to the scope of a {@link Signer} with
 * {@link Signer#setAllowedGroups(ECKeyPair.ECPublicKey...)}.
 */
public class GroupCondition extends GroupTypeCondition {

    public GroupCondition() {
        type = WitnessConditionType.GROUP;
    }

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#GROUP} with the given group's public key.
     *
     * @param group the group's public key.
     */
    public GroupCondition(ECKeyPair.ECPublicKey group) {
        this();
        this.group = group;
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.GroupCondition(getGroup());
    }

}
