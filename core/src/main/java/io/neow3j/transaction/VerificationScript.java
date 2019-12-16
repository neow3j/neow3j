package io.neow3j.transaction;

import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptHash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VerificationScript extends NeoSerializable {

    private byte[] script;

    public VerificationScript() {
        script = new byte[0];
    }

    public VerificationScript(byte[] script) {
        this.script = script;
    }

    public static VerificationScript fromPublicKey(BigInteger publicKey) {
        return new VerificationScript(Keys.getVerificationScriptFromPublicKey(publicKey));
    }

    public static VerificationScript fromPublicKeys(int signingThreshold, byte[]... publicKeys) {
        return new VerificationScript(
                Keys.getVerificationScriptFromPublicKeys(signingThreshold, publicKeys)
        );
    }

    public static VerificationScript fromPublicKeys(int signingThreshold,
            List<BigInteger> publicKeys) {
        return new VerificationScript(
                Keys.getVerificationScriptFromPublicKeys(signingThreshold, publicKeys)
        );
    }

    public byte[] getScript() {
        return script;
    }

    /**
     * Gets the script hash of this verification script.
     *
     * @return the script hash.
     */
    public ScriptHash getScriptHash() {
        if (this.script.length == 0) {
            return null;
        } else {
            return ScriptHash.fromScript(this.script);
        }
    }


    /**
     * Extracts (from the script itself) the number of signatures required for signing this
     * verification script.
     *
     * @return The signing threshold.
     */
    public int getSigningThreshold() {
        int scriptLen = this.script.length;
        byte opCode = this.script[scriptLen - 1];
        if (opCode == OpCode.CHECKSIG.getValue()) {
            return 1;
        } else if (opCode == OpCode.CHECKMULTISIG.getValue()) {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(script, 0, script.length)) {
                return new BinaryReader(stream).readPushInteger();
            } catch (IOException e) {
                throw new IllegalStateException("Got IOException without doing IO.");
            }
        } else {
            throw new IllegalArgumentException("The script is not a valid verification script.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationScript)) return false;
        VerificationScript that = (VerificationScript) o;
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
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            script = reader.readVarBytes();
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeVarBytes(script);
    }

    @Override
    public int getSize() {
        return IOUtils.getSizeOfVarInt(this.script.length) + this.script.length;
    }

}
