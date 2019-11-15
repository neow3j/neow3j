package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.utils.Keys;
import io.neow3j.crypto.WIF;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.model.types.GASAsset;
import io.neow3j.protocol.core.methods.response.NeoGetClaimable.Claim;
import io.neow3j.protocol.core.methods.response.NeoGetClaimable.Claimables;
import io.neow3j.utils.Numeric;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClaimTransactionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClaimTransactionTest.class);

    @Test
    public void serialize_Signed() {

        String claimableTxId = "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff";
        int idx = 0;
        String receivingAdr = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        byte[] invocationScript = Numeric.hexStringToByteArray("400c40efd5f4a37b09fb8dca3e9cd6486c1b2d46c0319ac216c348f546ff44bb5fc3a328a43f2f49c9b2aa4cb1ce3f40327fd8403966e117745eb5c1266614f7d4");
        BigInteger publicKey = Numeric.toBigIntNoPrefix("031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4a");
        ClaimTransaction signedTx = new ClaimTransaction.Builder()
                .output(new RawTransactionOutput(GASAsset.HASH_ID, "7264", receivingAdr))
                .claim(new RawTransactionInput(claimableTxId, idx))
                .script(new RawScript(invocationScript, RawVerificationScript.fromPublicKey(publicKey).getScript())
        ).build();

        byte[] signedTxArray = signedTx.toArray();
        LOG.info("serialized: " + Numeric.toHexStringNoPrefix(signedTxArray));

        assertEquals(
                "020001ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000000001e72d286979ee6cb1b7e65dfddfb2e384100b8d148e7758de42e4168b71792c600060d020a900000023ba2703c53263e8d6e522dc32203339dcd8eee90141400c40efd5f4a37b09fb8dca3e9cd6486c1b2d46c0319ac216c348f546ff44bb5fc3a328a43f2f49c9b2aa4cb1ce3f40327fd8403966e117745eb5c1266614f7d42321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac",
                Numeric.toHexStringNoPrefix(signedTxArray));
    }


    @Test
    public void deserialize_Signed() throws IllegalAccessException, InstantiationException {

        String receivingAdr = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        String claimValue = "7264";
        String txId = "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff";
        int index = 0;
        String rawTransaction = "020001ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000000001e72d286979ee6cb1b7e65dfddfb2e384100b8d148e7758de42e4168b71792c600060d020a900000023ba2703c53263e8d6e522dc32203339dcd8eee90141400c40efd5f4a37b09fb8dca3e9cd6486c1b2d46c0319ac216c348f546ff44bb5fc3a328a43f2f49c9b2aa4cb1ce3f40327fd8403966e117745eb5c1266614f7d42321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac";
        byte[] rawTransactionArray = Numeric.hexStringToByteArray(rawTransaction);
        ClaimTransaction claimTransaction = NeoSerializableInterface.from(rawTransactionArray, ClaimTransaction.class);
        assertNotNull(claimTransaction);
        assertEquals(
                new RawTransactionInput(txId, index),
                claimTransaction.getClaims().get(0));
        assertEquals(
                new RawTransactionOutput(GASAsset.HASH_ID, claimValue, receivingAdr),
                claimTransaction.getOutputs().get(0));
    }

    @Test
    public void create_raw_transaction() {

        String wif = "KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr";
        ECKeyPair ecKeyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif));
        String adr = Keys.getAddress(ecKeyPair.getPublicKey()); // "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"
        String txId = "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff";
        int index = 0;
        String claimValue = "7264";
        Claim claim = new Claim(txId, index, null, null , null, null, null, claimValue);
        Claimables claimables = new Claimables(Arrays.asList(claim), adr, claimValue);

        ClaimTransaction tx = ClaimTransaction.fromClaimables(claimables, adr);
        byte[] unsignedTxArray = tx.toArrayWithoutScripts();

        RawScript witness = RawScript.createWitness(unsignedTxArray, ecKeyPair);
        tx.addScript(witness);

        byte[] signedTxArray = tx.toArray();
        String txHexString = Numeric.toHexStringNoPrefix(signedTxArray);
        assertEquals(
                "020001ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000000001e72d286979ee6cb1b7e65dfddfb2e384100b8d148e7758de42e4168b71792c600060d020a900000023ba2703c53263e8d6e522dc32203339dcd8eee90141400c40efd5f4a37b09fb8dca3e9cd6486c1b2d46c0319ac216c348f546ff44bb5fc3a328a43f2f49c9b2aa4cb1ce3f40327fd8403966e117745eb5c1266614f7d42321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac",
                txHexString);
    }

    @Test
    public void testGetTxId() {
        RawTransactionInput in = new RawTransactionInput("887eb3e48f468dac5c42d4877cd5f0edde20bab79e288b43ad7cf8c0c6c657f1", 0);
        RawTransactionOutput out = new RawTransactionOutput(GASAsset.HASH_ID, "0.78719928", "Ach3RRuGyFC3vQFA6AZ5yktuYB4jtQyT12");

        ClaimTransaction tx = new ClaimTransaction.Builder().input(in).output(out).build();

        String expectedTxId = "47eb138abf497a50ee4b0111e87083c75647f16f3eee27c8f1de4f420da382cd";
        assertEquals(expectedTxId, tx.getTxId());
    }
}
