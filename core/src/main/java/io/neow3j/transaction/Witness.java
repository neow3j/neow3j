package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * A script used to validate a transaction.
 * Usually, a so-called witness, i.e. a transaction signature (invocation script) and the
 * verification script derived from the signing key.
 */
public class Witness extends NeoSerializable {

    private InvocationScript invocationScript;
    private VerificationScript verificationScript;
    private ScriptHash scriptHash;

    public Witness() {
    }

    /**
     * <p>Creates a new script from the given invocation and verification script.</p>
     * <br>
     * <p>Make sure that the scripts are proper NEO VM scripts. E.g. the invocation script byte
     * array must not only contain the serialized signature data but it also needs the prefix 40
     * which signifies that 64 bytes follow. It is safer to use the static creation methods from
     * {@link InvocationScript} and {@link VerificationScript} to create valid scripts.</p>
     *
     * @param invocationScript   the invocation script
     * @param verificationScript the verification script
     * @see Witness#Witness(InvocationScript, VerificationScript)
     */
    public Witness(byte[] invocationScript, byte[] verificationScript) {
        this(new InvocationScript(invocationScript),
                new VerificationScript(verificationScript));
    }

    /**
     * <p>Creates a new script from the given invocation and verification script.</p>
     * <br>
     * <p>The verification script cannot be null because the script hash is derived from it. If you
     * don't have a verification script you can use the constructor
     * {@link Witness#Witness(byte[], ScriptHash)} and just provide a script Hash instead of the
     * verification script.</p>
     *
     * @param invocationScript   the invocation script
     * @param verificationScript the verification script
     */
    public Witness(InvocationScript invocationScript, VerificationScript verificationScript) {
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
     * @deprecated Use {@link Witness#Witness(byte[], ScriptHash)} instead.
     */
    @Deprecated
    public Witness(byte[] invocationScript, String scriptHash) {
        this(invocationScript, new ScriptHash(scriptHash));
    }

    /**
     * Creates a new script from the given invocation script and script hash.
     * Use this if you don't need a verification script.
     *
     * @param invocationScript the invocation script
     * @param scriptHash       a script hash instead of a verification script.
     */
    public Witness(byte[] invocationScript, ScriptHash scriptHash) {
        this.invocationScript = new InvocationScript(invocationScript);
        this.verificationScript = new VerificationScript();
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
    public static Witness createWitness(byte[] messageToSign, ECKeyPair keyPair) {
        InvocationScript i = InvocationScript.fromMessageAndKeyPair(messageToSign, keyPair);
        VerificationScript v = new VerificationScript(keyPair.getPublicKey());
        return new Witness(i, v);
    }

    public static Witness createMultiSigWitness(int signingThreshold,
                                                  List<SignatureData> signatures,
                                                  List<ECPublicKey> publicKeys) {

        VerificationScript v = new VerificationScript(publicKeys, signingThreshold);
        return createMultiSigWitness(signatures, v);
    }

    public static Witness createMultiSigWitness(List<SignatureData> signatures,
                                                  VerificationScript verificationScript) {

        int signingThreshold = verificationScript.getSigningThreshold();
        if (signatures.size() < signingThreshold) {
            throw new IllegalArgumentException("Not enough signatures provided for the required " +
                    "signing threshold.");
        }
        return new Witness(
                InvocationScript.fromSignatures(signatures.subList(0, signingThreshold)),
                verificationScript);
    }

    public InvocationScript getInvocationScript() {
        return invocationScript;
    }

    public VerificationScript getVerificationScript() {
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
        if (!(o instanceof Witness)) return false;
        Witness script = (Witness) o;
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
    public void deserialize(BinaryReader reader) throws DeserializationException {
        try {
            this.invocationScript = reader.readSerializable(InvocationScript.class);
            this.verificationScript = reader.readSerializable(VerificationScript.class);
            this.scriptHash = verificationScript.getScriptHash();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        invocationScript.serialize(writer);
        verificationScript.serialize(writer);
    }

    @Override
    public int getSize() {
        return this.invocationScript.getSize() + this.verificationScript.getSize();
    }
}
