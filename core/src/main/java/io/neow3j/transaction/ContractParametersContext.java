package io.neow3j.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.ContractParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to produce a JSON object from a transaction that is then used for signing in neo-cli. The
 * {@code ContractParametersContext} contains the script of a Neo transaction and witnesses for that transaction,
 * i.e., verification scripts and the corresponding signatures.
 */
public class ContractParametersContext {

    @JsonProperty
    private final String type = "Neo.Network.P2P.Payloads.Transaction";

    @JsonProperty
    private String hash;

    @JsonProperty
    private String data;

    @JsonProperty
    private Map<String, ContextItem> items;

    @JsonProperty
    private long network;

    public ContractParametersContext(String hash, String data, Map<String, ContextItem> items, long network) {
        this.hash = hash;
        this.data = data;
        this.items = items == null ? new HashMap<>() : items;
        this.network = network;
    }

    /**
     * @return the type of parameter context, which is always {@code Neo.Network.P2P.Payloads.Transaction} for
     * transaction objects.
     */
    public String getType() {
        return type;
    }

    /**
     * @return the hash of the transaction.
     */
    public String getHash() {
        return hash;
    }

    /**
     * @return the Base64 string of the transaction data without witnesses.
     */
    public String getData() {
        return data;
    }

    /**
     * @return a mapping from the accounts (script hash) to their witnesses. These are the accounts used as signers on
     * the transaction.
     */
    public Map<String, ContextItem> getItems() {
        return items;
    }

    /**
     * @return the number of the network on which to operate, e.g., testnet.
     */
    public long getNetwork() {
        return network;
    }

    public static class ContextItem {

        @JsonProperty
        private String script;

        @JsonProperty
        private List<ContractParameter> parameters;

        @JsonProperty
        private Map<String, String> signatures;

        public ContextItem(String script, List<ContractParameter> parameters, Map<String, String> signatures) {
            this.script = script;
            this.parameters = parameters;
            this.signatures = signatures == null ? new HashMap<>() : signatures;
        }

        /**
         * @return the Base64 string of the verification script.
         */
        public String getScript() {
            return script;
        }

        /**
         * @return the parameters passed to the script, i.e., the signatures passed to the verification script.
         */
        public List<ContractParameter> getParameters() {
            return parameters;
        }

        /**
         * @return a Map of the signing public keys mapped to their corresponding signatures.
         */
        public Map<String, String> getSignatures() {
            return signatures;
        }
    }

}
