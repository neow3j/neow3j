package io.neow3j.transaction;

import io.neow3j.crypto.Credentials;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.utils.Numeric;

import java.math.BigInteger;

/**
 * Keys generated for unit testing purposes.
 */
public class SampleKeys {

    // Account 1:
    public static final String PRIVATE_KEY_STRING_1 =
            "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3";

    public static final String PUBLIC_KEY_STRING_1 = "0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6";

    public static final String ADDRESS_1 = "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ";
    public static final String PASSWORD_1 = "Insecure Pa55w0rd";

    static final BigInteger PRIVATE_KEY_1 = Numeric.toBigIntNoPrefix(PRIVATE_KEY_STRING_1);
    static final BigInteger PUBLIC_KEY_1 = Numeric.toBigIntNoPrefix(PUBLIC_KEY_STRING_1);

    static final ECKeyPair KEY_PAIR_1 = new ECKeyPair(PRIVATE_KEY_1, PUBLIC_KEY_1);

    public static final Credentials CREDENTIALS_1 = new Credentials(KEY_PAIR_1);


    // Account 2:
    public static final String PRIVATE_KEY_STRING_2 =
            "fbd35970456f1aa51fd8fb70b4a1f717eba072240f8a4f408f529fe3e7678ace";

    public static final String PUBLIC_KEY_STRING_2 = "025dd091303c62a683fab1278349c3475c958f4152292495350571d3e998611d43";

    public static final String ADDRESS_2 = "AdGPiWRqqoFMauM6anTNFB7MyBwQhEANyZ";
    public static final String PASSWORD_2 = "q1w2e3!@#";

    static final BigInteger PRIVATE_KEY_2 = Numeric.toBigIntNoPrefix(PRIVATE_KEY_STRING_2);

    static final BigInteger PUBLIC_KEY_2 = Numeric.toBigIntNoPrefix(PUBLIC_KEY_STRING_2);

    static final ECKeyPair KEY_PAIR_2 = new ECKeyPair(PRIVATE_KEY_2, PUBLIC_KEY_2);

    public static final Credentials CREDENTIALS_2 = new Credentials(KEY_PAIR_2);


    private SampleKeys() {
    }
}
