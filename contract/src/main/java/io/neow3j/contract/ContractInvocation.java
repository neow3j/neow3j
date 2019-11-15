package io.neow3j.contract;

import io.neow3j.crypto.SecureRandomUtils;
import io.neow3j.transaction.RawScript;
import io.neow3j.transaction.RawTransactionAttribute;
import io.neow3j.transaction.RawTransactionInput;
import io.neow3j.transaction.RawTransactionOutput;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.core.methods.response.InvocationResult;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.InvocationTransaction;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.InputCalculationStrategy;
import io.neow3j.wallet.Utxo;
import io.neow3j.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContractInvocation {

    private static final Logger LOG = LoggerFactory.getLogger(ContractInvocation.class);

    private Neow3j neow3j;
    private ScriptHash scriptHash;
    private String function;
    private List<ContractParameter> params;
    private Account account;
    private InvocationTransaction tx;

    private ContractInvocation() {
    }

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
     * @return this invocation transaction.
     */
    public InvocationTransaction getTransaction() {
        return tx;
    }

    /**
     * <p>Sends the serialized invocation transaction to the RPC node (synchronous).</p>
     * <br>
     * <p>Before calling this method you should make sure that the transaction is signed either by
     * calling {@link ContractInvocation#sign()}} to automatically sign or by adding a custom
     * witness with {@link ContractInvocation#addWitness(RawScript)}.</p>
     *
     * @return this contract invocation object.
     * @throws IOException            if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the execution of the invocation lead to an error on the RPC
     *                                node.
     */
    public ContractInvocation invoke() throws IOException, ErrorResponseException {
        String rawTx = Numeric.toHexStringNoPrefix(tx.toArray());
        NeoSendRawTransaction response = neow3j.sendRawTransaction(rawTx).send();
        response.throwOnError();
        return this;
    }

    /**
     * <p>Tests the contract invocation by calling the invoke/invokescript method of the RPC node.</p>
     * <br>
     * <p>Doing this does not affect the blockchain's state. It can be used to see what result the
     * invocation will create. But, with NEO 2 nodes this invoke will probably fail if the
     * invocation runs through a `CheckWitness()` statement in the smart contract code.</p>
     *
     * @return the result of the invocation.
     * @throws IOException            if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the call to the node lead to an error. Not due to the
     *                                contract invocation itself but due to the call in general.
     */
    public InvocationResult testInvoke() throws IOException, ErrorResponseException {
        Response<InvocationResult> response;
        if (function != null) {
            if (params.isEmpty()) {
                response = neow3j.invokeFunction(scriptHash.toString(), function).send();
            } else {
                response = neow3j.invokeFunction(scriptHash.toString(), function, params).send();
            }
        } else {
            response = neow3j.invoke(scriptHash.toString(), params).send();
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
        if (account == null) {
            throw new IllegalStateException("No account provided. Can't automatically sign " +
                    "transaction without account.");
        }
        if (account.getPrivateKey() == null) {
            throw new IllegalStateException("Account does not hold a decrypted private key for " +
                    "signing the transaction. Decrypt the private key before attempting to sign " +
                    "with it.");
        }
        tx.addScript(RawScript.createWitness(tx.toArrayWithoutScripts(), account.getECKeyPair()));
        return this;
    }

    /**
     * <p>Adds the given witness to the invocation transaction's witnesses.</p>
     * <br>
     * <p>Use this method for adding a custom witness to the invocation transaction.
     * This does the same as the method {@link Builder#witness(RawScript)}, namely just add the
     * provided witness. But here it allows to add a witness from the created invocation
     * transaction object ({@link ContractInvocation#getTransaction()}) which is not possible in the
     * builder.</p>
     *
     * @param witness The witness to be added.
     * @return this invocation object.
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
        private ScriptHash scriptHash;
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
            this.inputCalculationStrategy = InputCalculationStrategy.DEFAULT_STRATEGY;
        }

        /**
         * <p>Adds the given list of parameters to this invocation.</p>
         * <br>
         * <p>The order in which parameters are added is important. The ordering they have in the given
         * list is preserved.</p>
         *
         * @param params The parameters to add.
         * @return this Builder object.
         */
        public Builder parameters(List<ContractParameter> params) {
            this.params.addAll(params);
            return this;
        }

        /**
         * <p>Adds the given parameter to this invocation.</p>
         * <br>
         * <p>The order in which parameters are added is important.</p>
         *
         * @param param The parameter to add.
         * @return this Builder object.
         */
        public Builder parameter(ContractParameter param) {
            this.params.add(param);
            return this;
        }

        public Builder contract(Contract contract) {
            return contractScriptHash(contract.getContractScriptHash());
        }

        /**
         * Adds the given script hash to this invocation. The script hash specifies the contract
         * to call in this invocation.
         *
         * @param scriptHash The contract script hash in big-endian order.
         * @return this Builder object.
         * @deprecated Use {@link Builder#contractScriptHash(ScriptHash)} instead.
         */
        @Deprecated
        public Builder contractScriptHash(String scriptHash) {
            this.scriptHash = new ScriptHash(scriptHash);
            return this;
        }

        /**
         * Adds the given script hash to this invocation. The script hash specifies the contract
         * to call in this invocation.
         *
         * @param scriptHash The contract script hash in little-endian order.
         * @return this Builder object.
         * @deprecated Use {@link Builder#contractScriptHash(ScriptHash)} instead.
         */
        @Deprecated
        public Builder contractScriptHash(byte[] scriptHash) {
            this.scriptHash = new ScriptHash(scriptHash);
            return this;
        }

        /**
         * Adds the given script hash to this invocation. The script hash specifies the contract
         * to call in this invocation.
         *
         * @param scriptHash The contract script hash.
         * @return this Builder object.
         */
        public Builder contractScriptHash(ScriptHash scriptHash) {
            this.scriptHash = scriptHash;
            return this;
        }

        /**
         * Adds the given function name to this invocation. Use this if you want to call a specific
         * function of the contract and not the main method.
         *
         * @param function The function name.
         * @return this Builder object.
         * @see <a href="https://docs.neo.org/docs/en-us/reference/rpc/latest-version/api/invokefunction.html">
         * invokefunction in NEO API reference</a>
         */
        public Builder function(String function) {
            this.function = function;
            return this;
        }

        /**
         * <p>Adds the given witness to this invocation.</p>
         * <br>
         * <p>A witness can also be added later after the transaction has been constructed. E.g. for
         * creating a signature.</p>
         *
         * @param witness The witness to add.
         * @return this Builder object.
         */
        public Builder witness(RawScript witness) {
            this.witnesses.add(witness);
            return this;
        }

        /**
         * <p>Adds the given account to this invocation. It will be used to fetch inputs if there are
         * fees or other outputs attached to this invocation. It is also used for creating a
         * signature when {@link ContractInvocation#sign()} is called.</p>
         * <br>
         * <p>If you don't add an account, the invocation cannot have any fees or outputs attached, and
         * automatically signing the transaction with {@link ContractInvocation#sign()} will not
         * work. Additionally, you will have to manually add an attribute of type script with the
         * script hash of some address in order that it is clear which address should be used for
         * verifying the transaction.</p>
         *
         * @param account The account to add.
         * @return this Builder object.
         */
        public Builder account(Account account) {
            if (this.account != null) {
                throw new IllegalStateException("Account already set.");
            }
            this.account = account;
            return this;
        }

        /**
         * Adds the default account of the given wallet to this invocation.
         *
         * @param wallet the wallet to use the default account from.
         * @return this Builder object.
         * @see Builder#account(Account)
         */
        public Builder wallet(Wallet wallet) {
            if (this.account != null) {
                throw new IllegalStateException("Account already set.");
            }
            this.account = wallet.getDefaultAccount();
            return this;
        }

        /**
         * <p>Adds a network fee.</p>
         * <br>
         * <p>Network fees add priority to a transaction and are paid in GAS. If a fee is added the
         * GAS will be taken from the account used in this contract invocation.</p>
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         * @deprecated Use {@link Builder#networkFee(double)} and {@link Builder#networkFee(String)}
         * instead.
         */
        @Deprecated
        public Builder networkFee(BigDecimal networkFee) {
            this.networkFee = networkFee;
            return this;
        }

        /**
         * <p>Adds a network fee.</p>
         * <br>
         * <p>Network fees add priority to a transaction and are paid in GAS. If a fee is added the
         * GAS will be taken from the account used in this contract invocation.</p>
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         */
        public Builder networkFee(String networkFee) {
            this.networkFee = new BigDecimal(networkFee);
            return this;
        }

        /**
         * Adds a network fee.
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         * @see Builder#networkFee(String)
         */
        public Builder networkFee(double networkFee) {
            return networkFee(Double.toString(networkFee));
        }

        /**
         * <p>Adds a system fee.</p>
         * <br>
         * <p>The system fee is required for successfully executing the invocation. But, if the
         * invocation consumes less than 10 GAS, no system fee needs to be paid. If you know that
         * the invocation consumes more than 10 GAS, then add only the additional amount here.</p>
         *
         * @param systemFee The fee amount to add.
         * @return this Builder object.
         * @deprecated Use {@link Builder#systemFee(double)} and {@link Builder#systemFee(String)}
         * instead.
         */
        @Deprecated
        public Builder systemFee(BigDecimal systemFee) {
            this.systemFee = systemFee;
            return this;
        }

        /**
         * <p>Adds a system fee.</p>
         * <br>
         * <p>The system fee is required for successfully executing the invocation. But, if the
         * invocation consumes less than 10 GAS no system fee needs to be paid. If you know that the
         * invocation consumes more than 10 GAS, then add here only the additional amount.</p>
         *
         * @param systemFee The fee amount to add.
         * @return this Builder object.
         */
        public Builder systemFee(String systemFee) {
            this.systemFee = new BigDecimal(systemFee);
            return this;
        }

        /**
         * Adds a system fee.
         *
         * @param systemFee The fee amount to add.
         * @return this Builder object.
         * @see Builder#systemFee(String)
         */
        public Builder systemFee(double systemFee) {
            return systemFee(Double.toString(systemFee));
        }

        /**
         * Adds the strategy that will be used to calculate the UTXOs used as transaction inputs.
         *
         * @param strategy The strategy to use.
         * @return this Builder object.
         */
        public Builder inputCalculationStrategy(InputCalculationStrategy strategy) {
            this.inputCalculationStrategy = strategy;
            return this;
        }

        /**
         * Adds the given attribute to this invocation.
         *
         * @param attribute The attribute to add.
         * @return this Builder object.
         */
        public Builder attribute(RawTransactionAttribute attribute) {
            this.attributes.add(attribute);
            return this;
        }

        /**
         * Adds the given attributes to this invocation.
         *
         * @param attributes The attributes to add.
         * @return this Builder object.
         */
        public Builder attributes(List<RawTransactionAttribute> attributes) {
            this.attributes.addAll(attributes);
            return this;
        }

        // TODO 16.09.19 claude:
        // This needs to be implemented with UTXOs the same way as it is in AssetTransfer.
        public Builder inputs(List<RawTransactionInput> inputs) {
            throw new UnsupportedOperationException();
//            this.inputs.addAll(inputs);
//            return this;
        }

        // TODO 16.09.19 claude:
        // This needs to be implemented with UTXOs the same way as it is in AssetTransfer.
        public Builder input(RawTransactionInput input) {
            throw new UnsupportedOperationException();
//            this.inputs.add(input);
//            return this;

        }

        /**
         * Adds the given outputs to this invocation. The outputs are sent in the same transaction
         * with the invocation.
         *
         * @param outputs The outputs to add.
         * @return this Builder object.
         */
        public Builder outputs(List<RawTransactionOutput> outputs) {
            this.outputs.addAll(outputs);
            return this;
        }

        /**
         * Adds the given output to this invocation. The output are sent in the same transaction
         * with the invocation.
         *
         * @param output The output to add.
         * @return this Builder object.
         */
        public Builder output(RawTransactionOutput output) {
            this.outputs.add(output);
            return this;
        }

        /**
         * <p>Builds the contract invocation object ready for signing and invoking.</p>
         * <br>
         * <p>In more detail:</p>
         * <ul>
         * <li>Collects the necessary inputs, if this invocation has fees or other outputs attached.</li>
         * <li>Adds necessary attributes if the invocation does not have any fees and outputs.</li>
         * <li>Constructs an {@link InvocationTransaction} object.</li>
         * </ul>
         *
         * @return the constructed contract invocation object.
         */
        public ContractInvocation build() {
            if (neow3j == null) throw new IllegalStateException("Neow3j not set");
            if (scriptHash == null) throw new IllegalStateException("Contract script hash not set");

            List<RawTransactionOutput> intents = new ArrayList<>(outputs);
            intents.addAll(createOutputsFromFees(networkFee, systemFee));
            Map<String, BigDecimal> requiredAssets = calculateRequiredAssetsForIntents(intents);

            if (!requiredAssets.isEmpty()) {
                if (account == null) throw new IllegalStateException("No account set but needed " +
                        "for fetching transaction inputs.");
                calculateInputsAndChange(requiredAssets);
            }

            addAttributesIfTransactionIsEmpty();

            this.tx = buildTransaction();

            return new ContractInvocation(this);
        }

        private InvocationTransaction buildTransaction() {

            byte[] script = new ScriptBuilder()
                    .appCall(scriptHash, function, params)
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
                if (account != null) {
                    RawTransactionAttribute scriptAttr = new RawTransactionAttribute(
                            TransactionAttributeUsageType.SCRIPT,
                            account.getScriptHash().toArray());
                    this.attributes.add(scriptAttr);
                }

                RawTransactionAttribute remarkAttr = new RawTransactionAttribute(
                        TransactionAttributeUsageType.REMARK, createRandomRemark());

                this.attributes.add(remarkAttr);
            }
        }

        byte[] createRandomRemark() {
            return ArrayUtils.concatenate(
                    ByteBuffer.allocate(Long.BYTES).putLong(Instant.now().toEpochMilli()).array(),
                    SecureRandomUtils.generateRandomBytes(4));
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
