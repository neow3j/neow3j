package io.neow3j.crypto.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.model.types.TransactionType;

import java.util.List;

public class ContractTransaction extends RawTransaction {

    public ContractTransaction() {
    }

    public ContractTransaction(List<RawTransactionAttribute> attributes,
                               List<RawTransactionInput> inputs, List<RawTransactionOutput> outputs,
                               List<RawScript> scripts) {

        super(TransactionType.CONTRACT_TRANSACTION, attributes, inputs, outputs, scripts);
    }

    @Override
    public void serializeExclusive(BinaryWriter writer) {
        // no type-specific serialization.
    }

    @Override
    public void deserializeExclusive(BinaryReader reader) {
        // no type-specific deserialization.
    }
}
