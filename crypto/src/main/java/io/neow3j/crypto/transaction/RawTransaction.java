package io.neow3j.crypto.transaction;

import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.model.types.TransactionType;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transaction class used for signing transactions locally.<br>
 */
public abstract class RawTransaction extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(RawTransaction.class);

    private TransactionType transactionType;
    private byte version;
    private List<RawTransactionAttribute> attributes;
    private List<RawTransactionInput> inputs;
    private List<RawTransactionOutput> outputs;
    private List<RawScript> scripts;

    public RawTransaction() {}

    protected RawTransaction(Builder builder) {
        this.transactionType = builder.transactionType;
        this.version = builder.version;
        this.attributes = builder.attributes;
        this.inputs = builder.inputs;
        this.outputs = builder.outputs;
        this.scripts = builder.scripts;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public byte getVersion() {
        return version;
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

    /**
     * Adds the given invocation script (e.g. signatures) and the verification script to this
     * transaction's list of witnesses.
     * @param invocationScript The more invocation script of the witness.
     * @param verificationScript The verification script of the witness.
     */
    public void addScript(RawInvocationScript invocationScript, RawVerificationScript verificationScript) {
        this.scripts.add(new RawScript(invocationScript, verificationScript));
    }

    public void addScript(RawScript script) {
        this.scripts.add(script);
    }

    public String getTxId() {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            serializeWithoutScripts(new BinaryWriter(byteStream));
            byte[] hash = Hash.sha256(Hash.sha256(byteStream.toByteArray()));
            return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(hash));
        } catch (IOException e) {
            throw new IllegalStateException("Got an IOException without doing IO.");
        }
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.transactionType = TransactionType.valueOf(reader.readByte());
        this.version = reader.readByte();
        try {
            deserializeExclusive(reader);
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

    private void serializeWithoutScripts(BinaryWriter writer) throws IOException {
        writer.writeByte(this.transactionType.byteValue());
        writer.writeByte(this.version);
        serializeExclusive(writer);
        writer.writeSerializableVariable(this.attributes);
        writer.writeSerializableVariable(this.inputs);
        writer.writeSerializableVariable(this.outputs);
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        serializeWithoutScripts(writer);
        if (this.scripts.size() != 0) {
            writer.writeSerializableVariable(this.scripts);
        }
    }

    public abstract void serializeExclusive(BinaryWriter writer) throws IOException;

    public abstract void deserializeExclusive(BinaryReader reader) throws IOException, IllegalAccessException, InstantiationException;

    protected static abstract class Builder<T extends Builder<T>> {

        private TransactionType transactionType;
        private byte version;
        private List<RawTransactionAttribute> attributes;
        private List<RawTransactionInput> inputs;
        private List<RawTransactionOutput> outputs;
        private List<RawScript> scripts;

        protected Builder() {
            this.version = TransactionType.DEFAULT_VERSION;
            this.attributes = new ArrayList<>();
            this.inputs = new ArrayList<>();
            this.outputs = new ArrayList<>();
            this.scripts = new ArrayList<>();
        }

        protected T transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            this.version = transactionType.version();
            return (T) this;
        }

        public T version(byte version) {
            this.version = version; return (T) this;
        }

        public T attributes(List<RawTransactionAttribute> attributes) {
            this.attributes.addAll(attributes); return (T) this;
        }

        public T attributes(RawTransactionAttribute attribute) {
            return attributes(Arrays.asList(attribute));
        }

        public T inputs(List<RawTransactionInput> inputs) {
            this.inputs.addAll(inputs); return (T) this;
        }

        public T input(RawTransactionInput input) {
            return inputs(Arrays.asList(input));
        }

        public T outputs(List<RawTransactionOutput> outputs) {
            this.outputs.addAll(outputs); return (T) this;
        }

        public T output(RawTransactionOutput output) {
            return outputs(Arrays.asList(output));
        }

        public T scripts(List<RawScript> scripts) {
            this.scripts.addAll(scripts); return (T) this;
        }

        public T script(RawScript script) {
            return scripts(Arrays.asList(script));
        }

        public abstract RawTransaction build();
    }
}
