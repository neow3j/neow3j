package io.neow3j.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;
import io.neow3j.script.VerificationScript;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.types.Hash256;
import io.neow3j.wallet.Account;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.neow3j.constants.NeoConstants.MAX_TRANSACTION_SIZE;
import static io.neow3j.crypto.Hash.sha256;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.utils.ArrayUtils.concatenate;
import static io.neow3j.utils.ArrayUtils.reverseArray;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.lang.String.format;

public class Transaction extends NeoSerializable {

    public static final int HEADER_SIZE = 1 +  // Version byte
            4 +  // Nonce uint32
            8 +  // System fee int64
            8 +  // Network fee int64
            4; // Valid until block uint32

    protected Neow3j neow3j;

    private byte version;
    /**
     * Is a random number added to the transaction to prevent replay attacks. It is an unsigned 32-bit integer in the
     * neo C# implementation. It is represented as a integer here, but when serializing it
     */
    private long nonce;
    /**
     * Defines up to which block this transaction remains valid. If this transaction is not added into a block up to
     * this number it will become invalid and be dropped. It is an unsigned 32-bit integer in the neo C#
     * implementation. Here it is represented as a signed 32-bit integer which offers a smaller but still large
     * enough range.
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

    public Transaction(Neow3j neow3j, byte version, long nonce, long validUntilBlock, List<Signer> signers,
            long systemFee, long networkFee, List<TransactionAttribute> attributes, byte[] script,
            List<Witness> witnesses) {

        this.neow3j = neow3j;
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

    /**
     * Sets the {@code Neow3j} instance of this transaction.
     *
     * @param neow3j the Neow3j instance.
     */
    public void setNeow3j(Neow3j neow3j) {
        this.neow3j = neow3j;
    }

    /**
     * @return the version of this transaction.
     */
    public byte getVersion() {
        return version;
    }

    /**
     * @return the nonce of this transaction.
     */
    public long getNonce() {
        return nonce;
    }

    /**
     * @return the validity period of this transaction.
     */
    public long getValidUntilBlock() {
        return validUntilBlock;
    }

    /**
     * @return the signers of this transaction.
     */
    public List<Signer> getSigners() {
        return signers;
    }

    /**
     * Gets the sender of this transaction.
     * <p>
     * The sender is the account that pays for the transaction's fees.
     *
     * @return the sender account's script hash.
     */
    public Hash160 getSender() {
        // First we look for a signer that has the fee-only scope. The signer with that scope is the sender of the
        // transaction. If there is no such signer then the order of the signers defines the sender, i.e., the first
        // signer is the sender of the transaction.
        return signers.stream()
                .filter(signer -> signer.getScopes().contains(WitnessScope.NONE))
                .findFirst()
                .orElse(signers.get(0))
                .getScriptHash();
    }

    /**
     * @return the system fee of this transaction in GAS fractions.
     */
    public long getSystemFee() {
        return systemFee;
    }

    /**
     * @return the network fee of this transaction in GAS fractions.
     */
    public long getNetworkFee() {
        return networkFee;
    }

    /**
     * @return the attributes of this transaction.
     */
    public List<TransactionAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @return the script of this transaction.
     */
    public byte[] getScript() {
        return script;
    }

    /**
     * @return the witnesses of this transaction.
     */
    public List<Witness> getWitnesses() {
        return witnesses;
    }

    /**
     * Adds a witness to this transaction.
     * <p>
     * Note, that witnesses have to be added in the same order as signers were added.
     *
     * @param witness the transaction witness.
     * @return this.
     */
    public Transaction addWitness(Witness witness) {
        this.witnesses.add(witness);
        return this;
    }

    /**
     * Adds a witness to this transaction by signing it with the given account.
     * <p>
     * Note, that witnesses have to be added in the same order as signers were added.
     *
     * @param account the account to sign with.
     * @return this.
     * @throws IOException if an error occurs when fetching the network's magic number.
     */
    public Transaction addWitness(Account account) throws IOException {
        this.witnesses.add(Witness.create(getHashData(), account.getECKeyPair()));
        return this;
    }

