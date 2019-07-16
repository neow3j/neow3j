package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.model.ContractParameter;
import io.neow3j.protocol.core.Response;
import io.neow3j.utils.Keys;
import io.neow3j.crypto.SecureRandomUtils;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransactionAttribute;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.InvocationTransaction;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.InputCalculationStrategy;
import io.neow3j.wallet.Utxo;
import io.neow3j.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContractInvocation {

    private static final Logger LOG = LoggerFactory.getLogger(ContractInvocation.class);

    private Neow3j neow3j;
    private String scriptHash;
    private String function;
    private List<ContractParameter> params;
    private Account account;
    private InvocationTransaction tx;

    private ContractInvocation() {}

    private ContractInvocation(final Builder builder) {
        this.neow3j = builder.neow3j;
        this.scriptHash = builder.scriptHash;
        this.function = builder.function;
        this.params = builder.params;
        this.account = builder.account;
        this.tx = builder.tx;
    }

    /**
     * Gets the transaction object that will be serialized and sent to the RPC node when
     * {@link ContractInvocation#invoke()} is called.
     *
     */
    public InvocationTransaction getTransaction() {
        return tx;
    }

    /**
     * Sends the serialized invocation transaction to the RPC node (synchronous).
     * <br><br>
     * Before calling this method you should make sure that the transaction is signed either by
     * calling {@link ContractInvocation#sign()}} to automatically sign or by adding a custom
     * witness with {@link ContractInvocation#addWitness(RawScript)}.
     *
     * @return this contract invocation object.
     * @throws IOException if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the execution of the invocation lead to an error on the RPC
     * node.
     */
    public ContractInvocation invoke() throws IOException, ErrorResponseException {
        String rawTx = Numeric.toHexStringNoPrefix(tx.toArray());
        NeoSendRawTransaction response = neow3j.sendRawTransaction(rawTx).send();
        response.throwOnError();
        return this;
    }

    /**
     * Tests the contract invocation by calling the invoke/invokescript method of the RPC node.
     * <br><br>
     * Doing this does not affect the blockchain's state. It can be used to see what result the
     * invocation will create. But, with NEO 2 nodes this invoke will probably fail if the
     * invocation runs thorugh a `CheckWitness()` statement in the smart contract code.
     *
     * @return the result of the invocation.
     * @throws IOException if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the call to the node lead to an error. Not due to the
     *                                contract invocation itself but due to the call in general.
     */
    public InvocationResult testInvoke() throws IOException, ErrorResponseException {
        Response<InvocationResult> response;
        if (function != null) {
            if (params.isEmpty()) {
                response = neow3j.invokeFunction(scriptHash, function).send();
            } else {
                response = neow3j.invokeFunction(scriptHash, function, params).send();
            }
        } else {
            response = neow3j.invoke(scriptHash, params).send();
        }
        response.throwOnError();
        return response.getResult();
    }

    /**
     * Adds a witness to the transaction. The witness is created with the transaction in its current
     * state and the account involved in this invocation.
     *
     * @return this invocation object, updated with a witness.
     */
    public ContractInvocation sign() {
        if (account.getPrivateKey() == null) {
            throw new IllegalStateException("Account does not hold a decrypted private key for " +
                    "signing the transaction. Decrypt the private key before attempting to sign " +
                    "with it.");
        }
        tx.addScript(RawScript.createWitness(tx.toArray(), account.getECKeyPair()));
        return this;
    }

    /**
     * Adds the given witness to the invocation transaction's witnesses.
     * <br><br>
     * Use this method for adding a custom witness to the invocation transaction.
     * This does the same as the method {@link Builder#witness(RawScript)}, namely just add the
     * provided witness. But here it allows to add a witness from the created invocation
     * transaction object ({@link ContractInvocation#getTransaction()}) which is not possible in the
     * builder.
     *
     * @param witness   The witness to be added.
     * @return          this invocation object.
     */
    public ContractInvocation addWitness(RawScript witness) {
        tx.addScript(witness);
        return this;
    }

    // TODO 16.07.19 claude:
    // Enable this when the NEO RPC node software is handling `invoke` and `invokefunction` with
    // contracts that contain `CheckWitness()` statements correctly. At the moment those methods
    // fail in that case and so it is not save to rely on them.
//    /**
//     * Fetches the amount of GAS consumed by this invocation by calling the  RPC node and
//     * adds a system fee according to the consumption.
//     *
//     * A system fee is only added if the GAS consumption rises over the 10 GAS that are free per
//     * invocation.
//     *
//     * @return this invocation object, updated with a system fee if necessary.
//     * @throws IOException if a problem with the connection to the RPC node arises.
//     * @throws ErrorResponseException if the RPC node returns an error instead of processing the
//     * invocation.
//     * @throws ContractInvocationException if the contract execution finished with 'FAULT'.
//     */
//    public ContractInvocation calculateSystemFee() throws IOException, ErrorResponseException,
//            ContractInvocationException {
//
//        BigDecimal consumption = fetchGasConsumption();
//        if (consumption.compareTo(NeoConstants.FREE_GAS_AMOUNT) <= 0) {
//           this.systemFee = BigDecimal.ZERO;
//        } else {
//           this.systemFee = consumption.subtract(NeoConstants.FREE_GAS_AMOUNT);
//        }
//        return this;
//    }
//
//    private BigDecimal fetchGasConsumption() throws ErrorResponseException, ContractInvocationException,
//            IOException {
//
//        NeoInvoke response = neow3j.invoke(scriptHash, params).send();
//        response.throwOnError();
//        InvocationResult result = response.getInvocationResult();
//
//        if (result.getState().contains(NeoVMState.FAULT.toString())) {
//            throw new ContractInvocationException("Contract invocation ended with FAULT when " +
//                    "trying to fetch the invocations GAS consumption.", NeoVMState.FAULT,
//                    result.getStack());
//        }
//        // The GAS amount is returned in decimal form (not Fixed8) so we can directly convert to BigDecimal.
//        return new BigDecimal(result.getGasConsumed());
//    }

    public static class Builder {

        private Neow3j neow3j;
        private String scriptHash;
        private String function;
        private List<RawScript> witnesses;
        private List<ContractParameter> params;
        private Account account;
        private BigDecimal networkFee;
        private BigDecimal systemFee;
        private InputCalculationStrategy inputCalculationStrategy;
        private List<RawTransactionAttribute> attributes;
        private List<RawTransactionInput> inputs;
        private List<RawTransactionOutput> outputs;
        private InvocationTransaction tx;

        public Builder(Neow3j neow3j) {
            this.neow3j = neow3j;
            this.params = new ArrayList<>();
            this.witnesses = new ArrayList<>();
            this.attributes = new ArrayList<>();
            this.outputs = new ArrayList<>();
            this.inputs = new ArrayList<>();
            this.networkFee = BigDecimal.ZERO;
            this.systemFee = BigDecimal.ZERO;
            this.inputCalculationStrategy = InputCalculationStrategy.DEFAULT_INPUT_CALCULATION_STRATEGY;
        }

        public Builder parameters(List<ContractParameter> params) {
            params.addAll(params);
            return this;
        }

        public Builder parameter(ContractParameter param) {
            params.add(param);
            return this;
        }

        public Builder contract(Contract contract) {
            return contractScriptHash(contract.getContractScriptHash());
        }

        public Builder contractScriptHash(String scriptHash) {
            if (scriptHash == null) {
                throw new IllegalArgumentException("Script hash must not be null");
            }
            if (scriptHash.length() != NeoConstants.SCRIPTHASH_LENGHT_HEXSTRING) {
                throw new IllegalArgumentException("Script hash must be 20 bytes long but was " +
                        scriptHash.length()/2 + " bytes long.");
            }
            this.scriptHash = scriptHash;
            return this;
        }

        public Builder contractScriptHash(byte[] scriptHash) {
            return contractScriptHash(Numeric.toHexStringNoPrefix(scriptHash));
        }

        public Builder function(String function) {
            this.function = function;
            return this;
        }

        public Builder witness(RawScript script) {
            this.witnesses.add(script);
            return this;
        }

        public Builder account(Account account) {
            if (this.account != null) {
                throw new IllegalStateException("Account already set.");
            }
            this.account = account;
            return this;
        }

        public Builder wallet(Wallet wallet) {
            if (this.account != null) {
                throw new IllegalStateException("Account already set.");
            }
            this.account = wallet.getDefaultAccount();
            return this;
        }

        /**
         * Adds a network fee.
         * <br><br>
         * Network fees add priority to a transaction and are paid in GAS. If a fee is added the
         * GAS will be taken from the account used in contract invocation.
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         */
        public Builder networkFee(BigDecimal networkFee) {
            this.networkFee = networkFee;
            return this;
        }

        /**
         * Adds a system fee.
         * <br><br>
         * The system fee is required for successfully executing the invocation. But, if the
         * invocation consumes less than 10 GAS no system fee needs to be paid. If you know that the
         * invocation consumes more than 10 GAS, then add here only the additional amount.
         *
         * @param systemFee The fee amount to add.
         * @return this Builder object.
         */
        public Builder systemFee(BigDecimal systemFee) {
            this.systemFee = systemFee;
            return this;
        }

        /**
         * Add the strategy used to calculate which unspent transaction outputs from the involved
         * account should be use to pay for any outputs.
         *
         * @param strategy the strategy to use.
         * @return this Builder object.
         */
        public Builder inputCalculationStrategy(InputCalculationStrategy strategy) {
            this.inputCalculationStrategy = strategy;
            return this;
        }

        public Builder attribute(RawTransactionAttribute attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public Builder attributes(List<RawTransactionAttribute> attributes) {
            this.attributes.addAll(attributes);
            return this;
        }

        public Builder inputs(List<RawTransactionInput> inputs) {
            throw new UnsupportedOperationException();
//            this.inputs.addAll(inputs);
//            return this;
        }

        public Builder input(RawTransactionInput input) {
            throw new UnsupportedOperationException();
//            this.inputs.add(input);
//            return this;

        }

        public Builder outputs(List<RawTransactionOutput> outputs) {
            this.outputs.addAll(outputs);
            return this;
        }

        public Builder output(RawTransactionOutput output) {
            this.outputs.add(output);
            return this;
        }

        public ContractInvocation build() {
            if (neow3j == null) throw new IllegalStateException("Neow3j not set");
            if (account == null) throw new IllegalStateException("No account set");
            if (scriptHash == null) throw new IllegalStateException("Contract script hash not set");

            List<RawTransactionOutput> intents = new ArrayList<>(outputs);
            intents.addAll(createOutputsFromFees(networkFee, systemFee));
            Map<String, BigDecimal> requiredAssets = calculateRequiredAssetsForIntents(intents);

            calculateInputsAndChange(requiredAssets);

            addAttributesIfTransactionIsEmpty();

            this.tx = buildTransaction();

            return new ContractInvocation(this);
        }

        private InvocationTransaction buildTransaction() {

            byte[] script = new ScriptBuilder()
                    .appCall(Numeric.hexStringToByteArray(scriptHash), function, params)
                    .toArray();

            return new InvocationTransaction.Builder()
                    .outputs(this.outputs)
                    .inputs(this.inputs)
                    .systemFee(this.systemFee)
                    .attributes(this.attributes)
                    .scripts(this.witnesses)
                    .contractScript(script)
                    .build();
        }

        /**
         * Adds attributes such that the transaction is verifiable and repeatable even if it doesn't
         * have any inputs and outputs set.
         */
        private void addAttributesIfTransactionIsEmpty() {
            if (outputs.isEmpty() && inputs.isEmpty()) {
                RawTransactionAttribute scriptAttr =  new RawTransactionAttribute(
                        TransactionAttributeUsageType.SCRIPT,
                        Keys.toScriptHash(account.getAddress()));

                String remark = Long.toString(Instant.now().toEpochMilli())
                        + Numeric.toHexStringNoPrefix(SecureRandomUtils.generateRandomBytes(4));
                RawTransactionAttribute remarkAttr =  new RawTransactionAttribute(
                        TransactionAttributeUsageType.REMARK, remark);

                this.attributes.add(scriptAttr);
                this.attributes.add(remarkAttr);
            }
        }

        private List<RawTransactionOutput> createOutputsFromFees(BigDecimal... fees) {
            List<RawTransactionOutput> outputs = new ArrayList<>(fees.length);
            for (BigDecimal fee : fees) {
                if (fee.compareTo(BigDecimal.ZERO) > 0) {
                    outputs.add(new RawTransactionOutput(GASAsset.HASH_ID, fee.toPlainString(), null));
                }
            }
            return outputs;
        }

        private Map<String, BigDecimal> calculateRequiredAssetsForIntents(
                List<RawTransactionOutput> outputs) {

            Map<String, BigDecimal> assets = new HashMap<>();
            outputs.forEach(output -> {
                BigDecimal value = new BigDecimal(output.getValue());
                if (assets.containsKey(output.getAssetId())) {
                    value = assets.get(output.getAssetId()).add(value);
                }
                assets.put(output.getAssetId(), value);
            });
            return assets;
        }

        private void calculateInputsAndChange(Map<String, BigDecimal> requiredAssets) {
            requiredAssets.forEach((reqAssetId, reqValue) -> {
                List<Utxo> utxos = account.getUtxosForAssetAmount(reqAssetId, reqValue, inputCalculationStrategy);
                inputs.addAll(utxos.stream().map(Utxo::toTransactionInput).collect(Collectors.toList()));
                BigDecimal changeAmount = calculateChange(utxos, reqValue);
                if (changeAmount != null) outputs.add(
                        new RawTransactionOutput(reqAssetId, changeAmount.toPlainString(), account.getAddress()));
            });
        }

        private BigDecimal calculateChange(List<Utxo> utxos, BigDecimal reqValue) {
            BigDecimal inputAmount = utxos.stream().map(Utxo::getValue).reduce(BigDecimal::add).get();
            if (inputAmount.compareTo(reqValue) > 0) {
                return inputAmount.subtract(reqValue);
            }
            return null;
        }

    }

}
