package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.io.IOUtils;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.transaction.Cosigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttribute;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.transaction.Witness;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.AccountStateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Invocation {

    private Transaction transaction;
    private Wallet wallet;
    private Neow3j neow;

    protected Invocation(InvocationBuilder builder) {
        this.neow = builder.neow;
        this.wallet = builder.wallet;
        this.transaction = builder.tx;
    }

    /**
     * Sends this invocation transaction to the Neo node via the `sendrawtransaction` RPC.
     * <p>
     * Before sending, make sure to sign the transaction by calling {@link Invocation#sign()} or by
     * adding signatures manually with {@link Invocation#addWitnesses(Witness...)}.
     *
     * @return the Neo node's response.
     * @throws IOException if a problem in communicating with the Neo node occurs.
     */
    public NeoSendRawTransaction send() throws IOException {
        // TODO 14.05.20 claude: Consider checking for sufficient witnesses.
        String hex = Numeric.toHexStringNoPrefix(transaction.toArray());
        NeoSendRawTransaction response = neow.sendRawTransaction(hex).send();
        return response;
    }

    /**
     * Creates signatures for every cosigner of the invocation transaction and adds them to the
     * transaction as witnesses.
     * <p>
     * For each cosigner set on the transaction corresponding account must exist in the wallet.
     *
     * @return this.
     */
    public Invocation sign() {
        byte[] txBytes = getTransactionForSigning();
        for (Cosigner c : this.transaction.getCosigners()) {
            Account a = this.wallet.getAccount(c.getAccount());
            if (a == null) {
                throw new InvocationConfigurationException("Wallet does not contain the account "
                        + "for cosigner with script hash " + c.getAccount());
            }
            if (a.isMultiSig()) {
                // TODO 13.05.20 claude: Add the possiblity to automatically sign with a mutli-sig
                //  account. For this to work the participating accounts need to be in the wallet.
                throw new InvocationConfigurationException("Automatic signing with a "
                        + "multi-signature account is not supported. Create and add a signature "
                        + "manually. ");
            }
            try {
                ECKeyPair keyPair = a.getECKeyPair();
                this.transaction.addWitness(Witness.createWitness(txBytes, keyPair));
            } catch (AccountStateException e) {
                throw new InvocationConfigurationException("Cannot sign transaction with account "
                        + "with script hash " + c.getAccount(), e);
            }
        }
        return this;
    }

    /**
     * Gets the invocation transaction bytes for signing. No witnesses are included in the returned
     * byte array.
     *
     * @return the transaction as a byte array
     */
    public byte[] getTransactionForSigning() {
        return this.transaction.toArrayWithoutWitnesses();
    }

    /**
     * Gets the invocation transaction.
     */
    public Transaction getTransaction() {
        return this.transaction;
    }

    /**
     * Adds the given witnesses to the invocation transaction.
     * <p>
     * Use this method if you can't use the automatic signing method {@link Invocation#sign()},
     * e.g., because one of the cosigners is a multi-signature account.
     *
     * @param witnesses The witnesses to add.
     */
    public void addWitnesses(Witness... witnesses) {
        for (Witness witness : witnesses) {
            this.transaction.addWitness(witness);
        }
    }

    public static class InvocationBuilder {

        private String function;
        private List<ContractParameter> parameters;
        private long additionalNetworkFee;
        private ScriptHash scriptHash;
        private Neow3j neow;
        private Wallet wallet;
        private Transaction.Builder txBuilder;
        private Transaction tx;
        private boolean failOnFalse;

        // Should only be called by the SmartContract class. Therefore no checks on the arguments.
        protected InvocationBuilder(Neow3j neow, ScriptHash scriptHash, String function) {
            this.neow = neow;
            this.scriptHash = scriptHash;
            this.function = function;
            this.parameters = new ArrayList<>();
            this.txBuilder = new Transaction.Builder();
            this.failOnFalse = false;
        }

        /**
         * @param parameters
         * @return
         */
        public InvocationBuilder withParameters(ContractParameter... parameters) {
            this.parameters.addAll(Arrays.asList(parameters));
            return this;
        }

        /**
         * @param attributes
         * @return
         */
        public InvocationBuilder withAttributes(TransactionAttribute... attributes) {
            this.txBuilder.attributes(attributes);
            return this;
        }

        /**
         * @param blockNr
         * @return
         */
        public InvocationBuilder validUntilBlock(long blockNr) {
            this.txBuilder.validUntilBlock(blockNr);
            return this;
        }

        /**
         * @param nonce
         * @return
         */
        public InvocationBuilder withNonce(long nonce) {
            this.txBuilder.nonce(nonce);
            return this;
        }

        /**
         * @param fee
         * @return
         */
        public InvocationBuilder withAdditionalNetworkFee(long fee) {
            this.additionalNetworkFee = fee;
            return this;
        }

        public InvocationBuilder withWallet(Wallet wallet) {
            this.wallet = wallet;
            return this;
        }

        /**
         * @param sender
         * @return
         */
        public InvocationBuilder withSender(ScriptHash sender) {
            txBuilder.sender(sender);
            return this;
        }

        /**
         * Sets the invocation up so that it fails (NeoVM exits with state FAULT) if the return
         * value of the invocation is "False".
         *
         * @return this
         */
        public InvocationBuilder failOnFalse() {
            this.failOnFalse = true;
            return this;
        }

        /**
         * @return
         * @throws IOException
         */
        public NeoInvokeFunction call() throws IOException {
            // This list is required for `invokescript` calls that will hit ChecekWitness checks
            // in the smart contract.
            String[] signers = getSigners();
            if (this.parameters.isEmpty()) {
                return neow.invokeFunction(scriptHash.toString(), this.function, null, signers)
                        .send();
            }
            return neow.invokeFunction(scriptHash.toString(), this.function, this.parameters,
                    signers).send();
        }

        /*
         * Get scripthashes of all cosigners. If cosigners have not yet been set explicitely
         * this method adds the sender scripthash to the set.
         */
        private String[] getSigners() {
            Set<String> signersSet = this.txBuilder.getCosigners().stream()
                    .map(c -> c.getAccount().toString())
                    .collect(Collectors.toSet());
            if (this.txBuilder.getSender() != null) {
                signersSet.add(this.txBuilder.getSender().toString());
            }
            return signersSet.toArray(new String[]{});
        }

        public Invocation build() throws IOException {
            if (this.wallet == null) {
                throw new InvocationConfigurationException("Cannot create an invocation without a "
                        + "wallet.");
            }
            if (this.txBuilder.getValidUntilBlock() == null) {
                // If validUntilBlock is not set explicitly set it to the current max.
                this.txBuilder.validUntilBlock(
                        fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT);
            }
            if (this.txBuilder.getSender() == null) {
                // If sender is not set explicitly set it to the default account of the wallet.
                this.txBuilder.sender(this.wallet.getDefaultAccount().getScriptHash());
            }
            if (this.txBuilder.getCosigners().isEmpty() || !senderCosignerExists()) {
                // Set the standard cosigner if none has been specified.
                this.txBuilder.attributes(Cosigner.calledByEntry(this.txBuilder.getSender()));
            }
            this.txBuilder.script(createScript());
            this.txBuilder.systemFee(fetchSystemFee());
            // TODO: Maybe check if the sender has enough coin to do the invocation.
            this.txBuilder.networkFee(calcNetworkFee() + this.additionalNetworkFee);

            this.tx = this.txBuilder.build();
            return new Invocation(this);
        }

        private boolean senderCosignerExists() {
            return this.txBuilder.getCosigners().stream()
                    .anyMatch(c -> c.getAccount().equals(this.txBuilder.getSender()));
        }

        private long fetchCurrentBlockNr() throws IOException {
            return neow.getBlockCount().send().getBlockIndex().longValue();
        }

        private byte[] createScript() {
            ScriptBuilder b = new ScriptBuilder()
                    .contractCall(this.scriptHash, this.function, this.parameters);
            if (failOnFalse) {
                b.opCode(OpCode.ASSERT);
            }
            return b.toArray();
        }

        /*
         * Fetches the GAS consumed by this invocation. It does this by making an RPC call to the
         * Neo node. The returned GAS amount is in fractions of GAS (10^-8).
         */
        private long fetchSystemFee() throws IOException {
            // The signers are required for `invokescript` calls that will hit ChecekWitness checks
            // in the smart contract.
            String[] signers = getSigners();
            NeoInvokeFunction response;
            if (this.parameters.isEmpty()) {
                response = neow.invokeFunction(scriptHash.toString(), this.function, null, signers)
                        .send();
            } else {
                response = neow.invokeFunction(scriptHash.toString(), this.function,
                        this.parameters, signers).send();
            }
            // The GAS amount is returned in fractions (10^8)
            long systemFee = Long.parseLong(response.getInvocationResult().getGasConsumed());
            if (this.failOnFalse) {
                // The `invokefunction` call does not add the ASSERT OpCode at the end of the
                // script. Therefore, the fetched GAS systemfee needs to be adjusted.
                systemFee += OpCode.ASSERT.getPrice();
            }
            return systemFee;
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
                Account account = this.wallet.getAccount(cosigner.getAccount());
                if (account == null) {
                    throw new InvocationConfigurationException("Wallet does not contain the "
                            + "account for cosigner with script hash " + cosigner.getAccount());
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
