package io.neow3j.crypto.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.model.types.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction class used for signing transactions locally.<br>
 */
public class RawTransaction extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(RawTransaction.class);

    public static final byte TRANSACTION_VERSION = 0x00;

    private TransactionType transactionType;
    private byte version;
    private List<Object> specificTransactionData;
    private List<RawTransactionAttribute> attributes;
    private List<RawTransactionInput> inputs;
    private List<RawTransactionOutput> outputs;
    private List<RawScript> scripts;

    public RawTransaction() {
    }

    protected RawTransaction(TransactionType transactionType, byte version, List<Object> specificTransactionData, List<RawTransactionAttribute> attributes, List<RawTransactionInput> inputs, List<RawTransactionOutput> outputs, List<RawScript> scripts) {
        this.transactionType = transactionType;
        this.version = version;
        this.specificTransactionData = specificTransactionData != null ? specificTransactionData : new ArrayList<>();
        this.attributes = attributes != null ? attributes : new ArrayList<>();
        this.inputs = inputs != null ? inputs : new ArrayList<>();
        this.outputs = outputs != null ? outputs : new ArrayList<>();
        this.scripts = scripts != null ? scripts : new ArrayList<>();
    }

    public static RawTransaction createTransaction(TransactionType transactionType) {
        return createTransaction(transactionType, TRANSACTION_VERSION);
    }

    public static RawTransaction createTransaction(TransactionType transactionType, byte version) {
        return createTransaction(transactionType, version, null, null, null, null, null);
    }

    public static RawTransaction createTransaction(TransactionType transactionType, byte version, List<Object> specificTransactionData, List<RawTransactionAttribute> attributes, List<RawTransactionInput> inputs, List<RawTransactionOutput> outputs) {
        return new RawTransaction(transactionType, version, specificTransactionData, attributes, inputs, outputs, null);
    }

    public static RawTransaction createTransaction(TransactionType transactionType, byte version, List<Object> specificTransactionData, List<RawTransactionAttribute> attributes, List<RawTransactionInput> inputs, List<RawTransactionOutput> outputs, List<RawScript> scripts) {
        return new RawTransaction(transactionType, version, specificTransactionData, attributes, inputs, outputs, scripts);
    }

    public static RawTransaction createContractTransaction() {
        return createTransaction(TransactionType.CONTRACT_TRANSACTION);
    }

    public static RawTransaction createContractTransaction(List<Object> specificTransactionData, List<RawTransactionAttribute> attributes, List<RawTransactionInput> inputs, List<RawTransactionOutput> outputs) {
        return createTransaction(TransactionType.CONTRACT_TRANSACTION, TRANSACTION_VERSION, specificTransactionData, attributes, inputs, outputs);
    }

    public static RawTransaction createContractTransaction(List<Object> specificTransactionData, List<RawTransactionAttribute> attributes, List<RawTransactionInput> inputs, List<RawTransactionOutput> outputs, List<RawScript> scripts) {
        return createTransaction(TransactionType.CONTRACT_TRANSACTION, TRANSACTION_VERSION, specificTransactionData, attributes, inputs, outputs, scripts);
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public byte getVersion() {
        return version;
    }

    public List<Object> getSpecificTransactionData() {
        return specificTransactionData;
    }

    public List<RawTransactionAttribute> getAttributes() {
        return attributes;
    }

    public List<RawTransactionInput> getInputs() {
        return inputs;
    }

    public List<RawTransactionOutput> getOutputs() {
        return outputs;
    }

    public List<RawScript> getScripts() {
        return scripts;
    }

    public void addScript(List<RawInvocationScript> invocationScript, RawVerificationScript verificationScript) {
        this.scripts.add(new RawScript(invocationScript, verificationScript));
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.transactionType = TransactionType.valueOf(reader.readByte());
        this.version = reader.readByte();
        try {
            this.attributes = reader.readSerializableList(RawTransactionAttribute.class);
            this.inputs = reader.readSerializableList(RawTransactionInput.class);
            this.outputs = reader.readSerializableList(RawTransactionOutput.class);
            this.scripts = reader.readSerializableList(RawScript.class);
        } catch (IllegalAccessException e) {
            LOG.error("Can't access the specified object.", e);
        } catch (InstantiationException e) {
            LOG.error("Can't instantiate the specified object type.", e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte(this.transactionType.byteValue());
        writer.writeByte(this.version);
        writer.writeSerializableVariable(this.attributes);
        writer.writeSerializableVariable(this.inputs);
        writer.writeSerializableVariable(this.outputs);
        if (this.scripts.size() != 0) {
            writer.writeSerializableVariable(this.scripts);
        }
    }

}
