package io.neow3j.crypto;

import io.neow3j.constants.NeoConstants;
import io.neow3j.utils.Numeric;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static io.neow3j.crypto.Hash.hmacSha512;

/**
 * BIP-32 key pair.
 * <p>
 * Adapted from:
 * <a href="https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/crypto/DeterministicKey.java">DeterministicKey.java</a>
 * <p>
 * Implementation based and adapted from:
 * <a href="https://github.com/web3j/web3j/blob/116539fff875a083c896b2d569d17416dfeb8a6f/crypto/src/main/java/org/web3j/crypto/Bip32ECKeyPair.java">Bip32ECKeyPair.java</a>
 */
public class Bip32ECKeyPair extends ECKeyPair {

    public static final int HARDENED_BIT = 0x80000000;

    private final boolean parentHasPrivate;
    private final int childNumber;
    private final int depth;
    private final byte[] chainCode;
    private int parentFingerprint;

    private ECPoint publicKeyPoint;

    public Bip32ECKeyPair(ECPrivateKey privateKey, ECPublicKey publicKey, int childNumber, byte[] chainCode,
            Bip32ECKeyPair parent) {
        super(privateKey, publicKey);
        this.parentHasPrivate = parent != null && parent.hasPrivateKey();
        this.childNumber = childNumber;
        this.depth = parent == null ? 0 : parent.depth + 1;
        this.chainCode = Arrays.copyOf(chainCode, chainCode.length);
        this.parentFingerprint = parent != null ? parent.getFingerprint() : 0;
    }

    public static Bip32ECKeyPair create(ECPrivateKey privateKey, byte[] chainCode) {
        return create(privateKey.getInt(), chainCode);
    }

    public static Bip32ECKeyPair create(BigInteger privateKey, byte[] chainCode) {
        ECPrivateKey ecPrivateKey = new ECPrivateKey(privateKey);
        return new Bip32ECKeyPair(ecPrivateKey, Sign.publicKeyFromPrivate(ecPrivateKey), 0, chainCode, null);
    }

    public static Bip32ECKeyPair create(byte[] privateKey, byte[] chainCode) {
        return create(Numeric.toBigInt(privateKey), chainCode);
    }

    public static Bip32ECKeyPair generateKeyPair(byte[] seed) {
        byte[] i = hmacSha512("Bitcoin seed".getBytes(), seed);
        byte[] il = Arrays.copyOfRange(i, 0, 32);
        byte[] ir = Arrays.copyOfRange(i, 32, 64);
        Arrays.fill(i, (byte) 0);
        Bip32ECKeyPair keypair = Bip32ECKeyPair.create(il, ir);
        Arrays.fill(il, (byte) 0);
        Arrays.fill(ir, (byte) 0);

        return keypair;
    }

    public static Bip32ECKeyPair deriveKeyPair(Bip32ECKeyPair master, int[] path) {
        Bip32ECKeyPair curr = master;
        if (path != null) {
            for (int childNumber : path) {
                curr = curr.deriveChildKey(childNumber);
            }
        }

        return curr;
    }

    private static byte[] bigIntegerToBytes32(BigInteger b) {
        final int numBytes = 32;

        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    private static boolean isHardened(int a) {
        return (a & HARDENED_BIT) != 0;
    }

    private Bip32ECKeyPair deriveChildKey(int childNumber) {
        if (!hasPrivateKey()) {
            byte[] parentPublicKey = getPublicKeyPoint().getEncoded(true);
            ByteBuffer data = ByteBuffer.allocate(37);
            data.put(parentPublicKey);
            data.putInt(childNumber);
            byte[] i = hmacSha512(getChainCode(), data.array());
            byte[] il = Arrays.copyOfRange(i, 0, 32);
            byte[] chainCode = Arrays.copyOfRange(i, 32, 64);
            Arrays.fill(i, (byte) 0);
            BigInteger ilInt = new BigInteger(1, il);
            Arrays.fill(il, (byte) 0);
            ECPoint ki = Sign.publicPointFromPrivateKey(new ECPrivateKey(ilInt)).add(getPublicKeyPoint());

            final byte[] bytesPrivKeyEncoded = ki.getEncoded(true);
            ECPublicKey ecPublicKey = Sign.publicKeyFromPrivate(new ECPrivateKey(bytesPrivKeyEncoded));

            return new Bip32ECKeyPair(null, ecPublicKey, childNumber, chainCode, this);
        } else {
            ByteBuffer data = ByteBuffer.allocate(37);
            if (isHardened(childNumber)) {
                data.put(getPrivateKeyBytes33());
            } else {
                byte[] parentPublicKey = getPublicKeyPoint().getEncoded(true);
                data.put(parentPublicKey);
            }
            data.putInt(childNumber);
            byte[] i = hmacSha512(getChainCode(), data.array());
            byte[] il = Arrays.copyOfRange(i, 0, 32);
            byte[] chainCode = Arrays.copyOfRange(i, 32, 64);
            Arrays.fill(i, (byte) 0);
            BigInteger ilInt = new BigInteger(1, il);
            Arrays.fill(il, (byte) 0);
            BigInteger privateKey =
                    getPrivateKey().getInt().add(ilInt).mod(NeoConstants.secp256r1DomainParams().getN());
            ECPrivateKey ecPrivateKey = new ECPrivateKey(privateKey);
            ECPublicKey ecPublicKey = Sign.publicKeyFromPrivate(ecPrivateKey);

            return new Bip32ECKeyPair(ecPrivateKey, ecPublicKey, childNumber, chainCode, this);
        }
    }

    private int getFingerprint() {
        byte[] id = getIdentifier();
        return id[3] & 0xFF | (id[2] & 0xFF) << 8 | (id[1] & 0xFF) << 16 | (id[0] & 0xFF) << 24;
    }

    public int getDepth() {
        return depth;
    }

    public int getParentFingerprint() {
        return parentFingerprint;
    }

    public byte[] getChainCode() {
        return chainCode;
    }

    public int getChildNumber() {
        return childNumber;
    }

    private byte[] getIdentifier() {
        return Hash.sha256AndThenRipemd160(getPublicKeyPoint().getEncoded(true));
    }

    public ECPoint getPublicKeyPoint() {
        if (publicKeyPoint == null) {
            publicKeyPoint = Sign.publicPointFromPrivateKey(getPrivateKey());
        }
        return publicKeyPoint;
    }

    public byte[] getPrivateKeyBytes33() {
        final int numBytes = 33;

        byte[] bytes33 = new byte[numBytes];
        byte[] priv = bigIntegerToBytes32(getPrivateKey().getInt());
        System.arraycopy(priv, 0, bytes33, numBytes - priv.length, priv.length);
        return bytes33;
    }

    private boolean hasPrivateKey() {
        return this.getPrivateKey() != null || parentHasPrivate;
    }

}
