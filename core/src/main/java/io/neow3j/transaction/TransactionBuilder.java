package io.neow3j.transaction;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jConfig;
import io.neow3j.protocol.core.response.NeoInvokeScript;
import io.neow3j.script.VerificationScript;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.neow3j.constants.NeoConstants.MAX_TRANSACTION_ATTRIBUTES;
import static io.neow3j.crypto.Sign.signMessage;
import static io.neow3j.transaction.TransactionAttributeType.HIGH_PRIORITY;
import static io.neow3j.transaction.Witness.createContractWitness;
import static io.neow3j.transaction.Witness.createMultiSigWitness;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.util.Arrays.asList;

/**
 * Used to build a {@link Transaction}. When signing the {@code TransactionBuilder}, a transaction
 * is created that can be sent to the Neo node.
 */
public class TransactionBuilder {

    private static final Hash160 GAS_TOKEN_HASH = new Hash160(
            "d2a4cff31913016155e38e474a2c06d08be276cf");
    private static final String BALANCE_OF_FUNCTION = "balanceOf";

    protected Neow3j neow3j;
    protected Wallet wallet;
    protected Transaction transaction;

    private byte version;
    private long nonce;
    private Long validUntilBlock;
    private List<Signer> signers;
    private long additionalNetworkFee;
    private List<TransactionAttribute> attributes;
    private byte[] script;

    private BiConsumer<BigInteger, BigInteger> consumer;
    private Supplier<? extends Throwable> supplier;

