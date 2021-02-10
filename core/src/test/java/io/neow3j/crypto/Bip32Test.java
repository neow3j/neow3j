package io.neow3j.crypto;

import static io.neow3j.crypto.Bip32ECKeyPair.HARDENED_BIT;
import static io.neow3j.crypto.Hash.sha256;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.neow3j.constants.NeoConstants;
import io.neow3j.utils.Numeric;
import java.nio.ByteBuffer;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * BIP-32 implementation test.
 *
 * <p>Test vectors taken from BIP-32 definition
 * https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki
 * </p>
 *
 * <p>Test implementation strongly based from and adapted:
 * https://github.com/web3j/web3j/blob/116539fff875a083c896b2d569d17416dfeb8a6f/crypto/src/test
 * /java/org/web3j/crypto/Bip32Test.java
 * </p>
 */
public class Bip32Test {

    private static MockedStatic<NeoConstants> mockNeoConstants;

    static byte[] addChecksum(byte[] input) {
        int inputLength = input.length;
        byte[] checksummed = new byte[inputLength + 4];
        System.arraycopy(input, 0, checksummed, 0, inputLength);
        byte[] checksum = hashTwice(input);
        System.arraycopy(checksum, 0, checksummed, inputLength, 4);
        return checksummed;
    }

    static byte[] serializePublic(Bip32ECKeyPair pair) {
        return serialize(pair, 0x0488B21E, true);
    }

    static byte[] serializePrivate(Bip32ECKeyPair pair) {
        return serialize(pair, 0x0488ADE4, false);
    }

    private static byte[] hashTwice(byte[] input) {
        return sha256(sha256(input));
    }

    private static byte[] serialize(Bip32ECKeyPair pair, int header, boolean pub) {
        ByteBuffer ser = ByteBuffer.allocate(78);
        ser.putInt(header);
        ser.put((byte) pair.getDepth());
        ser.putInt(pair.getParentFingerprint());
        ser.putInt(pair.getChildNumber());
        ser.put(pair.getChainCode());
        ser.put(pub ? pair.getPublicKeyPoint().getEncoded(true) : pair.getPrivateKeyBytes33());
        return ser.array();
    }

    @BeforeClass
    public static void setUp() {
        setUpMock();
    }

    @AfterClass
    public static void cleanUp() {
        cleanUpMock();
    }

    @Test
    public void deriveKeyPairVector1() {

        // Chain m
        testGenerated(
                "000102030405060708090a0b0c0d0e0f",
                "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi",
                "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8",
                null);

        // Chain m/0H
        testGenerated(
                "000102030405060708090a0b0c0d0e0f",
                "xprv9uHRZZhk6KAJC1avXpDAp4MDc3sQKNxDiPvvkX8Br5ngLNv1TxvUxt4cV1rGL5hj6KCesnDYUhd7oWgT11eZG7XnxHrnYeSvkzY7d2bhkJ7",
                "xpub68Gmy5EdvgibQVfPdqkBBCHxA5htiqg55crXYuXoQRKfDBFA1WEjWgP6LHhwBZeNK1VTsfTFUHCdrfp1bgwQ9xv5ski8PX9rL2dZXvgGDnw",
                new int[]{0 | HARDENED_BIT});

        // Chain m/0H/1
        testGenerated(
                "000102030405060708090a0b0c0d0e0f",
                "xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs",
                "xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ",
                new int[]{0 | HARDENED_BIT, 1});

        // Chain m/0H/1/2H
        testGenerated(
                "000102030405060708090a0b0c0d0e0f",
                "xprv9z4pot5VBttmtdRTWfWQmoH1taj2axGVzFqSb8C9xaxKymcFzXBDptWmT7FwuEzG3ryjH4ktypQSAewRiNMjANTtpgP4mLTj34bhnZX7UiM",
                "xpub6D4BDPcP2GT577Vvch3R8wDkScZWzQzMMUm3PWbmWvVJrZwQY4VUNgqFJPMM3No2dFDFGTsxxpG5uJh7n7epu4trkrX7x7DogT5Uv6fcLW5",
                new int[]{0 | HARDENED_BIT, 1, 2 | HARDENED_BIT});

        // Chain m/0H/1/2H/2
        testGenerated(
                "000102030405060708090a0b0c0d0e0f",
                "xprvA2JDeKCSNNZky6uBCviVfJSKyQ1mDYahRjijr5idH2WwLsEd4Hsb2Tyh8RfQMuPh7f7RtyzTtdrbdqqsunu5Mm3wDvUAKRHSC34sJ7in334",
                "xpub6FHa3pjLCk84BayeJxFW2SP4XRrFd1JYnxeLeU8EqN3vDfZmbqBqaGJAyiLjTAwm6ZLRQUMv1ZACTj37sR62cfN7fe5JnJ7dh8zL4fiyLHV",
                new int[]{0 | HARDENED_BIT, 1, 2 | HARDENED_BIT, 2});

        // Chain m/0H/1/2H/2/1000000000
        testGenerated(
                "000102030405060708090a0b0c0d0e0f",
                "xprvA41z7zogVVwxVSgdKUHDy1SKmdb533PjDz7J6N6mV6uS3ze1ai8FHa8kmHScGpWmj4WggLyQjgPie1rFSruoUihUZREPSL39UNdE3BBDu76",
                "xpub6H1LXWLaKsWFhvm6RVpEL9P4KfRZSW7abD2ttkWP3SSQvnyA8FSVqNTEcYFgJS2UaFcxupHiYkro49S8yGasTvXEYBVPamhGW6cFJodrTHy",
                new int[]{0 | HARDENED_BIT, 1, 2 | HARDENED_BIT, 2, 1000000000});
    }

