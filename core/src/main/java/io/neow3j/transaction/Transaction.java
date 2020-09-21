package io.neow3j.transaction;

import static io.neow3j.crypto.Hash.hash256;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ScriptHash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.model.NeoConfig;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Transaction extends NeoSerializable {

    public static final int HEADER_SIZE = 1 +  // Version byte
            4 +  // Nonce uint32
            8 +  // System fee int64
            8 +  // Network fee int64
            4; // Valid until block uint32

    protected Neow3j neow;
    private byte version;
    /**
     * Is a random number added to the transaction to prevent replay attacks. It is an unsigned
     * 32-bit integer in the neo C# implementation. It is represented as a integer here, but when
     * serializing it
     */
    private long nonce;
    /**
     * Defines up to which block this transaction remains valid. If this transaction is not added
     * into a block up to this number it will become invalid and be dropped. It is an unsigned
     * 32-bit integer in the neo C# implementation. Here it is represented as a signed 32-bit
     * integer which offers a smaller but still large enough range.
     */
    private long validUntilBlock;
    private List<Signer> signers;
    private long systemFee;
    private long networkFee;
    private List<TransactionAttribute> attributes;
    private byte[] script;
    private List<Witness> witnesses;

    public Transaction() {
        signers = new ArrayList<>();
        attributes = new ArrayList<>();
        witnesses = new ArrayList<>();
    }

    public Transaction(Neow3j neow, byte version, long nonce, long validUntilBlock, List<Signer> signers,
            long systemFee, long networkFee, List<TransactionAttribute> attributes, byte[] script,
            List<Witness> witnesses) {
        this.neow = neow;
        this.version = version;
        this.nonce = nonce;
        this.validUntilBlock = validUntilBlock;
        this.signers = signers;
        this.systemFee = systemFee;
        this.networkFee = networkFee;
        this.attributes = attributes;
        this.script = script;
        this.witnesses = witnesses;
    }

    public byte getVersion() {
        return version;
    }

    public long getNonce() {
        return nonce;
    }

    public long getValidUntilBlock() {
        return validUntilBlock;
    }

    public List<Signer> getSigners() {
        return signers;
    }

    /**
     * Gets the sender of this transaction. The sender is the account that pays for the
     * transaction's fees.
     *
     * @return the sender account's script hash.
     */
    public ScriptHash getSender() {
        // First we look for a signer that has the fee-only scope. The signer with that scope is
        // the sender of the transaction. If there is no such signer then the order of the
        // signers defines the sender, i.e., the first signer is the sender of the transaction.
        return signers.stream()
                .filter(signer -> signer.getScopes().contains(WitnessScope.FEE_ONLY))
                .findFirst()
                .orElse(signers.get(0))
                .getScriptHash();
    }

    /**
     * Gets the system fee of this transaction.
     *
     * @return the system fee in GAS fractions.
     */
    public long getSystemFee() {
        return systemFee;
    }

    /**
     * Gets the network fee of this transaction.
     *
     * @return the network fee in GAS fractions.
     */
    public long getNetworkFee() {
        return networkFee;
    }

    public List<TransactionAttribute> getAttributes() {
        return attributes;
    }

    public byte[] getScript() {
        return script;
    }

    public List<Witness> getWitnesses() {
        return witnesses;
    }

    public void addWitness(Witness witness) {
        if (witness.getScriptHash() == null) {
            throw new IllegalArgumentException("The script hash of the given witness must not be "
                    + "null.");
        }
        this.witnesses.add(witness);
    }

    public String getTxId() {
        byte[] hash = hash256(getHashData());
        return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(hash));
    }

    /**
     * Sends this invocation transaction to the neo-node via the `sendrawtransaction` RPC.
     *
     * @return the Neo node's response.
     * @throws IOException                       if a problem in communicating with the Neo node
     *                                           occurs.
     * @throws TransactionConfigurationException if signatures are missing for one or more signers of
     *                                           the transaction.
     */
    public NeoSendRawTransaction send() throws IOException {
        List<ScriptHash> witnesses = this.getWitnesses().stream()
                .map(Witness::getScriptHash).collect(Collectors.toList());

        for (Signer signer : this.getSigners()) {
            if (!witnesses.contains(signer.getScriptHash())) {
                throw new TransactionConfigurationException("The transaction does not have a "
                        + "signature for each of its signers.");
            }
        }
        String hex = Numeric.toHexStringNoPrefix(this.toArray());
        return neow.sendRawTransaction(hex).send();
    }

    @Override
    public int getSize() {
        return HEADER_SIZE +
                IOUtils.getVarSize(this.signers) +
                IOUtils.getVarSize(this.attributes) +
                IOUtils.getVarSize(this.script) +
                IOUtils.getVarSize(this.witnesses);
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            this.version = reader.readByte();
            this.nonce = reader.readUInt32();
            this.systemFee = reader.readInt64();
            this.networkFee = reader.readInt64();
            this.validUntilBlock = reader.readUInt32();
            this.signers = reader.readSerializableList(Signer.class);
            readTransactionAttributes(reader);
            this.script = reader.readVarBytes();
            this.witnesses = reader.readSerializableList(Witness.class);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    private void readTransactionAttributes(BinaryReader reader)
            throws IOException, DeserializationException {
        long nrOfAttributes = reader.readVarInt();
        if (nrOfAttributes > NeoConstants.MAX_TRANSACTION_ATTRIBUTES) {
            throw new DeserializationException("A transaction can hold at most "
                    + NeoConstants.MAX_TRANSACTION_ATTRIBUTES + ". Input data had "
                    + nrOfAttributes + " attributes.");
        }
        for (int i = 0; i < nrOfAttributes; i++) {
            this.attributes.add(TransactionAttribute.deserializeAttribute(reader));
        }
    }

    private void serializeWithoutWitnesses(BinaryWriter writer) throws IOException {
        writer.writeByte(this.version);
        writer.writeUInt32(this.nonce);
        writer.writeInt64(this.systemFee);
        writer.writeInt64(this.networkFee);
        writer.writeUInt32(this.validUntilBlock);
        writer.writeSerializableVariable(this.signers);
        writer.writeSerializableVariable(this.attributes);
        writer.writeVarBytes(this.script);
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        serializeWithoutWitnesses(writer);
        this.witnesses.sort(Comparator.comparing(Witness::getScriptHash));
        writer.writeSerializableVariable(this.witnesses);
    }

    /**
     * Serializes this transaction to a raw byte array without any witnesses.
     * <p>
     * In this form, the transaction byte array can be used for example to create a signature.
     *
     * @return the serialized transaction
     */
    public byte[] toArrayWithoutWitnesses() {
        try (ByteArrayOutputStream ms = new ByteArrayOutputStream()) {
            try (BinaryWriter writer = new BinaryWriter(ms)) {
                serializeWithoutWitnesses(writer);
                writer.flush();
                return ms.toByteArray();
            }
        } catch (IOException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    /**
     * Gets this transaction's data in the format used to produce the transaction's hash. E.g., for
     * producing the transaction ID or a transaction signature.
     * <p>
     * The returned value depends on the configuration of {@link NeoConfig#magicNumber()}.
     *
     * @return the transaction data ready for hashing.
     */
    public byte[] getHashData() {
        return ArrayUtils.concatenate(NeoConfig.magicNumber(), toArrayWithoutWitnesses());
    }

    /**
     * Serializes this transaction to a raw byte array including witnesses.
     *
     * @return the serialized transaction.
     */
    @Override
    public byte[] toArray() {
        return super.toArray();
    }
}
