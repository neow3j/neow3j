package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
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
import java.util.Random;

public class Transaction extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(Transaction.class);

    private byte version;
    private Integer nonce;
    private Integer validUntilBlock;
    private ScriptHash sender;
    private Long systemFee;
    private Long networkFee;
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

    public Integer getNonce() {
        return nonce;
    }

    public Integer getValidUntilBlock() {
        return validUntilBlock;
    }

    public ScriptHash getSender() {
        return sender;
    }

    public Long getSystemFee() {
        return systemFee;
    }

    public Long getNetworkFee() {
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
        if (witness.getScriptHash() == null || witness.getScriptHash().length() == 0) {
            throw new IllegalArgumentException("The script hash of the given script is " +
                    "empty. Please set the script hash.");
        }
        this.witnesses.add(witness);
        this.witnesses.sort(Comparator.comparing(Witness::getScriptHash));
    }

    public String getTxId() {
        byte[] hash = Hash.sha256(Hash.sha256(toArrayWithoutWitnesses()));
        return Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(hash));
    }

    public int getSize() {
        return toArray().length;
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.version = reader.readByte();
        try {
            this.attributes = reader.readSerializableList(TransactionAttribute.class);
            this.witnesses = reader.readSerializableList(Witness.class);
        } catch (IllegalAccessException e) {
            LOG.error("Can't access the specified object.", e);
        } catch (InstantiationException e) {
            LOG.error("Can't instantiate the specified object type.", e);
        }
    }

    private void serializeWithoutWitnesses(BinaryWriter writer) throws IOException {
        writer.writeByte(this.version);
        writer.writeSerializableVariable(this.attributes);
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
     * Serializes this transaction to a raw byte array including witnesses.
     *
     * @return the serialized transaction.
     */
    @Override
    public byte[] toArray() {
        return super.toArray();
    }

    protected static class Builder {

        // The nonce must be handled as an unsigned integer.
        private Integer nonce;
        private byte version;
        private Integer validUntilBlock;
        private ScriptHash sender;
        private Long systemFee;
        private Long networkFee;
        private List<Cosigner> cosigners;
        private byte[] script;
        private List<TransactionAttribute> attributes;
        private List<Witness> witnesses;

        protected Builder() {
            this.nonce = new Random().nextInt();
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
         * The default is set to {@link NeoConstants#CURRENT_TX_VERSION}.
         *
         * @param version The version to set on the transaction.
         * @return this builder.
         */
        public Builder version(byte version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the nonce for this transaction. The nonce must be a number from 0 to 2^32, so no
         * negative number. It is treated as an unsigned integer.
         * <p>
         * The nonce default is a random value from 0 to 2^32.
         *
         * @param nonce The nonce (number used once) to set on the transaction.
         * @return this builder.
         */
        public Builder nonce(Integer nonce) {
            this.nonce = nonce;
            return this;
        }

        /**
         * Sets the number of the block up to which this transaction can be included.
         * <p>
         * If that block number is reached in the network and this transaction is not yet included
         * in a block, it becomes invalid.
         *
         * @param blockNr The block number until this transaction is valid.
         * @return this builder.
         */
        public Builder validUntilBlock(Integer blockNr) {
            this.validUntilBlock = blockNr;
            return this;
        }

        /**
         * Sets the sender of this transaction.
         * <p>
         * The sender's account will be charged with the network and system fees.
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
         *
         * @param script The contract script.
         * @return this builder.
         */
        // TODO 14.11.19 claude: Clarify what this script does in the common case and add the
        //  explanation to the doc.
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
         * @param cosigners The list of cosigners.
         * @return this builder.
         * @throws TransactionConfigurationException when attempting to add more than the {@link
         *                                           NeoConstants#MAX_COSIGNERS} Cosigners.
         */
        public Builder cosigners(List<Cosigner> cosigners) {
            if (this.cosigners.size() + cosigners.size() > NeoConstants.MAX_COSIGNERS) {
                throw new TransactionConfigurationException("Can't have more than " +
                        NeoConstants.MAX_COSIGNERS + " cosigners on a transaction.");
            }
            this.cosigners.addAll(cosigners);
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
         *                                           NeoConstants#MAX_COSIGNERS} Cosigners.
         */
        public Builder cosigners(Cosigner... cosigners) {
            return cosigners(Arrays.asList(cosigners));
        }

        /**
         * Adds the given cosigner to this transaction.
         * <p>
         * The maximum number of cosigners on a transaction is given in {@link
         * NeoConstants#MAX_COSIGNERS}.
         *
         * @param cosigner The cosigner to add.
         * @return this builder.
         */
        public Builder cosigner(Cosigner cosigner) {
            return cosigners(Arrays.asList(cosigner));
        }

        /**
         * Adds the given attributes to this transaction.
         * <p>
         * The maximum number of attributes on a transaction is given in {@link
         * NeoConstants#MAX_TRANSACTION_ATTRIBUTES}.
         *
         * @param attributes The list of attributes.
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
         * Adds the given attribute to this transaction.
         * <p>
         * The maximum number of attributes on a transaction is given in {@link
         * NeoConstants#MAX_TRANSACTION_ATTRIBUTES}.
         *
         * @param attribute The attribute to add.
         * @return this builder.
         */
        public Builder attribute(TransactionAttribute attribute) {
            return attributes(Arrays.asList(attribute));
        }

        /**
         * Adds the given witnesses to this transaction.
         * <p>
         * Witness data is used to check the transaction validity. It usually consists of the
         * signature generated by the transacting account but can also be other validating data.
         *
         * @param witnesses The witnesses to add.
         * @return this builder.
         */
        public Builder witnesses(List<Witness> witnesses) {
            for (Witness witness : witnesses) {
                if (witness.getScriptHash() == null || witness.getScriptHash().length() == 0) {
                    throw new IllegalArgumentException("The script hash of the given script is " +
                            "empty. Please set the script hash.");
                }
            }

            this.witnesses.addAll(witnesses);
            this.witnesses.sort(Comparator.comparing(Witness::getScriptHash));
            return this;
        }

        /**
         * Adds the given witnesses to this transaction.
         * <p>
         * Witness data is used to check the transaction validity. It usually consists of the
         * signature generated by the transacting account but can also be other validating data.
         *
         * @param witnesses The witnesses to add.
         * @return this builder.
         */
        public Builder witnesses(Witness... witnesses) {
            return witnesses(Arrays.asList(witnesses));
        }

        /**
         * Adds the given witness to this transaction.
         * <p>
         * Witness data is used to check the transaction validity. It usually consists of the
         * signature generated by the transacting account but can also be other validating data.
         *
         * @param witness The witness to add.
         * @return this builder.
         */
        public Builder witness(Witness witness) {
            return witnesses(Arrays.asList(witness));
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
            return new Transaction(this);
        }
    }
}
