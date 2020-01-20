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
import io.neow3j.wallet.exceptions.AccountException;
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
    public Invocation sign() {
        try {
            this.transaction.addWitness(
                    Witness.createWitness(getTransactionForSigning(), this.account.getECKeyPair()));
        } catch (AccountException e) {
            throw new InvocationConfigurationException("Cannot automatically sign with given "
                    + "account. The account object needs a decrypted private key.");
        }
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
        private Transaction.Builder txBuilder;
        private Account account;

        // Should only be called by the SmartContract class. Therefore no checks on the arguments.
        protected InvocationBuilder(Neow3j neow, ScriptHash scriptHash, String function) {
            this.neow = neow;
            this.scriptHash = scriptHash;
            this.function = function;
            this.parameters = new ArrayList<>();
            this.txBuilder = new Transaction.Builder();
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
            try {
                account.getVerificationScript();
            } catch (AccountException e) {
                throw new InvocationConfigurationException("Given account does not have a "
                        + "verification script but needs one to be used in an invocation.");
            }
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
            if (this.txBuilder.getSender() == null) {
                throw new InvocationConfigurationException("Cannot create an invocation without a "
                        + "sending account.");
            }
            if (this.txBuilder.getValidUntilBlock() == null) {
                this.txBuilder.validUntilBlock(
                        fetchCurrentBlockNr() + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT);
            }

            // Set the script on the transaction builder and on this builder because it is later
            // needed for the calculation of the network fee and the transaction builder has no
            // getters.
            this.txBuilder.script(createScript());
            this.txBuilder.systemFee(fetchSystemFee());
            // Before we can calculate the network fee the transaction must be completely setup.
            // Therefore, at least a standard cosigner must be set in case no cosigner has been
            // set specifically.
            if (this.txBuilder.getCosigners().isEmpty()) {
                this.txBuilder.cosigners(Cosigner.calledByEntry(this.account.getScriptHash()));
            }
            this.txBuilder.networkFee(calcNetworkFee() + this.additionalNetworkFee);
            // TODO: Maybe check if the sender has enough coin to do the invocation.
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
            predictedWitnesses.addAll(this.txBuilder.getWitnesses());

            return Transaction.HEADER_SIZE +
                    IOUtils.getSizeOfVarList(this.txBuilder.getAttributes()) +
                    IOUtils.getSizeOfVarList(this.txBuilder.getCosigners()) +
                    IOUtils.getSizeOfVarInt(this.txBuilder.getScript().length) +
                    this.txBuilder.getScript().length +
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
