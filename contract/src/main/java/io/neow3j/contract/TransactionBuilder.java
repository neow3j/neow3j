package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.IOUtils;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttribute;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to build transaction scripts.
 * When signing the {@code TransactionBuilder}, a transaction is created that can be sent to the
 * Neo node.
 */
public class TransactionBuilder {

    protected Neow3j neow;
    protected Wallet wallet;
    protected Transaction transaction;
    private ScriptHash sender;

    // from transaction class
    private byte version;
    private long nonce;
    private Long validUntilBlock;
    private List<Signer> signers;
    private long systemFee;
    private long additionalNetworkFee;
    private List<TransactionAttribute> attributes;
    private byte[] script;
    private List<Witness> witnesses;

    protected TransactionBuilder(Neow3j neow) {
        this.neow = neow;
        // The random value used to initialize the nonce does not need cryptographic security,
        // therefore, we can use ThreadLocalRandom to generate it.
        this.nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
        this.version = NeoConstants.CURRENT_TX_VERSION;
        this.script = new byte[]{};
        this.systemFee = 0L;
        this.additionalNetworkFee = 0L;
        this.signers = new ArrayList<>();
        this.witnesses = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    /**
     * Sets the wallet used for this transaction.
     *
     * @param wallet The wallet.
     * @return this transaction builder.
     */
    public TransactionBuilder wallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    /**
     * Sets the version for this transaction.
     * <p>
     * It is set to {@link NeoConstants#CURRENT_TX_VERSION} by default.
     *
     * @param version The transaction version number.
     * @return this transaction builder.
     */
    public TransactionBuilder version(byte version) {
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
     * @return this transaction builder.
     * @throws TransactionConfigurationException if the nonce is not in the range [0, 2^32).
     */
    public TransactionBuilder nonce(Long nonce) {
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
     * @return this transaction builder.
     * @throws TransactionConfigurationException if the block number is not in the range [0,
     *                                           2^32).
     */
    public TransactionBuilder validUntilBlock(long blockNr) {
        if (blockNr < 0 || blockNr >= (long) Math.pow(2, 32)) {
            throw new TransactionConfigurationException("The block number up to which this " +
                    "transaction can be included cannot be less than zero or more than 2^32.");
        }
        validUntilBlock = blockNr;
        return this;
    }

    /**
     * Configures the transaction to use the given sender.
     *
     * @param sender the sender account's script hash.
     * @return this transaction builder.
     */
    public TransactionBuilder sender(ScriptHash sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Adds the given signers to this transaction.
     * <p>
     * If no sender is set explicitly, the first signer will be used as the sender of this transaction,
     * i.e. the payer of the transaction fees.
     *
     * @param signers Signers for this transaction.
     * @return this transaction builder.
     */
    public TransactionBuilder signers(Signer... signers) {
        if (containsDuplicateSigners(signers)) {
            throw new TransactionConfigurationException("Can't add multiple signers" +
                    " concerning the same account.");
        }
        this.signers.addAll(Arrays.asList(signers));
        return this;
    }

    /**
     * Sets the system fee for this transaction.
     * <p>
     * The system fee is the amount of GAS needed to execute this transaction's script in the
     * NeoVM. It is distributed to all NEO holders.
     *
     * @param systemFee The system fee in fractions of GAS (10^-8)
     * @return this transaction builder.
     */
    public TransactionBuilder systemFee(Long systemFee) {
        this.systemFee = systemFee;
        return this;
    }

    /**
     * Configures the transaction with an additional network fee.
     * <p>
     * The basic network fee required to send this transaction is added automatically.
     *
     * @param fee The additional network fee in fractions of GAS.
     * @return this transaction builder.
     */
    public TransactionBuilder additionalNetworkFee(Long fee) {
        this.additionalNetworkFee = fee;
        return this;
    }

    /**
     * Sets the contract script for this transaction.
     * <p>
     * The script defines the actions that this transaction will perform on the blockchain.
     *
     * @param script The contract script.
     * @return this transaction builder.
     */
    public TransactionBuilder script(byte[] script) {
        this.script = script;
        return this;
    }

    /**
     * Adds the given attributes to this transaction.
     * <p>
     * The maximum number of attributes on a transaction is given in {@link
     * NeoConstants#MAX_TRANSACTION_ATTRIBUTES}.
     *
     * @param attributes The attributes.
     * @return this transaction builder.
     * @throws TransactionConfigurationException when attempting to add more than {@link
     *                                           NeoConstants#MAX_TRANSACTION_ATTRIBUTES}
     *                                           attributes.
     */
    public TransactionBuilder attributes(TransactionAttribute... attributes) {
        if (this.attributes.size() + attributes.length >
                NeoConstants.MAX_TRANSACTION_ATTRIBUTES) {
            throw new TransactionConfigurationException("A transaction cannot have more "
                    + "than " + NeoConstants.MAX_TRANSACTION_ATTRIBUTES + " attributes.");
        }
        this.attributes.addAll(Arrays.asList(attributes));
        return this;
    }

    private boolean containsDuplicateSigners(Signer... signers) {
        List<ScriptHash> newSignersList = Stream.of(signers)
                .map(Signer::getScriptHash)
                .collect(Collectors.toList());
        Set<ScriptHash> newSignersSet = new HashSet<>(newSignersList);
        if (newSignersList.size() != newSignersSet.size()) {
            // The new signers list contains duplicates in itself.
            return true;
        }
        return this.signers.stream()
                .map(Signer::getScriptHash)
                .anyMatch(newSignersSet::contains);
    }

    /**
     * Adds the given witnesses to this transaction.
     * <p>
     * Witness data is used to check the transaction validity. It usually consists of the
     * signature generated by the transacting account but can also be other validating data.
     *
     * @param witnesses The witnesses.
     * @return this transaction builder.
     */
    public TransactionBuilder witnesses(Witness... witnesses) {
        for (Witness witness : witnesses) {
            if (witness.getScriptHash() == null) {
                throw new IllegalArgumentException("The script hash of the given script is " +
                        "empty. Please set the script hash.");
            }
        }
        this.witnesses.addAll(Arrays.asList(witnesses));
        return this;
    }

    // package-private visible for testability purpose.
    Transaction buildTransaction() throws IOException {
        if (wallet == null) {
            throw new TransactionConfigurationException("Cannot build a transaction without a wallet.");
        }

        if (script == null || script.length == 0) {
            throw new TransactionConfigurationException("Cannot build a transaction without a script.");
        }

        if (validUntilBlock == null) {
            // If validUntilBlock is not set explicitly set, then set it to the current max.
            // It can happen that the neo-node refuses the valid until block because of
            // it being over the max. Therefore, we decrement it by 1, to make sure that
            // the node doesn't reject the transaction.
            this.validUntilBlock(
                    fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT - 1);
        }

        if (signers.isEmpty()) {
            signers(Signer.feeOnly(wallet.getDefaultAccount().getScriptHash()));
        }
//        if (signers.isEmpty()) {
//            throw new TransactionConfigurationException("No signers are specified for this transaction. " +
//                    "A transaction requires at least one signer account that can cover the network and " +
//                    "system fees.");
//        }
        prepareTransactionSigners();

        this.systemFee(getSystemFeeForScript());
        long networkFee = calcNetworkFee() + additionalNetworkFee;

        return new Transaction(neow, version, nonce, validUntilBlock, signers, systemFee,
                networkFee, attributes, script, witnesses);
    }

    private long fetchCurrentBlockNr() throws IOException {
        return neow.getBlockCount().send().getBlockIndex().longValue();
    }

    // Prepares the list of signers such that there is at least one signer that will be used
    // to cover the transaction fees.
    private void prepareTransactionSigners() {
        if (sender != null) {
            if (hasSignerWithFeeOnlyScope()) {
                throw new TransactionConfigurationException("The list of transaction signers "
                        + "contained a signer with the fee-only scope and at the same time a "
                        + "sender was set. Either a fee-only signer, or a sender is allowed, "
                        + "but not both at the same time.");
            }
            Optional<Signer> senderInSignersOpt = signers.stream()
                    .filter(s -> s.getScriptHash().equals(sender))
                    .findFirst();
            if (senderInSignersOpt.isPresent()) {
                // If there exists a signer matching the sender account, move it to the first position
                // in the signer list.
                signers.remove(senderInSignersOpt.get());
                signers.add(0, senderInSignersOpt.get());
            } else {
                // Add the sender to the first position in the signer list.
                signers.add(0, Signer.feeOnly(sender));
            }
        } else if (signers.isEmpty()) {
            // If no signer or sender is present, add the default account as a signer with
            // the feeOnly witness scope. It will be used to cover the fees.
            this.signers(Signer.feeOnly(
                    wallet.getDefaultAccount().getScriptHash()));
        }
    }

    // Checks if there is a signer (excluding the sender) with a feeOnly scope.
    private boolean hasSignerWithFeeOnlyScope() {
        return signers.stream()
                .anyMatch(s -> s.getScopes().contains(WitnessScope.FEE_ONLY)
                        && !s.getScriptHash().equals(sender));
    }

    /*
     * Fetches the GAS consumed by this transaction. It does this by making an RPC call to the
     * Neo node. The returned GAS amount is in fractions of GAS (10^-8).
     */
    private long getSystemFeeForScript() throws IOException {
        // The signers are required for `invokescript` calls that will hit a CheckWitness
        // check in the smart contract.
        Signer[] signers = this.signers.toArray(new Signer[0]);
        String script = Numeric.toHexStringNoPrefix(this.script);
        NeoInvokeScript response = neow.invokeScript(script, signers).send();
        // The GAS amount is returned in fractions (10^8)
        return Long.parseLong(response.getInvocationResult().getGasConsumed());
    }

    /*
     * Calculates the necessary network fee for the transaction being build in this builder.
     * The fee consists of the cost per transaction byte and the cost for signature
     * verification. Since the transaction is not signed yet, the calculation works with
     * expected signatures. This information is derived from the verification scripts of all
     * signers added to the transaction.
     */
    private long calcNetworkFee() {
        List<Account> sigAccounts = getSignerAccounts();

        // Base transaction size
        int size = Transaction.HEADER_SIZE // constant header size
                + IOUtils.getVarSize(signers)
                + IOUtils.getVarSize(attributes) // attributes
                + IOUtils.getVarSize(script) // script
                + IOUtils.getVarSize(sigAccounts.size()); // varInt for all necessary witnesses

        // Calculate fee for witness verification and collect size of witnesses.
        int execFee = 0;
        for (Account acc : sigAccounts) {
            if (acc.isMultiSig()) {
                size += calcSizeForMultiSigWitness(acc.getVerificationScript());
                execFee += calcExecutionFeeForMultiSigWitness(acc.getVerificationScript());
            } else {
                size += calcSizeForSingleSigWitness(acc.getVerificationScript());
                execFee += calcExecutionFeeForSingleSigWitness();
            }
        }
        return execFee + size * NeoConstants.GAS_PER_BYTE;
    }

    /**
     * Gets the signer accounts held in the wallet.
     *
     * @return a list containing the signer accounts
     */
    private List<Account> getSignerAccounts() {
        List<Account> sigAccounts = new ArrayList<>();
        signers.forEach(signer -> {
            if (wallet.holdsAccount(signer.getScriptHash())) {
                sigAccounts.add(wallet.getAccount(signer.getScriptHash()));
            } else {
                throw new TransactionConfigurationException("Wallet does not contain the "
                        + "account for signer with script hash " + signer.getScriptHash());
            }
        });
        return sigAccounts;
    }

    private long calcSizeForSingleSigWitness(VerificationScript verifScript) {
        return NeoConstants.SERIALIZED_INVOCATION_SCRIPT_SIZE + verifScript.getSize();
    }

    private long calcExecutionFeeForSingleSigWitness() {
        return OpCode.PUSHDATA1.getPrice() // Push invocation script
                + OpCode.PUSHDATA1.getPrice() // Push verification script
                // Push null because we don't want to verify a particular message but the
                // transaction itself.
                + OpCode.PUSHNULL.getPrice()
                + InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1.getPrice();
    }

    private long calcSizeForMultiSigWitness(VerificationScript verifScript) {
        int m = verifScript.getSigningThreshold();
        int sizeInvocScript = NeoConstants.INVOCATION_SCRIPT_SIZE * m;
        return IOUtils.getVarSize(sizeInvocScript) + sizeInvocScript + verifScript.getSize();
    }

    private long calcExecutionFeeForMultiSigWitness(VerificationScript verifScript) {
        int m = verifScript.getSigningThreshold();
        int n = verifScript.getNrOfAccounts();

        return OpCode.PUSHDATA1.getPrice() * m
                + OpCode.get(new ScriptBuilder().pushInteger(m).toArray()[0]).getPrice()
                + OpCode.PUSHDATA1.getPrice() * n
                + OpCode.get(new ScriptBuilder().pushInteger(n).toArray()[0]).getPrice()
                // Push null because we don't want to verify a particular message but the
                // transaction itself.
                + OpCode.PUSHNULL.getPrice()
                + InteropServiceCode.NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1.getPrice(n);
    }

    /**
     * Makes an {@code invokescript} call to the neo-node with the transaction in its current
     * configuration. No changes are made to the blockchain state.
     * <p>
     * Make sure to add all necessary signers to the builder before making this call. They are
     * required for a successful {@code invokescript} call.
     *
     * @return the call's response.
     * @throws IOException if something goes wrong when communicating with the neo-node.
     */
    public NeoInvokeScript callInvokeScript() throws IOException {
        if (this.signers == null || this.script.length == 0) {
            throw new TransactionConfigurationException("Cannot make an 'invokescript' call "
                    + "without the script being configured.");
        }
        // The list of signers is required for `invokescript` calls that will hit a
        // CheckWitness check in the smart contract. We add the signers even if that
        // is not the case because we cannot know if the invoked script needs it or not and it
        // doesn't lead to failures if we add them in any case.
        Signer[] signers = this.signers.toArray(new Signer[0]);
        String script = Numeric.toHexStringNoPrefix(this.script);
        return neow.invokeScript(script, signers).send();
    }

    //    // sign and getTransactionForSigning
    // private build method to check for valid construction of Transaction object
    /**
     * Creates signatures for every signer of the transaction and adds them to the
     * transaction as witnesses.
     * <p>
     * For each signer set on the transaction a corresponding account with an EC key pair must exist
     * in the wallet set on the builder.
     *
     * @return this.
     */
    public Transaction sign() throws IOException {
        transaction = buildTransaction();
        byte[] txBytes = getTransactionForSigning();
        transaction.getSigners().forEach(signer -> {
            if (!this.wallet.holdsAccount(signer.getScriptHash())) {
                throw new TransactionConfigurationException("Can't create transaction "
                        + "signature. Wallet does not contain the signer account with script "
                        + "hash " + signer.getScriptHash());
            } else {
                Account signerAcc = this.wallet.getAccount(signer.getScriptHash());
                if (signerAcc.isMultiSig()) {
                    signWithMultiSigAccount(txBytes, signerAcc);
                } else {
                    signWithNormalAccount(txBytes, signerAcc);
                }
            }
        });
        return this.transaction;
    }

    /**
     * Gets the transaction for signing it.
     *
     * @return the transaction data ready for creating a signature.
     */
    public byte[] getTransactionForSigning() {
        return this.transaction.getHashData();
    }

    private void signWithNormalAccount(byte[] txBytes, Account acc) {
        ECKeyPair keyPair = acc.getECKeyPair();
        if (keyPair == null) {
            throw new TransactionConfigurationException("Can't create transaction signature "
                    + "because account with script " + acc.getScriptHash() + " doesn't hold a "
                    + "private key.");
        }
        this.transaction.addWitness(Witness.createWitness(txBytes, keyPair));
    }

    private void signWithMultiSigAccount(byte[] txBytes, Account signerAcc) {
        List<SignatureData> sigs = new ArrayList<>();
        VerificationScript multiSigVerifScript = signerAcc.getVerificationScript();
        for (ECPublicKey pubKey : multiSigVerifScript.getPublicKeys()) {
            ScriptHash accScriptHash = ScriptHash.fromPublicKey(pubKey.getEncoded(true));
            Account a = this.wallet.getAccount(accScriptHash);
            if (a == null || a.getECKeyPair() == null) {
                continue;
            }
            sigs.add(Sign.signMessage(txBytes, a.getECKeyPair()));
        }
        int m = multiSigVerifScript.getSigningThreshold();
        if (sigs.size() < m) {
            throw new TransactionConfigurationException("Can't create transaction "
                    + "signature. Wallet does not contain enough accounts (with decrypted "
                    + "private keys) that are part of the multi-sig account with script "
                    + "hash " + signerAcc.getScriptHash() + ".");
        }
        this.transaction.addWitness(Witness.createMultiSigWitness(sigs,
                multiSigVerifScript));
    }

    /**
     * Checks if the sender account of this transaction can cover the network and system fees. If
     * not, executes the given consumer supplying it with the required fee and the sender's GAS
     * balance.
     *
     * @return this.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    public TransactionBuilder doIfSenderCannotCoverFees(BiConsumer<BigInteger, BigInteger> consumer)
            throws IOException {
        BigInteger fees = BigInteger.valueOf(
                this.transaction.getSystemFee() + this.transaction.getNetworkFee());
        BigInteger senderGasBalance = new GasToken(this.neow)
                .getBalanceOf(this.transaction.getSender());
        if (fees.compareTo(senderGasBalance) > 0) {
            consumer.accept(fees, senderGasBalance);
        }
        return this;
    }

    /**
     * Checks if the sender account of this transaction can cover the network and system fees. If
     * not, otherwise throw an exception created by the provided supplier.
     *
     * @return this.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    public <T extends Throwable> TransactionBuilder throwIfSenderCannotCoverFees(
            Supplier<? extends T> exceptionSupplier) throws IOException, T {

        if (!canSenderCoverFees()) {
            throw exceptionSupplier.get();
        }
        return this;
    }

    private boolean canSenderCoverFees() throws IOException {
        BigInteger fees = BigInteger.valueOf(
                this.transaction.getSystemFee() + this.transaction.getNetworkFee());
        BigInteger senderGasBalance = new GasToken(this.neow)
                .getBalanceOf(this.transaction.getSender());
        return fees.compareTo(senderGasBalance) < 0;
    }

    public byte[] getScript() {
        return script;
    }

    public List<Signer> getSigners() {
        return signers;
    }

    //    // send method
//
//    /**
//     * Sends this invocation transaction to the neo-node via the `sendrawtransaction` RPC.
//     *
//     * @return the Neo node's response.
//     * @throws IOException                      if a problem in communicating with the Neo node
//     *                                          occurs.
//     * @throws InvocationConfigurationException if signatures are missing for one or more signers of
//     *                                          the transaction.
//     */
//    public NeoSendRawTransaction send() throws IOException {
//        if (this.transaction == null) {
//            throw new IllegalStateException("No transaction present to be sent.");
//        }
//        List<ScriptHash> witnesses = this.transaction.getWitnesses().stream()
//                .map(Witness::getScriptHash).collect(Collectors.toList());
//
//        for (Signer signer : this.transaction.getSigners()) {
//            if (!witnesses.contains(signer.getScriptHash())) {
//                throw new InvocationConfigurationException("The transaction does not have a "
//                        + "signature for each of its signers.");
//            }
//        }
//        String hex = Numeric.toHexStringNoPrefix(this.transaction.toArray());
//        return neow.sendRawTransaction(hex).send();
//    }

//    /**
//     * Adds the given witnesses to the invocation transaction.
//     * <p>
//     * Use this method if you can't use the automatic signing method {@link TransactionBuilder#sign()},
//     * e.g., because the configured wallet does not contain all accounts needed for signing.
//     *
//     * @param witnesses The witnesses to add.
//     */
//    public void addWitnesses(Witness... witnesses) {
//        for (Witness witness : witnesses) {
//            this.transaction.addWitness(witness);
//        }
//    }

//    /**
//     * Gets the transaction.
//     *
//     * @return the transaction.
//     */
//    public Transaction getTransaction() {
//        return this.transaction;
//    }
//
//    // utility methods
//

//    // Checks if there is a signer (excluding the sender) with a feeOnly scope.
//    private boolean hasSignerWithFeeOnlyScope() {
//        return this.getSigners().stream()
//                .anyMatch(s -> s.getScopes().contains(WitnessScope.FEE_ONLY)
//                        && !s.getScriptHash().equals(this.sender));
//    }
}