    public TransactionBuilder(Neow3j neow3j) {
        this.neow3j = neow3j;
        // The random value used to initialize the nonce does not need cryptographic security,
        // therefore, we can use ThreadLocalRandom to generate it.
        this.nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
        this.version = NeoConstants.CURRENT_TX_VERSION;
        this.script = new byte[]{};
        this.additionalNetworkFee = 0L;
        this.signers = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    /**
     * Sets the wallet used for this transaction.
     * <p>
     * The wallet is required for retrieving the signer accounts when signing the transaction.
     *
     * @param wallet the wallet.
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
     * @param version the transaction version number.
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
     * @param nonce the transaction nonce.
     * @return this transaction builder.
     * @throws TransactionConfigurationException if the nonce is not in the range [0, 2^32).
     */
    public TransactionBuilder nonce(long nonce) {
        if (nonce < 0 || nonce >= (long) Math.pow(2, 32)) {
            throw new TransactionConfigurationException(
                    "The value of the transaction nonce must be in the interval [0, 2^32).");
        }
        this.nonce = nonce;
        return this;
    }

    /**
     * Sets the number of the block up to which this transaction can be included. If that block
     * number is reached in the network and this transaction is not yet included in a block, it
     * becomes invalid.
     * <p>
     * By default it is set to the maximum, which is the current chain height plus
     * {@link Neow3jConfig#getMaxValidUntilBlockIncrement()}.
     *
     * @param blockNr the block number.
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
     * Sets the signer belonging to the given {@code sender} account to the first index of the list
     * of signers for this transaction. The first signer covers the fees for the transaction if
     * there is no signer present with fee-only witness scope (see {@link WitnessScope#NONE}).
     *
     * @param sender the account of the signer to be set to the first index.
     * @return this transaction builder.
     */
    public TransactionBuilder firstSigner(Account sender) {
        return firstSigner(sender.getScriptHash());
    }

    /**
     * Sets the signer with script hash {@code sender} to the first index of the list of signers for
     * this transaction. The first signer covers the fees for the transaction if there is no signer
     * present with fee-only witness scope (see {@link WitnessScope#NONE}).
     *
     * @param sender the script hash of the signer to be set to the first index.
     * @return this transaction builder.
     */
    public TransactionBuilder firstSigner(Hash160 sender) {
        if (signers.stream().map(Signer::getScopes)
                .anyMatch(scopes -> scopes.contains(WitnessScope.NONE))) {
            throw new IllegalStateException("This transaction contains a signer with fee-only " +
                    "witness scope that will cover the fees. Hence, the order of the signers " +
                    "does not affect the payment of the fees.");
        } else {
            Signer s = signers.stream().filter(signer -> signer.getScriptHash().equals(sender))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find a signer with " +
                            "script hash " + sender.toString() + ". Make sure to add the signer " +
                            "before calling this method."));
            signers.remove(s);
            signers.add(0, s);
        }
        return this;
    }

    /**
     * Sets the signers of this transaction. If the list of signers already contains signers, they
     * are replaced.
     * <p>
     * If one of the signers has the fee-only witness scope (see {@link WitnessScope#NONE}), this
     * account is used to cover the transaction fees. Otherwise, the first signer is used as the
     * sender of this transaction, meaning that it is used to cover the transaction fees.
     *
     * @param signers the signers for this transaction.
     * @return this transaction builder.
     * @throws TransactionConfigurationException if multiple signers of the same account, or
     *                                           multiple signers with the fee-only witness scope
     *                                           are added.
     */
    public TransactionBuilder signers(Signer... signers) {
        if (containsDuplicateSigners(signers)) {
            throw new TransactionConfigurationException("Can't add multiple signers concerning " +
                    "the same account.");
        }
        checkAndThrowIfMaxAttributesExceeded(signers.length, attributes.size());
        this.signers = new ArrayList<>(asList(signers));
        return this;
    }

    private void checkAndThrowIfMaxAttributesExceeded(int totalSigners, int totalAttributes) {
        if (totalSigners + totalAttributes > MAX_TRANSACTION_ATTRIBUTES) {
            throw new TransactionConfigurationException("A transaction cannot have more than " +
                    MAX_TRANSACTION_ATTRIBUTES + " attributes (including signers).");
        }
    }

    /**
     * Configures the transaction with an additional network fee.
     * <p>
     * The basic network fee required to send this transaction is added automatically.
     *
     * @param fee the additional network fee in fractions of GAS.
     * @return this transaction builder.
     */
    public TransactionBuilder additionalNetworkFee(long fee) {
        this.additionalNetworkFee = fee;
        return this;
    }

    /**
     * Sets the script for this transaction. It defines the actions that this transaction will
     * perform on the blockchain.
     *
     * @param script the contract script.
     * @return this transaction builder.
     */
    public TransactionBuilder script(byte[] script) {
        this.script = script;
        return this;
    }

    /**
     * Adds the given attributes to this transaction.
     * <p>
     * The maximum number of attributes on a transaction is given in
     * {@link NeoConstants#MAX_TRANSACTION_ATTRIBUTES}.
     *
     * @param attributes the attributes.
     * @return this transaction builder.
     * @throws TransactionConfigurationException when attempting to add more than
     *                                           {@link NeoConstants#MAX_TRANSACTION_ATTRIBUTES}
     *                                           attributes.
     */
    public TransactionBuilder attributes(TransactionAttribute... attributes) {
        checkAndThrowIfMaxAttributesExceeded(signers.size(),
                this.attributes.size() + attributes.length);
        Arrays.stream(attributes).forEach(attr -> {
            if (attr.getType() == HIGH_PRIORITY) {
                safeAddHighPriorityAttribute((HighPriorityAttribute) attr);
            }
        });
        return this;
    }

    // Make sure that only one high priority attribute is present
    private void safeAddHighPriorityAttribute(HighPriorityAttribute attr) {
        if (!isHighPriority()) {
            attributes.add(attr);
        }
    }

    private boolean containsDuplicateSigners(Signer... signers) {
        List<Hash160> signerList = Stream.of(signers).map(Signer::getScriptHash)
                .collect(Collectors.toList());
        Set<Hash160> signerSet = new HashSet<>(signerList);
        return signerList.size() != signerSet.size();
    }

    // package-private visible for testability purpose.
    Transaction buildTransaction() throws Throwable {
        if (wallet == null) {
            throw new TransactionConfigurationException("Cannot build a transaction without a " +
                    "wallet that contains the accounts of the transaction signers.");
        }

        if (script == null || script.length == 0) {
            throw new TransactionConfigurationException("Cannot build a transaction without a " +
                    "script.");
        }

        if (validUntilBlock == null) {
            // If validUntilBlock is not set explicitly, then set it to the current max. It can
            // happen that the neo-node rejects the transaction when we set the validUntilBlock
            // to the max. To be sure that this does not happen, we decrement the max by 1.
            this.validUntilBlock(fetchCurrentBlockCount() + neow3j.getMaxValidUntilBlockIncrement()
                    - 1);
        }

        if (signers.isEmpty()) {
            throw new IllegalStateException("Can't create a transaction without signers. At least" +
                    " one signer with witness scope fee-only or higher is required.");
        }

        if (isHighPriority() && !isAllowedForHighPriority()) {
            throw new IllegalStateException("This transaction does not have a committee member as "
                    + "signer. Only committee members can send transactions with high priority.");
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
        return new Transaction(neow3j, version, nonce, validUntilBlock, signers, systemFee,
                networkFee, attributes, script, new ArrayList<>());
    }

    // Checks if this transaction builder contains a high priority attribute.
    private boolean isHighPriority() {
        return attributes.stream().anyMatch(t -> t.getType() == HIGH_PRIORITY);
    }

    // Checks if this transaction contains a signer that is a committee member.
    private boolean isAllowedForHighPriority() throws IOException {
        List<Hash160> committee = neow3j.getCommittee().send()
                .getCommittee()
                .stream().map(ECPublicKey::new)
                .map(key -> key.getEncoded(true))
                .map(Hash160::fromPublicKey)
                .collect(Collectors.toList());

        boolean signersContainSingleSigCommitteeMember = signers.stream()
                .map(Signer::getScriptHash).anyMatch(committee::contains);
        if (signersContainSingleSigCommitteeMember) {
            return true;
        }
        return signersContainMultiSigWithCommitteeMember(committee);
    }

    // Checks if the signers contains a multi-sig account that contains a
    // committee member.
    private boolean signersContainMultiSigWithCommitteeMember(List<Hash160> committee) {
        Iterator<Hash160> iterator = signers.stream().map(Signer::getScriptHash).iterator();
        while (iterator.hasNext()) {
            Hash160 scriptHash = iterator.next();
            try {
                Account account = wallet.getAccount(scriptHash);
                if (account.isMultiSig()) {
                    Stream<Hash160> accountStream = account
                            .getVerificationScript().getPublicKeys()
                            .stream().map(s -> s.getEncoded(true))
                            .map(Hash160::fromPublicKey);
                    boolean multiSigContainsCommitteeMember = accountStream
                            .filter(sh -> wallet.holdsAccount(sh))
                            .anyMatch(committee::contains);
                    if (multiSigContainsCommitteeMember) {
                        return true;
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return false;
    }

    private long fetchCurrentBlockCount() throws IOException {
        return neow3j.getBlockCount().send().getBlockCount().longValue();
    }

    /*
     * Fetches the GAS consumed by this transaction. It does this by making an RPC call to the
     * Neo node.
     * The returned GAS amount is in fractions of GAS (10^-8).
     */
    private long getSystemFeeForScript() throws IOException {
        // The signers are required for `invokescript` calls that will hit a
        // CheckWitness check in the smart contract.
        Signer[] signers = this.signers.toArray(new Signer[0]);
        String script = toHexStringNoPrefix(this.script);
        NeoInvokeScript response = neow3j.invokeScript(script, signers).send();
        if (response.getResult().hasStateFault()) {
            throw new TransactionConfigurationException("The vm exited due to the following " +
                    "exception: " + response.getResult().getException());
        }
        return new BigInteger(response.getInvocationResult().getGasConsumed()).longValue();
    }

    private long calcNetworkFee() throws IOException {
        Transaction tx = new Transaction(neow3j, version, nonce, validUntilBlock, signers, 0, 0,
                attributes, script, new ArrayList<>());
        // For each signer that is available in the wallet, we add a witness to a temporary
        // transaction object that is serialized and sent to the `getnetworkfee` RPC method.
        // Signers that are contracts do not need a verification script. Instead, their `verify`
        // method will be consulted by the neo-node. We use the static method
        // createContractWitness to instantiate a witness with the parameters for the verify
        // method in its invocation script.
        boolean hasAtLeastOneSigningAccount = false;
        for (Signer signer : signers) {
            if (signer instanceof ContractSigner) {
                ContractSigner contractSigner = (ContractSigner) signer;
                tx.addWitness(createContractWitness(contractSigner.getVerifyParameters()));
            } else {
                Account a = wallet.getAccount(signer.getScriptHash());
                if (a != null && a.getVerificationScript() != null) {
                    tx.addWitness(new Witness(new byte[]{}, a.getVerificationScript().getScript()));
                    hasAtLeastOneSigningAccount = true;
                } else {
                    throw new TransactionConfigurationException("The wallet does not hold the " +
                            "verification script of the signer with script hash '" +
                            signer.getScriptHash() + "'. If this signer is a contract, use the " +
                            "method 'asContract' in the class Signer.");
                }
            }
        }
        if (!hasAtLeastOneSigningAccount) {
            throw new TransactionConfigurationException("No signers were set for which an account" +
                    " with verification script exists in the wallet.");
        }
        String txHex = toHexStringNoPrefix(tx.toArray());
        return neow3j.calculateNetworkFee(txHex).send().getNetworkFee().getNetworkFee().longValue();
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
        if (signers == null || script.length == 0) {
            throw new TransactionConfigurationException(
                    "Cannot make an 'invokescript' call without the script being configured.");
        }
        // The list of signers is required for `invokescript` calls that will hit a
        // CheckWitness check in the smart contract. We add the signers even if that
        // is not the case because we cannot know if the invoked script needs it or not and it
        // doesn't lead to failures if we add them in any case.
        Signer[] signers = this.signers.toArray(new Signer[0]);
        String script = toHexStringNoPrefix(this.script);
        return neow3j.invokeScript(script, signers).send();
    }

    /**
     * Builds the transaction, creates signatures for every signer and adds them to the transaction
     * as witnesses.
     * <p>
     * For each signer of the transaction, a corresponding account with an EC key pair must exist in
     * the wallet set on this transaction builder.
     *
     * @return the signed transaction.
     * @throws TransactionConfigurationException if the builder is mis-configured.
     * @throws IOException                       if an error occurs when interacting with the
     *                                           Neo node.
     * @throws Throwable                         a custom exception if one was set to be thrown in
     *                                           the case the sender cannot cover the transaction
     *                                           fees.
     */
    public Transaction sign() throws Throwable {
        transaction = buildTransaction();
        byte[] txBytes = transaction.getHashData();
        transaction.getSigners().forEach(signer -> {
            if (signer instanceof ContractSigner) {
                ContractSigner contractSigner = (ContractSigner) signer;
                transaction.addWitness(createContractWitness(contractSigner.getVerifyParameters()));
            } else {
                // There's no need to check if every signer has its account in the wallet here.
                // This check has already been executed within building the transaction above
                // when calculating the network fees.
                Account signerAcc = wallet.getAccount(signer.getScriptHash());
                if (signerAcc.isMultiSig()) {
                    signWithMultiSigAccount(txBytes, signerAcc);
                } else {
                    signWithNormalAccount(txBytes, signerAcc);
                }
            }
        });
        return transaction;
    }

    /**
     * Builds the transaction without signing it.
     * @return the unsigned transaction.
     * @throws TransactionConfigurationException if the builder is mis-configured.
     * @throws IOException                       if an error occurs when interacting with the
     *                                           Neo node.
     * @throws Throwable                         a custom exception if one was set to be thrown in
     *                                           the case the sender cannot cover the transaction
     *                                           fees.
     */
    public Transaction getUnsignedTransaction() throws Throwable {
        return buildTransaction();
    }

    private void signWithNormalAccount(byte[] txBytes, Account acc) {
        ECKeyPair keyPair = acc.getECKeyPair();
        if (keyPair == null) {
            throw new TransactionConfigurationException("Can't create transaction signature " +
                    "because account with script " + acc.getScriptHash() + " doesn't hold a " +
                    "private key.");
        }
        transaction.addWitness(Witness.create(txBytes, keyPair));
    }

    private void signWithMultiSigAccount(byte[] txBytes, Account signerAcc) {
        List<SignatureData> sigs = new ArrayList<>();
        VerificationScript multiSigVerifScript = signerAcc.getVerificationScript();
        for (ECPublicKey pubKey : multiSigVerifScript.getPublicKeys()) {
            Hash160 accScriptHash = Hash160.fromPublicKey(pubKey.getEncoded(true));
            Account a;
            a = wallet.getAccount(accScriptHash);
            if (a == null) {
                continue;
            }
            ECKeyPair ecKeyPair = a.getECKeyPair();
            if (ecKeyPair == null) {
                continue;
            }
            sigs.add(signMessage(txBytes, ecKeyPair));
        }
        int m = multiSigVerifScript.getSigningThreshold();
        if (sigs.size() < m) {
            throw new TransactionConfigurationException("Can't create transaction signature. " +
                    "Wallet does not contain enough accounts (with decrypted private keys) that " +
                    "are part of the multi-sig account with script hash " +
                    signerAcc.getScriptHash() + ".");
        }
        transaction.addWitness(createMultiSigWitness(sigs, multiSigVerifScript));
    }

    /**
     * Checks if the sender account of this transaction can cover the network and system fees. If
     * not, executes the given consumer supplying it with the required fee and the sender's GAS
     * balance.
     * <p>
     * The check and potential execution of the consumer is only performed when the transaction is
     * built, i.e., when calling {@link TransactionBuilder#sign()} or
     * {@link TransactionBuilder#getUnsignedTransaction()}.
     *
     * @param consumer the consumer.
     * @return this transaction builder.
     */
    public TransactionBuilder doIfSenderCannotCoverFees(BiConsumer<BigInteger,
            BigInteger> consumer) {
        if (supplier != null) {
            throw new IllegalStateException("Can't handle a consumer for this case, since an " +
                    "exception will be thrown if the sender cannot cover the fees.");
        }
        this.consumer = consumer;
        return this;
    }

    /**
     * Checks if the sender account of this transaction can cover the network and system fees. If
     * not, otherwise throw an exception created by the provided supplier.
     * <p>
     * The check and potential throwing of the exception is only performed when the transaction is
     * built, i.e., when calling {@link TransactionBuilder#sign()} or
     * {@link TransactionBuilder#getUnsignedTransaction()}.
     *
     * @param exceptionSupplier the exception supplier.
     * @return this transaction builder.
     */
    public TransactionBuilder throwIfSenderCannotCoverFees(
            Supplier<? extends Throwable> exceptionSupplier) {
        if (consumer != null) {
            throw new IllegalStateException("Can't handle a supplier for this case, since a " +
                    "consumer will be executed if the sender cannot cover the fees.");
        }
        supplier = exceptionSupplier;
        return this;
    }

    private BigInteger getSenderGasBalance() throws IOException {
        return neow3j.invokeFunction(GAS_TOKEN_HASH, BALANCE_OF_FUNCTION,
                asList(hash160(getSender())))
                .send().getInvocationResult().getStack().get(0).getInteger();
    }

    private Hash160 getSender() {
        // First we look for a signer that has the fee-only scope. The signer with that scope is
        // the sender of the transaction. If there is no such signer then the order of the
        // signers defines the sender, i.e., the first signer is the sender of the transaction.
        return signers.stream()
                .filter(signer -> signer.getScopes().contains(WitnessScope.NONE))
                .findFirst()
                .orElse(signers.get(0))
                .getScriptHash();
    }

    private boolean canSenderCoverFees(BigInteger fees) throws IOException {
        return fees.compareTo(getSenderGasBalance()) < 0;
    }

    // Required for testability
    public byte[] getScript() {
        return script;
    }

    // Required for testability
    public List<Signer> getSigners() {
        return signers;
    }

}
