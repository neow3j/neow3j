package io.neow3j.script;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.neow3j.utils.Numeric.toHexStringNoPrefix;

/**
 * An invocation script is part of a witness and is simply a sequence of neo-vm instructions. The invocation script
 * usually is the input to the verification script. In most cases it will contain a signature that is checked in the
 * verification script.
 */
public class InvocationScript extends NeoSerializable {

    private byte[] script;

    /**
     * Constructs an empty invocation script.
     */
    public InvocationScript() {
        script = new byte[0];
    }

    /**
     * Creates an invocation script with the given script.
     * <p>
     * It is recommended to use {@link InvocationScript#fromSignature(Sign.SignatureData)} or
     * {@link InvocationScript#fromMessageAndKeyPair(byte[], ECKeyPair)} when you need a signature invocation script.
     *
     * @param script the script in an array of bytes.
     */
    public InvocationScript(byte[] script) {
        this.script = script;
    }

    /**
     * Creates an invocation script from the given signature.
     *
     * @param signature the signature to use in the script.
     * @return the constructed invocation script.
     */
    public static InvocationScript fromSignature(SignatureData signature) {
        byte[] script = new ScriptBuilder().pushData(signature.getConcatenated()).toArray();
        return new InvocationScript(script);
    }

    /**
     * Creates an invocation script from the signature of the given message signed with the given key pair.
     *
     * @param message the message to sign.
     * @param keyPair the key pair to use for signing.
     * @return the constructed invocation script.
     */
    public static InvocationScript fromMessageAndKeyPair(byte[] message, ECKeyPair keyPair) {
        SignatureData signature = Sign.signMessage(message, keyPair);
        return fromSignature(signature);
    }

    /**
     * Constructs an invocation script from the given signatures.
     *
     * @param sigs the signatures.
     * @return the invocation script.
     */
    public static InvocationScript fromSignatures(List<SignatureData> sigs) {
        ScriptBuilder builder = new ScriptBuilder();
        sigs.forEach(sig -> builder.pushData(sig.getConcatenated()));
        return new InvocationScript(builder.toArray());
    }

    /**
     * Gets this invocation script as a byte array.
     *
     * @return this invocation script as a byte array.
     */
    public byte[] getScript() {
        return script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InvocationScript)) {
            return false;
        }
        InvocationScript that = (InvocationScript) o;
        return Arrays.equals(getScript(), that.getScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript());
    }

    @Override
    public String toString() {
        return "InvocationScript{script=" + toHexStringNoPrefix(script) + '}';
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
        return IOUtils.getVarSize(this.script.length) + this.script.length;
    }

    /**
     * Unbundles the script into a list of signatures if this invocation script contains signatures.
     *
     * @return the list of signatures found in this script.
     */
    public List<SignatureData> getSignatures() {
        BinaryReader r = new BinaryReader(script);
        List<SignatureData> sigs = new ArrayList<>();
        try {
            while (r.available() > 0 && OpCode.PUSHDATA1.getCode() == r.readByte()) {
                r.readByte();
                sigs.add(SignatureData.fromByteArray(r.readBytes(NeoConstants.SIGNATURE_SIZE)));
            }
        } catch (IOException ignore) {
        }
        return sigs;
    }

}
