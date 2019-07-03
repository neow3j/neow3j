package io.neow3j.contract;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.neow3j.crypto.Hash.sha256AndThenRipemd160;

public class ContractDeploymentScript extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(ContractDeploymentScript.class);

    private byte[] scriptBinary;

    private ContractDescriptionProperties descriptionProperties;

    private ContractFunctionProperties functionProperties;

    private byte[] scriptHash;

    public ContractDeploymentScript(byte[] scriptBinary, ContractFunctionProperties functionProperties, ContractDescriptionProperties descriptionProperties) {
        this.scriptBinary = scriptBinary;
        this.functionProperties = functionProperties;
        this.descriptionProperties = descriptionProperties;
        this.scriptHash = getScriptHash(scriptBinary);
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
        return Numeric.toHexStringNoPrefix(scriptHash);
    }

    public byte[] getScriptHash() {
        return scriptHash;
    }

    private byte[] getScriptHash(byte[] script) {
        return sha256AndThenRipemd160(script);
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        try {
            this.descriptionProperties = reader.readSerializable(ContractDescriptionProperties.class);
            this.functionProperties = reader.readSerializable(ContractFunctionProperties.class);
            this.scriptBinary = reader.readPushData();
            this.scriptHash = getScriptHash(this.scriptBinary);
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
        writer.pushData(this.scriptBinary);
    }

}
