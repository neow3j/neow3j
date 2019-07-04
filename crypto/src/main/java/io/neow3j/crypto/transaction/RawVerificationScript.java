package io.neow3j.crypto.transaction;

import io.neow3j.constants.OpCode;
import io.neow3j.crypto.Hash;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.neow3j.constants.NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT;
import static io.neow3j.constants.OpCode.CHECKMULTISIG;
import static io.neow3j.constants.OpCode.CHECKSIG;
import static io.neow3j.constants.OpCode.PUSHBYTES33;

public class RawVerificationScript extends NeoSerializable {

    private byte[] script;

    public RawVerificationScript() {
        script = new byte[0];
    }

    public RawVerificationScript(byte[] script) {
        this.script = script;
    }

    public static RawVerificationScript fromPublicKey(BigInteger publicKey) {
        byte[] script = ArrayUtils.concatenate(
                new byte[]{PUSHBYTES33.getValue()},
                publicKey.toByteArray(),
                new byte[]{CHECKSIG.getValue()});

        return new RawVerificationScript(script);
    }

    public static RawVerificationScript fromPublicKeys(int signingThreshold, List<BigInteger> publicKeys) {
        if (signingThreshold < 2 || signingThreshold > publicKeys.size()
                || publicKeys.size() > MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
            throw new IllegalArgumentException();
        }
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            BinaryWriter w = new BinaryWriter(byteStream);
            w.pushInteger(signingThreshold);
            for (BigInteger key : publicKeys) {
                w.pushData(key.toByteArray());
            }
            w.pushInteger(publicKeys.size());
            w.writeByte(CHECKMULTISIG.getValue());
            return new RawVerificationScript(byteStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Got an IOException without doing IO.");
        }
    }

    public byte[] getScript() {
        return script;
    }

    public String getScriptHash() {
        if (script.length == 0) return "";
        else return Numeric.toHexStringNoPrefix(Hash.sha256AndThenRipemd160(script));
        // TODO Claude 02.07.19: Does the script hash need to be reversed?
        // Numeric.toHexStringNoPrefix(ArrayUtils.reverseArray(Hash.sha256AndThenRipemd160(script)));
    }

    /**
     * Extracts (from the script itself) the number of signatures required for signing this
     * verification script.
     * @return The signing threshold.
     */
    public int getSigningThreshold() {
        int scriptLen = this.script.length;
        byte opCode = this.script[scriptLen];
        if (opCode == OpCode.CHECKSIG.getValue()) {
            return 1;
        } else if (opCode == OpCode.CHECKMULTISIG.getValue()) {
            byte th = this.script[0];
            if (th < OpCode.PUSHM1.getValue()) {
                // TODO 02.07.19 claude: Handle variable length opcodes.
                return -1;
            } else if (th >= OpCode.PUSH1.getValue() && th <= OpCode.PUSH16.getValue()){
                return th - (OpCode.PUSHM1.getValue()-1);
            } else {
                throw new IllegalArgumentException("Can't read valid threshold from script.");
            }
        } else {
            throw new IllegalArgumentException("The script does not include a CHECKSIG or " +
                    "CHECKMULTISIG OpCode.");
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
