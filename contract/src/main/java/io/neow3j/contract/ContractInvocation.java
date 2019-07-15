package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.NeoVMState;
import io.neow3j.contract.exception.ContractInvocationException;
import io.neow3j.model.ContractParameter;
import io.neow3j.protocol.core.Request;
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
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.InvocationTransaction;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Balances.AssetBalance;
import io.neow3j.wallet.InputCalculationStrategy;
import io.neow3j.wallet.Utxo;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
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
    private byte[] scriptHash;
    private String operation;
    private List<RawScript> witnesses;
    private List<ContractParameter> params;
    private BigDecimal systemFee;
    private BigDecimal networkFee;
    private Account account;
    private List<RawTransactionOutput> outputs;
    private List<RawTransactionInput> inputs;
    private List<RawTransactionAttribute> attributes;
    private InputCalculationStrategy inputCalculationStrategy;
    private InvocationTransaction tx;

    private ContractInvocation() {}

    private ContractInvocation(final Builder builder) {
        this.neow3j = builder.neow3j;
        this.scriptHash = builder.scriptHash;
        this.operation = builder.operation;
        this.witnesses = builder.witnesses;
        this.params = builder.params;
        this.account = builder.account;
        this.networkFee = builder.networkFee;
        this.inputCalculationStrategy = builder.inputCalculationStrategy;
        this.attributes = builder.attributes;
        this.outputs = builder.outputs;
        this.inputs = builder.inputs;
        this.systemFee = BigDecimal.ZERO;
    }

    public NeoSendRawTransaction invoke() throws IOException, ErrorResponseException {
        String rawTx = Numeric.toHexStringNoPrefix(tx.toArray());
        NeoSendRawTransaction response = neow3j.sendRawTransaction(rawTx).send();
        response.throwOnError();
        return response;
    }

    public InvocationResult simulateInvoke() throws ErrorResponseException, IOException {
        NeoInvoke response = neow3j.invoke(Numeric.toHexStringNoPrefix(scriptHash), params).send();
        response.throwOnError();
        return response.getInvocationResult();
    }

    /**
     * Fetches the amount of GAS consumed by this invocation from a RPC node and calculates the
     * system networkFee that needs to be paid for executing the invocation.
     * The free GAS per contract invocation is deducted from the total GAS consumption of this
     * invocation.
     *
     * @return the system networkFee, in GAS, needed for this invocation.
     * @throws IOException if a problem with the connection to the RPC node arises.
     * @throws ErrorResponseException if the RPC node returns an error instead of processing the
     * invocation.
     * @throws ContractInvocationException if the contract execution finished with 'FAULT'.
     */
    public ContractInvocation fetchSystemFee() throws IOException, ErrorResponseException,
            ContractInvocationException {

        BigDecimal consumption = fetchGasConsumption();
        if (consumption.compareTo(NeoConstants.FREE_GAS_AMOUNT) <= 0) {
           this.systemFee = BigDecimal.ZERO;
        } else {
           this.systemFee = consumption.subtract(NeoConstants.FREE_GAS_AMOUNT);
        }
        return this;
    }

    private BigDecimal fetchGasConsumption() throws ErrorResponseException, ContractInvocationException,
            IOException {

        NeoInvoke response = neow3j.invoke(Numeric.toHexStringNoPrefix(scriptHash), params).send();
        response.throwOnError();
        InvocationResult result = response.getInvocationResult();

        if (result.getState().contains(NeoVMState.FAULT.toString())) {
            throw new ContractInvocationException(NeoVMState.FAULT, result.getStack());
        }
        // The GAS amount is returned in decimal form (not Fixed8) so we can directly convert to BigDecimal.
        LOG.info(result.getGasConsumed() + " GAS consumed in invocation of contract "
                + Numeric.toHexStringNoPrefix(scriptHash));
        return new BigDecimal(result.getGasConsumed());
    }

    public ContractInvocation createTransaction() {
        if (systemFee == null) throw new IllegalStateException("Can't build the transaction without " +
                "knowing the system networkFee.");
        Map<String, BigDecimal> requiredAssets = calculateRequiredAssets(networkFee.add(systemFee));
        calculateInputsAndChange(requiredAssets);
        handleEmptyTransaction();
        byte[] script = new ScriptBuilder()
                .appCall(this.scriptHash, operation, params)
                .toArray();

        this.tx = new InvocationTransaction.Builder()
            .outputs(this.outputs)
            .inputs(this.inputs)
            .systemFee(this.systemFee)
            .contractScript(script)
            .attributes(this.attributes)
            .build();

        return this;
    }

    /**
     * Adds attributes such that the transaction is verifiable and repeatable even if it doesn't
     * have any inputs and outputs set.
     */
    private void handleEmptyTransaction() {
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

    public ContractInvocation signTransaction() {
        if (account.getPrivateKey() == null) throw new IllegalStateException("Account does not " +
                "hold a decrypted private key for signing the transaction.");
        tx.addScript(RawScript.createWitness(tx.toArray(), account.getECKeyPair()));
        return this;
    }

    private static Map<String, BigDecimal> calculateRequiredAssets(BigDecimal fees) {
        return calculateRequiredAssets(new ArrayList<RawTransactionOutput>(), fees);
    }

    private static Map<String, BigDecimal> calculateRequiredAssets(List<RawTransactionOutput> outputs,
                                                            BigDecimal fees) {

        outputs = new ArrayList<>(outputs);
        if (fees != null && fees.compareTo(BigDecimal.ZERO) > 0) {
            outputs.add(new RawTransactionOutput(GASAsset.HASH_ID, fees.toPlainString(), null));
        }

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
        requiredAssets.forEach((assetId, value) -> {
            List<Utxo> utxos = fetchUtxosForAsset(account, assetId, value, inputCalculationStrategy);
            inputs.addAll(utxos.stream().map(Utxo::toTransactionInput).collect(Collectors.toList()));
            BigDecimal changeAmount = calculcateChange(utxos, value);
            if (changeAmount != null) outputs.add(
                    new RawTransactionOutput(assetId, changeAmount.toPlainString(), account.getAddress()));
        });
    }

    private static List<Utxo> fetchUtxosForAsset(Account account, String assetId, BigDecimal assetValue,
                                                 InputCalculationStrategy strategy) {

        if (account.getBalances() == null) {
            throw new IllegalStateException("Account does not have any asset balances. " +
                    "Update account's asset balances first.");
        }
        if (!account.getBalances().hasAsset(assetId)) {
            throw new InsufficientFundsException("Account balance does not contain the asset " +
                    "with ID " + assetId);
        }
        AssetBalance balance = account.getBalances().getAssetBalance(assetId);
        if (balance.getAmount().compareTo(assetValue) < 0) {
            throw new InsufficientFundsException("Needed " + assetValue + " but only found " +
                    balance.getAmount() + " for asset with ID " + assetId);
        }
        return strategy.calculateInputs(balance.getUtxos(), assetValue);
    }

    private BigDecimal calculcateChange(List<Utxo> utxos, BigDecimal reqValue) {
        BigDecimal inputAmount = utxos.stream().map(Utxo::getValue).reduce(BigDecimal::add).get();
        if (inputAmount.compareTo(reqValue) > 0) {
            return inputAmount.subtract(reqValue);
        }
        return null;
    }

    public void sign() {

    }

    public static class Builder {

        private Neow3j neow3j;
        private byte[] scriptHash;
        private String operation;
        private List<RawScript> witnesses;
        private List<ContractParameter> params;
        private Account account;
        private BigDecimal networkFee;
        private InputCalculationStrategy inputCalculationStrategy;
        private List<RawTransactionAttribute> attributes;
        private List<RawTransactionInput> inputs;
        private List<RawTransactionOutput> outputs;

        public Builder(Neow3j neow3j) {
            this.neow3j = neow3j;
            this.params = new ArrayList<>();
            this.witnesses = new ArrayList<>();
            this.attributes = new ArrayList<>();
            this.outputs = new ArrayList<>();
            this.inputs = new ArrayList<>();
            this.networkFee = BigDecimal.ZERO;
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
            return contractScriptHash(Numeric.hexStringToByteArray(scriptHash));
        }

        public Builder contractScriptHash(byte[] scriptHash) {
            if (scriptHash == null || scriptHash.length != NeoConstants.SCRIPTHASH_LENGHT_BYTES)
                throw new IllegalArgumentException("Script hash must be 160 bits long.");
            this.scriptHash = scriptHash;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder witness(RawScript script) {
            this.witnesses.add(script);
            return this;
        }

        public Builder account(Account account) {
            this.account = account;
            return this;
        }

        /**
         * Adds the given amount as a network networkFee to the invocation. This networkFee is applied
         * additionally to the system networkFee which is calculated from the invocations GAS consumption.
         */
        public Builder fee(BigDecimal fee) {
            this.networkFee = fee;
            return this;
        }

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
            this.inputs.addAll(inputs);
            return this;
        }

        public Builder input(RawTransactionInput input) {
            this.inputs.add(input);
            return this;
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
            if (scriptHash == null) throw new IllegalStateException("Contract script hash not set");
            if (account == null) throw new IllegalStateException("No account set");

            return new ContractInvocation(this);
        }
    }

}
