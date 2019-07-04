package io.neow3j.crypto.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.utils.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * A script used to validate a transaction.
 * Usually, a so-called witness, i.e. a transaction signature (invocation script) and the
 * verification script derived from the signing key.
 */
public class RawScript extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(RawScript.class);

    private RawInvocationScript invocationScript;

    private RawVerificationScript verificationScript;

    public RawScript() {
        this.invocationScript = new RawInvocationScript();
        this.verificationScript = new RawVerificationScript();
    }

    public RawScript(RawInvocationScript invocationScript, RawVerificationScript verificationScript) {
        this.invocationScript = invocationScript;
        this.verificationScript = verificationScript;
    }

    public RawScript(byte[] invocationScript, byte[] verificationScript) {
        this.invocationScript = new RawInvocationScript(invocationScript);
        this.verificationScript = new RawVerificationScript(verificationScript);
    }

    /**
     * Creates a witness (invocation and verification scripts) from the given message, using the
     * given keys for signing the message.
     * @param messageToSign The message from which the signature is added to the invocation script.
     * @param keyPair The key pair which is used for signing. The verification script is created
     *                from the public key.
     * @return the constructed witness/script.
     */
    public static RawScript createWitness(byte[] messageToSign, ECKeyPair keyPair) {
        RawInvocationScript i = RawInvocationScript.fromMessageAndKeyPair(messageToSign, keyPair);
        RawVerificationScript v = RawVerificationScript.fromPublicKey(keyPair.getPublicKey());
        return new RawScript(i, v);
    }

    public static RawScript createMultiSigWitness(int signingThreshold,
                                                  List<SignatureData> signatures,
                                                  List<BigInteger> publicKeys) {

        RawVerificationScript v = RawVerificationScript.fromPublicKeys(signingThreshold, publicKeys);
        return createMultiSigWitness(signingThreshold, signatures, v);
    }

    public static RawScript createMultiSigWitness(List<SignatureData> signatures,
                                                  RawVerificationScript verificationScript) {

        int signingThreshold = Script.extractSigningThreshold(verificationScript.getScript());
        return createMultiSigWitness(signingThreshold, signatures, verificationScript);
    }

    public static RawScript createMultiSigWitness(int signingThreshold,
                                                   List<SignatureData> signatures,
                                                   RawVerificationScript verificationScript) {

        if (signatures.size() < signingThreshold) {
            throw new IllegalArgumentException("Not enough signatures provided for the required " +
                    "signing threshold.");
        }
        return new RawScript(
                RawInvocationScript.fromSignatures(signatures.subList(0, signingThreshold)),
                verificationScript);
    }

    public RawInvocationScript getInvocationScript() {
        return invocationScript;
    }

    public RawVerificationScript getVerificationScript() {
        return verificationScript;
    }

    /**
     * The script hash is the hash of the verification script.
     * @return the script hash of this script.
     */
    public String getScriptHash() {
        if (verificationScript == null) {
                throw new IllegalStateException("Can't obtain script hash because verification " +
                        "script is not set.");
        }
        return verificationScript.getScriptHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawScript)) return false;
        RawScript script = (RawScript) o;
        return Objects.equals(getInvocationScript(), script.getInvocationScript()) &&
                Objects.equals(getVerificationScript(), script.getVerificationScript());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvocationScript(), getVerificationScript());
    }

    @Override
    public String toString() {
        return "Script{" +
                "invocationScript='" + invocationScript + '\'' +
                ", verificationScript='" + verificationScript + '\'' +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        try {
            this.invocationScript = reader.readSerializable(RawInvocationScript.class);
            this.verificationScript = reader.readSerializable(RawVerificationScript.class);
        } catch (IllegalAccessException e) {
            LOG.error("Can't access the specified object.", e);
        } catch (InstantiationException e) {
            LOG.error("Can't instantiate the specified object type.", e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        invocationScript.serialize(writer);
        verificationScript.serialize(writer);
//        writer.writeSerializableVariableBytes(this.invocationScript);
//        writer.writeSerializableVariableBytes(this.verificationScript);
    }
}
