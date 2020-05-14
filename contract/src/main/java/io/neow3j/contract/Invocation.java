package io.neow3j.contract;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.IOUtils;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
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
     * Before sending, make sure to sign the transaction by calling {@link Invocation#sign()} or
     * by adding signatures manually with {@link Invocation#addWitnesses(Witness...)}.
     *
     * @return the Neo node's response.
     * @throws IOException if a problem in communicating with the Neo node occurs.
     */
    public NeoSendRawTransaction send() throws IOException {
        // TODO 14.05.20 claude: Consider checking for sufficient witnesses.
        String hex = Numeric.toHexStringNoPrefix(transaction.toArray());
        NeoSendRawTransaction response = neow.sendRawTransaction(hex).send();
        response.throwOnError();
        return "";
        // TODO: Adapt as soon as JSON-RPC implementation is adapted to Neo 3.
        // The new return type of the `sendrawtransaction` RPC call in Neo 3 is the transaction
        // hash.
        // return response.getResult();
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
            ECKeyPair kp;
            try {
                kp = a.getECKeyPair();
            } catch (AccountStateException e) {
                throw new InvocationConfigurationException("Cannot sign transaction with account "
                        + "with script hash " + c.getAccount(), e);
            }
            this.transaction.addWitness(Witness.createWitness(txBytes, kp));
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

    public void addSignatures(List<SignatureData> signatures) {
        Account sendingAcc = this.wallet.getAccount(this.transaction.getSender());
        VerificationScript verScript = sendingAcc.getVerificationScript();
        if (signatures.size() != verScript.getSigningThreshold()) {
            throw new InvocationConfigurationException("The number of signatures must be equal to "
                    + "the signing threshold of the multi-sig account performing his invocation. "
                    + "The network fee for the invocation was set according to that signing "
                    + "threshold.");
        }
        Witness wit = Witness.createMultiSigWitness(signatures, verScript);
        this.transaction.addWitness(wit);
    }

    public static class InvocationBuilder {

        private String function;
        private List<ContractParameter> parameters;
        private long additionalNetworkFee;
        private ScriptHash scriptHash;
        private Neow3j neow;
        private Wallet wallet;
        private ScriptHash sender;
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
         * @param witnesses
         * @return
         */
        public InvocationBuilder addWitnesses(Witness... witnesses) {
            this.txBuilder.witnesses(witnesses);
            return this;
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
         * @param cosigners
         * @return
         */
        public InvocationBuilder withCosigners(Cosigner... cosigners) {
            this.txBuilder.cosigners(cosigners);
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
        public NeoInvokeFunction run() throws IOException {
            if (this.parameters.isEmpty()) {
                return neow.invokeFunction(scriptHash.toString(), this.function).send();
            }
            return neow.invokeFunction(scriptHash.toString(), this.function, this.parameters)
                    .send();
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
                this.txBuilder.cosigners(Cosigner.calledByEntry(this.txBuilder.getSender()));
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
            NeoInvokeFunction response;
            if (this.parameters.isEmpty()) {
                response = neow.invokeFunction(scriptHash.toString(), this.function).send();
            } else {
                response = neow.invokeFunction(scriptHash.toString(), this.function,
                        this.parameters).send();
            }
            // The GAS amount is returned in fractions (10^8) by the preview private network node
            // for Neo 3. But in Neo 2 the amount was given as decimals of whole GAS tokens (e.g.
            // 0.12).
            // TODO: Remove above comment once it is clear which format Neo 3 returns here.
//            String gasConsumed = response.getInvocationResult().getGasConsumed();
//            BigDecimal fee = new BigDecimal(gasConsumed).multiply(GasToken.FACTOR);
//            return fee.longValue();
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
                    + IOUtils.getVarSize(this.txBuilder.getCosigners()) // cosigners
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
                    + InteropServiceCode.NEO_CRYPTO_ECDSAVERIFY.getPrice();
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
                    + InteropServiceCode.NEO_CRYPTO_ECDSACHECKMULTISIG.getPrice(n);
        }

    }
}
