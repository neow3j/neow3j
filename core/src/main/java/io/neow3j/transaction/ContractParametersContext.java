package io.neow3j.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.types.ContractParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class models a transaction signature context that can be exchanged with Neo CLI's {@code sign <json>} and
 * {@code relay <json>} commands, and with the RPC {@code sign} and {@code relay} methods.
 * <p>
 * The {@code ContractParametersContext} contains an unsigned Neo transaction, its hash, the target network, and one
 * context item per signer. Each context item contains the signer's verification script, the parameters required by
 * that script, and any signatures that have already been collected.
 * <p>
 * The JSON produced by Neo CLI for incomplete signature contexts, e.g., multi-signature transactions, can be
 * deserialized into this class and passed directly to the RPC {@code sign} method. Likewise, a context returned by
 * the RPC {@code sign} method can be serialized and passed back to Neo CLI's {@code sign <json>} command on another
 * node if more signatures are required. Once all required signatures are present, the completed context can be passed
 * to the RPC {@code relay} method or Neo CLI's {@code relay <json>} command to broadcast the transaction.
 * <p>
 * Example RPC request payload:
 * <pre>{@code
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "method": "sign",
 *   "params": [{
 *     "type": "Neo.Network.P2P.Payloads.Transaction",
 *     "hash": "A Hash256 transaction hash",
 *     "data": "A Base64-encoded serialized unsigned transaction",
 *     "items": {
 *       "0xScriptHash": {
 *         "script": "A Base64-encoded verification script",
 *         "parameters": [{ "type": "Signature", "value": null }],
 *         "signatures": { "Hex public key": "A Base64-encoded signature" }
 *       }
 *     },
 *     "network": 894710606
 *   }]
 * }
 * }</pre>
 * <p>
 * Example RPC response payload:
 * <pre>{@code
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "result": {
 *     "type": "Neo.Network.P2P.Payloads.Transaction",
 *     "hash": "A Hash256 transaction hash",
 *     "data": "A Base64-encoded serialized unsigned transaction",
 *     "items": {
 *       "0xScriptHash": {
 *         "script": "A Base64-encoded verification script",
 *         "parameters": [{ "type": "Signature", "value": null }],
 *         "signatures": { "Hex public key": "A Base64-encoded signature" }
 *       }
 *     },
 *     "network": 894710606
 *   }
 * }
 * }</pre>
 */
public class ContractParametersContext {

    @JsonProperty
    private final String type = "Neo.Network.P2P.Payloads.Transaction";

    @JsonProperty
    private String hash;

    @JsonProperty
    private String data;

    @JsonProperty
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, ContextItem> items;

    @JsonProperty
    private long network;

    public ContractParametersContext() {
        this.items = new HashMap<>();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractParametersContext)) {
            return false;
        }
        ContractParametersContext that = (ContractParametersContext) o;
        return getNetwork() == that.getNetwork() &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(getHash(), that.getHash()) &&
                Objects.equals(getData(), that.getData()) &&
                Objects.equals(getItems(), that.getItems());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getHash(), getData(), getItems(), getNetwork());
    }

    @Override
    public String toString() {
        return "ContractParametersContext{" +
                "type='" + type + '\'' +
                ", hash='" + hash + '\'' +
                ", data='" + data + '\'' +
                ", items=" + items +
                ", network=" + network +
                '}';
    }

    public static class ContextItem {

        @JsonProperty
        private String script;

        @JsonProperty
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<ContractParameter> parameters = new ArrayList<>();

        @JsonProperty
        private Map<String, String> signatures = new HashMap<>();

        public ContextItem() {
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContextItem)) {
                return false;
            }
            ContextItem that = (ContextItem) o;
            return Objects.equals(getScript(), that.getScript()) &&
                    Objects.equals(getParameters(), that.getParameters()) &&
                    Objects.equals(getSignatures(), that.getSignatures());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getScript(), getParameters(), getSignatures());
        }

        @Override
        public String toString() {
            return "ContextItem{" +
                    "script='" + script + '\'' +
                    ", parameters=" + parameters +
                    ", signatures=" + signatures +
                    '}';
        }
    }

}
