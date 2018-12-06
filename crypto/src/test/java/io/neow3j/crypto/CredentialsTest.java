package io.neow3j.crypto;

import io.neow3j.utils.Numeric;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static io.neow3j.crypto.WIF.getPrivateKeyFromWIF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CredentialsTest {

    private static final Logger LOG = LoggerFactory.getLogger(CredentialsTest.class);

    @Test
    public void test1() {

        String privateKeyAsWIF = "L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A";
        byte[] privateKey = getPrivateKeyFromWIF(privateKeyAsWIF);

        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);

        Credentials credentials = Credentials.create(ecKeyPair);

        assertEquals("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", credentials.getAddress());

        String privateKeyAsWIFExported = credentials.exportAsWIF();
        assertEquals(privateKeyAsWIF, privateKeyAsWIFExported);

    }

    @Test
    public void test2() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {

        ECKeyPair ecKeyPair1 = Keys.createEcKeyPair();
        Credentials credentials1 = Credentials.create(ecKeyPair1);

        String privateKeyAsWIF1 = credentials1.exportAsWIF();
        String address1 = credentials1.getAddress();

        byte[] privateKey = getPrivateKeyFromWIF(privateKeyAsWIF1);
        ECKeyPair ecKeyPair2 = ECKeyPair.create(privateKey);
        Credentials credentials2 = Credentials.create(ecKeyPair2);

        assertEquals(address1, credentials2.getAddress());
        assertEquals(privateKeyAsWIF1, credentials2.exportAsWIF());

    }

    @Test
    public void test3() {
        Credentials credentials = Credentials.create(
                "9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3",
                "0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6");
        assertEquals("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", credentials.getAddress());
        String privateKeyAsWIFExported = credentials.exportAsWIF();
        assertEquals("L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A", privateKeyAsWIFExported);
    }

    @Test
    public void test4() {
        Credentials credentials = Credentials.create("9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3",
                "0465bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d601d2ea55bbc8eb03bc449a2a1692c2521714ef31c7183ea098f27b7098e8981c"
        );
        assertEquals("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", credentials.getAddress());
        String privateKeyAsWIFExported = credentials.exportAsWIF();
        assertEquals("L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A", privateKeyAsWIFExported);
    }

    @Test
    public void test5() {
        String privateKeyAsWIF = "KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr";
        byte[] privateKey = getPrivateKeyFromWIF(privateKeyAsWIF);

        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);

        Credentials credentials = Credentials.create(ecKeyPair);

        assertEquals("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", credentials.getAddress());

        String privateKeyAsWIFExported = credentials.exportAsWIF();
        assertEquals(privateKeyAsWIF, privateKeyAsWIFExported);
    }

    @Test
    public void test6() {
        byte[] publicKeyEncoded = Keys.getPublicKeyEncoded(
                Numeric.hexStringToByteArray("0465bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d601d2ea55bbc8eb03bc449a2a1692c2521714ef31c7183ea098f27b7098e8981c")
        );

        ECKeyPair ecKeyPair = new ECKeyPair(
                Numeric.toBigIntNoPrefix("9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3"),
                Numeric.toBigInt(publicKeyEncoded)
        );

        Credentials credentials = Credentials.create(ecKeyPair);

        assertEquals("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", credentials.getAddress());
        String privateKeyAsWIFExported = credentials.exportAsWIF();
        assertEquals("L25kgAQJXNHnhc7Sx9bomxxwVSMsZdkaNQ3m2VfHrnLzKWMLP13A", privateKeyAsWIFExported);
    }

    @Test
    public void test7() {
        ECKeyPair ecKeyPair = new ECKeyPair(
                Numeric.toBigIntNoPrefix("9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3"),
                Numeric.toBigIntNoPrefix("0465bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d601d2ea55bbc8eb03bc449a2a1692c2521714ef31c7183ea098f27b7098e8981c")
        );

        Credentials credentials = Credentials.create(ecKeyPair);

        assertNotEquals("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", credentials.getAddress());
    }

}
