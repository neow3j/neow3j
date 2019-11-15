package io.neow3j.contract;

import io.neow3j.contract.abi.NeoABIUtils;
import io.neow3j.contract.abi.exceptions.NEP3Exception;
import io.neow3j.contract.abi.model.NeoContractInterface;
import io.neow3j.transaction.RawScript;
import io.neow3j.transaction.RawTransactionInput;
import io.neow3j.transaction.RawTransactionOutput;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.GASAsset;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.transaction.InvocationTransaction;
import io.neow3j.utils.Numeric;
import io.neow3j.utils.TransactionUtils;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.InputCalculationStrategy;
import io.neow3j.wallet.Utxo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContractDeployment {

    private static final Logger LOG = LoggerFactory.getLogger(ContractDeployment.class);

    private Neow3j neow3j;
    private Account account;
    private NeoContractInterface abi;
    private InvocationTransaction tx;
    private ContractDeploymentScript deploymentScript;

    private ContractDeployment(final Builder builder) {
        this.neow3j = builder.neow3j;
        this.account = builder.account;
        this.deploymentScript = builder.deploymentScript;
        this.abi = builder.abi;
        this.tx = builder.tx;
    }

    /**
     * Signs the transaction in its current state with the private key of the account that was
     * added.
     *
     * @return this invocation object, updated with a witness.
     * @throws IllegalStateException If this ContractDeployment was constructed without an account
     *                               or if the account does not provide a decrypted private key.
     */
    public ContractDeployment sign() {
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
     * <p>Sends the serialized transaction to the RPC node (synchronous).</p>
     * <br>
     * <p>Before calling this method you should make sure that the transaction is signed either by
     * calling {@link ContractDeployment#sign()}} to automatically sign or by adding a custom
     * witness with {@link ContractDeployment#addWitness(RawScript)}.</p>
     *
     * @return the contract that has been deployed.
     * @throws IOException            if a connection problem with the RPC node arises.
     * @throws ErrorResponseException if the execution of the deployment lead to an error on the RPC
     *                                node.
     */
    public Contract deploy() throws IOException, ErrorResponseException {
        String rawTx = Numeric.toHexStringNoPrefix(tx.toArray());
        NeoSendRawTransaction response = neow3j.sendRawTransaction(rawTx).send();
        response.throwOnError();
        return new Contract(this.deploymentScript, this.abi);
    }

    /**
     * Adds the given witness to the transaction.
     *
     * @param witness The witness to be added.
     * @return this.
     */
    public ContractDeployment addWitness(RawScript witness) {
        tx.addScript(witness);
        return this;
    }

    public InvocationTransaction getTransaction() {
        return this.tx;
    }

    public static class Builder {

        private Neow3j neow3j;
        private Account account;
        private String name;
        private String version;
        private String author;
        private String email;
        private String description;
        private List<ContractParameterType> parameters;
        private ContractParameterType returnType;
        private boolean needsStorage;
        private boolean needsDynamicInvoke;
        private boolean isPayable;
        private byte[] scriptBinary;
        private NeoContractInterface abi;
        private InputCalculationStrategy inputCalculationStrategy;
        private BigDecimal networkFee;
        private ContractDeploymentScript deploymentScript;
        private InvocationTransaction tx;

        public Builder(final Neow3j neow3j) {
            this.neow3j = neow3j;
            this.parameters = new ArrayList<>();
            this.inputCalculationStrategy = InputCalculationStrategy.DEFAULT_STRATEGY;
            this.networkFee = BigDecimal.ZERO;
            this.name = "";
            this.version = "";
            this.author = "";
            this.email = "";
            this.description = "";
        }

        public Builder account(Account account) {
            this.account = account;
            return this;
        }

        public Builder loadAVMFile(String absoluteFileName) throws IOException {
            return loadAVMFile(new File(absoluteFileName));
        }

        public Builder loadAVMFile(File source) throws IOException {
            return loadAVMFile(new FileInputStream(source));
        }

        public Builder loadAVMFile(InputStream source) throws IOException {
            this.scriptBinary = new byte[source.available()];
            source.read(scriptBinary);
            return this;
        }

        public Builder loadABIFile(String absoluteFileName) throws NEP3Exception, IOException {
            return loadABIFile(new File(absoluteFileName));
        }

        public Builder loadABIFile(File source) throws NEP3Exception, IOException {
            return loadABIFile(new FileInputStream(source));
        }

        public Builder loadABIFile(InputStream source) throws NEP3Exception {
            this.abi = NeoABIUtils.loadABIFile(source);
            return this;
        }

        public Builder needsStorage(boolean needsStorage) {
            this.needsStorage = needsStorage;
            return this;
        }

        public Builder needsStorage() {
            return needsStorage(true);
        }

        public Builder needsDynamicInvoke(boolean needsDynamicInvoke) {
            this.needsDynamicInvoke = needsDynamicInvoke;
            return this;
        }

        public Builder needsDynamicInvoke() {
            return needsDynamicInvoke(true);
        }

        public Builder isPayable(boolean isPayable) {
            this.isPayable = isPayable;
            return this;
        }

        public Builder isPayable() {
            return isPayable(true);
        }

        public Builder parameter(ContractParameterType parameterType) {
            return parameters(parameterType);
        }

        public Builder parameters(List<ContractParameterType> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder parameters(ContractParameterType... parameters) {
            return parameters(Arrays.asList(parameters));
        }

        public Builder returnType(ContractParameterType returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Adds the strategy that will be used to determine which UTXOs should be used as
         * transaction inputs.
         *
         * @param strategy The strategy to use.
         * @return this Builder object.
         */
        public Builder inputCalculationStrategy(InputCalculationStrategy strategy) {
            this.inputCalculationStrategy = strategy;
            return this;
        }

        /**
         * <p>Adds a network fee.</p>
         * <br>
         * <p>The network fee (measured in GAS) can be used to add priority to a transaction. It is
         * required for a successful transaction if the transaction is larger than 1024 bytes.
         * Use {@link TransactionUtils#calcNecessaryNetworkFee(int)} to calculate the necessary
         * network fee for large transactions.</p>
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
         * @see ContractDeployment.Builder#networkFee(String)
         */
        public Builder networkFee(double networkFee) {
            return networkFee(Double.toString(networkFee));
        }

        public ContractDeployment build() {
            if (this.neow3j == null) {
                throw new IllegalStateException("Neow3j not set.");
            }
            if (this.account == null) {
                throw new IllegalStateException("Account not set.");
            }
            if (this.scriptBinary == null) {
                throw new IllegalStateException("AVM script binary not set.");
            }
            ContractDescriptionProperties cdp = new ContractDescriptionProperties(
                    this.name, this.version, this.author, this.email, this.description);
            ContractFunctionProperties cfp = new ContractFunctionProperties(
                    this.parameters, this.returnType, this.needsStorage, this.needsDynamicInvoke, this.isPayable);
            this.deploymentScript = new ContractDeploymentScript(this.scriptBinary, cfp, cdp);

            BigDecimal systemFee = this.deploymentScript.getDeploymentSystemFee();

            Map<String, BigDecimal> requiredAssets = calculateRequiredAssetsForIntents(null, systemFee, networkFee);

            List<RawTransactionInput> inputs = new ArrayList<>();
            List<RawTransactionOutput> outputs = new ArrayList<>();

            if (!requiredAssets.isEmpty()) {
                if (this.account == null)
                    throw new IllegalStateException("No account set but needed " +
                            "for fetching transaction inputs.");

                requiredAssets.forEach((reqAssetId, reqValue) -> {
                    List<Utxo> utxos = this.account.getUtxosForAssetAmount(reqAssetId, reqValue, inputCalculationStrategy);
                    inputs.addAll(utxos.stream().map(Utxo::toTransactionInput).collect(Collectors.toList()));
                    BigDecimal changeAmount = calculateChange(utxos, reqValue);
                    if (changeAmount != null) outputs.add(
                            new RawTransactionOutput(reqAssetId, changeAmount.toPlainString(), this.account.getAddress()));
                });
            }

            this.tx = new InvocationTransaction.Builder()
                    .outputs(outputs)
                    .inputs(inputs)
                    .systemFee(systemFee)
                    .contractScript(deploymentScript.toArray())
                    .build();

            return new ContractDeployment(this);
        }

        private Map<String, BigDecimal> calculateRequiredAssetsForIntents(
                List<RawTransactionOutput> outputs, BigDecimal... fees) {

            List<RawTransactionOutput> intents = outputs == null ? new ArrayList<>() : new ArrayList<>(outputs);
            intents.addAll(createOutputsFromFees(fees));
            Map<String, BigDecimal> assets = new HashMap<>();
            intents.forEach(output -> {
                BigDecimal value = new BigDecimal(output.getValue());
                if (assets.containsKey(output.getAssetId())) {
                    value = assets.get(output.getAssetId()).add(value);
                }
                assets.put(output.getAssetId(), value);
            });
            return assets;
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

        private static BigDecimal calculateChange(List<Utxo> utxos, BigDecimal reqValue) {
            BigDecimal inputAmount = utxos.stream().map(Utxo::getValue).reduce(BigDecimal::add).get();
            if (inputAmount.compareTo(reqValue) > 0) {
                return inputAmount.subtract(reqValue);
            }
            return null;
        }
    }

}
