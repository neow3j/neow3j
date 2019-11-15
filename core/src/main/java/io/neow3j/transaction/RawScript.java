package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
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
    private ScriptHash scriptHash;

    public RawScript() {
    }

    /**
     * <p>Creates a new script from the given invocation and verification script.</p>
     * <br>
     * <p>Make sure that the scripts are proper NEO VM scripts. E.g. the invocation script byte
     * array must not only contain the serialized signature data but it also needs the prefix 40
     * which signifies that 64 bytes follow. It is safer to use the static creation methods from
     * {@link RawInvocationScript} and {@link RawVerificationScript} to create valid scripts.</p>
     *
     * @param invocationScript   the invocation script
     * @param verificationScript the verification script
     * @see RawScript#RawScript(RawInvocationScript, RawVerificationScript)
     */
    public RawScript(byte[] invocationScript, byte[] verificationScript) {
        this(new RawInvocationScript(invocationScript),
                new RawVerificationScript(verificationScript));
    }

    /**
     * <p>Creates a new script from the given invocation and verification script.</p>
     * <br>
     * <p>The verification script cannot be null because the script hash is derived from it. If you
     * don't have a verification script you can use the constructor
     * {@link RawScript#RawScript(byte[], ScriptHash)} and just provide a script Hash instead of the
     * verification script.</p>
     *
     * @param invocationScript   the invocation script
     * @param verificationScript the verification script
     */
    public RawScript(RawInvocationScript invocationScript, RawVerificationScript verificationScript) {
        this.invocationScript = invocationScript;
        this.verificationScript = verificationScript;
        if (verificationScript == null || verificationScript.getScriptHash() == null) {
            throw new IllegalArgumentException("The script hash cannot be produced. " +
                    "The verification script must not be null because the script hash is derived " +
                    "from it.");
        }
        this.scriptHash = verificationScript.getScriptHash();
    }

    /**
     * Creates a new script from the given invocation script and script hash.
     * Use this if you don't need a verification script.
     *
     * @param invocationScript the invocation script
     * @param scriptHash       a script hash in big-endian order.
     * @deprecated Use {@link RawScript#RawScript(byte[], ScriptHash)} instead.
     */
    @Deprecated
    public RawScript(byte[] invocationScript, String scriptHash) {
        this(invocationScript, new ScriptHash(scriptHash));
    }

    /**
     * Creates a new script from the given invocation script and script hash.
     * Use this if you don't need a verification script.
     *
     * @param invocationScript the invocation script
     * @param scriptHash       a script hash instead of a verification script.
     */
    public RawScript(byte[] invocationScript, ScriptHash scriptHash) {
        this.invocationScript = new RawInvocationScript(invocationScript);
        this.verificationScript = new RawVerificationScript();
        this.scriptHash = scriptHash;
    }

    /**
     * Creates a witness (invocation and verification scripts) from the given message, using the
     * given keys for signing the message.
     *
     * @param messageToSign The message from which the signature is added to the invocation script.
     * @param keyPair       The key pair which is used for signing. The verification script is created
     *                      from the public key.
     * @return the constructed witness/script.
     */
    public static RawScript createWitness(byte[] messageToSign, ECKeyPair keyPair) {
        RawInvocationScript i = RawInvocationScript.fromMessageAndKeyPair(messageToSign, keyPair);
        RawVerificationScript v = RawVerificationScript.fromPublicKey(keyPair.getPublicKey());
        return new RawScript(i, v);
    }

    public static RawScript createMultiSigWitness(int signingThreshold,
                                                  List<SignatureData> signatures,
                                                  byte[]... publicKeys) {

        RawVerificationScript v = RawVerificationScript.fromPublicKeys(signingThreshold, publicKeys);
        return createMultiSigWitness(signingThreshold, signatures, v);
    }

    public static RawScript createMultiSigWitness(int signingThreshold,
                                                  List<SignatureData> signatures,
                                                  List<BigInteger> publicKeys) {

        RawVerificationScript v = RawVerificationScript.fromPublicKeys(signingThreshold, publicKeys);
        return createMultiSigWitness(signingThreshold, signatures, v);
    }

    public static RawScript createMultiSigWitness(List<SignatureData> signatures,
                                                  RawVerificationScript verificationScript) {

        int signingThreshold = verificationScript.getSigningThreshold();
        return createMultiSigWitness(signingThreshold, signatures, verificationScript);
    }

    public static RawScript createMultiSigWitness(List<SignatureData> signatures,
                                                  byte[] verificationScript) {

        return createMultiSigWitness(signatures, new RawVerificationScript(verificationScript));
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
     * @return the script hash of this script in big-endian order.
     */
    public ScriptHash getScriptHash() {
        return scriptHash;
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
            this.scriptHash = verificationScript.getScriptHash();
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
    }
}
