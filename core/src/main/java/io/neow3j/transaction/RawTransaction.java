package io.neow3j.transaction;

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
import java.util.Comparator;
import java.util.List;

/**
 * <p>Transaction class used for signing transactions locally.</p>
 */
@SuppressWarnings("unchecked")
public abstract class RawTransaction extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(RawTransaction.class);

    private TransactionType transactionType;
    private byte version;
    private List<RawTransactionAttribute> attributes;
    private List<RawTransactionInput> inputs;
    private List<RawTransactionOutput> outputs;
    private List<RawScript> scripts;

    public RawTransaction() {
    }

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
     *
     * @param invocationScript   The invocation script of the witness.
     * @param verificationScript The verification script of the witness.
     */
    public void addScript(RawInvocationScript invocationScript, RawVerificationScript verificationScript) {
        addScript(new RawScript(invocationScript, verificationScript));
    }

    public void addScript(RawScript script) {
        if (script.getScriptHash() == null || script.getScriptHash().length() == 0) {
            throw new IllegalArgumentException("The script hash of the given script is " +
                    "empty. Please set the script hash.");
        }
        this.scripts.add(script);
        this.scripts.sort(Comparator.comparing(RawScript::getScriptHash));
    }

    public String getTxId() {
        byte[] hash = Hash.sha256(Hash.sha256(toArrayWithoutScripts()));
        return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(hash));
    }

    public int getSize() {
        // TODO 2019-08-05 claude:
        // Implement more efficiently, e.g. with fixed byte values and calls to the getSize() of
        // the transaction components.
        return toArray().length;
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
        writer.writeSerializableVariable(this.scripts);
    }

    public abstract void serializeExclusive(BinaryWriter writer) throws IOException;

    public abstract void deserializeExclusive(BinaryReader reader) throws IOException, IllegalAccessException, InstantiationException;

    /**
     * Serializes this transaction to a raw byte array without any scripts. This is required if the
     * serialized transaction gets signed, e.g. by an external keypair/provider.
     *
     * @return the serialized transaction
     */
    public byte[] toArrayWithoutScripts() {
        try (ByteArrayOutputStream ms = new ByteArrayOutputStream()) {
            try (BinaryWriter writer = new BinaryWriter(ms)) {
                serializeWithoutScripts(writer);
                writer.flush();
                return ms.toByteArray();
            }
        } catch (IOException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    /**
     * Serializes this transaction to a raw byte array including scripts (witnesses/signatures).
     * The byte array can be sent as a transaction with the `sendrawtransaction` RPC method.
     *
     * @return the serialized transaction.
     */
    @Override
    public byte[] toArray() {
        return super.toArray();
    }

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
            this.version = version;
            return (T) this;
        }

        public T attributes(List<RawTransactionAttribute> attributes) {
            this.attributes.addAll(attributes);
            return (T) this;
        }

        public T attribute(RawTransactionAttribute attribute) {
            return attributes(Arrays.asList(attribute));
        }

        public T inputs(List<RawTransactionInput> inputs) {
            this.inputs.addAll(inputs);
            return (T) this;
        }

        public T input(RawTransactionInput input) {
            return inputs(Arrays.asList(input));
        }

        public T outputs(List<RawTransactionOutput> outputs) {
            this.outputs.addAll(outputs);
            return (T) this;
        }

        public T output(RawTransactionOutput output) {
            return outputs(Arrays.asList(output));
        }

        public T scripts(List<RawScript> scripts) {
            for (RawScript script : scripts) {
                if (script.getScriptHash() == null || script.getScriptHash().length() == 0) {
                    throw new IllegalArgumentException("The script hash of the given script is " +
                            "empty. Please set the script hash.");
                }
            }

            this.scripts.addAll(scripts);
            this.scripts.sort(Comparator.comparing(RawScript::getScriptHash));
            return (T) this;
        }

        public T script(RawScript script) {
            return scripts(Arrays.asList(script));
        }

        public abstract RawTransaction build();
    }
}
