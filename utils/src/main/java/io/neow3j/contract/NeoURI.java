package io.neow3j.contract;

import io.neow3j.utils.AddressUtils;
import io.neow3j.utils.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoURI {

    private String uri;

    protected NeoURI(Builder builder) {
        this.uri = builder.uri;
    }

    public String getUri() {
        return uri;
    }

    public static class Builder {
        private String scheme;
        private String address;

        private Map<String, String> query;
        private List<String> hashs;
        private List<String> remarks;

        private String uri;

        public Builder() {
            this.scheme = "neo";
            this.query = new HashMap<>();
            this.hashs = new ArrayList<>();
            this.remarks = new ArrayList<>();
        }

        public Builder toAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder asset(String asset) {
            this.query.put("asset", asset);
            return this;
        }

        public Builder amount(String amount) {
            // TODO: 20.04.20 Michael: handle decimals if needed.
            this.query.put("amount", amount);
            return this;
        }

        public Builder amount(int amount) {
            this.query.put("amount", String.valueOf((double) amount));
            return this;
        }

        public Builder contractHash(String contractHash) {
            this.query.put("contractHash", contractHash);
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

        // TODO: 20.04.20 Michael: add hashs and remarks to URI
        public Builder customHash(String hash) {
            if (this.hashs.size() <= 15) {
                this.hashs.add(hash);
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
            if (!AddressUtils.isValidAddress(this.address)) {
                throw new IllegalStateException("Invalid address used.");
            }

            String basePart = buildBasePart();
            String queryPart = buildQueryPart();

            if (queryPart.isEmpty()) {
                this.uri = basePart;
            } else {
                this.uri = basePart + "?" + queryPart;
            }

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

            return Strings.join(query, "&");
        }

        public static boolean isNotEmptyAndNotNull(String str) {
            return str != null && !str.isEmpty();
        }
    }
}