    /**
     * Adds a multi-sig witness to this transaction. Use this to add a witness of a multi-sig signer that is part of
     * this transaction.
     * <p>
     * The witness is constructed from the multi-sig account's {@code verificationScript} and the {@code signatures}.
     * Obviously, the signatures should be derived from this transaction's hash data (see
     * {@link Transaction#getHashData()}).
     * <p>
     * Note, that witnesses have to be added in the same order as signers were added.
     *
     * @param verificationScript the verification script of the multi-sig account.
     * @param pubKeySigMap       a map of participating public keys mapped to the signatures created with their
     *                           corresponding private key.
     * @return this.
     */
    public Transaction addMultiSigWitness(VerificationScript verificationScript,
            Map<ECKeyPair.ECPublicKey, Sign.SignatureData> pubKeySigMap) {

        List<Sign.SignatureData> signatures = pubKeySigMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        Witness multiSigWitness = createMultiSigWitness(signatures, verificationScript);
        this.witnesses.add(multiSigWitness);
        return this;
    }

    /**
     * Adds a multi-sig witness to this transaction. Use this to add a witness of a multi-sig signer that is part of
     * this transaction.
     * <p>
     * The witness is constructed from the multi-sig account's {@code verificationScript} and by signing this
     * transaction with the given accounts.
     * <p>
     * Note, that witnesses have to be added in the same order as signers were added.
     *
     * @param verificationScript the verification script of the multi-sig account.
     * @param accounts           the accounts to use for signing. They need to hold decrypted private keys.
     * @return this.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Transaction addMultiSigWitness(VerificationScript verificationScript, Account... accounts)
            throws IOException {

        byte[] hashData = getHashData();
        List<Sign.SignatureData> signatures = Arrays.stream(accounts)
                .map(Account::getECKeyPair)
                .sorted(Comparator.comparing(ECKeyPair::getPublicKey))
                .map(a -> signMessage(hashData, a))
                .collect(Collectors.toList());
        Witness multiSigWitness = createMultiSigWitness(signatures, verificationScript);
        this.witnesses.add(multiSigWitness);
        return this;
    }

    /**
     * @return this transaction's uniquely identifying ID/hash.
     */
    public Hash256 getTxId() {
        return new Hash256(reverseArray(sha256(toArrayWithoutWitnesses())));
    }

    /**
     * Sends this invocation transaction to the Neo node via the `sendrawtransaction` RPC.
     *
     * @return the Neo node's response.
     * @throws TransactionConfigurationException if the number of signers and witnesses on the transaction are not
     *                                           equal.
     * @throws IOException                       if a problem in communicating with the Neo node occurs.
     */
    public NeoSendRawTransaction send() throws IOException {
        if (getSigners().size() != getWitnesses().size()) {
            throw new TransactionConfigurationException("The transaction does not have the same number of signers and" +
                    " witnesses. For every signer there has to be one witness, even if that witness is empty.");
        }
        int size = getSize();
        if (size > MAX_TRANSACTION_SIZE) {
            throw new TransactionConfigurationException(format("The transaction exceeds the maximum transaction size." +
                    " The maximum size is %s bytes while the transaction has size %s.", MAX_TRANSACTION_SIZE, size));
        }
        String hex = toHexStringNoPrefix(toArray());
        blockCountWhenSent = neow3j.getBlockCount().send().getBlockCount();
        return neow3j.sendRawTransaction(hex).send();
    }

    /**
     * Creates an {@code Observable} that emits the block number containing this transaction as soon as it has been
     * integrated in one. The observable completes right after emitting the block number.
     * <p>
     * The observable starts tracking the blocks from the point at which the transaction has been sent.
     *
     * @return the observable.
     * @throws IllegalStateException if this transaction has not yet been sent.
     */
    public Observable<Long> track() {
        if (blockCountWhenSent == null) {
            throw new IllegalStateException("Cannot subscribe before transaction has been sent.");
        }

        Predicate<NeoGetBlock> pred = neoGetBlock -> neoGetBlock.getBlock().getTransactions() != null &&
                neoGetBlock.getBlock().getTransactions().stream().anyMatch(tx -> tx.getHash().equals(getTxId()));

        return neow3j.catchUpToLatestAndSubscribeToNewBlocksObservable(blockCountWhenSent, true)
                .takeUntil(pred)
                .filter(pred)
                .map(neoGetBlock -> neoGetBlock.getBlock().getIndex());
    }

