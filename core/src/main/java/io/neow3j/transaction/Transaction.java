package io.neow3j.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.constants.NeoConstants;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.Numeric;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.neow3j.constants.NeoConstants.MAX_TRANSACTION_SIZE;
import static io.neow3j.crypto.Hash.sha256;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static java.lang.String.format;

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
    private BigInteger blockCountWhenSent;

    public Transaction() {
        signers = new ArrayList<>();
        attributes = new ArrayList<>();
        witnesses = new ArrayList<>();
    }

    public Transaction(Neow3j neow, byte version, long nonce, long validUntilBlock,
            List<Signer> signers, long systemFee, long networkFee,
            List<TransactionAttribute> attributes, byte[] script, List<Witness> witnesses) {
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
    public Hash160 getSender() {
        // First we look for a signer that has the fee-only scope. The signer with that scope is
        // the sender of the transaction. If there is no such signer then the order of the
        // signers defines the sender, i.e., the first signer is the sender of the transaction.
        return signers.stream()
                .filter(signer -> signer.getScopes().contains(WitnessScope.NONE))
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
        this.witnesses.add(witness);
    }

    /**
     * Gets this transactions uniquely identifying ID/hash.
     *
     * @return the transaction ID.
     */
    public Hash256 getTxId() {
        return new Hash256(reverseArray(sha256(toArrayWithoutWitnesses())));
    }

    /**
     * Sends this invocation transaction to the Neo node via the `sendrawtransaction` RPC.
     *
     * @return the Neo node's response.
     * @throws IOException                       if a problem in communicating with the Neo node
     *                                           occurs.
     * @throws TransactionConfigurationException if the number of signers and witnesses on the
     *                                           transaction are not equal.
     */
    public NeoSendRawTransaction send() throws IOException {
        if (getSigners().size() != getWitnesses().size()) {
            throw new TransactionConfigurationException("The transaction does not have the same " +
                    "number of signers and witnesses. For every signer there has to be one " +
                    "witness, even if that witness is empty.");
        }
        int size = getSize();
        if (size > MAX_TRANSACTION_SIZE) {
            throw new TransactionConfigurationException(format("The transaction exceeds the " +
                    "maximum transaction size. The maximum size is {} bytes. This transaction " +
                    "has size {}", MAX_TRANSACTION_SIZE, size));
        }
        String hex = Numeric.toHexStringNoPrefix(toArray());
        blockCountWhenSent = neow.getBlockCount().send().getBlockCount();
        return neow.sendRawTransaction(hex).send();
    }

    /**
     * Creates an {@code Observable} that emits the block number containing this transaction as soon
     * as it has been integrated in one. The observable completes right after emitting the block
     * number.
     * <p>
     * The observable starts tracking the blocks from the point at which the transaction has been
     * sent.
     *
     * @return The observable.
     * @throws IllegalStateException if this transaction has not yet been sent.
     */
    public Observable<Long> track() {
        if (blockCountWhenSent == null) {
            throw new IllegalStateException("Can't subscribe before transaction has been sent.");
        }

        Predicate<NeoGetBlock> pred = neoGetBlock ->
                neoGetBlock.getBlock().getTransactions() != null &&
                        neoGetBlock.getBlock().getTransactions().stream()
                                .anyMatch(tx -> tx.getHash().equals(getTxId()));

        return neow.catchUpToLatestAndSubscribeToNewBlocksObservable(blockCountWhenSent, true)
                .takeUntil(pred)
                .filter(pred)
                .map(neoGetBlock -> neoGetBlock.getBlock().getIndex());
    }

    /**
     * Gets the application log of this transaction.
     * <p>
     * The application log is not cached locally. Every time this method is called, requests are
     * send to the Neo node.
     * <p>
     * If the application log could not be fetched, {@code null} is returned.
     *
     * @return the application log.
     */
    public NeoApplicationLog getApplicationLog() {
        if (blockCountWhenSent == null) {
            throw new IllegalStateException("Can't get the application log before transaction has" +
                    " been sent.");
        }
        NeoApplicationLog applicationLog = null;
        try {
            applicationLog = neow.getApplicationLog(getTxId()).send().getApplicationLog();
        } catch (IOException ignore) {
        }
        return applicationLog;
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
        if (nrOfAttributes + this.signers.size() > NeoConstants.MAX_TRANSACTION_ATTRIBUTES) {
            throw new DeserializationException("A transaction can hold at most " +
                    NeoConstants.MAX_TRANSACTION_ATTRIBUTES + " attributes (including signers). " +
                    "Input data had " + nrOfAttributes + " attributes.");
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
     * The returned value depends on the magic number of the used Neo network, which is retrieved
     * from the Neo node via the {@code getversion} RPC method if not already available locally.
     *
     * @return the transaction data ready for hashing.
     * @throws IOException if an error occurs when fetching the network's magic number
     */
    public byte[] getHashData() throws IOException {
        return concatenate(neow.getNetworkMagicNumber(), sha256(toArrayWithoutWitnesses()));
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

    public String toJson() throws JsonProcessingException {
        io.neow3j.protocol.core.response.Transaction dtoTx =
                new io.neow3j.protocol.core.response.Transaction(this);
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(dtoTx);
    }

}
