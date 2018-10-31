package com.axlabs.neow3j.protocol.core.methods.response;

import com.axlabs.neow3j.protocol.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class NeoGetValidators extends Response<List<NeoGetValidators.Validator>> {

    public List<Validator> getValidators() {
        return getResult();
    }

    public static class Validator {

        @JsonProperty("publickey")
        private String publicKey;

        @JsonProperty("votes")
        private String votes;

        @JsonProperty("active")
        private Boolean active;

        public Validator() {
        }

        public Validator(String publicKey, String votes, Boolean active) {
            this.publicKey = publicKey;
            this.votes = votes;
            this.active = active;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getVotes() {
            return votes;
        }

        public BigInteger getVotesAsBigInteger() {
            return new BigInteger(votes);
        }

        public void setVotes(String votes) {
            this.votes = votes;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Validator)) return false;
            Validator validator = (Validator) o;
            return Objects.equals(getPublicKey(), validator.getPublicKey()) &&
                    Objects.equals(getVotes(), validator.getVotes()) &&
                    Objects.equals(getActive(), validator.getActive());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPublicKey(), getVotes(), getActive());
        }

        @Override
        public String toString() {
            return "Validator{" +
                    "publicKey='" + publicKey + '\'' +
                    ", votes='" + votes + '\'' +
                    ", active=" + active +
                    '}';
        }
    }

}