    @Test
    public void deriveKeyPairVector2() {
        // Chain m
        testGenerated(
                "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
                "xprv9s21ZrQH143K31xYSDQpPDxsXRTUcvj2iNHm5NUtrGiGG5e2DtALGdso3pGz6ssrdK4PFmM8NSpSBHNqPqm55Qn3LqFtT2emdEXVYsCzC2U",
                "xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47LJhkJ8UB7WEGuduB",
                null);

        // Chain m/0
        testGenerated(
                "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
                "xprv9vHkqa6EV4sPZHYqZznhT2NPtPCjKuDKGY38FBWLvgaDx45zo9WQRUT3dKYnjwih2yJD9mkrocEZXo1ex8G81dwSM1fwqWpWkeS3v86pgKt",
                "xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r1D3tgFxHmwMkQTPH",
                new int[]{0});

        // Chain m/0/2147483647H
        testGenerated(
                "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
                "xprv9wSp6B7kry3Vj9m1zSnLvN3xH8RdsPP1Mh7fAaR7aRLcQMKTR2vidYEeEg2mUCTAwCd6vnxVrcjfy2kRgVsFawNzmjuHc2YmYRmagcEPdU9",
                "xpub6ASAVgeehLbnwdqV6UKMHVzgqAG8Gr6riv3Fxxpj8ksbH9ebxaEyBLZ85ySDhKiLDBrQSARLq1uNRts8RuJiHjaDMBU4Zn9h8LZNnBC5y4a",
                new int[]{0, 2147483647 | HARDENED_BIT});

        // Chain m/0/2147483647H/1
        testGenerated(
                "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
                "xprv9zFnWC6h2cLgpmSA46vutJzBcfJ8yaJGg8cX1e5StJh45BBciYTRXSd25UEPVuesF9yog62tGAQtHjXajPPdbRCHuWS6T8XA2ECKADdw4Ef",
                "xpub6DF8uhdarytz3FWdA8TvFSvvAh8dP3283MY7p2V4SeE2wyWmG5mg5EwVvmdMVCQcoNJxGoWaU9DCWh89LojfZ537wTfunKau47EL2dhHKon",
                new int[]{0, 2147483647 | HARDENED_BIT, 1});

        // Chain m/0/2147483647H/1/2147483646H
        testGenerated(
                "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
                "xprvA1RpRA33e1JQ7ifknakTFpgNXPmW2YvmhqLQYMmrj4xJXXWYpDPS3xz7iAxn8L39njGVyuoseXzU6rcxFLJ8HFsTjSyQbLYnMpCqE2VbFWc",
                "xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL",
                new int[]{0, 2147483647 | HARDENED_BIT, 1, 2147483646 | HARDENED_BIT});

        // Chain m/0/2147483647H/1/2147483646H/2
        testGenerated(
                "fffcf9f6f3f0edeae7e4e1dedbd8d5d2cfccc9c6c3c0bdbab7b4b1aeaba8a5a29f9c999693908d8a8784817e7b7875726f6c696663605d5a5754514e4b484542",
                "xprvA2nrNbFZABcdryreWet9Ea4LvTJcGsqrMzxHx98MMrotbir7yrKCEXw7nadnHM8Dq38EGfSh6dqA9QWTyefMLEcBYJUuekgW4BYPJcr9E7j",
                "xpub6FnCn6nSzZAw5Tw7cgR9bi15UV96gLZhjDstkXXxvCLsUXBGXPdSnLFbdpq8p9HmGsApME5hQTZ3emM2rnY5agb9rXpVGyy3bdW6EEgAtqt",
                new int[]{0, 2147483647 | HARDENED_BIT, 1, 2147483646 | HARDENED_BIT, 2});
    }

