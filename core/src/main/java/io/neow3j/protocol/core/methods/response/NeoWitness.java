package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NeoWitness {

    @JsonProperty("invocation")
    private String invocation;

    @JsonProperty("verification")
    private String verification;

    public NeoWitness() {
    }

    public NeoWitness(String invocation, String verification) {
        this.invocation = invocation;
        this.verification = verification;
    }

    public String getInvocation() {
        return invocation;
    }

    public String getVerification() {
        return verification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeoWitness)) return false;
        NeoWitness script = (NeoWitness) o;
        return Objects.equals(getInvocation(), script.getInvocation()) &&
                Objects.equals(getVerification(), script.getVerification());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvocation(), getVerification());
    }

    @Override
    public String toString() {
        return "Script{" +
                "invocation='" + invocation + '\'' +
                ", verification='" + verification + '\'' +
                '}';
    }
}
