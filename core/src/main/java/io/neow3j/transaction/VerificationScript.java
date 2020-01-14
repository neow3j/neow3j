package io.neow3j.transaction;

import static io.neow3j.constants.NeoConstants.MAX_PUBLIC_KEYS_PER_MULTISIG_ACCOUNT;

import io.neow3j.constants.InteropServiceCode;
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
import java.io.ByteArrayInputStream;
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
        if (signingThreshold < 2 || signingThreshold > publicKeys.size()) {
            throw new IllegalArgumentException("Signing threshold must be at least 2 and not " +
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

    public byte[] getScript() {
        return script;
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
        return IOUtils.getSizeOfVarInt(this.script.length) + this.script.length;
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
            try (ByteArrayInputStream stream = new ByteArrayInputStream(script, 0, script.length)) {
                return new BinaryReader(stream).readPushInteger();
            } catch (IOException e) {
                // IOExceptions will not occur when using the ByteArrayInputStream.
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
        String interopCode = Numeric.toHexString(ArrayUtils.getLastNBytes(this.script, 4));
        return interopCode.equals(InteropServiceCode.NEO_CRYPTO_CHECKSIG.getCode());
    }

    /**
     * Checks if this verification script is from multi signature account.
     *
     * @return true if this script is from a multi signature account. False, otherwise.
     */
    public boolean isMultiSigScript() {
        String interopCode = Numeric.toHexString(ArrayUtils.getLastNBytes(this.script, 4));
        return interopCode.equals(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIG.getCode());
    }

    /**
     * Gets the public keys that are encoded in this verification script. If this script is from a
     * single signature account the resulting list will only contain one key.
     *
     * @return the list of public keys encoded in this script.
     */
    public List<ECPublicKey> getPublicKeys() {
        try (ByteArrayInputStream stream =
                new ByteArrayInputStream(this.script, 0, this.script.length)) {
            BinaryReader reader = new BinaryReader(stream);
            List<ECPublicKey> keys = new ArrayList<>();
            if (isSingleSigScript()) {
                reader.readByte(); // OpCode.PUSHBYTES33;
                keys.add(new ECPublicKey(reader.readECPoint()));
                return keys;
            } else if (isMultiSigScript()) {
                reader.readPushInteger();
                while (reader.readByte() == OpCode.PUSHBYTES33.getValue()) {
                    keys.add(new ECPublicKey(reader.readECPoint()));
                }
                return keys;
            }
        } catch (IOException e) {
            // IOExceptions will not occur when using the ByteArrayInputStream.
        }
        throw new ScriptFormatException("No public keys can be determined because this script does "
                + "not apply to the format of a signature verification script.");
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
                "script=" + Numeric.toHexStringNoPrefix(script) + '}';
    }
}
