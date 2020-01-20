package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Transaction extends NeoSerializable {

    public static final int HEADER_SIZE = 1 +  // Version byte
        4 +  // Nonce uint32
        NeoConstants.SCRIPTHASH_LENGHT_BYTES + // Sender script hash
        8 +  // System fee int64
        8 +  // Network fee int64
        4; // Valid until block uint32

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
    private ScriptHash sender;
    private long systemFee;
    private long networkFee;
    private List<TransactionAttribute> attributes;
    private List<Cosigner> cosigners;
    private byte[] script;
    private List<Witness> witnesses;

    public Transaction() {
    }

    protected Transaction(Builder builder) {
        this.version = builder.version;
        this.nonce = builder.nonce;
        this.validUntilBlock = builder.validUntilBlock;
        this.sender = builder.sender;
        this.systemFee = builder.systemFee;
        this.networkFee = builder.networkFee;
        this.attributes = builder.attributes;
        this.cosigners = builder.cosigners;
        this.script = builder.script;
        this.witnesses = builder.witnesses;
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

    public ScriptHash getSender() {
        return sender;
    }

    public long getSystemFee() {
        return systemFee;
    }

    public long getNetworkFee() {
        return networkFee;
    }

    public List<TransactionAttribute> getAttributes() {
        return attributes;
    }

    public List<Cosigner> getCosigners() {
        return cosigners;
    }

    public byte[] getScript() {
        return script;
    }

    public List<Witness> getWitnesses() {
        return witnesses;
    }

    public void addWitness(Witness witness) {
        if (witness.getScriptHash() == null) {
            throw new IllegalArgumentException("The script hash of the given script is " +
                "empty. Please set the script hash.");
        }
        this.witnesses.add(witness);
    }

    public String getTxId() {
        byte[] hash = Hash.sha256(Hash.sha256(toArrayWithoutWitnesses()));
        return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(hash));
    }

    @Override
    public int getSize() {
        return HEADER_SIZE +
        IOUtils.getSizeOfVarList(this.attributes) +
        IOUtils.getSizeOfVarList(this.cosigners) +
        IOUtils.getSizeOfVarInt(this.script.length) + this.script.length +
        IOUtils.getSizeOfVarList(this.witnesses);
    }

    @Override
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            this.version = reader.readByte();
            this.nonce = reader.readUInt32();
            this.sender = reader.readSerializable(ScriptHash.class);
            this.systemFee = reader.readInt64();
            this.networkFee = reader.readInt64();
            this.validUntilBlock = reader.readUInt32();
            this.attributes = reader.readSerializableList(TransactionAttribute.class);
            this.cosigners = reader.readSerializableList(Cosigner.class);
            this.script = reader.readVarBytes();
            this.witnesses = reader.readSerializableList(Witness.class);
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            throw new DeserializationException(e);
        }
    }

    private void serializeWithoutWitnesses(BinaryWriter writer) throws IOException {
        writer.writeByte(this.version);
        writer.writeUInt32(this.nonce);
        writer.writeSerializableFixed(this.sender);
        writer.writeInt64(this.systemFee);
        writer.writeInt64(this.networkFee);
        writer.writeUInt32(this.validUntilBlock);
        writer.writeSerializableVariable(this.attributes);
        writer.writeSerializableVariable(this.cosigners);
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
     * Serializes this transaction to a raw byte array including witnesses.
     *
     * @return the serialized transaction.
     */
    @Override
    public byte[] toArray() {
        return super.toArray();
    }

    public static class Builder {

        private long nonce;
        private byte version;
        private Long validUntilBlock;
        private ScriptHash sender;
        private long systemFee;
        private long networkFee;
        private List<Cosigner> cosigners;
        private byte[] script;
        private List<TransactionAttribute> attributes;
        private List<Witness> witnesses;

        public Builder() {
            // The random value used to initialize the nonce does not need cryptographic security,
            // therefore we can use ThreadLocalRandom to generate it.
            this.nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
            this.version = NeoConstants.CURRENT_TX_VERSION;
            this.networkFee = 0L;
            this.systemFee = 0L;
            this.cosigners = new ArrayList<>();
            this.attributes = new ArrayList<>();
            this.witnesses = new ArrayList<>();
            this.script = new byte[]{};
        }

        /**
         * Sets the version for this transaction.
         * <p>
         * It is set to {@link NeoConstants#CURRENT_TX_VERSION} by default.
         *
         * @param version The transaction version number.
         * @return this builder.
         */
        public Builder version(byte version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the nonce (number used once) for this transaction. The nonce is a number from 0 to
         * 2<sup>32</sup>.
         * <p>
         * It is set to a random value by default.
         *
         * @param nonce The transaction nonce.
         * @return this builder.
         * @throws TransactionConfigurationException if the nonce is not in the range [0, 2^32).
         */
        public Builder nonce(Long nonce) {
            if (nonce < 0 || nonce >= (long) Math.pow(2, 32)) {
                throw new TransactionConfigurationException("The value of the transaction nonce " +
                    "must be in the interval [0, 2^32).");
            }
            this.nonce = nonce;
            return this;
        }

        /**
         * Sets the number of the block up to which this transaction can be included.
         * <p>
         * If that block number is reached in the network and this transaction is not yet included
         * in a block, it becomes invalid. Note that the given block number must not be higher than
         * the current chain height plus the increment specified in {@link
         * NeoConstants#MAX_VALID_UNTIL_BLOCK_INCREMENT}.
         * <p>
         * This property is <b>mandatory</b>.
         *
         * @param blockNr The block number.
         * @return this builder.
         * @throws TransactionConfigurationException if the block number is not in the range [0,
         *                                           2^32).
         */
        public Builder validUntilBlock(long blockNr) {
            if (blockNr < 0 || blockNr >= (long) Math.pow(2, 32)) {
                throw new TransactionConfigurationException("The block number up to which this " +
                    "transaction can be included cannot be less than zero or more than 2^32.");
            }
            this.validUntilBlock = blockNr;
            return this;
        }

        /**
         * Sets the sender of this transaction.
         * <p>
         * The sender's account will be charged with the network and system fees.
         * <p>
         * This property is <b>mandatory</b>.
         *
         * @param sender The sender account's script hash.
         * @return this builder.
         */
        public Builder sender(ScriptHash sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Sets the system fee for this transaction.
         * <p>
         * The system fee is the amount of GAS needed to execute this transaction's script in the
         * NeoVM. It is distributed to all NEO holders.
         *
         * @param systemFee The system fee in fractions of GAS (10^-8)
         * @return this builder.
         */
        public Builder systemFee(Long systemFee) {
            this.systemFee = systemFee;
            return this;
        }

        /**
         * Sets the network fee for this transaction.
         * <p>
         * The network fee is the GAS cost for transaction size and verification. It is distributed
         * to the consensus nodes.
         *
         * @param networkFee The network fee in fractions of GAS (10^-8)
         * @return this builder.
         */
        public Builder networkFee(Long networkFee) {
            this.networkFee = networkFee;
            return this;
        }


        /**
         * Sets the contract script for this transaction.
         * <p>
         * The script defines the actions that this transaction will perform on the blockchain.
         *
         * @param script The contract script.
         * @return this builder.
         */
        public Builder script(byte[] script) {
            this.script = script;
            return this;
        }

        /**
         * Adds the given cosigners to this transaction.
         * <p>
         * The maximum number of cosigners on a transaction is given in {@link
         * NeoConstants#MAX_COSIGNERS}.
         *
         * @param cosigners The cosigners.
         * @return this builder.
         * @throws TransactionConfigurationException when attempting to add more than the {@link
         *                                           NeoConstants#MAX_COSIGNERS} Cosigners or
         *                                           multiple Cosigners concerning the same
         *                                           account.
         */
        public Builder cosigners(List<Cosigner> cosigners) {
            if (this.cosigners.size() + cosigners.size() > NeoConstants.MAX_COSIGNERS) {
                throw new TransactionConfigurationException("Can't have more than " +
                    NeoConstants.MAX_COSIGNERS + " cosigners on a transaction.");
            }
            if (hasDuplicateCosignerAccounts(cosigners)) {
                throw new TransactionConfigurationException("Can't add multiple cosigners" +
                    " concerning the same account.");
            }
            this.cosigners.addAll(cosigners);
            return this;
        }

        private boolean hasDuplicateCosignerAccounts(List<Cosigner> cosigners) {
            Set<ScriptHash> newAccts = cosigners.stream().map(Cosigner::getAccount)
                .collect(Collectors.toSet());
            boolean duplicateCosignerAccts = cosigners.size() != newAccts.size();
            Set<ScriptHash> existingAccts = this.cosigners.stream().map(Cosigner::getAccount)
                .collect(Collectors.toSet());
            existingAccts.retainAll(newAccts);
            return duplicateCosignerAccts || existingAccts.size() != 0;
        }

        /**
         * Adds the given cosigners to this transaction.
         * <p>
         * The maximum number of cosigners on a transaction is given in {@link
         * NeoConstants#MAX_COSIGNERS}.
         *
         * @param cosigners The cosigners.
         * @return this builder.
         * @throws TransactionConfigurationException when attempting to add more than the {@link
         *                                           NeoConstants#MAX_COSIGNERS} Cosigners.
         */
        public Builder cosigners(Cosigner... cosigners) {
            return cosigners(Arrays.asList(cosigners));
        }

        /**
         * Adds the given attributes to this transaction.
         * <p>
         * The maximum number of attributes on a transaction is given in {@link
         * NeoConstants#MAX_TRANSACTION_ATTRIBUTES}.
         *
         * @param attributes The attributes.
         * @return this builder.
         * @throws TransactionConfigurationException when attempting to add more than {@link
         *                                           NeoConstants#MAX_TRANSACTION_ATTRIBUTES}
         *                                           attributes.
         */
        public Builder attributes(List<TransactionAttribute> attributes) {
            if (this.attributes.size() + attributes.size() >
                NeoConstants.MAX_TRANSACTION_ATTRIBUTES) {
                throw new TransactionConfigurationException("Can't have more than " +
                    NeoConstants.MAX_TRANSACTION_ATTRIBUTES + " attributes on a transaction.");
            }
            this.attributes.addAll(attributes);
            return this;
        }

        /**
         * Adds the given attributes to this transaction.
         * <p>
         * The maximum number of attributes on a transaction is given in {@link
         * NeoConstants#MAX_TRANSACTION_ATTRIBUTES}.
         *
         * @param attributes The attributes.
         * @return this builder.
         */
        public Builder attributes(TransactionAttribute... attributes) {
            return attributes(Arrays.asList(attributes));
        }

        /**
         * Adds the given witnesses to this transaction.
         * <p>
         * Witness data is used to check the transaction validity. It usually consists of the
         * signature generated by the transacting account but can also be other validating data.
         *
         * @param witnesses The witnesses.
         * @return this builder.
         */
        public Builder witnesses(List<Witness> witnesses) {
            for (Witness witness : witnesses) {
                if (witness.getScriptHash() == null)  {
                    throw new IllegalArgumentException("The script hash of the given script is " +
                        "empty. Please set the script hash.");
                }
            }

            this.witnesses.addAll(witnesses);
            return this;
        }

        /**
         * Adds the given witnesses to this transaction.
         * <p>
         * Witness data is used to check the transaction validity. It usually consists of the
         * signature generated by the transacting account but can also be other validating data.
         *
         * @param witnesses The witnesses.
         * @return this builder.
         */
        public Builder witnesses(Witness... witnesses) {
            return witnesses(Arrays.asList(witnesses));
        }

        /**
         * Builds the transaction.
         *
         * @return The transaction.
         * @throws TransactionConfigurationException if either the sender account or the
         *                                           "validUntilBlock" property was not set.
         */
        public Transaction build() {
            if (this.sender == null) {
                throw new TransactionConfigurationException("A transaction requires a sender " +
                    "account.");
            }

            if (this.validUntilBlock == null) {
                throw new TransactionConfigurationException("A transaction needs to be set up " +
                    "with a block number up to which this it is considered valid.");
            }

            if (this.cosigners.isEmpty()) {
                // Add default restrictive witness scope.
                this.cosigners.add(Cosigner.calledByEntry(this.sender));
            }
            return new Transaction(this);
        }

        public long getNonce() {
            return nonce;
        }

        public byte getVersion() {
            return version;
        }

        public Long getValidUntilBlock() {
            return validUntilBlock;
        }

        public ScriptHash getSender() {
            return sender;
        }

        public long getSystemFee() {
            return systemFee;
        }

        public long getNetworkFee() {
            return networkFee;
        }

        public List<Cosigner> getCosigners() {
            return cosigners;
        }

        public byte[] getScript() {
            return script;
        }

        public List<TransactionAttribute> getAttributes() {
            return attributes;
        }

        public List<Witness> getWitnesses() {
            return witnesses;
        }
    }
}
