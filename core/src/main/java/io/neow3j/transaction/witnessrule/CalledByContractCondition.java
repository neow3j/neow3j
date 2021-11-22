package io.neow3j.transaction.witnessrule;

import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash160;

import java.io.IOException;

/**
 * This condition defines that the calling contract must match the specified script hash. I.e., a
 * contract can only use the witness when it is invoked by the here specified contract.
 */
public class CalledByContractCondition extends WitnessCondition {

    private Hash160 hash;

    public CalledByContractCondition() {
        type = WitnessConditionType.CALLED_BY_CONTRACT;
    }

    /**
     * Constructs a condition with the given contract script hash.
     *
     * @param hash The contract script hash.
     */
    public CalledByContractCondition(Hash160 hash) {
        this();
        this.hash = hash;
    }

    @Override
    protected void deserializeWithoutType(BinaryReader reader) throws DeserializationException {
        hash = reader.readSerializable(Hash160.class);
    }

    @Override
    protected void serializeWithoutType(BinaryWriter writer) throws IOException {
        writer.writeSerializableFixed(hash);
    }


    @Override
    public int getSize() {
        return hash.getSize();
    }
}
