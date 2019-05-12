package io.neow3j.crypto;

import io.neow3j.crypto.transaction.ClaimTransaction;
import io.neow3j.crypto.transaction.ContractTransaction;
import io.neow3j.crypto.transaction.RawInvocationScript;
import io.neow3j.crypto.transaction.RawScript;
import io.neow3j.crypto.transaction.RawTransaction;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.crypto.transaction.RawVerificationScript;
import io.neow3j.crypto.transaction.SignedRawTransaction;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.utils.Numeric;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ClaimTransactionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClaimTransactionTest.class);

    @Test
    public void serialize_Signed() {

        String claimableTxId = "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff";
        int idx = 0;
        String receivingAdr = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";
        // These scripts in hex format do not include the byte length at the front. This is prepended by the instances
        // of RawInvocationScript and RawVerificationScript.
        String invocationScript = "0c40efd5f4a37b09fb8dca3e9cd6486c1b2d46c0319ac216c348f546ff44bb5fc3a328a43f2f49c9b2aa4cb1ce3f40327fd8403966e117745eb5c1266614f7d4";
        String verificationScript = "031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4a";
        ClaimTransaction signedTx = new ClaimTransaction(
            null,
            Arrays.asList(new RawTransactionOutput(0, GASAsset.HASH_ID, "7264", receivingAdr)),
            Arrays.asList(new RawTransactionInput(claimableTxId, idx)),
            Arrays.asList(
                new RawScript(
                    Arrays.asList(new RawInvocationScript(Numeric.hexStringToByteArray(invocationScript))),
                    new RawVerificationScript(Arrays.asList(Numeric.toBigIntNoPrefix(verificationScript)), 1)
                )
            )
        );
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
                new RawTransactionOutput(0, GASAsset.HASH_ID, claimValue, receivingAdr),
                claimTransaction.getOutputs().get(0));
    }

    @Test
    public void create_raw_transaction() {

        String wif = "KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr";
        ECKeyPair ecKeyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif));
        String adr = Keys.getAddress(ecKeyPair.getPublicKey()); // "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"
        byte[] publicKeyByteArray = Numeric.hexStringToByteArray(Numeric.toHexStringNoPrefix(ecKeyPair.getPublicKey()));
        String txId = "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff";
        int index = 0;
        BigDecimal claimValue = BigDecimal.valueOf(7264);
        BigInteger neoValue = BigInteger.valueOf(100000000);
        Claim claim = new Claim(claimValue, txId, index, neoValue, 2007, 2915);

        ClaimTransaction tx = ClaimTransaction.fromClaims(Arrays.asList(claim), adr);
        byte[] unsignedTxArray = tx.toArray();

        List<RawInvocationScript> rawInvocationScriptList = Arrays.asList(
                new RawInvocationScript(Sign.signMessage(unsignedTxArray, ecKeyPair)));
        RawVerificationScript verificationScript = Keys.getVerificationScriptFromPublicKey(publicKeyByteArray);
        tx.addScript(rawInvocationScriptList, verificationScript);

        byte[] signedTxArray = tx.toArray();
        String txHexString = Numeric.toHexStringNoPrefix(signedTxArray);
        assertEquals(
                "020001ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000000001e72d286979ee6cb1b7e65dfddfb2e384100b8d148e7758de42e4168b71792c600060d020a900000023ba2703c53263e8d6e522dc32203339dcd8eee90141400c40efd5f4a37b09fb8dca3e9cd6486c1b2d46c0319ac216c348f546ff44bb5fc3a328a43f2f49c9b2aa4cb1ce3f40327fd8403966e117745eb5c1266614f7d42321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac",
                txHexString);
    }
}
