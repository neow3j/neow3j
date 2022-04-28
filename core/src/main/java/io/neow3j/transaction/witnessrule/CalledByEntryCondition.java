package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.ContractSigner;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

/**
 * This is the same as a signer with {@link WitnessScope#CALLED_BY_ENTRY} (see
 * {@link AccountSigner#calledByEntry(Account)} or {@link ContractSigner#calledByEntry(Hash160, ContractParameter...)}).
 */
public class CalledByEntryCondition extends WitnessCondition {

    /**
     * Constructs a witness condition of type {@link WitnessConditionType#CALLED_BY_ENTRY}.
     */
    public CalledByEntryCondition() {
        type = WitnessConditionType.CALLED_BY_ENTRY;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) {
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) {
    }

    @Override
    public int getSize() {
        return super.getSize();
    }

    @Override
    public io.neow3j.protocol.core.witnessrule.WitnessCondition toDTO() {
        return new io.neow3j.protocol.core.witnessrule.CalledByEntryCondition();
    }

}
