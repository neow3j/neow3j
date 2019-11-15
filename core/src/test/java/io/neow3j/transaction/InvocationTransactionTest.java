package io.neow3j.transaction;

import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.GASAsset;
import io.neow3j.utils.Numeric;
import org.junit.Test;

import java.math.BigDecimal;

import static io.neow3j.model.types.TransactionAttributeUsageType.SCRIPT;
import static org.junit.Assert.assertEquals;

public class InvocationTransactionTest {

    public static final String PRIVATE_KEY = "1dd37fba80fec4e6a6f13fd708d8dcb3b29def768017052f6c930fa1c5d90bbb";
    public static final String PUBLIC_KEY = "031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4a";
    public static final String ADDRESS = "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y";

    @Test
    public void serialize_tx() {

        ScriptHash scriptHash = ScriptHash.fromAddress(ADDRESS);
        String contractScript = "1423ba2703c53263e8d6e522dc32203339dcd8eee9076e656f2e636f6d52c108726567697374657267d42cf7a931ce3c46550fd90de482583fc5ea701a";


        RawTransaction unsigTx = new InvocationTransaction.Builder()
                .systemFee(BigDecimal.ONE)
                .input(new RawTransactionInput("d28e2bb7ad6f0e670da0ac466e584366a1ff30c623a34da1e1a27c710bfeeb41", 0))
                .output(new RawTransactionOutput(GASAsset.HASH_ID, "420.98755", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"))
                .attribute(new RawTransactionAttribute(SCRIPT, scriptHash.toArray()))
                .contractScript(Numeric.hexStringToByteArray(contractScript))
                .build();

        String unsignedTxHexStr = "d1013d1423ba2703c53263e8d6e522dc32203339dcd8eee9076e656f2e636f6d52c108726567697374" +
                "657267d42cf7a931ce3c46550fd90de482583fc5ea701a00e1f50500000000012023ba2703c53263e8d6e522dc32203339dc" +
                "d8eee90141ebfe0b717ca2e1a14da323c630ffa16643586e46aca00d670e6fadb72b8ed2000001e72d286979ee6cb1b7e65d" +
                "fddfb2e384100b8d148e7758de42e4168b71792c60b80548cd0900000023ba2703c53263e8d6e522dc32203339dcd8eee9";

        assertEquals(unsignedTxHexStr, Numeric.toHexStringNoPrefix(unsigTx.toArrayWithoutScripts()));

//        ECKeyPair keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(PRIVATE_KEY));
//        unsigTx.addScript(RawScript.createWitness(unsigTx.toArray(), keyPair));
    }

}