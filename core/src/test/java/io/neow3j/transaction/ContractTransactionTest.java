package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.crypto.WIF;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.utils.Keys;
import io.neow3j.utils.Numeric;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ContractTransactionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ContractTransactionTest.class);

    @Test
    public void serialize_Unsigned() {
        BigInteger publicKey = Numeric.toBigIntNoPrefix("0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6");
        RawTransaction tUnsigned = new ContractTransaction.Builder()
                .inputs(Arrays.asList(new RawTransactionInput("c94d0f94b0ac9bacd86737c428344cb2d8be9aad296659e85c065d4f88cd2dd2", 0)))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "10.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "90.0", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"))
                .script(new RawScript(new RawInvocationScript(), RawVerificationScript.fromPublicKey(publicKey)))
                .build();

        byte[] tUnsignedArray = tUnsigned.toArray();
        LOG.info("serialized: " + Numeric.toHexStringNoPrefix(tUnsignedArray));

        assertEquals(
                "80000001d22dcd884f5d065ce8596629ad9abed8b24c3428c43767d8ac9bacb0940f4dc90000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d7010023210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac",
                Numeric.toHexStringNoPrefix(tUnsignedArray));
    }

    @Test
    public void serialize_Signed() {
        BigInteger publicKey = Numeric.toBigIntNoPrefix("0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6");
        byte[] invocationScript = Numeric.hexStringToByteArray(
                "40a1c29ef0b8215d5bf8f3649ff1eae3fd5d74bf38c92007ce6aceea60efa5a986ed1c3d7669f9073f572a52dbbdc7ad7908fe22c2859e85d979e405807ce3d644");
        RawTransaction tUnsigned = new ContractTransaction.Builder()
                .input(new RawTransactionInput("c94d0f94b0ac9bacd86737c428344cb2d8be9aad296659e85c065d4f88cd2dd2", 0))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "10.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "90.0", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"))
                .script((new RawScript(new RawInvocationScript(invocationScript), RawVerificationScript.fromPublicKey(publicKey))))
                .build();

        byte[] tUnsignedArray = tUnsigned.toArray();

        assertEquals(
                "80000001d22dcd884f5d065ce8596629ad9abed8b24c3428c43767d8ac9bacb0940f4dc90000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d7014140a1c29ef0b8215d5bf8f3649ff1eae3fd5d74bf38c92007ce6aceea60efa5a986ed1c3d7669f9073f572a52dbbdc7ad7908fe22c2859e85d979e405807ce3d64423210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac",
                Numeric.toHexStringNoPrefix(tUnsignedArray));
    }

    @Test
    public void serialize_Signing_Normal_Address() {

        ECKeyPair keyPair = ECKeyPair.create(Numeric.hexStringToByteArray("9117f4bf9be717c9a90994326897f4243503accd06712162267e77f18b49c3a3"));

        RawTransaction tUnsigned = new ContractTransaction.Builder()
                .input(new RawTransactionInput("65827ac7308f401dfe110555b41b967e3c1177134bd977a21ca036e703ab05d4", 0))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "10.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "90.0", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"))
                .build();

        byte[] tUnsignedArray = tUnsigned.toArrayWithoutScripts();
        tUnsigned.addScript(RawScript.createWitness(tUnsignedArray, keyPair));

        assertEquals(
                "80000001d405ab03e736a01ca277d94b1377113c7e961bb4550511fe1d408f30c77a82650000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d7",
                Numeric.toHexStringNoPrefix(tUnsignedArray)
        );
        assertEquals(
                "80000001d405ab03e736a01ca277d94b1377113c7e961bb4550511fe1d408f30c77a82650000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d70141403711e366fc99e77a110b6c96b5f8828ef956a6d5cfa5cb63273419149011b0f30dc5458faa59e4867d0ac7537e324c98124bb691feca5c5ddf6ed20f4adb778223210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac",
                Numeric.toHexStringNoPrefix(tUnsigned.toArray())
        );
    }

    @Test
    public void serialize_Signing_CheckMultiSig_10_PublicKeys_7_Signatures() {

        ECKeyPair ecKeyPair1 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("Kx9xMQVipBYAAjSxYEoZVatdVQfhYHbMFWSYPinSgAVd1d4Qgbpf"));
        ECKeyPair ecKeyPair2 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("KzbKux44feMetfqdA5Cze9FNAkydRmphoFKnK5TGDdEQ8Nv1poXV"));
        ECKeyPair ecKeyPair3 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("L3hxLFUsNDmkzW6QoLH2PGc2DqGG5Kj1gCVwmr7duWJ9FReYWnjU"));
        ECKeyPair ecKeyPair4 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("Kwc1zMAmYHYQ77jZuopaEgL7FejdyDRxd9jpPQJQFkUH39MQpab9"));
        ECKeyPair ecKeyPair5 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("L4LefFC58PPsPyacCLwGJ7RhuPZLnfLdQUiwNyW6gu11JkSRyx73"));
        ECKeyPair ecKeyPair6 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("L4ToFoNfJR1XwzSEHVQonxjmLfoRW91oVLZ5hGGLakkkKUMHanWC"));
        ECKeyPair ecKeyPair7 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("L1RirHWwQcwEcsUVmF6m4s9wUB8zufUXEHrHmcvuP4cuNTXA1nSM"));
        ECKeyPair ecKeyPair8 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("L1CcDgYLRT1VzGQEzHATyHwPoYHwGTDdD4ugJYESYfGehZ6AtnHw"));
        ECKeyPair ecKeyPair9 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("L2xUnihSNBTh8jrYzLyrxMfFwDbPp1HvDe92r7HxsZQ8rL4NktfE"));
        ECKeyPair ecKeyPair10 = ECKeyPair.create(WIF.getPrivateKeyFromWIF("KyRPzRthggPA3SSRTBgma3VyjKhS3yhSM2jsKVrLRfVkFP5g5bQi"));

        List<BigInteger> publicKeys = new ArrayList<>();
        publicKeys.add(ecKeyPair1.getPublicKey());
        publicKeys.add(ecKeyPair2.getPublicKey());
        publicKeys.add(ecKeyPair3.getPublicKey());
        publicKeys.add(ecKeyPair4.getPublicKey());
        publicKeys.add(ecKeyPair5.getPublicKey());
        publicKeys.add(ecKeyPair6.getPublicKey());
        publicKeys.add(ecKeyPair7.getPublicKey());
        publicKeys.add(ecKeyPair8.getPublicKey());
        publicKeys.add(ecKeyPair9.getPublicKey());
        publicKeys.add(ecKeyPair10.getPublicKey());

        // the multiSig address should be "AJqgaX57U9ua5WxBKfA27wbfEgtR8HwER3"
        String multiSigAddress = Keys.getMultiSigAddress(7, publicKeys);

        RawTransaction tUnsigned = new ContractTransaction.Builder()
                .input(new RawTransactionInput("9feac4774eb0f01ab5d6817c713144b7c020b98f257c30b1105062d434e6f254", 0))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "100.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "900.0", multiSigAddress))
                .build();

        byte[] tUnsignedArray = tUnsigned.toArrayWithoutScripts();

        List<SignatureData> sigs = new ArrayList<>();
        sigs.add(Sign.signMessage(tUnsignedArray, ecKeyPair1));
        sigs.add(Sign.signMessage(tUnsignedArray, ecKeyPair2));
        sigs.add(Sign.signMessage(tUnsignedArray, ecKeyPair3));
        sigs.add(Sign.signMessage(tUnsignedArray, ecKeyPair4));
        sigs.add(Sign.signMessage(tUnsignedArray, ecKeyPair5));
        sigs.add(Sign.signMessage(tUnsignedArray, ecKeyPair6));
        sigs.add(Sign.signMessage(tUnsignedArray, ecKeyPair7));
        tUnsigned.addScript(RawScript.createMultiSigWitness(7, sigs, publicKeys));

        assertEquals(
                "8000000154f2e634d4625010b1307c258fb920c0b74431717c81d6b51af0b04e77c4ea9f0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500e40b540200000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500046bf41400000021a0cd353225ebfe85236802beddc4e6ddcdd372",
                Numeric.toHexStringNoPrefix(tUnsignedArray)
        );
        assertEquals(
                "8000000154f2e634d4625010b1307c258fb920c0b74431717c81d6b51af0b04e77c4ea9f0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500e40b540200000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500046bf41400000021a0cd353225ebfe85236802beddc4e6ddcdd37201fdc7014060a221cf01a80c652663f4d29b7722fc59e256e10a9b6f2fecd7063bfa4f2220f601120d8680e0bd5515f3e9bee20c86e4e88f310dd9236062e577dab01f4afe40d1e817d8cd4e4d661318a771ace005cb935834821e979eb6f47cbb7672df5836ed282b7de0ba8df074127374c4929bbe63fb7eb39f8c9cb5713c6f2a6b672e12409573d2146a7f36374b1994fe06f7fb0896b8fe36be43deda78796930831a65749868348f74c67d2d82fe33b4ac17949fe18ad41f2a69b12bd8731671b3be33684040e2c7807e9babdad84fdd2a015c8e13c5fca79bee052050c4318c7146834b06de291b2648d0ee1e3b3d87608b4a8e4cbe4653d1096963e8fc1f73ea3492613c40569f3d90b577ab93bc8972098788f2b34349f1cbafe3c8dfe64932af0990b802a070fdbce3aad636efd1dfdd59057f61ca6ba0447aec34d823c13ee55b91d4ae405b119765d12a333e449637cbbf07db64150087c9f238cb5dcd466a9eb32ded5c80532f783f6494b61b746ce71d13ed2c46c1a2ecedf7846c5083af190644813b40a5ddacdefe707e91cbc8d07f56cc0c1cb5f97ea5c075cbf3e299bbef764021b8d3d897b14e9dd66fdc810cd045dfbf8c75bd7ca93ca80c455f4d0b9b4f6089adfd5701572102789a9e63a054711b1ce7f91bf0d56886fee3bd9166e91761a92bb0a90fcfd4422103a87b798cb30a4eb0602f3d4a3cc6a8672e4ab7df4c4716e25bfa2ee4299fc5d92103522f02afaf0c14182efc9079501b81c3f60a9568c122706ea308902e59a770e22103001ad1dd28b52453e8059e8c100134eaf8fd3719b8b562a9ab605d9e2f4de0da210243ba6483c794966d0be4642eef7ee88cf5718231bd9ed20382ccd4c0a847251421028143c5faef6c1587cae3230b5d33eed861e34b9247799dc87f508a41cc2e89582102e27a346ae6fda8ab9a63b94b7ffb817f0544c16e8436d970d54ce5a6a2b4a20f2102ba5053463b66b4968249de9854ba3c708352bb126ee2b3276aca0495b6c48dbf2103f38690c96a579df193992e839246859c4d45a38a2e9ef7e9ebbb556b713e0706210292d77b056cbd66f7d28bf031fa51dc672cf4f44692b3b8b584519776f395a3cc5aae",
                Numeric.toHexStringNoPrefix(tUnsigned.toArray())
        );
    }

    @Test
    public void deserialize_CheckSig() throws IllegalAccessException, InstantiationException {
        String rawTransaction = "80000001d405ab03e736a01ca277d94b1377113c7e961bb4550511fe1d408f30c77a82650000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83fc439f56e6e1fb062d89c6f538263d70141403711e366fc99e77a110b6c96b5f8828ef956a6d5cfa5cb63273419149011b0f30dc5458faa59e4867d0ac7537e324c98124bb691feca5c5ddf6ed20f4adb778223210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac";
        byte[] rawTransactionArray = Numeric.hexStringToByteArray(rawTransaction);

        ContractTransaction tx = NeoSerializableInterface.from(rawTransactionArray, ContractTransaction.class);
        assertNotNull(tx);
        byte[] invScript =  Numeric.hexStringToByteArray("403711e366fc99e77a110b6c96b5f8828ef956a6d5cfa5cb63273419149011b0f30dc5458faa59e4867d0ac7537e324c98124bb691feca5c5ddf6ed20f4adb7782");
        assertArrayEquals(invScript, tx.getScripts().get(0).getInvocationScript().getScript());
        byte[] verScript = Numeric.hexStringToByteArray("210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac");
        assertArrayEquals(verScript, tx.getScripts().get(0).getVerificationScript().getScript());
    }

    @Test
    public void deserialize_CheckMultiSig() throws IllegalAccessException, InstantiationException {
        String rawTransaction = "800000016f291e0c1f333d837b84fb707f2f0c91b3f25b6f8c4e397b1d20cc6758e4aed50000019b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc32203339dcd8eee9018240ee9596a52a9033b1103f9a710467b1ac84575426c8a9a2a3c001cb04b2a5b08e266a19e3c216ed1ab8ae2c00b23b9e8ee8d9d8700958fb8655097d789dc990c9409060c6ce30864233dc96bbedaaf2c98fc5e12f673493b63ed0e6f83d760037e56347408cf8fae3cb2a3266a98aaccfcad10c7591a683b5701cf085e1f9c5aa4a695221036245f426b4522e8a2901be6ccc1f71e37dc376726cc6665d80c5997e240568fb210303897394935bb5418b1c1c4cf35513e276c6bd313ddd1330f113ec3dc34fbd0d2102e2baf21e36df2007189d05b9e682f4192a101dcdf07eed7d6313625a930874b453ae";
        byte[] rawTransactionArray = Numeric.hexStringToByteArray(rawTransaction);

        RawTransaction rawTransactionObj = NeoSerializableInterface.from(rawTransactionArray, ContractTransaction.class);
        assertNotNull(rawTransactionObj);
    }

    @Test
    public void deserialize_CheckMultiSig_10_PublicKeys_7_Signatures() throws IllegalAccessException, InstantiationException {
        String rawTransaction = "8000000154f2e634d4625010b1307c258fb920c0b74431717c81d6b51af0b04e77c4ea9f0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500e40b540200000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500046bf41400000021a0cd353225ebfe85236802beddc4e6ddcdd37201fdc7014060a221cf01a80c652663f4d29b7722fc59e256e10a9b6f2fecd7063bfa4f2220f601120d8680e0bd5515f3e9bee20c86e4e88f310dd9236062e577dab01f4afe40d1e817d8cd4e4d661318a771ace005cb935834821e979eb6f47cbb7672df5836ed282b7de0ba8df074127374c4929bbe63fb7eb39f8c9cb5713c6f2a6b672e12409573d2146a7f36374b1994fe06f7fb0896b8fe36be43deda78796930831a65749868348f74c67d2d82fe33b4ac17949fe18ad41f2a69b12bd8731671b3be33684040e2c7807e9babdad84fdd2a015c8e13c5fca79bee052050c4318c7146834b06de291b2648d0ee1e3b3d87608b4a8e4cbe4653d1096963e8fc1f73ea3492613c40569f3d90b577ab93bc8972098788f2b34349f1cbafe3c8dfe64932af0990b802a070fdbce3aad636efd1dfdd59057f61ca6ba0447aec34d823c13ee55b91d4ae405b119765d12a333e449637cbbf07db64150087c9f238cb5dcd466a9eb32ded5c80532f783f6494b61b746ce71d13ed2c46c1a2ecedf7846c5083af190644813b40a5ddacdefe707e91cbc8d07f56cc0c1cb5f97ea5c075cbf3e299bbef764021b8d3d897b14e9dd66fdc810cd045dfbf8c75bd7ca93ca80c455f4d0b9b4f6089adfd5701572102789a9e63a054711b1ce7f91bf0d56886fee3bd9166e91761a92bb0a90fcfd4422103a87b798cb30a4eb0602f3d4a3cc6a8672e4ab7df4c4716e25bfa2ee4299fc5d92103522f02afaf0c14182efc9079501b81c3f60a9568c122706ea308902e59a770e22103001ad1dd28b52453e8059e8c100134eaf8fd3719b8b562a9ab605d9e2f4de0da210243ba6483c794966d0be4642eef7ee88cf5718231bd9ed20382ccd4c0a847251421028143c5faef6c1587cae3230b5d33eed861e34b9247799dc87f508a41cc2e89582102e27a346ae6fda8ab9a63b94b7ffb817f0544c16e8436d970d54ce5a6a2b4a20f2102ba5053463b66b4968249de9854ba3c708352bb126ee2b3276aca0495b6c48dbf2103f38690c96a579df193992e839246859c4d45a38a2e9ef7e9ebbb556b713e0706210292d77b056cbd66f7d28bf031fa51dc672cf4f44692b3b8b584519776f395a3cc5aae";
        byte[] rawTransactionArray = Numeric.hexStringToByteArray(rawTransaction);

        RawTransaction rawTransactionObj = NeoSerializableInterface.from(rawTransactionArray, ContractTransaction.class);
        assertNotNull(rawTransactionObj);
    }

    @Test
    public void verify_Signature() throws SignatureException {

        ECKeyPair ecKeyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF("Kx9xMQVipBYAAjSxYEoZVatdVQfhYHbMFWSYPinSgAVd1d4Qgbpf"));
        String address = ecKeyPair.getAddress();

        RawTransaction unsignedTx = new ContractTransaction.Builder()
                .input(new RawTransactionInput("9feac4774eb0f01ab5d6817c713144b7c020b98f257c30b1105062d434e6f254", 0))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "100.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "900.0", address))
                .build();

        Sign.SignatureData signatureDataTx = Sign.signMessage(unsignedTx.toArrayWithoutScripts(), ecKeyPair);

        String fromAddress = Sign.recoverSigningAddress(unsignedTx.toArrayWithoutScripts(), signatureDataTx);

        assertEquals(address, fromAddress);
    }

    @Test
    public void testGetTxId() {
        RawTransactionInput in = new RawTransactionInput("7aadf91ca8ac1e2c323c025a7e492bee2dd90c783b86ebfc3b18db66b530a76d", 0);
        RawTransactionOutput out = new RawTransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "100000000", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");

        ContractTransaction tx = new ContractTransaction.Builder().input(in).output(out).build();

        String expectedTxId = "dc44739e2f97743f2ed258988327560e2185ed13eec0097938eef4aea584bf04";
        assertEquals(expectedTxId, tx.getTxId());
    }


    @Test
    public void serialize_without_scripts() {
        ContractTransaction tUnsigned = new ContractTransaction.Builder()
                .input(new RawTransactionInput("c94d0f94b0ac9bacd86737c428344cb2d8be9aad296659e85c065d4f88cd2dd2", 0))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "10.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "90.0", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"))
                .build();

        byte[] tx = tUnsigned.toArray();
        String expectedTx = "80000001d22dcd884f5d065ce8596629ad9abed8b24c3428c43767d8ac9bacb0940f4dc90000029b7cffdaa6" +
                "74beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b0000000023ba2703c53263e8d6e522dc322033" +
                "39dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001a711802000000295f83f83f" +
                "c439f56e6e1fb062d89c6f538263d700";

        assertEquals(expectedTx, Numeric.toHexStringNoPrefix(tx));

    }

    @Test
    public void serialize_then_deserialize_without_scripts() throws IllegalAccessException, InstantiationException {
        ContractTransaction tx = new ContractTransaction.Builder()
                .input(new RawTransactionInput("c94d0f94b0ac9bacd86737c428344cb2d8be9aad296659e85c065d4f88cd2dd2", 0))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "10.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "90.0", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"))
                .build();

        tx = NeoSerializableInterface.from(tx.toArray(), ContractTransaction.class);

        assertEquals(0, tx.getScripts().size());
        assertEquals(2, tx.getOutputs().size());
        assertEquals("c94d0f94b0ac9bacd86737c428344cb2d8be9aad296659e85c065d4f88cd2dd2", tx.getInputs().get(0).getPrevHash());
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(0).getAssetId());
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(1).getAssetId());
        assertEquals("10", tx.getOutputs().get(0).getValue());
        assertEquals("90", tx.getOutputs().get(1).getValue());
        assertEquals("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", tx.getOutputs().get(0).getAddress());
        assertEquals("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", tx.getOutputs().get(1).getAddress());
    }

    @Test
    public void serialize_then_deserialize_with_script() throws IllegalAccessException, InstantiationException {
        BigInteger publicKey = Numeric.toBigIntNoPrefix("0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6");
        byte[] invocationScript = Numeric.hexStringToByteArray("40a1c29ef0b8215d5bf8f3649ff1eae3fd5d74bf38c92007ce6ac" +
                "eea60efa5a986ed1c3d7669f9073f572a52dbbdc7ad7908fe22c2859e85d979e405807ce3d644");

        ContractTransaction tx = new ContractTransaction.Builder()
                .input(new RawTransactionInput("c94d0f94b0ac9bacd86737c428344cb2d8be9aad296659e85c065d4f88cd2dd2", 0))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "10.0", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .output(new RawTransactionOutput(NEOAsset.HASH_ID, "90.0", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"))
                .script((new RawScript(new RawInvocationScript(invocationScript), RawVerificationScript.fromPublicKey(publicKey))))
                .build();

        tx = NeoSerializableInterface.from(tx.toArray(), ContractTransaction.class);

        assertEquals(2, tx.getOutputs().size());
        assertEquals("c94d0f94b0ac9bacd86737c428344cb2d8be9aad296659e85c065d4f88cd2dd2", tx.getInputs().get(0).getPrevHash());
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(0).getAssetId());
        assertEquals(NEOAsset.HASH_ID, tx.getOutputs().get(1).getAssetId());
        assertEquals("10", tx.getOutputs().get(0).getValue());
        assertEquals("90", tx.getOutputs().get(1).getValue());
        assertEquals("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", tx.getOutputs().get(0).getAddress());
        assertEquals("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", tx.getOutputs().get(1).getAddress());
        assertEquals(1, tx.getScripts().size());
        assertArrayEquals(invocationScript, tx.getScripts().get(0).getInvocationScript().getScript());
        assertArrayEquals(
                Keys.getVerificationScriptFromPublicKey(publicKey),
                tx.getScripts().get(0).getVerificationScript().getScript()
        );
    }
}
