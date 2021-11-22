package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.ContractSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

/**
 * This condition allows including or excluding a contract group (with the defined EC point)
 * from using the witness. This is the same as a called by entry signer (see
 * {@link AccountSigner#calledByEntry(Account)} or
 * {@link ContractSigner#calledByEntry(Hash160, ContractParameter...)}).
 */
public class CalledByEntryCondition extends WitnessCondition {

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
        return 0;
    }
}
