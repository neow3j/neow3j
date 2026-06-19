package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.Objects;

public class NeoVerifyMessage extends Response<NeoVerifyMessage.VerifiedMessage> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public VerifiedMessage getVerifiedMessage() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifiedMessage {

        @JsonProperty("address")
        private String address;

        @JsonProperty("publickey")
        private String publicKey;

        @JsonProperty("signature")
        private String signature;

        @JsonProperty("salt")
        private String salt;

        @JsonProperty("status")
        private String status;

        public VerifiedMessage() {
        }

        public VerifiedMessage(String address, String publicKey, String signature, String salt, String status) {
            this.address = address;
            this.publicKey = publicKey;
            this.signature = signature;
            this.salt = salt;
            this.status = status;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isValid() {
            return "Valid".equals(status);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof VerifiedMessage)) {
                return false;
            }
            VerifiedMessage that = (VerifiedMessage) o;
            return Objects.equals(getAddress(), that.getAddress()) &&
                    Objects.equals(getPublicKey(), that.getPublicKey()) &&
                    Objects.equals(getSignature(), that.getSignature()) &&
                    Objects.equals(getSalt(), that.getSalt()) &&
                    Objects.equals(getStatus(), that.getStatus());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAddress(), getPublicKey(), getSignature(), getSalt(), getStatus());
        }

        @Override
        public String toString() {
            return "VerifiedMessage{" +
                    "address='" + address + '\'' +
                    ", publicKey='" + publicKey + '\'' +
                    ", signature='" + signature + '\'' +
                    ", salt='" + salt + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }

    }

}
