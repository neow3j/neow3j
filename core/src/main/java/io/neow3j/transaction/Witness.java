package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.script.InvocationScript;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.script.VerificationScript;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.ContractParameter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * A script (invocation and verification script) used to validate a transaction. Usually, a witness is made up of a
 * signature (invocation script) and a check-signature script (verification script) that together prove that the
 * signer has witnessed the signed data.
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
     * @param invocationScript   the invocation script.
     * @param verificationScript the verification script.
     * @see Witness#Witness(InvocationScript, VerificationScript)
     */
    public Witness(byte[] invocationScript, byte[] verificationScript) {
        this(new InvocationScript(invocationScript), new VerificationScript(verificationScript));
    }

    /**
     * Creates a new script from the given invocation and verification script.
     *
     * @param invocationScript   the invocation script.
     * @param verificationScript the verification script.
     */
    public Witness(InvocationScript invocationScript, VerificationScript verificationScript) {
        this.invocationScript = invocationScript;
        this.verificationScript = verificationScript;
    }

    /**
     * Creates a witness (invocation and verification scripts) from the given message, using the given keys for
     * signing the message.
     *
     * @param messageToSign the message from which the signature is added to the invocation script.
     * @param keyPair       the key pair which is used for signing. The verification script is created from the
     *                      public key.
     * @return the constructed witness/script.
     */
    public static Witness create(byte[] messageToSign, ECKeyPair keyPair) {
        InvocationScript i = InvocationScript.fromMessageAndKeyPair(messageToSign, keyPair);
        VerificationScript v = new VerificationScript(keyPair.getPublicKey());
        return new Witness(i, v);
    }

    /**
     * Creates a witness in which the invocation script contains the given signatures and the verification script
     * checks the signatures according to the given public keys and signing threshold.
     * <p>
     * The signatures must appear in the same order as their associated public keys. Example: Given the public
     * keys {p1, p2, p3} and signatures {s1, s2}. Where s1 belongs to p1 and s2 to p2. Assume that the natural
     * ordering of the keys is p3 &lt; p2 &lt; p1. Then you need to pass the signatures in the ordering {s2,
     * s1}.
     *
     * @param signingThreshold the minimum number of signatures required for successful multi-sig verification.
     * @param signatures       the signatures to add to the invocation script.
     * @param publicKeys       the public keys to add to verification script.
     * @return the witness.
     */
    public static Witness createMultiSigWitness(int signingThreshold, List<SignatureData> signatures,
            List<ECPublicKey> publicKeys) {
        VerificationScript v = new VerificationScript(publicKeys, signingThreshold);
        return createMultiSigWitness(signatures, v);
    }

    /**
     * Constructs a witness with the given verification script and an invocation script containing the given
     * signatures. The number of signatures must reach the signing threshold given in the verification script.
     * <p>
     * Note, the signatures must be in the order of their associated public keys in the verifications script. E.g.,
     * if we have public keys {p1, p2, p3} appear in the verification script as {p3, p2, p1} (due to their natural
     * ordering), then the signatures {s1, s3} would have to be ordered {s3, s1} when passed as an argument.
     *
     * @param signatures         the signatures to add to the invocation script.
     * @param verificationScript the verification script to use in the witness.
     * @return the witness.
     */
    public static Witness createMultiSigWitness(List<SignatureData> signatures, VerificationScript verificationScript) {
        int signingThreshold = verificationScript.getSigningThreshold();
        if (signatures.size() < signingThreshold) {
            throw new IllegalArgumentException("Not enough signatures provided for the required signing threshold.");
        }
        return new Witness(
                InvocationScript.fromSignatures(signatures.subList(0, signingThreshold)), verificationScript);
    }

    /**
     * Constructs a witness with an invocation script based on the provided parameters for the contract's verify method.
     * <p>
     * This method is used if no signature is present, i.e. if the signer is a contract. In that case the invocation
     * script is built based on the parameters of its verify method. No verification script is needed.
     *
     * @param verifyParams the parameters for the contract's verify method.
     * @return the witness.
     */
    public static Witness createContractWitness(List<ContractParameter> verifyParams) {
        if (verifyParams.isEmpty()) {
            return new Witness();
        }
        ScriptBuilder invocationScript = new ScriptBuilder();
        verifyParams.forEach(invocationScript::pushParam);
        return new Witness(invocationScript.toArray(), new byte[]{});
    }

    public InvocationScript getInvocationScript() {
        return invocationScript;
    }

    public VerificationScript getVerificationScript() {
        return verificationScript;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Witness)) {
            return false;
        }
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
        return "Witness{" +
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
