package com.axlabs.neow3j.crypto.transaction;

import com.axlabs.neow3j.io.BinaryReader;
import com.axlabs.neow3j.io.BinaryWriter;
import com.axlabs.neow3j.io.NeoSerializable;
import com.axlabs.neow3j.utils.Numeric;

import java.io.IOException;
import java.util.Arrays;

public class RawInvocationScript extends NeoSerializable {

    public byte[] signature;

    public RawInvocationScript() {
    }

    public RawInvocationScript(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawInvocationScript)) return false;
        RawInvocationScript that = (RawInvocationScript) o;
        return Arrays.equals(getSignature(), that.getSignature());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getSignature());
    }

    @Override
    public String toString() {
        return "InvocationScript{" +
                "signature=" + (signature != null ? Numeric.toHexStringNoPrefix(signature) : "null") +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        byte[] bytes = reader.readVarBytes();
        System.out.println("test: " + Numeric.toHexStringNoPrefix(bytes));
        this.signature = bytes;
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeVarBytes(signature);
    }

}
