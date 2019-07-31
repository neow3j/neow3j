package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransactionAttribute;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.ContractTransaction;
import io.neow3j.utils.Numeric;
import io.neow3j.utils.Strings;
import io.neow3j.wallet.Balances.AssetBalance;
import io.neow3j.wallet.exceptions.InsufficientFundsException;

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

        public Builder amount(BigDecimal amount) {
            throwIfOutputsAreSet();
            this.amount = amount;
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

        /**
         * <p>Adds a network fee.</p>
         * <br>
         * <p>Network fees add priority to a transaction and are paid in GAS. If a fee is added the
         * GAS will be taken from the account used in the asset transfer.</p>
         *
         * @param networkFee The fee amount to add.
         * @return this Builder object.
         */
        public Builder networkFee(BigDecimal networkFee) {
            this.networkFee = networkFee;
            return this;
        }

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

            if (fromContractScriptHash != null) {
                handleTransferFromContract(requiredAssets);
            } else {
                calculateInputsAndChange(requiredAssets);
            }

            this.tx = buildTransaction();

            return new AssetTransfer(this);
        }

        private void handleTransferFromContract(Map<String, BigDecimal> requiredAssets) {
            NeoGetUnspents unspents = null;
            try {
                unspents = neow3j.getUnspents(fromContractScriptHash.toAddress()).send();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Map<String, AssetBalance> balances = getBalances(unspents.getUnspents());
            requiredAssets.forEach((reqAssetId, reqValue) -> {
                List<Utxo> utxos = getUtxosForAssetAmount(reqAssetId, reqValue, balances, inputCalculationStrategy);
                inputs.addAll(utxos.stream().map(Utxo::toTransactionInput).collect(Collectors.toList()));
                BigDecimal changeAmount = calculateChange(utxos, reqValue);
                if (changeAmount != null) outputs.add(
                        new RawTransactionOutput(reqAssetId, changeAmount.toPlainString(), fromContractScriptHash.toAddress()));
            });

            attributes.add(new RawTransactionAttribute(TransactionAttributeUsageType.SCRIPT, fromContractScriptHash.toArray()));
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

        private List<Utxo> getUtxosForAssetAmount(String assetId, BigDecimal amount,
                                                  Map<String, AssetBalance> balances,
                                                  InputCalculationStrategy strategy) {

            if (balances.get(assetId) == null) {
                throw new InsufficientFundsException("Balances do not contain the asset with ID " + assetId);
            }
            AssetBalance balance = balances.get(assetId);
            if (balance.getAmount().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Needed " + amount + " but only found " +
                        balance.getAmount() + " for asset with ID " + assetId);
            }
            return strategy.calculateInputs(balance.getUtxos(), amount);
        }

        private Map<String, AssetBalance> getBalances(NeoGetUnspents.Unspents unspents) {
            Map<String, AssetBalance> balances = new HashMap<>(unspents.getBalances().size());
            unspents.getBalances().forEach(b -> {
                List<Utxo> utxos = b.getUnspentTransactions().stream()
                        .map(utxo -> new Utxo(utxo.getTxId(), utxo.getIndex(), utxo.getValue()))
                        .collect(Collectors.toList());
                balances.put(b.getAssetHash(), new AssetBalance(utxos));
            });
            return balances;
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

