package io.neow3j.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;

/**
 * Covers parts of a neo-express configuration JSON file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoExpressConfig {

    @JsonProperty()
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<Wallet> wallets;

    @JsonProperty("consensus-nodes")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<ConsensusNode> consensusNodes;

    public NeoExpressConfig() {
    }

    public NeoExpressConfig(List<Wallet> wallets) {
        this.wallets = wallets;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public List<ConsensusNode> getConsensusNodes() {
        return consensusNodes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConsensusNode {

        private Wallet wallet;

        public ConsensusNode() {
        }

        public ConsensusNode(Wallet wallet) {
            this.wallet = wallet;
        }

        public Wallet getWallet() {
            return wallet;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wallet {

        @JsonProperty
        private String name;

        @JsonProperty
        private List<Account> accounts;

        public Wallet() {
        }

        public Wallet(String name, List<Account> accounts) {
            this.name = name;
            this.accounts = accounts;
        }

        public String getName() {
            return name;
        }

        public List<Account> getAccounts() {
            return accounts;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Account {

            @JsonProperty("private-key")
            private String privateKey;

            @JsonProperty("script-hash")
            private String scriptHash;

            @JsonProperty
            private String label;

            @JsonProperty("is-default")
            private boolean isDefault;

            @JsonPropertyOrder
            private Contract contract;

            public Account() {
            }

            public Account(String privateKey, String scriptHash, String label, boolean isDefault, Contract contract) {
                this.privateKey = privateKey;
                this.scriptHash = scriptHash;
                this.label = label;
                this.isDefault = isDefault;
                this.contract = contract;
            }

            public String getPrivateKey() {
                return privateKey;
            }

            public String getScriptHash() {
                return scriptHash;
            }

            public String getLabel() {
                return label;
            }

            public boolean isDefault() {
                return isDefault;
            }

            public Contract getContract() {
                return contract;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Contract {

                @JsonProperty
                private String script;

                @JsonProperty
                private List<String> parameters;

                public Contract() {
                }

                public Contract(String script, List<String> parameters) {
                    this.script = script;
                    this.parameters = parameters;
                }

                public String getScript() {
                    return script;
                }

                public List<String> getParameters() {
                    return parameters;
                }

            }

        }

    }

}
