package io.neow3j.crypto.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RawScript extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(RawScript.class);

    private List<RawInvocationScript> invocation;

    private RawVerificationScript verification;

    public RawScript() {
    }

    public RawScript(List<RawInvocationScript> invocation, RawVerificationScript verification) {
        this.invocation = invocation;
        this.verification = verification;
    }

    public List<RawInvocationScript> getInvocation() {
        return invocation;
    }

    public RawVerificationScript getVerification() {
        return verification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawScript)) return false;
        RawScript script = (RawScript) o;
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

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        try {
            this.invocation = reader.readSerializableListVarBytes(RawInvocationScript.class);
            this.verification = reader.readSerializable(RawVerificationScript.class);
        } catch (IllegalAccessException e) {
            LOG.error("Can't access the specified object.", e);
        } catch (InstantiationException e) {
            LOG.error("Can't instantiate the specified object type.", e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeSerializableVariableBytes(this.invocation);
        writer.writeSerializableVariableBytes(this.verification);
    }
}
