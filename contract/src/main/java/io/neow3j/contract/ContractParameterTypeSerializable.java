package io.neow3j.contract;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.model.types.ContractParameterType;

import java.io.IOException;

public class ContractParameterTypeSerializable extends NeoSerializable {

    private ContractParameterType contractParameterType;

    public ContractParameterTypeSerializable() {
    }

    public ContractParameterTypeSerializable(ContractParameterType contractParameterType) {
        this.contractParameterType = contractParameterType;
    }

    public ContractParameterType getContractParameterType() {
        return contractParameterType;
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.contractParameterType = ContractParameterType.valueOf(reader.readByte());
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(contractParameterType.byteValue());
    }

}
