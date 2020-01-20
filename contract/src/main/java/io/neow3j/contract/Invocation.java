package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.IOUtils;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.Cosigner;
import io.neow3j.transaction.InvocationScript;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttribute;
import io.neow3j.transaction.VerificationScript;
import io.neow3j.transaction.Witness;
import io.neow3j.utils.FeeUtils;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Invocation {

    private Transaction transaction;
    private Account account;
    private Neow3j neow;

    protected Invocation(InvocationBuilder builder) {
        this.neow = builder.neow;
        this.account = builder.account;
        this.transaction = builder.txBuilder.build();
    }

    public Invocation send() throws IOException, ErrorResponseException {
        NeoSendRawTransaction response =
            neow.sendRawTransaction(Numeric.toHexString(transaction.toArray())).send();
        response.throwOnError();
        // At this point we don't care if the invocation finished in a successful VM state. An
        // exception is only thrown if the node responds with an error.
        return this;
    }

    /**
     * Signs the transaction and add the signature to the transaction as a witness.
     * <p>
     * The signature is created with the account set on the transaction.
     *
     * @return this invocation object.
     */
    public Invocation sign() throws Exception {
        Witness wit = Witness.createWitness(this.transaction.toArrayWithoutWitnesses(),
            this.account.getECKeyPair());
        this.transaction.addWitness(wit);
        return this;
    }

    /**
     * Gets the transaction bytes for signing. The witnesses are not included in the array.
     *
     * @return the transaction as a byte array
     */
    public byte[] getTransactionForSigning() {
        return this.transaction.toArrayWithoutWitnesses();
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void addSignatures(List<SignatureData> signatures) {
        VerificationScript verScript = this.account.getVerificationScript();
        if (signatures.size() != verScript.getSigningThreshold()) {
            throw new InvocationConfigurationException("The number of signatures must be equal to "
                + "the signing threshold of the multi-sig account performing his invocation. The "
                + "network fee for the invocation was set according to that signing threshold.");
        }
        Witness wit = Witness.createMultiSigWitness(signatures, verScript);
        this.transaction.addWitness(wit);
    }

    public static class InvocationBuilder {

        private String function;
        private List<ContractParameter> parameters;
        private List<Witness> witnesses;
        private List<TransactionAttribute> attributes;
        private List<Cosigner> cosigners;
        private long additionalNetworkFee;
        private boolean isValidUntilBlockSet;
        private ScriptHash scriptHash;
        private Neow3j neow;
        private Account account;
        private byte[] script;
        private Transaction.Builder txBuilder;

        // Should only be called by the SmartContract class. Therefore no checks on the arguments.
        protected InvocationBuilder(Neow3j neow, ScriptHash scriptHash, String function) {
            this.neow = neow;
            this.scriptHash = scriptHash;
            this.function = function;
            this.witnesses = new ArrayList<>();
            this.cosigners = new ArrayList<>();
            this.attributes = new ArrayList<>();
            this.parameters = new ArrayList<>();
            this.txBuilder = new Transaction.Builder();
            this.script = new byte[]{};
            this.isValidUntilBlockSet = false;
        }

        /**
         * @param witnesses
         * @return
         */
        public InvocationBuilder addWitnesses(Witness... witnesses) {
            // Add to transaction builder but also to local variable because it is needed later
            // and the transaction builder does not have getters.
            this.txBuilder.witnesses(witnesses);
            this.witnesses.addAll(Arrays.asList(witnesses));
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
            // Add to transaction builder but also to local variable because it is needed later
            // and the transaction builder does not have getters.
            this.txBuilder.attributes(attributes);
            this.attributes.addAll(Arrays.asList(attributes));
            return this;
        }

        /**
         * @param cosigners
         * @return
         */
        public InvocationBuilder withCosigners(Cosigner... cosigners) {
            // Add to transaction builder but also to local variable because it is needed later
            // and the transaction builder does not have getters.
            this.txBuilder.cosigners(cosigners);
            this.cosigners.addAll(Arrays.asList(cosigners));
            return this;
        }

        /**
         * @param blockNr
         * @return
         */
        public InvocationBuilder validUntilBlock(long blockNr) {
            this.txBuilder.validUntilBlock(blockNr);
            this.isValidUntilBlockSet = true;
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

        /**
         * @param account
         * @return
         */
        public InvocationBuilder withAccount(Account account) {
            // Add to transaction builder but also to local variable because it is needed later
            // and the transaction builder does not have getters.
            this.txBuilder.sender(account.getScriptHash());
            this.account = account;
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
            if (this.account == null) {
                throw new InvocationConfigurationException("Cannot create an invocation without a "
                    + "sending account.");
            }
            if (!this.isValidUntilBlockSet) {
                this.txBuilder.validUntilBlock(
                    fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT);
            }

            this.script = createScript();
            this.txBuilder.script(this.script);
            this.txBuilder.systemFee(fetchSystemFee());
            // Before we can calculate the network fee the transaction must be completely setup.
            // Therefore, at least a standard cosigner must be set in case no cosigner has been
            // set specifically.
            if (this.cosigners.isEmpty()) {
                this.cosigners.add(Cosigner.calledByEntry(this.account.getScriptHash()));
            }
            this.txBuilder.networkFee(calcNetworkFee() + this.additionalNetworkFee);

            return new Invocation(this);
        }

        private long fetchCurrentBlockNr() throws IOException {
            return neow.getBlockCount().send().getBlockIndex().longValue();
        }

        private byte[] createScript() {
            return new ScriptBuilder()
                .contractCall(this.scriptHash, this.function, this.parameters)
                .toArray();
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
            // The GAS amount is received in decimal form (not Fixed8) so we can directly convert
            // to BigDecimal. We explicitly do not check if the VM exit state is FAULT. The GAS
            // consumption is determined independent of the invocations outcome.
            String gasConsumed = response.getInvocationResult().getGasConsumed();
            BigDecimal fee = new BigDecimal(gasConsumed).multiply(GasToken.FACTOR);
            return fee.longValue();
        }

        /*
         * Calculates the necessary network fee for the transaction being build in this builder.
         * The fee consists of the cost per transaction byte and the cost for signature
         * verification. Since the builder's transaction is not signed yet, the expected
         * signature is derived from the verification script of the set account.
         */
        private long calcNetworkFee() {
            long sigVerificationFee;
            if (this.account.isMultiSig()) {
                VerificationScript verScript = this.account.getVerificationScript();
                int threshold = verScript.getSigningThreshold();
                int nrOfAccounts = verScript.getNrOfAccounts();
                sigVerificationFee = FeeUtils.calcNetworkFeeForMultiSig(threshold, nrOfAccounts);
            } else {
                sigVerificationFee = FeeUtils.calcNetworkFeeForSingleSig();
            }
            int txSize = calcPredictedTransactionSize();
            return sigVerificationFee + FeeUtils.calcTransactionSizeFee(txSize);
        }

        /*
         * Calculates the size of the transaction being build in this builder. The size is
         * calculated from all the fields set on the transaction (e.g. script, attributes,
         * non-signature witnesses) and the expected signature witness. The calculation must be
         * used before signing the transaction since it includes the size of a witness that is
         * expected to be added when signing the transaction.
         */
        private int calcPredictedTransactionSize() {
            List<Witness> predictedWitnesses = Arrays.asList(createMockWitness());
            predictedWitnesses.addAll(this.witnesses);

            return Transaction.HEADER_SIZE +
                IOUtils.getSizeOfVarList(this.attributes) +
                IOUtils.getSizeOfVarList(this.cosigners) +
                IOUtils.getSizeOfVarInt(this.script.length) + this.script.length +
                IOUtils.getSizeOfVarList(predictedWitnesses);
        }

        /*
         * Creates a witness object mocking a witness created with the given account. The
         * account's verification script is used to determine how many signatures the account
         * requires when signing a transaction (single-sig or multi-sig). The required signatures
         * are instantiated as zero-valued byte arrays. If it is a multi-sig account, the number
         * of mock signatures created is equal to the signing threshold of the account.
         */
        private Witness createMockWitness() {
            VerificationScript verScript = this.account.getVerificationScript();
            int signatures = verScript.getSigningThreshold();
            byte[] mockSignatureBytes = new byte[NeoConstants.SIGNATURE_SIZE_BYTES];
            if (signatures == 1) {
                // Account is a single-sig account
                SignatureData mockSignature = SignatureData.fromByteArray(mockSignatureBytes);
                InvocationScript invScript = InvocationScript.fromSignature(mockSignature);
                return new Witness(invScript, verScript);
            } else if (signatures > 1) {
                // Account is a multi-sig account
                List<SignatureData> mockSignatures = IntStream.range(0, signatures)
                    .mapToObj((i) -> SignatureData.fromByteArray(mockSignatureBytes))
                    .collect(Collectors.toList());
                return Witness.createMultiSigWitness(mockSignatures, verScript);
            }
            return null;
        }
    }
}
