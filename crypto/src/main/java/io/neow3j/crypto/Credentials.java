package io.neow3j.crypto;

import io.neow3j.utils.Numeric;

/**
 * Credentials wrapper.
 */
public class Credentials {

    private final ECKeyPair ecKeyPair;
    private final String address;

    private Credentials(ECKeyPair ecKeyPair, String address) {
        this.ecKeyPair = ecKeyPair;
        this.address = address;
    }

    public ECKeyPair getEcKeyPair() {
        return ecKeyPair;
    }

    public String getAddress() {
        return address;
    }

    public static Credentials create(ECKeyPair ecKeyPair) {
        String address = Keys.getAddress(ecKeyPair);
        return new Credentials(ecKeyPair, address);
    }

    public byte[] toScriptHash() {
        return KeyUtils.toScriptHash(this.address);
    }

    public static Credentials create(String privateKey, String publicKey) {
        byte[] pubKey = Numeric.hexStringToByteArray(publicKey);
        if (!Keys.isPublicKeyEncoded(pubKey)) {
            pubKey = Keys.getPublicKeyEncoded(pubKey);
        }
        return create(new ECKeyPair(Numeric.toBigInt(privateKey), Numeric.toBigInt(pubKey)));
    }

    public static Credentials create(String privateKey) {
        return create(ECKeyPair.create(Numeric.toBigInt(privateKey)));
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
