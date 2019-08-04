package io.neow3j.contract;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractDeploymentScript extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(ContractDeploymentScript.class);

    private byte[] scriptBinary;

    private ContractDescriptionProperties descriptionProperties;

    private ContractFunctionProperties functionProperties;

    private ScriptHash contractScriptHash;

    public ContractDeploymentScript() {
    }

    public ContractDeploymentScript(byte[] scriptBinary, ContractFunctionProperties functionProperties, ContractDescriptionProperties descriptionProperties) {
        this.scriptBinary = scriptBinary;
        this.functionProperties = functionProperties;
        this.descriptionProperties = descriptionProperties;
        this.contractScriptHash = ScriptHash.fromScript(scriptBinary);
    }

    public byte[] getScriptBinary() {
        return scriptBinary;
    }

    public ContractDescriptionProperties getDescriptionProperties() {
        return descriptionProperties;
    }

    public ContractFunctionProperties getFunctionProperties() {
        return functionProperties;
    }

    public String getScriptHashHexNoPrefix() {
        return Numeric.toHexStringNoPrefix(contractScriptHash.toArray());
    }

    public ScriptHash getContractScriptHash() {
        return contractScriptHash;
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        try {
            this.descriptionProperties = reader.readSerializable(ContractDescriptionProperties.class);
            this.functionProperties = reader.readSerializable(ContractFunctionProperties.class);
            this.scriptBinary = reader.readPushData();
            this.contractScriptHash = ScriptHash.fromScript(this.scriptBinary);;
            // TODO: 2019-08-01 Guil:
            // Should we read the syscall?
        } catch (IllegalAccessException e) {
            LOG.error("Can't access the specified object.", e);
        } catch (InstantiationException e) {
            LOG.error("Can't instantiate the specified object type.", e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        // description properties (i.e., description,
        // email, author, version, name)
        writer.writeSerializableFixed(this.descriptionProperties);
        // function properties (i.e., parameter types, return type,
        // needs storage, needs dynamic invoke, is payable)
        writer.writeSerializableFixed(this.functionProperties);
        // script binary (.avm)
        writer.write(new ScriptBuilder()
                .pushData(this.scriptBinary)
                .toArray());
        // syscall "Neo.Contract.Create"
        writer.write(new ScriptBuilder()
                .sysCall("Neo.Contract.Create")
                .toArray());
    }

}
