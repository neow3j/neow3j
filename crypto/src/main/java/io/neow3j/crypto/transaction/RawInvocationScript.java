package io.neow3j.crypto.transaction;

import io.neow3j.crypto.Sign;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class RawInvocationScript extends NeoSerializable {

    public Sign.SignatureData signature;

    public RawInvocationScript() {
    }

    public RawInvocationScript(Sign.SignatureData signature) {
        this.signature = signature;
    }

    public RawInvocationScript(byte[] signature) {
        this.signature = getSignatureData(signature);
    }

    public Sign.SignatureData getSignature() {
        return signature;
    }

    private Sign.SignatureData getSignatureData(byte[] signature) {
        return new Sign.SignatureData(
                (byte) 0x00,
                Arrays.copyOfRange(signature, 0, 32),
                Arrays.copyOfRange(signature, 32, 64)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawInvocationScript)) return false;
        RawInvocationScript that = (RawInvocationScript) o;
        return Objects.equals(getSignature(), that.getSignature());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSignature());
    }

    @Override
    public String toString() {
        return "InvocationScript{" +
                "signature=" + (signature != null ? Numeric.toHexStringNoPrefix(signature.getConcatenated()) : "null") +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        byte[] bytes = reader.readVarBytes();
        this.signature = getSignatureData(bytes);

    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeVarBytes(signature.getConcatenated());
    }

}
