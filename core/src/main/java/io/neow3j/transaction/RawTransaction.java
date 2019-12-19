package io.neow3j.transaction;

import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.types.TransactionType;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
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

    private TransactionType transactionType;
    private byte version;
    private List<TransactionAttribute> attributes;
    private List<RawTransactionInput> inputs;
    private List<RawTransactionOutput> outputs;
    private List<Witness> scripts;

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

    public List<TransactionAttribute> getAttributes() {
        return attributes;
    }

    public List<RawTransactionInput> getInputs() {
        return inputs;
    }

    public List<RawTransactionOutput> getOutputs() {
        return outputs;
    }

    public List<Witness> getScripts() {
        return scripts;
    }

    /**
     * Adds the given invocation script (e.g. signatures) and the verification script to this
     * transaction's list of witnesses.
     *
     * @param invocationScript   The invocation script of the witness.
     * @param verificationScript The verification script of the witness.
     */
    public void addScript(InvocationScript invocationScript, VerificationScript verificationScript) {
        addScript(new Witness(invocationScript, verificationScript));
    }

    public void addScript(Witness script) {
        if (script.getScriptHash() == null || script.getScriptHash().length() == 0) {
            throw new IllegalArgumentException("The script hash of the given script is " +
                    "empty. Please set the script hash.");
        }
        this.scripts.add(script);
        this.scripts.sort(Comparator.comparing(Witness::getScriptHash));
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
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            this.transactionType = TransactionType.valueOf(reader.readByte());
            this.version = reader.readByte();
            deserializeExclusive(reader);
            this.attributes = reader.readSerializableList(TransactionAttribute.class);
            this.inputs = reader.readSerializableList(RawTransactionInput.class);
            this.outputs = reader.readSerializableList(RawTransactionOutput.class);
            this.scripts = reader.readSerializableList(Witness.class);
        } catch (IllegalAccessException | InstantiationException | IOException e) {
            throw new DeserializationException(e);
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

    public abstract void deserializeExclusive(BinaryReader reader) throws DeserializationException;

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
        private List<TransactionAttribute> attributes;
        private List<RawTransactionInput> inputs;
        private List<RawTransactionOutput> outputs;
        private List<Witness> scripts;

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

        public T attributes(List<TransactionAttribute> attributes) {
            this.attributes.addAll(attributes);
            return (T) this;
        }

        public T attribute(TransactionAttribute attribute) {
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

        public T scripts(List<Witness> scripts) {
            for (Witness script : scripts) {
                if (script.getScriptHash() == null || script.getScriptHash().length() == 0) {
                    throw new IllegalArgumentException("The script hash of the given script is " +
                            "empty. Please set the script hash.");
                }
            }

            this.scripts.addAll(scripts);
            this.scripts.sort(Comparator.comparing(Witness::getScriptHash));
            return (T) this;
        }

        public T script(Witness script) {
            return scripts(Arrays.asList(script));
        }

        public abstract RawTransaction build();
    }
}
