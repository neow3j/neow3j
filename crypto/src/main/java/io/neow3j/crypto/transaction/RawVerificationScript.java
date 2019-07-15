package io.neow3j.crypto.transaction;

import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptReader;
import io.neow3j.crypto.Hash;
import io.neow3j.utils.Keys;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RawVerificationScript extends NeoSerializable {

    private byte[] script;

    public RawVerificationScript() {
        script = new byte[0];
    }

    public RawVerificationScript(byte[] script) {
        this.script = script;
    }

    public static RawVerificationScript fromPublicKey(BigInteger publicKey) {
        return new RawVerificationScript(Keys.getVerificationScriptFromPublicKey(publicKey));
    }

    public static RawVerificationScript fromPublicKeys(int signingThreshold, byte[]... publicKeys) {
        return new RawVerificationScript(
                Keys.getVerificationScriptFromPublicKeys(signingThreshold, publicKeys)
        );
    }

    public static RawVerificationScript fromPublicKeys(int signingThreshold, List<BigInteger> publicKeys) {
        return new RawVerificationScript(
                Keys.getVerificationScriptFromPublicKeys(signingThreshold, publicKeys)
        );
    }

    public byte[] getScript() {
        return script;
    }

    /** Calculates the script hash of this verification script.
     * I.e. applies SHA256 and RIPMED160 to the script byte array.
     * Apparently, after hashing, the script hash is considered to be in little-endian order. That
     * means, it can directly be used in transactions without reversing it.
     * @return the script hash.
     */
    public byte[] getScriptHash() {
        if (script.length == 0) return new byte[0];
        else return Hash.sha256AndThenRipemd160(script);
    }


    /**
     * Extracts (from the script itself) the number of signatures required for signing this
     * verification script.
     * @return The signing threshold.
     */
    public int getSigningThreshold() {
        int scriptLen = this.script.length;
        byte opCode = this.script[scriptLen-1];
        if (opCode == OpCode.CHECKSIG.getValue()) {
            return 1;
        } else if (opCode == OpCode.CHECKMULTISIG.getValue()) {
            return ScriptReader.readInteger(this.script).intValue();
        } else {
            throw new IllegalArgumentException("The script is not a valid verification script.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawVerificationScript)) return false;
        RawVerificationScript that = (RawVerificationScript) o;
        return Arrays.equals(getScript(), that.getScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript());
    }

    @Override
    public String toString() {
        return "VerificationScript{" +
                "script=" + Numeric.toHexStringNoPrefix(script) + '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        script = reader.readVarBytes();
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeVarBytes(script);
    }

}
