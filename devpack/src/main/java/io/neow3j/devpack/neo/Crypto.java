package io.neow3j.devpack.neo;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.devpack.ECPoint;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Hash256;
import io.neow3j.devpack.annotations.Syscall;

/**
 * Offers cryptographic functions for use in smart contracts.
 */
public class Crypto {

    /**
     * Applies the SHA-256 hashing algorithm to the given byte array.
     *
     * @param value The bytes to hash.
     * @return the 256 bit long hash.
     */
    @Syscall(InteropServiceCode.NEO_CRYPTO_SHA256)
    public native static Hash256 sha256(byte[] value);

    /**
     * Applies the RIPEMD-160 hashing algorithm to the given byte array.
     *
     * @param value The bytes to hash.
     * @return the 160 bit long hash.
     */
    @Syscall(InteropServiceCode.NEO_CRYPTO_RIPEMD160)
    public native static byte[] ripemd160(byte[] value);

    /**
     * First applies SHA-256 to the given byte array and then RIPEMD-160 to the intermediary hash.
     *
     * @param value The bytes to hash.
     * @return the 160 bit long hash.
     */
    @Syscall(InteropServiceCode.NEO_CRYPTO_SHA256)
    @Syscall(InteropServiceCode.NEO_CRYPTO_RIPEMD160)
    public native static Hash160 hash160(byte[] value);

    /**
     * Applies SHA-256 twice to the given byte array.
     *
     * @param value The bytes to hash.
     * @return the 256 bit long hash.
     */
    @Syscall(InteropServiceCode.NEO_CRYPTO_SHA256)
    @Syscall(InteropServiceCode.NEO_CRYPTO_SHA256)
    public native static Hash256 hash256(byte[] value);

    public static class ECDSA {

        public static class Secp256r1 {

            /**
             * Verifies if the given ECDSA signature was produces from the given message and public
             * key using the secp256r1 curve.
             *
             * @param message The signed message
             * @param pubKey The public key of the signing key pair.
             * @param signature The signature created from the message.
             * @return {@code True}, if the signature is correct. {@code False}, otherwise.
             */
            @Syscall(InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1)
            public native static boolean verify(byte[] message, ECPoint pubKey, byte[] signature);

            /**
             * Verifies if the given ECDSA signatures were produced from the given message and
             * public keys using the secp256r1 curve.
             *
             * @param message The signed message
             * @param pubKeys The public keys of the signing key pairs.
             * @param signature The signatures created from the message.
             * @return {@code True}, if the signatures are correct. {@code False}, otherwise.
             */
            @Syscall(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1)
            public native static boolean checkMultiSig(byte[] message, ECPoint[] pubKeys,
                    byte[][] signature);
        }

        public static class Secp256k1 {

            /**
             * Verifies if the given ECDSA signature was produces from the given message and public
             * key using the secp256k1 curve.
             *
             * @param message The signed message
             * @param pubKey The public key of the signing key pair.
             * @param signature The signature created from the message.
             * @return {@code True}, if the signature is correct. {@code False}, otherwise.
             */
            @Syscall(InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256K1)
            public native static boolean verify(byte[] message, ECPoint pubKey, byte[] signature);

            /**
             * Verifies if the given ECDSA signatures were produced from the given message and
             * public keys using the secp256k1 curve.
             *
             * @param message The signed message
             * @param pubKeys The public keys of the signing key pairs.
             * @param signature The signatures created from the message.
             * @return {@code True}, if the signatures are correct. {@code False}, otherwise.
             */
            @Syscall(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1)
            public native static boolean checkMultiSig(byte[] message, ECPoint[] pubKeys,
                    byte[][] signature);
        }
    }
}
