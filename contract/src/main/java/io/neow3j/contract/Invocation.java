package io.neow3j.contract;

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
import io.neow3j.transaction.Cosigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttribute;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.transaction.Witness;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Invocation {

    protected Neow3j neow;
    protected Wallet wallet;
    protected Transaction transaction;

    protected Invocation(Builder builder) {
        this.neow = builder.neow;
        this.wallet = builder.wallet;
        this.transaction = builder.transaction;
    }

    /**
     * Sends this invocation transaction to the neo-node via the `sendrawtransaction` RPC.
     *
     * @return the Neo node's response.
     * @throws IOException                      if a problem in communicating with the Neo node
     *                                          occurs.
     * @throws InvocationConfigurationException if signatures are missing for one or more cosigners
     *                                          of the transaction.
     */
    public NeoSendRawTransaction send() throws IOException {
        Stream<Witness> witnesses = this.transaction.getWitnesses().stream();
        for (Cosigner cosigner : this.transaction.getCosigners()) {
            if (witnesses.noneMatch(w -> w.getScriptHash().equals(cosigner.getScriptHash()))) {
                throw new InvocationConfigurationException("The transaction does not have a "
                        + "signature for each of its cosigners.");
            }
        }
        String hex = Numeric.toHexStringNoPrefix(this.transaction.toArray());
        return neow.sendRawTransaction(hex).send();
    }

    /**
     * Creates signatures for every cosigner of the invocation transaction and adds them to the
     * transaction as witnesses.
     * <p>
     * For each cosigner set on the transaction a corresponding account with an EC key pair must
     * exist in the wallet set on the builder.
     *
     * @return this.
     */
    public Invocation sign() {
        byte[] txBytes = getTransactionForSigning();
        for (Cosigner c : this.transaction.getCosigners()) {
            Account cosignerAcc = this.wallet.getAccount(c.getScriptHash());
            if (cosignerAcc == null) {
                throw new InvocationConfigurationException("Can't create transaction "
                        + "signature. Wallet does not contain the cosigner account with script "
                        + "hash " + c.getScriptHash());
            }
            if (cosignerAcc.isMultiSig()) {
                signWithMultiSigAccount(txBytes, cosignerAcc);
            } else {
                signWithNormalAccount(txBytes, cosignerAcc);
            }
        }
        return this;
    }

    private void signWithNormalAccount(byte[] txBytes, Account acc) {
        ECKeyPair keyPair = acc.getECKeyPair();
        if (keyPair == null) {
            throw new InvocationConfigurationException("Can't create transaction signature "
                    + "because account with script " + acc.getScriptHash() + " doesn't hold a "
                    + "private key.");
        }
        this.transaction.addWitness(Witness.createWitness(txBytes, keyPair));
    }

    private void signWithMultiSigAccount(byte[] txBytes, Account cosignerAcc) {
        List<SignatureData> sigs = new ArrayList<>();
        VerificationScript multiSigVerifScript = cosignerAcc.getVerificationScript();
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
                    + "hash " + cosignerAcc.getScriptHash() + ".");
        }
        this.transaction.addWitness(Witness.createMultiSigWitness(sigs,
                multiSigVerifScript));
    }

    /**
     * Gets the invocation transaction for signing it.
     *
     * @return the transaction data ready for creating a signature.
     */
    public byte[] getTransactionForSigning() {
        return this.transaction.getHashData();
    }

    /**
     * Gets the invocation transaction.
     *
     * @return the transaction.
     */
    public Transaction getTransaction() {
        return this.transaction;
    }

    /**
     * Adds the given witnesses to the invocation transaction.
     * <p>
     * Use this method if you can't use the automatic signing method {@link Invocation#sign()},
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
     * Checks if the sender account of this invocation can cover the network and system fees. If
     * not, executes the given consumer supplying it with the required fee and the sender's GAS
     * balance.
     *
     * @return this.
     * @throws IOException if something goes wrong in the communication with the neo-node.
     */
    public Invocation doIfSenderCannotCoverFees(BiConsumer<BigInteger, BigInteger> consumer)
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
    public <T extends Throwable> Invocation throwIfSenderCannotCoverFees(
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

    public static class Builder {

        private long additionalNetworkFee;
        protected Neow3j neow;
        protected Wallet wallet;
        private String contractFunction;
        private List<ContractParameter> contractParams;
        private ScriptHash contract;
        protected Transaction.Builder txBuilder;
        protected Transaction transaction;
        protected boolean failOnFalse;

        // TODO: Add javadoc.
        protected Builder(Neow3j neow) {
            if (neow == null) {
                throw new IllegalArgumentException("Neow3j instance must not be null.");
            }
            this.txBuilder = new Transaction.Builder();
            this.contractParams = new ArrayList<>();
            this.neow = neow;
        }

        /**
         * Configures the invocation with the given attributes. (Optional)
         *
         * @param attributes The attributes.
         * @return this.
         */
        public Builder withAttributes(TransactionAttribute... attributes) {
            this.txBuilder.attributes(attributes);
            return this;
        }

        /**
         * Configures the invocation such that it is valid until the given block number. (Optional)
         * <p>
         * By default it is set to the maximum.
         *
         * @param blockNr The black number
         * @return this.
         * @see Transaction.Builder#validUntilBlock(long)
         */
        public Builder withValidUntilBlock(long blockNr) {
            this.txBuilder.validUntilBlock(blockNr);
            return this;
        }

        /**
         * Configures the invocation with the given nonce. (Optional)
         * <p>
         * By default the nonce is set to a random value.
         *
         * @param nonce The nonce.
         * @return this.
         * @see Transaction.Builder#nonce(Long)
         */
        public Builder withNonce(long nonce) {
            this.txBuilder.nonce(nonce);
            return this;
        }

        /**
         * Configures the invocation with an additional network fee. (Optional)
         * <p>
         * The basic network fee required to send this invocation is added automatically.
         *
         * @param fee The additional network fee.
         * @return this.
         */
        public Builder withAdditionalNetworkFee(long fee) {
            this.additionalNetworkFee = fee;
            return this;
        }

        /**
         * Configures the invocation to use the given wallet. (Mandatory)
         * <p>
         * The wallet's default account is used as the transaction sender if no sender is specified
         * explicitly.
         *
         * @param wallet The wallet.
         * @return this.
         */
        public Builder withWallet(Wallet wallet) {
            this.wallet = wallet;
            return this;
        }

        /**
         * Configures the invocation to use the given sender. (Optional)
         *
         * @param sender the sender account's script hash.
         * @return this.
         */
        public Builder withSender(ScriptHash sender) {
            txBuilder.sender(sender);
            return this;
        }

        /**
         * Configures the invocation to call the function (configured via {@link
         * Invocation.Builder#withFunction(String)}) with the provided parameters. The order of the
         * parameters is relevant.
         *
         * @param parameters The contract parameters.
         * @return this.
         */
        public Builder withParameters(ContractParameter... parameters) {
            this.contractParams.addAll(Arrays.asList(parameters));
            return this;
        }

        /**
         * Configures the invocation to call the provided function on the contract configured via
         * {@link Invocation.Builder#withContract(ScriptHash)}.
         *
         * @param function The contract function to call.
         * @return this.
         */
        public Builder withFunction(String function) {
            this.contractFunction = function;
            return this;
        }

        /**
         * Configures the invocation to call the given contract.
         *
         * @param contract The script hash of the contract to call.
         * @return this.
         */
        public Builder withContract(ScriptHash contract) {
            this.contract = contract;
            return this;
        }

        /**
         * Configures the invocation to run the given script.
         *
         * @param script The script to invoke.
         * @return this.
         */
        public Builder withScript(byte[] script) {
            this.txBuilder.script(script);
            return this;
        }

        /**
         * Configures the invocation such that it fails (NeoVM exits with state FAULT) if the return
         * value of the invocation is "False". (Optional)
         *
         * @return this
         */
        public Builder failOnFalse() {
            this.failOnFalse = true;
            return this;
        }

        /**
         * Makes an <tt>invokescript</tt> call to the neo-node with the invocation in its current
         * configuration. No changes are made to the blockchain state.
         * <p>
         * Make sure to add all necessary cosigners to the builder before making this call. They are
         * required for a successful <tt>invokescript</tt> call.
         *
         * @return the call's response.
         * @throws IOException if something goes wrong when communicating with the neo-node.
         */
        public NeoInvokeScript invokeScript() throws IOException {
            if (this.txBuilder.getScript() == null || this.txBuilder.getScript().length == 0) {
                throw new InvocationConfigurationException("Cannot make an 'invokescript' call "
                        + "without the script being configured.");
            }
            // The list of signers is required for `invokescript` calls that will hit a
            // ChecekWitness check in the smart contract. We add the signers even if that
            // is not the case because we cannot know if the invoked script needs it or not and it
            // doesn't lead to failures if we add them in any case.
            String[] signers = getSigners().toArray(new String[]{});
            String script = Numeric.toHexStringNoPrefix(this.txBuilder.getScript());
            return neow.invokeScript(script, signers).send();
        }

        /**
         * Makes an <tt>invokefunction</tt> call to the neo-node with the invocation in its current
         * configuration. No changes are made to the blockchain state.
         * <p>
         * Make sure to add all necessary cosigners to the builder before making this call. They are
         * required for a successful <tt>invokefunction</tt> call.
         *
         * @return the call's response.
         * @throws IOException if something goes wrong when communicating with the neo-node.
         */
        public NeoInvokeFunction invokeFunction() throws IOException {
            if (this.contract == null) {
                throw new InvocationConfigurationException("Cannot make an 'invokefunction' call "
                        + "without the contract to call being configured.");
            }
            if (this.contractFunction == null) {
                throw new InvocationConfigurationException("Cannot make an 'invokefunction' call "
                        + "without the function to call being configured.");
            }

            // The list of signers is required for `invokefunction` calls that will hit a
            // ChecekWitness check in the smart contract. We add the signers even if that is not the
            // case because we cannot know if the invoked function needs it or not and it doesn't
            // lead to failures if we add them in any case.
            String[] signers = getSigners().toArray(new String[]{});
            if (this.contractParams.isEmpty()) {
                return neow.invokeFunction(this.contract.toString(), this.contractFunction, null,
                        signers).send();
            }
            return neow.invokeFunction(this.contract.toString(), this.contractFunction,
                    this.contractParams, signers).send();
        }

        private Set<String> getSigners() {
            Set<String> signerSet = this.txBuilder.getCosigners().stream()
                    .map(c -> c.getScriptHash().toString())
                    .collect(Collectors.toSet());
            if (this.txBuilder.getSender() != null) {
                // If the sender account is not in the cosigners then add it here.
                signerSet.add(this.txBuilder.getSender().toString());
            } else if (wallet != null) {
                // If the sender is not set, then take the default account form the wallet
                signerSet.add(this.wallet.getDefaultAccount().getScriptHash().toString());
            }
            return signerSet;
        }

        /**
         * Builds the invocation, enforces correct configuration, fetches the system fee and
         * calculates the network fee.
         *
         * @return the <tt>Invocation</tt> ready for signing and sending.
         * @throws IOException if something goes wrong when communicating with the neo-node.
         */
        public Invocation build() throws IOException {
            if (this.wallet == null) {
                throw new InvocationConfigurationException("Cannot build a transaction without a "
                        + "wallet.");
            }
            if (this.txBuilder.getValidUntilBlock() == null) {
                // If validUntilBlock is not set explicitly set, then set it to the current max.
                // It can happen that the neo-node refuses the valid until block because of
                // it being over the max. Therefore, we decrement it by 1, to make sure that
                // the node doesn't reject the transaction.
                this.txBuilder.validUntilBlock(
                        fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT - 1);
            }
            if (this.txBuilder.getSender() == null) {
                // If sender is not set explicitly set it to the default account of the wallet.
                this.txBuilder.sender(this.wallet.getDefaultAccount().getScriptHash());
            }
            if (this.txBuilder.getCosigners().isEmpty() || !senderCosignerExists()) {
                // Set the standard cosigner if none has been specified.
                this.txBuilder.attributes(Cosigner.calledByEntry(this.txBuilder.getSender()));
            }
            if (this.txBuilder.getScript() == null || this.txBuilder.getScript().length == 0) {
                // The builder was not configured with a script. Therefore, try to construct one
                // from the contract, function, and parameters.
                this.txBuilder.script(buildScript());
            }
            this.txBuilder.systemFee(getSystemFeeForScript());
            this.txBuilder.networkFee(calcNetworkFee() + this.additionalNetworkFee);
            this.transaction = this.txBuilder.build();
            return new Invocation(this);
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

        private boolean senderCosignerExists() {
            return this.txBuilder.getCosigners().stream()
                    .anyMatch(c -> c.getScriptHash().equals(this.txBuilder.getSender()));
        }

        private long fetchCurrentBlockNr() throws IOException {
            return neow.getBlockCount().send().getBlockIndex().longValue();
        }

        /*
         * Fetches the GAS consumed by this invocation. It does this by making an RPC call to the
         * Neo node. The returned GAS amount is in fractions of GAS (10^-8).
         */
        private long getSystemFeeForScript() throws IOException {
            // The signers are required for `invokescript` calls that will hit a ChecekWitness
            // check in the smart contract.
            String[] signers = this.txBuilder.getCosigners().stream()
                    .map(c -> c.getScriptHash().toString()).toArray(String[]::new);
            String script = Numeric.toHexStringNoPrefix(this.txBuilder.getScript());
            NeoInvokeScript response = neow.invokeScript(script, signers).send();
            // The GAS amount is returned in fractions (10^8)
            return Long.parseLong(response.getInvocationResult().getGasConsumed());
        }

        /*
         * Calculates the necessary network fee for the transaction being build in this builder.
         * The fee consists of the cost per transaction byte and the cost for signature
         * verification. Since the transaction is not signed yet, the calculation works with
         * expected signatures. This information is derived from the verification scripts of all
         * cosigners added to the transaction.
         */
        private long calcNetworkFee() {
            List<Account> cosigAccs = getCosignerAccounts();

            // Base transaction size
            int size = Transaction.HEADER_SIZE // constant header size
                    + IOUtils.getVarSize(this.txBuilder.getAttributes()) // attributes
                    + IOUtils.getVarSize(this.txBuilder.getScript()) // script
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

        private List<Account> getCosignerAccounts() {
            List<Account> accounts = new ArrayList<>();
            for (Cosigner cosigner : txBuilder.getCosigners()) {
                Account account = this.wallet.getAccount(cosigner.getScriptHash());
                if (account == null) {
                    throw new InvocationConfigurationException("Wallet does not contain the "
                            + "account for cosigner with script hash " + cosigner.getScriptHash());
                }
                accounts.add(account);
            }
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
                    + InteropServiceCode.NEO_CRYPTO_ECDSA_SECP256R1_VERIFY.getPrice();
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
                    + OpCode.valueOf(new ScriptBuilder().pushInteger(m).toArray()[0]).getPrice()
                    + OpCode.PUSHDATA1.getPrice() * n
                    + OpCode.valueOf(new ScriptBuilder().pushInteger(n).toArray()[0]).getPrice()
                    // Push null because we don't want to verify a particular message but the
                    // transaction itself.
                    + OpCode.PUSHNULL.getPrice()
                    + InteropServiceCode.NEO_CRYPTO_ECDSA_SECP256R1_CHECKMULTISIG.getPrice(n);
        }
    }
}