    @Test
    public void deriveKeyPairVector3() {
        // Chain m
        testGenerated(
                "4b381541583be4423346c643850da4b320e46a87ae3d2a4e6da11eba819cd4acba45d239319ac14f863b8d5ab5a0d0c64d2e8a1e7d1457df2e5a3c51c73235be",
                "xprv9s21ZrQH143K25QhxbucbDDuQ4naNntJRi4KUfWT7xo4EKsHt2QJDu7KXp1A3u7Bi1j8ph3EGsZ9Xvz9dGuVrtHHs7pXeTzjuxBrCmmhgC6",
                "xpub661MyMwAqRbcEZVB4dScxMAdx6d4nFc9nvyvH3v4gJL378CSRZiYmhRoP7mBy6gSPSCYk6SzXPTf3ND1cZAceL7SfJ1Z3GC8vBgp2epUt13",
                null);

        // Chain m/0H
        testGenerated(
                "4b381541583be4423346c643850da4b320e46a87ae3d2a4e6da11eba819cd4acba45d239319ac14f863b8d5ab5a0d0c64d2e8a1e7d1457df2e5a3c51c73235be",
                "xprv9uPDJpEQgRQfDcW7BkF7eTya6RPxXeJCqCJGHuCJ4GiRVLzkTXBAJMu2qaMWPrS7AANYqdq6vcBcBUdJCVVFceUvJFjaPdGZ2y9WACViL4L",
                "xpub68NZiKmJWnxxS6aaHmn81bvJeTESw724CRDs6HbuccFQN9Ku14VQrADWgqbhhTHBaohPX4CjNLf9fq9MYo6oDaPPLPxSb7gwQN3ih19Zm4Y",
                new int[]{0 | HARDENED_BIT});
    }

    private void testGenerated(String seed, String expectedPriv, String expectedPub, int[] path) {
        Bip32ECKeyPair pair = Bip32ECKeyPair.generateKeyPair(Numeric.hexStringToByteArray(seed));
        assertNotNull(pair);

        pair = Bip32ECKeyPair.deriveKeyPair(pair, path);
        assertNotNull(pair);

        assertEquals(expectedPriv, Base58.encode(addChecksum(serializePrivate(pair))));
        assertEquals(expectedPub, Base58.encode(addChecksum(serializePublic(pair))));
    }

    private static void setUpMock() {
        // using the secp256k1 curve in order to keep the same test vectors from:
        // https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki
        // and bitcoinj implementation.
        if (mockNeoConstants == null) {
            mockNeoConstants = Mockito.mockStatic(NeoConstants.class);
            X9ECParameters secp256k1 = CustomNamedCurves.getByName("secp256k1");
            ECDomainParameters curve = new ECDomainParameters(
                    secp256k1.getCurve(), secp256k1.getG(),
                    secp256k1.getN(), secp256k1.getH());
            mockNeoConstants.when(NeoConstants::curve)
                    .thenReturn(curve);
            mockNeoConstants.when(NeoConstants::curveParams)
                    .thenReturn(secp256k1);
            mockNeoConstants.when(NeoConstants::halfCurveOrder)
                    .thenReturn(secp256k1.getN().shiftRight(1));
        }
    }

    private static void cleanUpMock() {
        mockNeoConstants.reset();
        mockNeoConstants.clearInvocations();
        mockNeoConstants.close();
    }

}
