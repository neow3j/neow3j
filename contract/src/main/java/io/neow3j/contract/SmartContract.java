package io.neow3j.contract;

import io.neow3j.contract.Invocation.InvocationBuilder;
import io.neow3j.protocol.Neow3j;

public class SmartContract {

    protected ScriptHash scriptHash;
    protected Neow3j neow;

    /**
     * @param scriptHash
     * @param neow
     */
    public SmartContract(ScriptHash scriptHash, Neow3j neow) {
        if (scriptHash == null)  {
            throw new IllegalArgumentException("The contract script hash must not be null.");
        }
        if (neow == null)  {
            throw new IllegalArgumentException("The Neow3j object must not be null.");
        }
        this.scriptHash = scriptHash;
        this.neow = neow;
    }

    /**
     * @param function
     * @return
     */
    public InvocationBuilder invoke(String function) {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException(
                "The invocation function must not be null or empty.");
        }
        return new InvocationBuilder(neow, scriptHash, function);
    }

//    protected abstract class BaseInvocationBuilder<T extends BaseInvocationBuilder<T>> {
//
//        private Transaction.Builder txBuilder;
//        private String function;
//        private List<ContractParameter> parameters;
//        private BigInteger additionalNetworkFee;
//        private boolean isValidUntilBlockSet;
//
//        protected BaseInvocationBuilder(String function) {
//            if (function == null || function.isEmpty()) {
//                throw new InvocationConfigurationException(
//                    "The function to invoke must not be null or empty.");
//            }
//            this.function = function;
//            this.txBuilder = new Transaction.Builder();
//            this.parameters = new ArrayList<>();
//            this.isValidUntilBlockSet = false;
//        }
//
//        abstract protected T getThis();
//
//        /**
//         * @param parameters
//         * @return
//         */
//        public T withParameters(ContractParameter... parameters) {
//            this.parameters.addAll(Arrays.asList(parameters));
//            return getThis();
//        }
//
//        /**
//         * @param attributes
//         * @return
//         */
//        public T withAttributes(TransactionAttribute... attributes) {
//            this.txBuilder.attributes(attributes);
//            return getThis();
//        }
//
//        /**
//         * @param cosigners
//         * @return
//         */
//        public T withWitnessScopes(Cosigner... cosigners) {
//            this.txBuilder.cosigners(cosigners);
//            return getThis();
//        }
//
//        /**
//         * @param blockNr
//         * @return
//         */
//        public T validUntilBlock(long blockNr) {
//            this.txBuilder.validUntilBlock(blockNr);
//            this.isValidUntilBlockSet = true;
//            return getThis();
//        }
//
//        /**
//         * @param fee
//         * @return
//         */
//        public T withAdditionalNetworkFee(BigInteger fee) {
//            this.additionalNetworkFee = fee;
//            return getThis();
//        }
//
//        /**
//         * @param sender
//         * @return
//         */
//        public T withSender(ScriptHash sender) {
//
//            return getThis();
//        }
//
//        /**
//         * @return
//         * @throws IOException
//         */
//        public NeoInvokeFunction test() throws IOException {
//            if (this.parameters.isEmpty()) {
//                return neow.invokeFunction(scriptHash.toString(), this.function).send();
//            }
//            return neow.invokeFunction(scriptHash.toString(), this.function, this.parameters)
//                .send();
//            // TODO: Should error checking be done here?
//        }
//
//        /**
//         *
//         */
//        public Transaction send() throws Exception {
//
//            byte[] script = new ScriptBuilder()
//                .appCall(SmartContract.this.scriptHash, this.function, this.parameters)
//                .toArray();
//            this.txBuilder.script(script);
//
//            if (!this.isValidUntilBlockSet) {
//                setDefaultValidUntilBlock();
//            }
//
//            this.txBuilder.systemFee(fetchSystemFee());
//            // TODO: Calculate the network fee of the function invocation and add the amount to the
//            //  transaction
//            // TODO: Create the witness by signing the transaction.
//            Transaction tx = this.txBuilder.build();
//            boolean result = neow.sendRawTransaction(Numeric.toHexString(tx.toArray())).send()
//                .getResult();
//            // TODO: Handle the result.
//            // TODO: Can we retrieve the invocation results from the blockchain in Neo 3?
//            return tx;
//        }
//
//        /**
//         * Calculates the network fee for the given transaction size in bytes.
//         * <p>
//         * Signature verification cost in NeoVM plus transaction length times the fee per byte
//         * (0.00001 GAS)
//         *
//         * @param bytes The number of bytes.
//         * @return the necessary network fee for the transaction of the given byte size.
//         */
//        private BigDecimal calcNetworkFee(int bytes) {
//            return new BigDecimal(bytes).multiply(NeoConstants.FEE_PER_EXTRA_BYTE)
//                .add(NeoConstants.FEE_FOR_SIG_VERIFICATION);
//        }
//
//        private void setDefaultValidUntilBlock() throws Exception {
//            long blockNr;
//            try {
//                blockNr = neow.getBlockCount().send().getBlockIndex().longValue();
//            } catch (IOException e) {
//                //TODO: Create a specific exception
//                throw new Exception();
//            }
//            this.txBuilder.validUntilBlock(blockNr + NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT);
//        }
//
//        private long fetchSystemFee() throws IOException {
//            // The GAS amount is received in decimal form (not Fixed8) so we can directly convert
//            // to BigDecimal. We explicitly do not check if the VM exit state is FAULT. The GAS
//            // consumption is determined independent of the invocations outcome.
//            return new BigDecimal(test().getInvocationResult().getGasConsumed())
//                .multiply(BigDecimal.TEN.pow(GasToken.DECIMALS)).longValue();
//        }
//    }

//    public class InvocationBuilder extends BaseInvocationBuilder<InvocationBuilder> {
//
//        public InvocationBuilder(String function) {
//            super(function);
//        }
//
//        public InvocationBuilder getThis() {
//            return this;
//        }
//    }

}
