package io.neow3j.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.types.ContractParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to produce a JSON object from a transaction that is then used for signing in neo-cli.
 * The {@code ContractParametersContext} contains the script of a Neo transaction and witnesses for that transaction,
 * i.e., verification scripts and the corresponding signatures.
 */
public class ContractParametersContext {

    public ContractParametersContext(String hash, String data, Map<String, ContextItem> items, long network) {
        this.hash = hash;
        this.data = data;
        this.items = items == null ? new HashMap<>() : items;
        this.network = network;
    }

    /**
     * The type can be different in neo-core code, but, we only need the one for transactions here.
     */
    @JsonProperty
    private final String type = "Neo.Network.P2P.Payloads.Transaction";

    /**
     * The hash of the transaction.
     */
    @JsonProperty
    private String hash;

    /**
     * Base64 string of the transaction data without witnesses.
     */
    @JsonProperty
    private String data;

    /**
     * A mapping from the accounts to their witnesses.
     */
    @JsonProperty
    private Map<String, ContextItem> items;

    /**
     * The number of the network on which to operate, e.g., testnet.
     */
    @JsonProperty
    private long network;

    public static class ContextItem {

        public ContextItem(String script, List<ContractParameter> parameters, Map<String, String> signatures) {
            this.script = script;
            this.parameters = parameters;
            this.signatures = signatures == null ? new HashMap<>() : signatures;
        }

        /**
         * Base64 string of the verification script
         */
        @JsonProperty
        private String script;

        /**
         * The parameters passed to the script, i.e., the signatures passed to the verification script
         */
        @JsonProperty
        private List<ContractParameter> parameters;

        /**
         * Map of signing public key to their corresponding signature
         */
        @JsonProperty
        private Map<String, String> signatures;

        public String getScript() {
            return script;
        }

        public List<ContractParameter> getParameters() {
            return parameters;
        }

        public Map<String, String> getSignatures() {
            return signatures;
        }
    }

}