    /**
     * Gets the application log of this transaction.
     * <p>
     * The application log is not cached locally. Every time this method is called, requests are sent to the Neo node.
     * <p>
     * If the application log could not be fetched, {@code null} is returned.
     *
     * @return the application log.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     * @throws RpcResponseErrorException if the Neo node returns an error.
     */
    public NeoApplicationLog getApplicationLog() throws IOException {
        if (blockCountWhenSent == null) {
            throw new IllegalStateException("Cannot get the application log before transaction has been sent.");
        }
        return neow3j.getApplicationLog(getTxId()).send().getApplicationLog();
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
            if (reader.available() > 0) {
                this.witnesses = reader.readSerializableList(Witness.class);
            }
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    private void readTransactionAttributes(BinaryReader reader) throws IOException, DeserializationException {
        long nrOfAttributes = reader.readVarInt();
        if (nrOfAttributes + this.signers.size() > NeoConstants.MAX_TRANSACTION_ATTRIBUTES) {
            throw new DeserializationException(
                    format("A transaction can hold at most %s attributes (including signers). Input data had %s " +
                            "attributes.", NeoConstants.MAX_TRANSACTION_ATTRIBUTES, nrOfAttributes));
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
     * @return the serialized transaction.
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
     * Gets this transaction's data in the format used to produce the transaction's hash. E.g., for producing the
     * transaction ID or a transaction signature.
     * <p>
     * The returned value depends on the magic number of the used Neo network, which is retrieved from the Neo node
     * via the {@code getversion} RPC method if not already available locally.
     *
     * @return the transaction data ready for hashing.
     * @throws IOException if an error occurs when fetching the network's magic number.
     */
    public byte[] getHashData() throws IOException {
        return concatenate(neow3j.getNetworkMagicNumberBytes(), sha256(toArrayWithoutWitnesses()));
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
        io.neow3j.protocol.core.response.Transaction dtoTx = new io.neow3j.protocol.core.response.Transaction(this);
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(dtoTx);
    }

    /**
     * Produces a JSON object that can be used in neo-cli for further signing and relaying of this transaction.
     *
     * @return neo-cli compatible json of this transaction.
     * @throws IOException if an error occurs when trying to fetch the network's magic number.
     */
    public ContractParametersContext toContractParametersContext() throws IOException {
        String hash = getTxId().toString();
        String data = Base64.encode(toArrayWithoutWitnesses());
        long network = neow3j.getNetworkMagicNumber();

        Map<String, ContractParametersContext.ContextItem> items = signers.stream().map(signer -> {
            if (signer instanceof ContractSigner) {
                throw new UnsupportedOperationException("Cannot handle contract signers");
            }
            AccountSigner accountSigner = (AccountSigner) signer;
            VerificationScript verificationScript = accountSigner.getAccount().getVerificationScript();

            // Check if there's a witness for this signer and add all corresponding signatures as parameters.
            List<ContractParameter> params = new ArrayList<>();
            witnesses.stream().filter(w -> w.getVerificationScript().equals(verificationScript))
                    .map(Witness::getInvocationScript).findFirst()
                    .ifPresent(invocationScript -> invocationScript.getSignatures().stream()
                            .map(Sign.SignatureData::getConcatenated)
                            .forEach(s -> params.add(new ContractParameter(ContractParameterType.SIGNATURE, s))));
            if (params.isEmpty()) {
                // If no witness was found we need to set the parameter without a value.
                IntStream.range(0, verificationScript.getSigningThreshold())
                        .forEach(i -> params.add(new ContractParameter(ContractParameterType.SIGNATURE)));
            }

            Map<String, String> pubKeyToSignature = new HashMap<>();
            if (verificationScript.isSingleSigScript() && params.get(0).getValue() != null) {
                String pubKey = verificationScript.getPublicKeys().get(0).getEncodedCompressedHex();
                pubKeyToSignature.put(pubKey, Base64.encode((byte[]) params.get(0).getValue()));
            }
            String script = Base64.encode(verificationScript.getScript());
            return new ContractParametersContext.ContextItem(script, params, pubKeyToSignature);
        }).collect(Collectors.toMap(i -> "0x" + Hash160.fromScript(Base64.decode(i.getScript())), Function.identity()));

        return new ContractParametersContext(hash, data, items, network);
    }

}
