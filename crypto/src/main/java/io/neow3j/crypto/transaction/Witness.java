package io.neow3j.crypto.transaction;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.utils.Numeric;

import java.io.IOException;

public class Witness extends RawScript {
    private byte[] invocationScript;
    private byte[] verificationScript;

    public Witness(String invocation, String verification) {
        invocationScript = Numeric.hexStringToByteArray(invocation);
        verificationScript = Numeric.hexStringToByteArray(verification);
    }

    public int getSize() {
        return invocationScript.length + verificationScript.length;
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.invocationScript = reader.readVarBytes(65536);
        this.verificationScript = reader.readVarBytes(65536);
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeVarBytes(invocationScript);
        writer.writeVarBytes(verificationScript);
    }
}