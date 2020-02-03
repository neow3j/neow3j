package io.neow3j.crypto;

import io.neow3j.utils.Numeric;

import java.math.BigInteger;

/**
 * Keys generated for unit testing purposes.
 */
public class TestKeys {

    // Example keys taken from the Neo 3 developers guide
    // https://github.com/neo-ngd/NEO3-Development-Guide/tree/master/en/Wallets
    public static final String pubKey1 =
            "02208aea0068c429a03316e37be0e3e8e21e6cda5442df4c5914a19b3a9b6de375";
    public static final String privKey1 =
            "3bf2c2c3a43ee817c5a7704b60e5265e73e585eb85b17091c451ddf72fd80c41";
    public static final String address1 = "Aa63RMYRWHPRcrZNzUnq5SNrPqoV866Spu";
    public static final String verificationScript1 =
            "2102208aea0068c429a03316e37be0e3e8e21e6cda5442df4c5914a19b3a9b6de37568747476aa";

    // Multi-sig keys
    public static final String pubKey2_1 =
            "035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff50";
    public static final String privKey2_1 =
            "97374afac1e801407d6a60006e00d555297c5019788795f017d4cd1fff3df529";
    public static final String pubKey2_2 =
            "03eda286d19f7ee0b472afd1163d803d620a961e1581a8f2704b52c0285f6e022d";
    public static final String privKey2_2 =
            "aab9d4e4223e088aa6eb1f0ce75c11d149625f6d6a19452d765f8737200a4c35";
    public static final String address2 = "AQuqfBZmzejZt4CQc7mkgvEXmSvdMUEBok";
    public static final String verificationScript2 =
            "5221035fdb1d1f06759547020891ae97c729327853aeb1256b6fe0473bc2e9fa42ff502103eda286d19f7e"
                    + "e0b472afd1163d803d620a961e1581a8f2704b52c0285f6e022d5268c7c34cba";

    // Account 1:
    public static final String PRIVATE_KEY_STRING_1 =
            "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3";

    public static final String PUBLIC_KEY_STRING_1 =
            "0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6";

    public static final String ADDRESS_1 = "AUcY65mkxygUB5bXZqYhNKsrq1khuncqr3";
    public static final String PASSWORD_1 = "Insecure Pa55w0rd";

    static final BigInteger PRIVATE_KEY_1 = Numeric.toBigIntNoPrefix(PRIVATE_KEY_STRING_1);
    static final BigInteger PUBLIC_KEY_1 = Numeric.toBigIntNoPrefix(PUBLIC_KEY_STRING_1);

    static final ECKeyPair KEY_PAIR_1 = new ECKeyPair(PRIVATE_KEY_1, PUBLIC_KEY_1);

    // Account 2:
    public static final String PRIVATE_KEY_STRING_2 =
            "fbd35970456f1aa51fd8fb70b4a1f717eba072240f8a4f408f529fe3e7678ace";

    public static final String PUBLIC_KEY_STRING_2 =
            "025dd091303c62a683fab1278349c3475c958f4152292495350571d3e998611d43";

    public static final String ADDRESS_2 = "ALRwuyZTGjV7kN2uTVoKQ2LaeH8e1jbSJ9";
    public static final String PASSWORD_2 = "q1w2e3!@#";

    static final BigInteger PRIVATE_KEY_2 = Numeric.toBigIntNoPrefix(PRIVATE_KEY_STRING_2);

    static final BigInteger PUBLIC_KEY_2 = Numeric.toBigIntNoPrefix(PUBLIC_KEY_STRING_2);

    static final ECKeyPair KEY_PAIR_2 = new ECKeyPair(PRIVATE_KEY_2, PUBLIC_KEY_2);

    private TestKeys() {
    }
}
