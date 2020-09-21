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
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttribute;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to build a {@link Transaction}. When signing the {@code TransactionBuilder}, a transaction
 * is created that can be sent to the Neo node.
 */
public class TransactionBuilder {

    protected Neow3j neow;
    protected Wallet wallet;
    protected Transaction transaction;

    private byte version;
    private long nonce;
    private Long validUntilBlock;
    private List<Signer> signers;
    private long additionalNetworkFee;
    private List<TransactionAttribute> attributes;
    private byte[] script;
    private List<Witness> witnesses;

    private BiConsumer<BigInteger, BigInteger> consumer;
    private Supplier<? extends Throwable> supplier;

    protected TransactionBuilder(Neow3j neow) {
        this.neow = neow;
        // The random value used to initialize the nonce does not need cryptographic security,
        // therefore, we can use ThreadLocalRandom to generate it.
        this.nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
        this.version = NeoConstants.CURRENT_TX_VERSION;
        this.script = new byte[]{};
        this.additionalNetworkFee = 0L;
        this.signers = new ArrayList<>();
        this.witnesses = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    /**
     * Sets the wallet used for this transaction.
     * <p>
     * The wallet is required for retrieving the signer accounts when signing the transaction.
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
     * Sets the number of the block up to which this transaction can be included. If that block
     * number is reached in the network and this transaction is not yet included in a block, it
     * becomes invalid.
     * <p>
     * By default it is set to the maximum, which is the current chain height plus {@link
     * NeoConstants#MAX_VALID_UNTIL_BLOCK_INCREMENT}.
     *
     * @param blockNr The block number.
     * @return this transaction builder.
     * @throws TransactionConfigurationException if the block number is not in the range [0, 2^32).
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
     * Sets the signer with script hash {@code sender} to the first index of the list of signers
     * for this transaction. The first signer covers the fees for the transaction if there is
     * no signer present with fee-only witness scope (see {@link WitnessScope#FEE_ONLY}).
     *
     * @param sender the script hash of the signer to be set to the first index.
     * @return this transaction builder.
     */
    public TransactionBuilder setFirstSigner(ScriptHash sender) {
        if (signers.stream()
                .map(Signer::getScopes)
                .anyMatch(scopes -> scopes.contains(WitnessScope.FEE_ONLY))) {
            throw new IllegalStateException("This transaction contains a signer with " +
                    "fee-only witness scope that will cover the fees. Hence, the order " +
                    "of the signers does not affect the payment of the fees.");
        } else {
            Signer s = signers.stream()
                    .filter(signer -> signer.getScriptHash().equals(sender))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find a signer with " +
                            "script hash " + sender.toString() + ". Make sure to add the " +
                            "signer before calling this method."));
            signers.remove(s);
            signers.add(0, s);
            }
        return this;
    }

    /**
     * Sets the given signers to this transaction. If one of the signers has the fee-only witness
     * scope (see {@link WitnessScope#FEE_ONLY}), this account is used to cover the transaction fees.
     * Otherwise, the first signer is used as the sender of this transaction, meaning that it is used
     * to cover the transaction fees.
     *
     * @param signers Signers for this transaction.
     * @return this transaction builder.
     * @throws TransactionConfigurationException if multiple signers of the same account, or
     *                                           multiple signers with the fee-only witness scope
     *                                           are added.
     */
    public TransactionBuilder signers(Signer... signers) {
        if (containsDuplicateSigners(signers)) {
            throw new TransactionConfigurationException("Can't add multiple signers" +
                    " concerning the same account.");
        }
        if (containsMultipleFeeOnlySigners(signers)) {
            throw new TransactionConfigurationException("Can't add multiple signers with the "
                    + "fee-only witness scope. Only one signer can be used to cover the "
                    + "transaction fees.");
        }
        this.signers = new ArrayList<>(Arrays.asList(signers));
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
     * Sets the script for this transaction. It defines the actions that this transaction will
     * perform on the blockchain.
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
            throw new TransactionConfigurationException("A transaction cannot have more than "
                    + NeoConstants.MAX_TRANSACTION_ATTRIBUTES + " attributes.");
        }
        this.attributes.addAll(Arrays.asList(attributes));
        return this;
    }

    private boolean containsDuplicateSigners(Signer... signers) {
        List<ScriptHash> signerList = Stream.of(signers)
                .map(Signer::getScriptHash)
                .collect(Collectors.toList());
        Set<ScriptHash> signerSet = new HashSet<>(signerList);
        return signerList.size() != signerSet.size();
    }

    private boolean containsMultipleFeeOnlySigners(Signer... signers) {
        return Stream.of(signers)
                .filter(s -> s.getScopes().contains(WitnessScope.FEE_ONLY))
                .count() > 1;
    }

    // package-private visible for testability purpose.
    Transaction buildTransaction() throws Throwable {
        if (wallet == null) {
            throw new TransactionConfigurationException(
                    "Cannot build a transaction without a wallet.");
        }

        if (script == null || script.length == 0) {
            throw new TransactionConfigurationException(
                    "Cannot build a transaction without a script.");
        }

        if (validUntilBlock == null) {
            // If validUntilBlock is not set explicitly, then set it to the current max. It can
            // happen that the neo-node rejects the transaction when we set the validUntilBlock
            // to the max. To be sure that this does not happen, we decrement the max by 1.
            this.validUntilBlock(
                    fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT - 1);
        }

        if (signers.isEmpty()) {
            throw new IllegalStateException("Can't create a transaction without any signer. " +
                    "A transaction requires at least one signer with witness scope fee-only " +
                    "or higher.");
        }

        long systemFee = getSystemFeeForScript();
        long networkFee = calcNetworkFee() + additionalNetworkFee;
        BigInteger fees = BigInteger.valueOf(systemFee + networkFee);

        if (supplier != null && !canSenderCoverFees(fees)) {
            throw supplier.get();
        } else if (consumer != null) {
            BigInteger senderGasBalance = getSenderGasBalance();
            if (fees.compareTo(senderGasBalance) > 0) {
                consumer.accept(fees, senderGasBalance);
            }
        }
        return new Transaction(neow, version, nonce, validUntilBlock, signers, systemFee,
                networkFee, attributes, script, witnesses);
    }

    private long fetchCurrentBlockNr() throws IOException {
        return neow.getBlockCount().send().getBlockIndex().longValue();
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
                throw new TransactionConfigurationException("Cannot find account with script hash '"
                        + signer.getScriptHash().toString() + "' in wallet set on this transaction "
                        + "builder.");
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

    /**
     * Builds the transaction, creates signatures for every signer and adds them to the transaction
     * as witnesses.
     * <p>
     * For each signer of the transaction, a corresponding account with an EC key pair must exist in
     * the wallet set on this transaction builder.
     *
     * @return the signed transaction.
     */
    public Transaction sign() throws Throwable {
        transaction = buildTransaction();
        byte[] txBytes = transaction.getHashData();
        transaction.getSigners().forEach(signer -> {
            // There's no need to check if every signer has its account in the wallet here.
            // This check has already been executed within building the transaction above
            // when calculating the network fees.
            Account signerAcc = wallet.getAccount(signer.getScriptHash());
            if (signerAcc.isMultiSig()) {
                signWithMultiSigAccount(txBytes, signerAcc);
            } else {
                signWithNormalAccount(txBytes, signerAcc);
            }
        });
        return this.transaction;
    }

    /**
     * Builds the transaction without signing it.
     *
     * @return the unsigned transaction.
     */
    public Transaction getUnsignedTransaction() throws Throwable {
        return buildTransaction();
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
            throw new TransactionConfigurationException("Can't create transaction signature. "
                    + "Wallet does not contain enough accounts (with decrypted private keys) that "
                    + "are part of the multi-sig account with script hash "
                    + signerAcc.getScriptHash() + ".");
        }
        this.transaction.addWitness(Witness.createMultiSigWitness(sigs,
                multiSigVerifScript));
    }

    /**
     * Checks if the sender account of this transaction can cover the network and system fees. If
     * not, executes the given consumer supplying it with the required fee and the sender's GAS
     * balance.
     * <p>
     * The check and potential execution of the consumer is only performed when the transaction is
     * built, i.e., when calling {@link TransactionBuilder#sign()} or {@link
     * TransactionBuilder#getUnsignedTransaction()}.
     *
     * @return this transaction builder.
     */
    public TransactionBuilder doIfSenderCannotCoverFees(
            BiConsumer<BigInteger, BigInteger> consumer) {
        if (supplier != null) {
            throw new IllegalStateException(
                    "Can't handle a consumer for this case, since an exception " +
                            "will be thrown if the sender cannot cover the fees.");
        }
        this.consumer = consumer;
        return this;
    }

    /**
     * Checks if the sender account of this transaction can cover the network and system fees. If
     * not, otherwise throw an exception created by the provided supplier.
     * <p>
     * The check and potential throwing of the exception is only performed when the transaction is
     * built, i.e., when calling {@link TransactionBuilder#sign()} or {@link
     * TransactionBuilder#getUnsignedTransaction()}.
     *
     * @return this transaction builder.
     */
    public TransactionBuilder throwIfSenderCannotCoverFees(
            Supplier<? extends Throwable> exceptionSupplier) {
        if (consumer != null) {
            throw new IllegalStateException(
                    "Can't handle a supplier for this case, since a consumer " +
                            "will be executed if the sender cannot cover the fees.");
        }
        supplier = exceptionSupplier;
        return this;
    }

    private BigInteger getSenderGasBalance() throws IOException {
        return new GasToken(neow).getBalanceOf(getSender());
    }

    private ScriptHash getSender() {
        // First we look for a signer that has the fee-only scope. The signer with that scope is
        // the sender of the transaction. If there is no such signer then the order of the
        // signers defines the sender, i.e., the first signer is the sender of the transaction.
        return signers.stream()
                .filter(signer -> signer.getScopes().contains(WitnessScope.FEE_ONLY))
                .findFirst()
                .orElse(signers.get(0))
                .getScriptHash();
    }

    private boolean canSenderCoverFees(BigInteger fees) throws IOException {
        return fees.compareTo(getSenderGasBalance()) < 0;
    }

    // For testability only
    protected byte getVersion() {
        return version;
    }
    // For testability only
    protected byte[] getScript() {
        return script;
    }

    // For testability only
    protected List<Signer> getSigners() {
        return signers;
    }
}
