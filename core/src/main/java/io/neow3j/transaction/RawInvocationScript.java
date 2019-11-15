package io.neow3j.transaction;

import io.neow3j.contract.ScriptBuilder;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An invocation script used in the script/witness part of a transaction.
 * It can hold an arbitrary byte array. In the case of a witness, the invocation script is
 * constructed from the OpCode {@link io.neow3j.constants.OpCode#PUSHBYTES64} and the transaction
 * signature.
 */
public class RawInvocationScript extends NeoSerializable {

    private byte[] script;

    public RawInvocationScript() {
        script = new byte[0];
    }

    /**
     * Creates an invocation script with the given script.
     * If the script represents a signature make sure that it starts with the {@link
     * io.neow3j.constants.OpCode#PUSHBYTES64}. The opcode is not added automatically. Better even,
     * use {@link RawInvocationScript#fromSignature(Sign.SignatureData)} or {@link
     * RawInvocationScript#fromMessageAndKeyPair(byte[], ECKeyPair)} when you need
     * a signature invocation script.
     *
     * @param script The script in an array of bytes.
     */
    public RawInvocationScript(byte[] script) {
        this.script = script;
    }

    private RawInvocationScript(byte[] script, SignatureData signature) {
        this(script);
    }


    /**
     * Creates an invocation script from the given signature.
     *
     * @param signature The signature to use in the script.
     * @return the constructed invocation script.
     */
    public static RawInvocationScript fromSignature(SignatureData signature) {
        byte[] script = new ScriptBuilder().pushData(signature.getConcatenated()).toArray();
        return new RawInvocationScript(script, signature);
    }

    /**
     * Creates an invocation script constructed from the signature of the given message, signed with
     * the given key pair.
     *
     * @param message Message to sign.
     * @param keyPair Key pair to use for signing
     * @return the constructed invocation script.
     */
    public static RawInvocationScript fromMessageAndKeyPair(byte[] message, ECKeyPair keyPair) {
        SignatureData signature = Sign.signMessage(message, keyPair);
        return fromSignature(signature);
    }

    public static RawInvocationScript fromSignatures(List<SignatureData> sigs) {
        ScriptBuilder builder = new ScriptBuilder();
        sigs.forEach(sig -> builder.pushData(sig.getConcatenated()));
        return new RawInvocationScript(builder.toArray());
    }

    public byte[] getScript() {
        return script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawInvocationScript)) return false;
        RawInvocationScript that = (RawInvocationScript) o;
        return Arrays.equals(getScript(), that.getScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript());
    }

    @Override
    public String toString() {
        return "InvocationScript{" +
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
