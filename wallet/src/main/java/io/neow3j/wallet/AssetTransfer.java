package io.neow3j.wallet;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Keys;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.transaction.RawInvocationScript;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.crypto.transaction.RawVerificationScript;
import io.neow3j.model.types.GASAsset;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.transaction.ContractTransaction;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Balances.AssetBalance;
import io.neow3j.wallet.exceptions.InsufficientFundsException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssetTransfer {

    private Neow3j neow3j;
    private ContractTransaction tx;

    private AssetTransfer(Builder builder) {
        this.neow3j = builder.neow3j;
        this.tx = builder.tx;
    }

    public ContractTransaction getTransaction() {
        return tx;
    }

    public void addWitness(List<RawInvocationScript> invocationScripts,
                           RawVerificationScript verificationScript) {

        tx.addScript(invocationScripts, verificationScript);
    }

    public ContractTransaction send() throws IOException, ErrorResponseException {
        String rawTx = Numeric.toHexStringNoPrefix(tx.toArray());
        NeoSendRawTransaction response = neow3j.sendRawTransaction(rawTx).send();
        response.throwOnError();
        return tx;
    }


    public static class Builder {

        private Neow3j neow3j;
        private Account account;
        private BigDecimal fee;
        private List<RawTransactionOutput> outputs;
        private List<RawTransactionInput> inputs;
        private InputCalculationStrategy inputCalculationStrategy;
        private ContractTransaction tx;
        private boolean signManually;

        public Builder() {
            outputs = new ArrayList<>();
            inputs = new ArrayList<>();
            inputCalculationStrategy = InputCalculationStrategy.DEFAULT_INPUT_CALCULATION_STRATEGY;
            signManually = false;
        }

        public Builder neow3j(Neow3j neow3j) {
            this.neow3j = neow3j;
            return this;
        }

        public Builder account(Account account) {
            this.account = account;
            return this;
        }

        public Builder output(RawTransactionOutput output) {
            this.outputs.add(output);
            return this;
        }

        public Builder outputs(List<RawTransactionOutput> outputs) {
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

        public Builder fee(BigDecimal fee) {
            this.fee = fee;
            return this;
        }

        public Builder inputCalculationStrategy(InputCalculationStrategy strategy) {
            this.inputCalculationStrategy = strategy;
            return this;
        }

        public Builder signManually() {
            this.signManually = true;
            return this;
        }

        public AssetTransfer build() {
            if (neow3j == null) throw new IllegalStateException("Neow3j not set");
            if (outputs == null || outputs.isEmpty())
                throw new IllegalStateException("No outputs set");
            if (account == null) throw new IllegalStateException("Account not set");
            Map<String, BigDecimal> requiredAssets = calculateRequiredAssets();
            if (!inputs.isEmpty()) {
                // TODO Claude 19.06.19:
                // Try building the transaction with given inputs. Calculate change only. Fee must be
                // covered by the given inputs as well.
            } else {
                calculateInputsAndChange(requiredAssets);
            }

            this.tx = new ContractTransaction.Builder()
                    .outputs(outputs)
                    .inputs(inputs)
                    .build();

            // TODO Claude 19.06.19:
            // Cover other possible scenarios. E.g. verification script is given in the mutli-sig
            // account's contract and can be added automatically.
            if (!signManually) {
                createAndAddWitness();
            }
            return new AssetTransfer(this);
        }

        private Map<String, BigDecimal> calculateRequiredAssets() {
            // Create a new list of outputs, because the fee ouput is only used for calculations but
            // must not appear in the final transaction as an output.
            List<RawTransactionOutput> intendedOutputs = new ArrayList<>(outputs);
            if (fee != null && fee.compareTo(BigDecimal.ZERO) > 0) {
                intendedOutputs.add(new RawTransactionOutput(GASAsset.HASH_ID, fee.toPlainString(), null));
            }

            Map<String, BigDecimal> assets = new HashMap<>();
            intendedOutputs.forEach(o -> {
                BigDecimal value = new BigDecimal(o.getValue());
                if (assets.containsKey(o.getAssetId())) {
                    value = assets.get(o.getAssetId()).add(value);
                }
                assets.put(o.getAssetId(), value);
            });
            return assets;
        }

        private void calculateInputsAndChange(Map<String, BigDecimal> requiredAssets) {
            requiredAssets.forEach((reqAssetId, reqValue) -> {
                List<Utxo> utxos = fetchUtxosForAsset(reqAssetId, reqValue);
                inputs.addAll(utxos.stream().map(Utxo::toTransactionInput).collect(Collectors.toList()));
                BigDecimal changeAmount = calculcateChange(utxos, reqValue);
                if (changeAmount != null) outputs.add(
                        new RawTransactionOutput(reqAssetId, changeAmount.toPlainString(), account.getAddress()));
            });
        }

        private List<Utxo> fetchUtxosForAsset(String assetId, BigDecimal assetValue) {
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
            return inputCalculationStrategy.calculateInputs(balance.getUtxos(), assetValue);
        }

        private BigDecimal calculcateChange(List<Utxo> utxos, BigDecimal reqValue) {
            BigDecimal inputAmount = utxos.stream().map(Utxo::getValue).reduce(BigDecimal::add).get();
            if (inputAmount.compareTo(reqValue) > 0) {
                return inputAmount.subtract(reqValue);
            }
            return null;
        }

        public void createAndAddWitness() {
            if (account.getPrivateKey() == null) {
                throw new IllegalStateException("Account does not hold a decrypted private key " +
                        "for signing the transaction. Either add the private key to the account " +
                        "or create the transaction signature manually.");
            }
            byte[] rawUnsignedTx = tx.toArray();
            ECKeyPair keyPair = account.getECKeyPair();
            tx.addScript(
                    Arrays.asList(new RawInvocationScript(Sign.signMessage(rawUnsignedTx, keyPair))),
                    Keys.getVerificationScriptFromPublicKey(account.getPublicKey())
            );
        }
    }
}

