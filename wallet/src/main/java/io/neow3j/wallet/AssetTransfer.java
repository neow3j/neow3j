package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransactionAttribute;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.ContractTransaction;
import io.neow3j.utils.Numeric;
import io.neow3j.utils.Strings;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssetTransfer {

    private Neow3j neow3j;
    private ContractTransaction tx;
    private Account account;

    private AssetTransfer(Builder builder) {
        this.neow3j = builder.neow3j;
        this.tx = builder.tx;
        this.account = builder.account;
    }

    public ContractTransaction getTransaction() {
        return tx;
    }

    /**
     * <p>Adds the given witness to the transaction's witnesses.</p>
     * <br>
     * <p>Use this method for adding a custom witness to the transaction.
     * This does the same as the method {@link Builder#witness(RawScript)}, namely just adds the
     * provided witness. But here it allows to add a witness from the created transaction object
     * ({@link AssetTransfer#getTransaction()}) which is not possible in the builder.</p>
     *
     * @param witness The witness to be added.
     * @return this asset transfer object.
     */
    public AssetTransfer addWitness(RawScript witness) {
        tx.addScript(witness);
        return this;
    }

    public AssetTransfer send() throws IOException, ErrorResponseException {
        String rawTx = Numeric.toHexStringNoPrefix(tx.toArray());
        NeoSendRawTransaction response = neow3j.sendRawTransaction(rawTx).send();
        response.throwOnError();
        return this;
    }

    /**
     * Adds a witness to the transaction. The witness is created with the transaction in its current
     * state and the account involved in this asser transfer.
     *
     * @return this asset transfer object, updated with a witness.
     */
    public AssetTransfer sign() {
        if (account.getPrivateKey() == null) {
            throw new IllegalStateException("Account does not hold a decrypted private key for " +
                    "signing the transaction. Decrypt the private key before attempting to sign " +
                    "with it.");
        }
        tx.addScript(RawScript.createWitness(tx.toArrayWithoutScripts(), account.getECKeyPair()));
        return this;
    }

    public static class Builder {

        private Neow3j neow3j;
        private Account account;
        private BigDecimal networkFee;
        private List<RawTransactionOutput> outputs;
        private List<RawTransactionInput> inputs;
        private List<RawScript> witnesses;
        private List<RawTransactionAttribute> attributes;
        private InputCalculationStrategy inputCalculationStrategy;
        private ContractTransaction tx;
        private String assetId;
        private String toAddress;
        private BigDecimal amount;
        private ScriptHash fromContractScriptHash;

        public Builder(Neow3j neow3j) {
            this.neow3j = neow3j;
            this.outputs = new ArrayList<>();
            this.inputs = new ArrayList<>();
            this.attributes = new ArrayList<>();
            this.witnesses = new ArrayList<>();
            this.networkFee = BigDecimal.ZERO;
            this.inputCalculationStrategy = InputCalculationStrategy.DEFAULT_INPUT_CALCULATION_STRATEGY;
        }

        public Builder account(Account account) {
            this.account = account;
            return this;
        }

        public Builder fromContract(ScriptHash contractScriptHash) {
            this.fromContractScriptHash = contractScriptHash;
            return this;
        }

        public Builder output(RawTransactionOutput output) {
            throwIfSingleOutputIsUsed();
            this.outputs.add(output);
            return this;
        }

        public Builder outputs(List<RawTransactionOutput> outputs) {
            throwIfSingleOutputIsUsed();
            this.outputs.addAll(outputs);
            return this;
        }

        public Builder inputs(List<RawTransactionInput> inputs) {
            // TODO Claude 19.06.19:
            // Remove exception when inputs are handled correctly in transaction building.
            throw new UnsupportedOperationException();
            // this.inputs.addAll(inputs); return this;
        }

        public Builder input(RawTransactionInput input) {
            // TODO Claude 19.06.19:
            // Remove exception when inputs are handled correctly in transaction building.
            throw new UnsupportedOperationException();
            // this.inputs.add(input); return this;
        }

        public Builder witness(RawScript script) {
            this.witnesses.add(script);
            return this;
        }

        public Builder asset(String assetId) {
            throwIfOutputsAreSet();
            this.assetId = assetId;
            return this;
        }

        public Builder toAddress(String address) {
            throwIfOutputsAreSet();
            this.toAddress = address;
            return this;
        }

        /**
         * Specifies the asset amount to spend in the transfer.
         *
         * @param amount The amount to transfer.
         * @return this Builder object.
         * @deprecated Use {@link Builder#amount(double)} or {@link Builder#amount(String)}
         * instead.
         */
        @Deprecated
        public Builder amount(BigDecimal amount) {
            throwIfOutputsAreSet();
            this.amount = amount;
            return this;
        }

        /**
         * Specifies the asset amount to spend in the transfer.
         *
         * @param amount The amount to transfer.
         * @return this Builder object.
         */
        public Builder amount(String amount) {
            throwIfOutputsAreSet();
            this.amount = new BigDecimal(amount);
            return this;
        }

        /**
         * Specifies the asset amount to spend in the transfer.
         *
         * @param amount The amount to transfer.
         * @return this Builder object.
         */
        public Builder amount(double amount) {
            return amount(Double.toString(amount));
        }

        public Builder attribute(RawTransactionAttribute attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public Builder attributes(List<RawTransactionAttribute> attributes) {
            this.attributes.addAll(attributes);
            return this;
        }

        /**
         * <p>Adds a network fee to the transfer.</p>
         * <br>
         * <p>Network fees add priority to a transaction and are paid in GAS. If a fee is added the
         * GAS will be taken from the account used in the asset transfer.</p>
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         * @deprecated Use {@link Builder#amount(String)} or {@link Builder#amount(double)} instead.
         */
        @Deprecated
        public Builder networkFee(BigDecimal networkFee) {
            this.networkFee = networkFee;
            return this;
        }

        /**
         * <p>Adds a network fee to the transfer.</p>
         * <br>
         * <p>Network fees add priority to a transaction and are paid in GAS. If a fee is added the
         * GAS will be taken from the account used in the asset transfer.</p>
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         */
        public Builder networkFee(String networkFee) {
            this.networkFee = new BigDecimal(networkFee);
            return this;
        }

        /**
         * Adds a network fee to the transfer.
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         * @see Builder#networkFee(String)
         */
        public Builder networkFee(double networkFee) {
            return networkFee(Double.toString(networkFee));
        }

        /**
         * Add the strategy that will be used to calculate the UTXOs used as transaction inputs.
         *
         * @param strategy The strategy to use.
         * @return this Builder object.
         */
        public Builder inputCalculationStrategy(InputCalculationStrategy strategy) {
            this.inputCalculationStrategy = strategy;
            return this;
        }

        public AssetTransfer build() {
            if (neow3j == null) throw new IllegalStateException("Neow3j not set");
            if (account == null) throw new IllegalStateException("Account not set");
            if (outputs.isEmpty()) {
                if (allSingleOutputAttributesSet()) {
                    outputs.add(new RawTransactionOutput(assetId, amount.toPlainString(), toAddress));
                } else {
                    throw new IllegalStateException("No or incomplete transaction outputs set");
                }
            }

            List<RawTransactionOutput> intents = new ArrayList<>(outputs);
            intents.addAll(createOutputsFromFees(networkFee));
            Map<String, BigDecimal> requiredAssets = calculateRequiredAssetsForIntents(intents);

            if (fromContractScriptHash == null) {
                handleNormalTransfer(requiredAssets);
            } else {
                handleTransferFromContract(requiredAssets);
            }

            this.tx = buildTransaction();

            return new AssetTransfer(this);
        }

        private void handleNormalTransfer(Map<String, BigDecimal> requiredAssets) {
            calculateInputsAndChange(requiredAssets, this.account);
        }

        private void handleTransferFromContract(Map<String, BigDecimal> requiredAssets) {
            Account contractAcct = Account.fromAddress(fromContractScriptHash.toAddress()).build();
            try {
                contractAcct.updateAssetBalances(neow3j);
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch UTXOs for the contract with " +
                        "script hash " + fromContractScriptHash.toString());
            }
            calculateInputsAndChange(requiredAssets, contractAcct);
            // Because in a transaction that withdraws from a contract address the transaction
            // inputs are coming from the contract, there are now inputs from the account that
            // initiates the transfer. Therefore it needs to be mentioned in an script attribute.
            attributes.add(new RawTransactionAttribute(
                    TransactionAttributeUsageType.SCRIPT, account.getScriptHash().toArray()));

            NeoGetContractState contractState;
            try {
                contractState = neow3j.getContractState(fromContractScriptHash.toString()).send();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int nrOfParams = contractState.getContractState().getContractParameters().size();
            byte[] invocationScript = Numeric.hexStringToByteArray(Strings.zeros(nrOfParams * 2));
            witnesses.add(new RawScript(invocationScript, fromContractScriptHash));
        }

        private void calculateInputsAndChange(Map<String, BigDecimal> requiredAssets, Account acct) {
            requiredAssets.forEach((assetId, requiredValue) -> {
                List<Utxo> utxos = acct.getUtxosForAssetAmount(assetId, requiredValue, inputCalculationStrategy);
                inputs.addAll(utxos.stream().map(Utxo::toTransactionInput).collect(Collectors.toList()));
                outputs.add(getChangeTransactionOutput(assetId, requiredValue, utxos, acct.getAddress()));
            });
        }

        private RawTransactionOutput getChangeTransactionOutput(String assetId,
                                                                BigDecimal requiredValue,
                                                                List<Utxo> utxos,
                                                                String changeAddress) {

            BigDecimal inputAmount = utxos.stream().map(Utxo::getValue).reduce(BigDecimal::add).get();
            if (inputAmount.compareTo(requiredValue) <= 0) {
                return null;
            }
            BigDecimal change = inputAmount.subtract(requiredValue);
            return new RawTransactionOutput(assetId, change.toPlainString(), changeAddress);
        }

        private ContractTransaction buildTransaction() {
            return new ContractTransaction.Builder()
                    .outputs(this.outputs)
                    .inputs(this.inputs)
                    .scripts(this.witnesses)
                    .attributes(this.attributes)
                    .build();
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

        private void throwIfOutputsAreSet() {
            if (!outputs.isEmpty()) {
                throw new IllegalStateException("Don't set transaction outputs and use the " +
                        "single output methods `asset()`, `toAddress()` and `amount()` " +
                        "simultaneously");
            }
        }

        private void throwIfSingleOutputIsUsed() {
            if (amount != null || toAddress != null || assetId != null) {
                throw new IllegalStateException("Don't set transaction outputs and use the " +
                        "single output methods `asset()`, `toAddress()` and `amount()` " +
                        "simultaneously");
            }
        }

        private boolean allSingleOutputAttributesSet() {
            return amount != null && toAddress != null && assetId != null;
        }
    }
}

