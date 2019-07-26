package io.neow3j.crypto;

import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;

/**
 * Credentials wrapper.
 */
public class Credentials {

    private final ECKeyPair ecKeyPair;
    private final String address;

    /**
     * Creates credentials from the given key pair. Derives corresponding address from the key pair.
     *
     * @param ecKeyPair the {@link ECKeyPair} to be encrypted
     */
    public Credentials(ECKeyPair ecKeyPair) {
        this.ecKeyPair = ecKeyPair;
        this.address = ecKeyPair.getAddress();
    }

    /**
     * Constructs credentials only with the address. The key pair is set to null.
     * Use this constructor when you don't have the full key material available yet. E.g. private
     * key is not yet decrypted.
     *
     * @param address The address of which the {@link Credentials} to be created.
     */
    public Credentials(String address) {
        this.address = address;
        this.ecKeyPair = null;
    }

    /**
     * Constructs credentials with the given private and public key. Derives corresponding address
     * from the key pair.
     *
     * @param privateKey The private key to construct the {@link Credentials}.
     * @param publicKey  The private key to construct the {@link Credentials}.
     */
    public Credentials(String privateKey, String publicKey) {
        byte[] pubKey = Numeric.hexStringToByteArray(publicKey);
        if (!Keys.isPublicKeyEncoded(pubKey)) {
            pubKey = Keys.getPublicKeyEncoded(pubKey);
        }
        this.ecKeyPair = new ECKeyPair(Numeric.toBigInt(privateKey), Numeric.toBigInt(pubKey));
        this.address = ecKeyPair.getAddress();
    }


    public ECKeyPair getEcKeyPair() {
        return ecKeyPair;
    }

    public String getAddress() {
        return address;
    }

    public byte[] toScriptHash() {
        return Keys.toScriptHash(this.address);
    }


    public String exportAsWIF() {
        return ecKeyPair.exportAsWIF();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Credentials that = (Credentials) o;

        if (ecKeyPair != null ? !ecKeyPair.equals(that.ecKeyPair) : that.ecKeyPair != null) {
            return false;
        }

        return address != null ? address.equals(that.address) : that.address == null;
    }

    @Override
    public int hashCode() {
        int result = ecKeyPair != null ? ecKeyPair.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }
}
