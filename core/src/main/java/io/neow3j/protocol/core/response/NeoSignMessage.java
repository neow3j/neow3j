package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeoSignMessage extends Response<NeoSignMessage.SignedMessage> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public SignedMessage getSignedMessage() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SignedMessage {

        @JsonProperty("curve")
        private String curve;

        @JsonProperty("algorithm")
        private String algorithm;

        @JsonProperty("mode")
        private String mode;

        @JsonProperty("payload")
        private String payload;

        @JsonProperty("signatures")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        private List<MessageSignature> signatures = new ArrayList<>();

        public SignedMessage() {
        }

        public SignedMessage(String curve, String algorithm, String mode, String payload,
                List<MessageSignature> signatures) {
            this.curve = curve;
            this.algorithm = algorithm;
            this.mode = mode;
            this.payload = payload;
            this.signatures = signatures;
        }

        public String getCurve() {
            return curve;
        }

        public void setCurve(String curve) {
            this.curve = curve;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }

        public List<MessageSignature> getSignatures() {
            return signatures;
        }

        public void setSignatures(List<MessageSignature> signatures) {
            this.signatures = signatures;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SignedMessage)) {
                return false;
            }
            SignedMessage that = (SignedMessage) o;
            return Objects.equals(getCurve(), that.getCurve()) &&
                    Objects.equals(getAlgorithm(), that.getAlgorithm()) &&
                    Objects.equals(getMode(), that.getMode()) &&
                    Objects.equals(getPayload(), that.getPayload()) &&
                    Objects.equals(getSignatures(), that.getSignatures());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCurve(), getAlgorithm(), getMode(), getPayload(), getSignatures());
        }

        @Override
        public String toString() {
            return "SignedMessage{" +
                    "curve='" + curve + '\'' +
                    ", algorithm='" + algorithm + '\'' +
                    ", mode='" + mode + '\'' +
                    ", payload='" + payload + '\'' +
                    ", signatures=" + signatures +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MessageSignature {

            @JsonProperty("address")
            private String address;

            @JsonProperty("publickey")
            private String publicKey;

            @JsonProperty("signature")
            private String signature;

            @JsonProperty("salt")
            private String salt;

            public MessageSignature() {
            }

            public MessageSignature(String address, String publicKey, String signature, String salt) {
                this.address = address;
                this.publicKey = publicKey;
                this.signature = signature;
                this.salt = salt;
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getPublicKey() {
                return publicKey;
            }

            public void setPublicKey(String publicKey) {
                this.publicKey = publicKey;
            }

            public String getSignature() {
                return signature;
            }

            public void setSignature(String signature) {
                this.signature = signature;
            }

            public String getSalt() {
                return salt;
            }

            public void setSalt(String salt) {
                this.salt = salt;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof MessageSignature)) {
                    return false;
                }
                MessageSignature that = (MessageSignature) o;
                return Objects.equals(getAddress(), that.getAddress()) &&
                        Objects.equals(getPublicKey(), that.getPublicKey()) &&
                        Objects.equals(getSignature(), that.getSignature()) &&
                        Objects.equals(getSalt(), that.getSalt());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getAddress(), getPublicKey(), getSignature(), getSalt());
            }

            @Override
            public String toString() {
                return "MessageSignature{" +
                        "address='" + address + '\'' +
                        ", publicKey='" + publicKey + '\'' +
                        ", signature='" + signature + '\'' +
                        ", salt='" + salt + '\'' +
                        '}';
            }

        }

    }

}
