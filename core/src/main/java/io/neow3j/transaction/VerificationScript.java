package io.neow3j.transaction;

import static io.neow3j.constants.NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.IOUtils;
import io.neow3j.io.NeoSerializable;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.ScriptFormatException;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * <p>
     * The bytes do not necessarily have to be a signature verification script.
     *
     * @param script The script
     */
    public VerificationScript(byte[] script) {
        this.script = script;
    }

    /**
     * Creates a verification script for the given public key.
     *
     * @param publicKey Key to create the script for.
     */
    public VerificationScript(ECPublicKey publicKey) {
        this.script = ScriptBuilder.buildVerificationScript(publicKey.getEncoded(true));
    }

    /**
     * Creates a multi-sig verification script for the given keys and signing threshold.
     *
     * @param publicKeys       The public keys to create the script for.
     * @param signingThreshold The minimum number of public keys needed to sign transactions from
     *                         the given public keys.
     */
    public VerificationScript(List<ECPublicKey> publicKeys, int signingThreshold) {
        if (signingThreshold < 1 || signingThreshold > publicKeys.size()) {
            throw new IllegalArgumentException("Signing threshold must be at least 1 and not " +
                    "higher than the number of public keys.");
        }
        if (publicKeys.size() > MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
            throw new IllegalArgumentException("At max " + MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT +
                    " public keys can take part in a multi-sig account");
        }
        List<byte[]> encodedKeys = publicKeys.stream()
                .map(key -> key.getEncoded(true))
                .collect(Collectors.toList());
        this.script = ScriptBuilder.buildVerificationScript(encodedKeys, signingThreshold);
    }

    /**
     * Gets the raw script.
     *
     * @return the script as a byte array.
     */
    public byte[] getScript() {
        return script;
    }

    /**
     * Serializes this script to a byte array. This is only meant to be used in transaction
     * serialization because it adds the size of the script as a prefix. Use {@link
     * VerificationScript#getScript()} instead to get this scripts byte array.
     *
     * @return the serialized script for usage in a transaction array.
     */
    @Override
    public byte[] toArray() {
        // Only overridden for documentation.
        return super.toArray();
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

    @Override
    public int getSize() {
        return IOUtils.getVarSize(this.script.length) + this.script.length;
    }

    /**
     * Extracts the number of signatures required for signing this verification script.
     *
     * @return The signing threshold.
     * @throws ScriptFormatException if this verification script is not an ordinary address script
     *                               or multi-address script.
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
        throw new ScriptFormatException("The signing threshold cannot be determined because this "
                + "script does not apply to the format of a signature verification script.");
    }

    /**
     * Gets the number of accounts taking part in this verification script.
     *
     * @return The number of accounts.
     * @throws ScriptFormatException if this verification script is not an ordinary address script
     *                               or multi-address script.
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
        if (script.length != 41) {
            return false;
        }
        String interopService = Numeric.toHexStringNoPrefix(ArrayUtils.getLastNBytes(script, 4));
        return script[0] == OpCode.PUSHDATA1.getValue()
                && script[1] == 33 // 33 bytes of public key
                && script[35] == OpCode.PUSHNULL.getValue()
                && script[36] == OpCode.SYSCALL.getValue()
                && interopService.equals(
                        InteropServiceCode.NEO_CRYPTO_ECDSA_SECP256R1_VERIFY.getHash());
    }

    /**
     * Checks if this verification script is from a multi signature account.
     *
     * @return true if this script is from a multi signature account. False, otherwise.
     */
    public boolean isMultiSigScript() {
        if (script.length < 43) {
            return false;
        }
        try {
            BinaryReader reader = new BinaryReader(this.script);
            int n = reader.readPushInteger(); // Signing Threshold (n of m)
            if (n < 1 || n > NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT) {
                return false;
            }

            int m = 0; // Number of participating keys
            while (reader.readByte() == OpCode.PUSHDATA1.getValue()) {
                // Position at PUSHDATA1 + (1 byte data size + 33 bytes + 1 byte to make sure
                // script does not end after the key.
                if (script.length <= reader.getPosition() + 35) {
                    return false;
                }
                // Byte after PUSHDATA1 must have value 33 for 33 bytes of public key data.
                if (reader.readByte() != 33) {
                    return false;
                }
                reader.readEncodedECPoint();
                m++;
                // Mark the current position to be able to reset the last readBytes() which is not a
                // PUSHDATA1 anymore.
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
            if (reader.readByte() != OpCode.PUSHNULL.getValue()) {
                return false;
            }
            if (reader.readByte() != OpCode.SYSCALL.getValue()) {
                return false;
            }
            byte[] interopServiceCode = new byte[4];
            reader.read(interopServiceCode, 0, 4);
            if (!Numeric.toHexStringNoPrefix(interopServiceCode)
                    .equals(InteropServiceCode.NEO_CRYPTO_ECDSA_SECP256R1_CHECKMULTISIG.getHash())) {
                return false;
            }
        } catch (DeserializationException | IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets the public keys that are encoded in this verification script. If this script is from a
     * single signature account the resulting list will only contain one key.
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
                while (reader.readByte() == OpCode.PUSHDATA1.getValue()) {
                    reader.readByte(); // size byte
                    keys.add(new ECPublicKey(reader.readECPoint()));
                }
                return keys;
            }
        } catch (IOException | DeserializationException e) {
            // Shouldn't happen because the underlying stream is a ByteArrayInputStream.
            throw new RuntimeException(e);
        }
        throw new ScriptFormatException("The verification script is in an incorrect format. No "
                + "public keys can be read from it.");
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
}
