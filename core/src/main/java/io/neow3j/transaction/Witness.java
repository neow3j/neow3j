package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.script.InvocationScript;
import io.neow3j.script.VerificationScript;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * A script (invocation and verification script) used to validate a transaction.
 * Usually, a witness is made up of a signature (invocation script) and a check-signature script
 * (verification script) that together prove that the signer has witnessed the signed data.
 */
public class Witness extends NeoSerializable {

    private InvocationScript invocationScript;
    private VerificationScript verificationScript;

    /**
     * Constructs an empty witness.
     */
    public Witness() {
        invocationScript = new InvocationScript();
        verificationScript = new VerificationScript();
    }

    /**
     * Creates a new witness from the given invocation and verification script.
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
     * Creates a new script from the given invocation and verification script.
     *
     * @param invocationScript   the invocation script
     * @param verificationScript the verification script
     */
    public Witness(InvocationScript invocationScript, VerificationScript verificationScript) {
        this.invocationScript = invocationScript;
        this.verificationScript = verificationScript;
    }

    /**
     * Creates a witness (invocation and verification scripts) from the given message, using the
     * given keys for signing the message.
     *
     * @param messageToSign The message from which the signature is added to the invocation script.
     * @param keyPair       The key pair which is used for signing. The verification script is
     *                      created from the public key.
     * @return the constructed witness/script.
     */
    public static Witness create(byte[] messageToSign, ECKeyPair keyPair) {
        InvocationScript i = InvocationScript.fromMessageAndKeyPair(messageToSign, keyPair);
        VerificationScript v = new VerificationScript(keyPair.getPublicKey());
        return new Witness(i, v);
    }

    /**
     * Creates a witness in which the invocation script contains the given signatures and the
     * verification script checks the signatures according to the given public keys and signing
     * threshold.
     *
     * @param signingThreshold The minimum number of signatures required for successful multi-sig
     *                         verification.
     * @param signatures       The signatures to add to the invocation script.
     * @param publicKeys       The public keys to add to verification script.
     * @return the witness.
     */
    public static Witness createMultiSigWitness(int signingThreshold,
            List<SignatureData> signatures, List<ECPublicKey> publicKeys) {

        VerificationScript v = new VerificationScript(publicKeys, signingThreshold);
        return createMultiSigWitness(signatures, v);
    }

    /**
     * Constructs a witness with the given verification script and an invocation script
     * containing the given signatures. The number of signatures must reach the signing threshold
     * given in the verification script.
     *
     * @param signatures         The signatures to add to the invocation script.
     * @param verificationScript The verification script to use in the witness.
     * @return the witness.
     */
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
        this.invocationScript = reader.readSerializable(InvocationScript.class);
        this.verificationScript = reader.readSerializable(VerificationScript.class);
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
