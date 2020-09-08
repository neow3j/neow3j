package io.neow3j.contract;

import static java.util.Arrays.asList;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.IOUtils;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
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
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Used to build transaction scripts.
 * When signing the {@code TransactionBuilder}, a transaction is created that can be sent to the
 * Neo node.
 */
public class TransactionBuilder {

    protected Neow3j neow;
    protected Wallet wallet;
    protected Transaction transaction;

    private long additionalNetworkFee;
    private String contractFunction;
    private List<ContractParameter> contractParams;
    private ScriptHash contract;
    protected boolean failOnFalse;
    private ScriptHash sender;

    // from transaction class
    private byte version;
    private long nonce;
    private Long validUntilBlock;
    private List<Signer> signers;
    private long systemFee;
    private long networkFee;
    private List<TransactionAttribute> attributes;
    private byte[] script;
    private List<Witness> witnesses;

    protected TransactionBuilder(Neow3j neow) {
        this.neow = neow;
    }

    // sign and getTransactionForSigning
    // private build method to check for valid construction of Transaction object
    /**
     * Creates signatures for every signer of the invocation transaction and adds them to the
     * transaction as witnesses.
     * <p>
     * For each signer set on the transaction a corresponding account with an EC key pair must exist
     * in the wallet set on the builder.
     *
     * @return this.
     */
    public Transaction sign() throws IOException {
        this.transaction = build();
        byte[] txBytes = getTransactionForSigning();
        this.transaction.getSigners().forEach(signer -> {
            if (!this.wallet.holdsAccount(signer.getScriptHash())) {
                throw new InvocationConfigurationException("Can't create transaction "
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
    //        if (validUntilBlock == null) {
//             // If validUntilBlock is not set explicitly set, then set it to the current max.
//             // It can happen that the neo-node refuses the valid until block because of
//             // it being over the max. Therefore, we decrement it by 1, to make sure that
//             // the node doesn't reject the transaction.
//             this.validUntilBlock(
//                     fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT - 1);
//         }

    private void signWithNormalAccount(byte[] txBytes, Account acc) {
        ECKeyPair keyPair = acc.getECKeyPair();
        if (keyPair == null) {
            throw new InvocationConfigurationException("Can't create transaction signature "
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
            throw new InvocationConfigurationException("Can't create transaction "
                    + "signature. Wallet does not contain enough accounts (with decrypted "
                    + "private keys) that are part of the multi-sig account with script "
                    + "hash " + signerAcc.getScriptHash() + ".");
        }
        this.transaction.addWitness(Witness.createMultiSigWitness(sigs,
                multiSigVerifScript));
    }

    // build method

    // package-private visible for testability purpose.
    Transaction build() throws IOException {
        if (this.wallet == null) {
            throw new TransactionConfigurationException("Cannot build a transaction without a wallet.");
        }

        if (validUntilBlock == null) {
            // If validUntilBlock is not set explicitly set, then set it to the current max.
            // It can happen that the neo-node refuses the valid until block because of
            // it being over the max. Therefore, we decrement it by 1, to make sure that
            // the node doesn't reject the transaction.
            this.validUntilBlock(
                    fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT - 1);
        }

        if (this.signers.isEmpty()) {
            throw new TransactionConfigurationException("No signers are specified for this "
                    + "transaction. A transaction requires at least one signer account that "
                    + "can cover the network and system fees.");
        }

        prepareTransactionSigners();
        if (script == null || script.length == 0) {
            // The builder was not configured with a script. Therefore, try to construct one
            // from the contract, function, and parameters.
            buildScript(); // TODO: 08.09.20 build and set `script`.
        }

        setSystemFee(getSystemFeeForScript());
        setNetworkFee(calcNetworkFee() + additionalNetworkFee);
        return new Transaction(neow, version, nonce, validUntilBlock, signers, systemFee,
                networkFee, attributes, script, witnesses);
    }

    // send method

    /**
     * Sends this invocation transaction to the neo-node via the `sendrawtransaction` RPC.
     *
     * @return the Neo node's response.
     * @throws IOException                      if a problem in communicating with the Neo node
     *                                          occurs.
     * @throws InvocationConfigurationException if signatures are missing for one or more signers of
     *                                          the transaction.
     */
    public NeoSendRawTransaction send() throws IOException {
        List<ScriptHash> witnesses = this.transaction.getWitnesses().stream()
                .map(Witness::getScriptHash).collect(Collectors.toList());

        for (Signer signer : this.transaction.getSigners()) {
            if (!witnesses.contains(signer.getScriptHash())) {
                throw new InvocationConfigurationException("The transaction does not have a "
                        + "signature for each of its signers.");
            }
        }
        String hex = Numeric.toHexStringNoPrefix(this.transaction.toArray());
        return neow.sendRawTransaction(hex).send();
    }

    // setters
    /**
     * Adds the given witnesses to the invocation transaction.
     * <p>
     * Use this method if you can't use the automatic signing method {@link TransactionBuilder#sign()},
     * e.g., because the configured wallet does not contain all accounts needed for signing.
     *
     * @param witnesses The witnesses to add.
     */
    public void addWitnesses(Witness... witnesses) {
        for (Witness witness : witnesses) {
            this.transaction.addWitness(witness);
        }
    }

    /**
     * Adds the given attributes to this transaction builder.
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
    public TransactionBuilder attributes(TransactionAttribute... attributes) {
        if (this.attributes.size() + attributes.length >
                NeoConstants.MAX_TRANSACTION_ATTRIBUTES) {
            throw new TransactionConfigurationException("A transaction cannot have more "
                    + "than " + NeoConstants.MAX_TRANSACTION_ATTRIBUTES + " attributes.");
        }
        this.attributes.addAll(Arrays.asList(attributes));
        return this;
    }

    /**
     * Configures the invocation such that it is valid until the given block number.
     * <p>
     * By default it is set to the maximum.
     *
     * @param blockNr The black number
     * @return this.
     */
    public TransactionBuilder validUntilBlock(long blockNr) {
        if (blockNr < 0 || blockNr >= (long) Math.pow(2, 32)) {
            throw new TransactionConfigurationException("The block number up to which this " +
                    "transaction can be included cannot be less than zero or more than 2^32.");
        }
        this.validUntilBlock = blockNr;
        return this;
    }

    /**
     * Configures the invocation with the given signers.
     * <p>
     * If no sender is specified explicitly, the first signer parameter is used as the
     * transaction sender.
     *
     * @param signers The signers.
     * @return this.
     */
    public TransactionBuilder signers(Signer... signers) {
        this.signers.addAll(Arrays.asList(signers));
        return this;
    }

    /**
     * Configures the invocation with the given nonce.
     * <p>
     * By default the nonce is set to a random value.
     *
     * @param nonce The nonce.
     * @return this.
     * @see Transaction.Builder#nonce(Long)
     */
    public TransactionBuilder nonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    /**
     * Configures the invocation with an additional network fee.
     * <p>
     * The basic network fee required to send this invocation is added automatically.
     *
     * @param fee The additional network fee in fractions of GAS.
     * @return this.
     */
    public TransactionBuilder additionalNetworkFee(long fee) {
        this.additionalNetworkFee = fee;
        return this;
    }

    /**
     * Configures the invocation to use the given wallet.
     * <p>
     * The wallet's default account is used as the transaction sender if no signers and no
     * sender is specified explicitly.
     *
     * @param wallet The wallet.
     * @return this.
     */
    public TransactionBuilder wallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    /**
     * Configures the invocation to use the given sender.
     *
     * @param sender the sender account's script hash.
     * @return this.
     */
    public TransactionBuilder sender(ScriptHash sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Configures the invocation to call the function (configured via {@link
     * TransactionBuilder.Builder#withFunction(String)}) with the provided parameters. The order of the
     * parameters is relevant.
     *
     * @param parameters The contract parameters.
     * @return this.
     */
    public TransactionBuilder parameters(ContractParameter... parameters) {
        this.contractParams.addAll(asList(parameters));
        return this;
    }

    /**
     * Configures the invocation to call the provided function on the contract configured via
     * {@link TransactionBuilder#contract(ScriptHash)}.
     *
     * @param function The contract function to call.
     * @return this.
     */
    public TransactionBuilder function(String function) {
        this.contractFunction = function;
        return this;
    }

    /**
     * Configures the invocation to call the given contract.
     *
     * @param contract The script hash of the contract to call.
     * @return this.
     */
    public TransactionBuilder contract(ScriptHash contract) {
        this.contract = contract;
        return this;
    }

    /**
     * Configures the invocation to run the given script.
     *
     * @param script The script to invoke.
     * @return this.
     */
    public TransactionBuilder script(byte[] script) {
        this.script(script);
        return this;
    }

    /**
     * Configures the invocation such that it fails (NeoVM exits with state FAULT) if the return
     * value of the invocation is "False".
     *
     * @return this
     */
    public TransactionBuilder failOnFalse() {
        this.failOnFalse = true;
        return this;
    }

    private void setNetworkFee(long networkFee) {
        this.networkFee = networkFee;
    }

    private void setSystemFee(long systemFee) {
        this.systemFee = systemFee;
    }

    // getters

    /**
     * Gets the transaction for signing it.
     *
     * @return the transaction data ready for creating a signature.
     */
    public byte[] getTransactionForSigning() {
        return this.transaction.getHashData();
    }

    /**
     * Gets the transaction.
     *
     * @return the transaction.
     */
    public Transaction getTransaction() {
        return this.transaction;
    }

    // utility methods

    /**
     * Checks if the sender account of this invocation can cover the network and system fees. If
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
     * Checks if the sender account of this invocation can cover the network and system fees. If
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

    /**
     * Makes an {@code invokescript} call to the neo-node with the invocation in its current
     * configuration. No changes are made to the blockchain state.
     * <p>
     * Make sure to add all necessary signers to the builder before making this call. They are
     * required for a successful {@code invokescript} call.
     *
     * @return the call's response.
     * @throws IOException if something goes wrong when communicating with the neo-node.
     */
    public NeoInvokeScript invokeScript() throws IOException {
        if (this.getScript() == null || this.getScript().length == 0) {
            throw new InvocationConfigurationException("Cannot make an 'invokescript' call "
                    + "without the script being configured.");
        }
        // The list of signers is required for `invokescript` calls that will hit a
        // ChecekWitness check in the smart contract. We add the signers even if that
        // is not the case because we cannot know if the invoked script needs it or not and it
        // doesn't lead to failures if we add them in any case.
        Signer[] signers = this.getSigners().toArray(new Signer[0]);
        String script = Numeric.toHexStringNoPrefix(this.getScript());
        return neow.invokeScript(script, signers).send();
    }

    /**
     * Makes an {@code invokefunction} call to the neo-node with the invocation in its current
     * configuration. No changes are made to the blockchain state.
     * <p>
     * Make sure to add all necessary signers to the builder before making this call. They are
     * required for a successful {@code invokefunction} call.
     *
     * @return the call's response.
     * @throws IOException if something goes wrong when communicating with the neo-node.
     */
    public NeoInvokeFunction invokeFunction() throws IOException {
        if (this.contract == null) {
            throw new InvocationConfigurationException("Cannot make an 'invokefunction' call "
                    + "without having configured the contract to call.");
        }
        if (this.contractFunction == null) {
            throw new InvocationConfigurationException("Cannot make an 'invokefunction' call "
                    + "without having a configured function to call.");
        }

        // The list of signers is required for `invokefunction` calls that will hit a
        // CheckWitness check in the smart contract. We add the signers even if that is not the
        // case because we cannot know if the invoked function needs it or not and it doesn't
        // lead to failures if we add them in any case.
        Signer[] signers = this.getSigners().toArray(new Signer[0]);
        if (this.contractParams.isEmpty()) {
            return neow.invokeFunction(this.contract.toString(), this.contractFunction, null,
                    signers).send();
        }
        return neow.invokeFunction(this.contract.toString(), this.contractFunction,
                this.contractParams, signers).send();
    }

    private Transaction buildTransaction() throws IOException {
        if (this.wallet == null) {
            throw new TransactionConfigurationException("Cannot build a transaction without a "
                    + "wallet.");
        }
        if (this.getValidUntilBlock() == null) {
            // If validUntilBlock is not set explicitly set, then set it to the current max.
            // It can happen that the neo-node refuses the valid until block because of
            // it being over the max. Therefore, we decrement it by 1, to make sure that
            // the node doesn't reject the transaction.
            this.validUntilBlock(
                    fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT - 1);
        }
        prepareTransactionSigners();
        if (this.getScript() == null || this.getScript().length == 0) {
            // The builder was not configured with a script. Therefore, try to construct one
            // from the contract, function, and parameters.
            this.script(buildScript());
        }
        this.systemFee(getSystemFeeForScript());
        this.networkFee(calcNetworkFee() + this.additionalNetworkFee);
        this.transaction = new Transaction(neow, version, nonce, validUntilBlock, signers, systemFee,
                networkFee, attributes, script, witnesses);
        return this.transaction;
    }

    private Long getValidUntilBlock() {
        return validUntilBlock;
    }

    // Prepares the list of signers such that there is at least one signer that will be used
    // to cover the transaction fees.
    private void prepareTransactionSigners() {
        if (this.sender != null) {
            if (hasSignerWithFeeOnlyScope()) {
                throw new InvocationConfigurationException("The list of transaction signers "
                        + "contained a signer with the fee-only scope and at the same time a "
                        + "sender was set. Either a fee-only signer, or a sender is allowed, "
                        + "but not both at the same time.");
            }
            Optional<Signer> senderInSignersOpt = this.getSigners().stream()
                    .filter(s -> s.getScriptHash().equals(this.sender))
                    .findFirst();
            if (senderInSignersOpt.isPresent()) {
                // If the sender is already in the signers list move it to the first position in
                // the signer list, but don't change the scope (can be more than feeOnly).
                this.getSigners().remove(senderInSignersOpt.get());
                this.getSigners().add(0, senderInSignersOpt.get());
            } else {
                // Add the sender to the first position in the signer list.
                this.getSigners().add(0, Signer.feeOnly(this.sender));
            }
        } else if (this.getSigners().isEmpty()) {
            // If no signer or sender is present, add the default account as a signer with
            // the feeOnly witness scope. It will be used to cover the fees.
            this.signers(Signer.feeOnly(
                    this.wallet.getDefaultAccount().getScriptHash()));
        }
    }

    // Checks if there is a signer (excluding the sender) with a feeOnly scope.
    private boolean hasSignerWithFeeOnlyScope() {
        return this.getSigners().stream()
                .anyMatch(s -> s.getScopes().contains(WitnessScope.FEE_ONLY)
                        && !s.getScriptHash().equals(this.sender));
    }

    // Tries to build a script from the properties contract, function and parameters
    // configured on this builder.
    private byte[] buildScript() {
        if (this.contract == null) {
            throw new InvocationConfigurationException("The invocation doesn't have a script to"
                    + " invoke and can't generate one from because the contract to invoke was "
                    + "not set.");
        }
        if (this.contractFunction == null) {
            throw new InvocationConfigurationException("The invocation is configured to call a "
                    + "contract but is missing a specific function to call.");
        }
        ScriptBuilder b = new ScriptBuilder()
                .contractCall(this.contract, this.contractFunction, this.contractParams);
        if (this.failOnFalse) {
            b.opCode(OpCode.ASSERT);
        }
        return b.toArray();
    }

    private long fetchCurrentBlockNr() throws IOException {
        return neow.getBlockCount().send().getBlockIndex().longValue();
    }

    /*
     * Fetches the GAS consumed by this invocation. It does this by making an RPC call to the
     * Neo node. The returned GAS amount is in fractions of GAS (10^-8).
     */
    private long getSystemFeeForScript() throws IOException {
        // The signers are required for `invokescript` calls that will hit a CheckWitness
        // check in the smart contract.
        Signer[] signers = this.getSigners().toArray(new Signer[0]);
        String script = Numeric.toHexStringNoPrefix(this.getScript());
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
        List<Account> cosigAccs = getSignerAccounts();

        // Base transaction size
        int size = Transaction.HEADER_SIZE // constant header size
                + IOUtils.getVarSize(this.getSigners())
                + IOUtils.getVarSize(this.getAttributes()) // attributes
                + IOUtils.getVarSize(this.getScript()) // script
                + IOUtils.getVarSize(cosigAccs.size()); // varInt for all necessary witnesses

        // Calculate fee for witness verification and collect size of witnesses.
        int execFee = 0;
        for (Account acc : cosigAccs) {
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
        List<Account> accounts = new ArrayList<>();
        getSigners().forEach(signer -> {
            if (this.wallet.holdsAccount(signer.getScriptHash())) {
                accounts.add(this.wallet.getAccount(signer.getScriptHash()));
            } else {
                throw new InvocationConfigurationException("Wallet does not contain the "
                        + "account for signer with script hash " + signer.getScriptHash());
            }
        });
        return accounts;
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
}
