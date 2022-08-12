package io.neow3j.script;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.serialization.BinaryReader;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.IOUtils;
import io.neow3j.serialization.NeoSerializable;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.ScriptFormatException;
import io.neow3j.types.Hash160;
import io.neow3j.utils.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.neow3j.constants.NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT;
import static io.neow3j.script.ScriptBuilder.buildVerificationScript;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.lang.String.format;

/**
 * A verification script is part of a witness and is simply a sequence of neo-vm instructions. The verification
 * script is the part of a witness that describes what has to be verified such that the witness is valid. E.g., for a
 * regular signature witness the verification script is made up of a check-signature call and it expects a signature
 * as input.
 */
public class VerificationScript extends NeoSerializable {

    private byte[] script;

    /**
     * Creates an empty verification script.
     */
    public VerificationScript() {
        script = new byte[0];
    }

    /**
     * Creates a verification script from the given byte array.
     *
     * @param script the script.
     */
    public VerificationScript(byte[] script) {
        this.script = script;
    }

    /**
     * Creates a verification script for the given public key. The resulting verification script contains a signature
     * check with the given public key as the expected signer.
     *
     * @param publicKey the public key to create the script for.
     */
    public VerificationScript(ECPublicKey publicKey) {
        this.script = buildVerificationScript(publicKey.getEncoded(true));
    }

    /**
     * Creates a multi-sig verification script for the given keys and signing threshold. The resulting verification
     * script contains a multi-signature check with the given public keys as the expected signer.
     *
     * @param publicKeys       the public keys to create the script for.
     * @param signingThreshold the minimum number of public keys needed to sign transactions from the given public keys.
     */
    public VerificationScript(List<ECPublicKey> publicKeys, int signingThreshold) {
        if (signingThreshold < 1 || signingThreshold > publicKeys.size()) {
            throw new IllegalArgumentException(
                    "Signing threshold must be at least 1 and not higher than the number of public keys.");
        }
        if (publicKeys.size() > MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
            throw new IllegalArgumentException(format("At max %s public keys can take part in a multi-sig account",
                    MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT));
        }
        this.script = buildVerificationScript(publicKeys, signingThreshold);
    }

    /**
     * @return the verification script as a byte array.
     */
    public byte[] getScript() {
        return script;
    }

    /**
     * Serializes this script to a byte array. This is only meant to be used in transaction serialization because it
     * adds the size of the script as a prefix. Use {@link VerificationScript#getScript()} instead to get this
     * scripts byte array.
     *
     * @return the serialized script for usage in a transaction array.
     */
    @Override
    public byte[] toArray() {
        // Only overridden for documentation.
        return super.toArray();
    }

    /**
     * @return the script hash of this verification script.
     */
    public Hash160 getScriptHash() {
        if (this.script.length == 0) {
            return null;
        } else {
            return Hash160.fromScript(this.script);
        }
    }

    @Override
    public int getSize() {
        return IOUtils.getVarSize(this.script.length) + this.script.length;
    }

    /**
     * Extracts the number of signatures required for signing this verification script.
     *
     * @return the signing threshold.
     * @throws ScriptFormatException if this verification script is not an ordinary address script or multi-address
     *                               script.
     */
    public int getSigningThreshold() {
        if (isSingleSigScript()) {
            return 1;
        } else if (isMultiSigScript()) {
            try {
                return new BinaryReader(script).readPushInteger();
            } catch (DeserializationException e) {
                throw new RuntimeException(e);
            }
        }
        throw new ScriptFormatException("The signing threshold cannot be determined because this script does not " +
                "apply to the format of a signature verification script.");
    }

    /**
     * Gets the number of accounts taking part in this verification script.
     *
     * @return the number of accounts.
     * @throws ScriptFormatException if this verification script is not an ordinary address script or multi-address
     *                               script.
     */
    public int getNrOfAccounts() {
        return getPublicKeys().size();
    }

    /**
     * Checks if this verification script is from single signature account.
     *
     * @return true if this script is from a single signature account. False, otherwise.
     */
    public boolean isSingleSigScript() {
        if (script.length != 40) {
            return false;
        }
        String interopService = toHexStringNoPrefix(ArrayUtils.getLastNBytes(script, 4));
        return script[0] == OpCode.PUSHDATA1.getCode() &&
                script[1] == 33 && // 33 bytes of public key
                script[35] == OpCode.SYSCALL.getCode() &&
                interopService.equals(InteropService.SYSTEM_CRYPTO_CHECKSIG.getHash());
    }

    /**
     * Checks if this verification script is from a multi signature account.
     *
     * @return true if this script is from a multi signature account. False, otherwise.
     */
    public boolean isMultiSigScript() {
        if (script.length < 42) {
            return false;
        }
        try {
            BinaryReader reader = new BinaryReader(this.script);
            int n = reader.readPushInteger(); // Signing Threshold (n of m)
            if (n < 1 || n > NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
                return false;
            }

            int m = 0; // Number of participating keys
            while (reader.readByte() == OpCode.PUSHDATA1.getCode()) {
                // Position at PUSHDATA1 + (1 byte data size + 33 bytes + 1 byte to make sure script does not end
                // after the key.
                if (script.length <= reader.getPosition() + 35) {
                    return false;
                }
                // Byte after PUSHDATA1 must have value 33 for 33 bytes of public key data.
                if (reader.readByte() != 33) {
                    return false;
                }
                reader.readEncodedECPoint();
                m++;
                // Mark the current position to be able to reset the last readBytes() which is not a PUSHDATA1 anymore.
                reader.mark(0);
            }
            if (n > m || m > MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
                return false;
            }

            reader.reset(); // Reset the last performed readBytes() from the while loop.
            int alsoM = reader.readPushInteger();
            if (m != alsoM) {
                return false;
            }
            if (reader.readByte() != OpCode.SYSCALL.getCode()) {
                return false;
            }
            byte[] interopServiceCode = new byte[4];
            reader.read(interopServiceCode, 0, 4);
            if (!toHexStringNoPrefix(interopServiceCode).equals(InteropService.SYSTEM_CRYPTO_CHECKMULTISIG.getHash())) {
                return false;
            }
        } catch (DeserializationException | IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets the public keys that are encoded in this verification script. If this script is from a single signature
     * account the resulting list will only contain one key.
     * <p>
     * In case of a multi-sig script, the public keys are returned in their natural ordering (public key value). This
     * is also the order in which they appear in the script.
     *
     * @return the list of public keys encoded in this script.
     */
    public List<ECPublicKey> getPublicKeys() {
        BinaryReader reader = new BinaryReader(this.script);
        List<ECPublicKey> keys = new ArrayList<>();
        try {
            if (isSingleSigScript()) {
                reader.readByte(); // OpCode.PUSHDATA1;
                reader.readByte(); // size byte
                keys.add(new ECPublicKey(reader.readECPoint()));
                return keys;
            } else if (isMultiSigScript()) {
                reader.readPushInteger(); // Signing Threshold (n of m)
                while (reader.readByte() == OpCode.PUSHDATA1.getCode()) {
                    reader.readByte(); // size byte
                    keys.add(new ECPublicKey(reader.readECPoint()));
                }
                return keys;
            }
        } catch (IOException | DeserializationException e) {
            // Shouldn't happen because the underlying stream is a ByteArrayInputStream.
            throw new RuntimeException(e);
        }
        throw new ScriptFormatException("The verification script is in an incorrect format. No public keys can be " +
                "read from it.");
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VerificationScript)) {
            return false;
        }
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
                "script=" + toHexStringNoPrefix(script) + '}';
    }

}
