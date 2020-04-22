package io.neow3j.contract;

import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoURI {

    private URI uri;

    private String address;
    private Map<String, String> query;
    private List<String> hashes;
    private List<String> remarks;

    protected NeoURI(Builder builder) {
        this.uri = builder.uri;

        this.address = builder.address;
        this.query = builder.query;
        this.hashes = builder.hashes;
        this.remarks = builder.remarks;
    }

    private NeoURI(String uriString) {
        this.uri = URI.create(uriString);
        // TODO: 22.04.20 Michael: make individual parts better accessible for better handling in using classes.
        //  Getter for every individual part?
    }

    public static NeoURI fromURI(String uriString) {
        return new NeoURI(uriString);
    }

    public String getUriAsString() {
        return this.uri.toString();
    }

    public URI getUri() {
        return this.uri;
    }

    public ScriptHash getAddress() {
        return ScriptHash.fromAddress(this.address);
    }

    public ScriptHash getAsset() {
        String asset = this.query.get("asset");
        return new ScriptHash(asset);
    }

    public BigDecimal getAmount() {
        String amount = this.query.get("amount");
        return new BigDecimal(amount);
    }

    public List<String> getHashes() {
        return this.hashes;
    }

    public List<String> getRemarks() {
        return this.remarks;
    }

    public static class Builder {
        private String scheme;
        private String address;

        private Map<String, String> query;
        private List<String> hashes;
        private List<String> remarks;

        private URI uri;

        public Builder() {
            this.scheme = "neo";
            this.query = new HashMap<>();
            this.hashes = new ArrayList<>();
            this.remarks = new ArrayList<>();
        }

        public Builder toAddress(String address) {
            if (!AddressUtils.isValidAddress(address)) {
                throw new IllegalStateException("Invalid address used.");
            }

            this.address = address;
            return this;
        }

        public Builder asset(String asset) {
            this.query.put("asset", asset);
            return this;
        }

        public Builder asset(ScriptHash asset) {
            this.query.put("asset", asset.toString());
            return this;
        }

        public Builder amount(String amount) {
            // TODO: 20.04.20 Michael: handle decimals if needed.
            this.query.put("amount", amount);
            return this;
        }

        public Builder amount(Integer amount) {
            this.query.put("amount", Integer.toString(amount));
            return this;
        }

        public Builder amount(BigInteger amount) {
            this.query.put("amount", amount.toString());
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.query.put("amount", amount.toString());
            return this;
        }

        public Builder contractHash(String contractHash) {
            this.query.put("contractHash", contractHash);
            return this;
        }

        public Builder contractHash(ScriptHash contractHash) {
            this.query.put("contractHash", contractHash.toString());
            return this;
        }

        public Builder ecdh02(String publicKey) {
            this.query.put("ecdh02", publicKey);
            return this;
        }

        public Builder ecdh03(String publicKey) {
            this.query.put("ecdh03", publicKey);
            return this;
        }

        public Builder script(String script) {
            this.query.put("script", script);
            return this;
        }

        public Builder vote(String vote) {
            this.query.put("vote", vote);
            return this;
        }

        public Builder certUrl(String certUrl) {
            this.query.put("certUrl", certUrl);
            return this;
        }

        public Builder descriptionUrl(String descriptionUrl) {
            this.query.put("descriptionUrl", descriptionUrl);
            return this;
        }

        public Builder description(String description) {
            this.query.put("description", description);
            return this;
        }

        // TODO: 20.04.20 Michael: add hashes and remarks to URI
        public Builder customHash(String hash) {
            if (this.hashes.size() <= 15) {
                this.hashes.add(hash);
            }
            return this;
        }

        public Builder remark(String remark) {
            if (this.remarks.size() <= 15) {
                this.remarks.add(remark);
            }
            return this;
        }

        public NeoURI build() {
            if (this.scheme == null) {
                throw new IllegalStateException("Scheme not set.");
            }
            if (this.address == null) {
                throw new IllegalStateException("Address not set.");
            }

            String basePart = buildBasePart();
            String queryPart = buildQueryPart();

            String uriString;
            if (queryPart.isEmpty()) {
                uriString = basePart;
            } else {
                uriString = basePart + "?" + queryPart;
            }

            // Create a URI object from the generated URI string
            this.uri = URI.create(uriString);

            return new NeoURI(this);
        }

        private String buildBasePart() {
            return this.scheme + ":" + this.address;
        }

        private String buildQueryPart() {
            List<String> query = new ArrayList<>();

            if (isNotEmptyAndNotNull(this.query.get("asset"))) {
                query.add("asset=" + this.query.get("asset"));
            }
            if (isNotEmptyAndNotNull(this.query.get("amount"))) {
                query.add("amount=" + this.query.get("amount"));
            }
            if (isNotEmptyAndNotNull(this.query.get("contractHash"))) {
                query.add("contractHash=" + this.query.get("contractHash"));
            }
            if (isNotEmptyAndNotNull(this.query.get("ecdh02"))) {
                query.add("ecdh02=" + this.query.get("ecdh02"));
            }
            if (isNotEmptyAndNotNull(this.query.get("ecdh03"))) {
                query.add("ecdh03=" + this.query.get("ecdh03"));
            }
            if (isNotEmptyAndNotNull(this.query.get("script"))) {
                query.add("script=" + this.query.get("script"));
            }
            if (isNotEmptyAndNotNull(this.query.get("vote"))) {
                query.add("vote=" + this.query.get("vote"));
            }
            if (isNotEmptyAndNotNull(this.query.get("certUrl"))) {
                query.add("certUrl=" + this.query.get("certUrl"));
            }
            if (isNotEmptyAndNotNull(this.query.get("descriptionUrl"))) {
                query.add("descriptionUrl=" + this.query.get("descriptionUrl"));
            }
            if (isNotEmptyAndNotNull(this.query.get("description"))) {
                query.add("description=" + this.query.get("description"));
            }

            for (int i = 0; i < this.hashes.size(); i++) {
                int hashNr = i+1;
                query.add("hash"+hashNr+"="+this.hashes.get(i));
            }

            for (int i = 0; i < this.remarks.size(); i++) {
                if (i==0) {
                    query.add("remark="+this.remarks.get(0));
                } else {
                    int remarkNr = i + 1;
                    query.add("remark"+remarkNr+"="+this.remarks.get(i));
                }
            }


            return Strings.join(query, "&");
        }

        public static boolean isNotEmptyAndNotNull(String str) {
            return str != null && !str.isEmpty();
        }
    }
}
