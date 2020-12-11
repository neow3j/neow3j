package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash256;
import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.devpack.neo.Blockchain;
import io.neow3j.devpack.neo.Contract;
import io.neow3j.devpack.neo.Json;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.ByteStringStackItem;
import io.neow3j.protocol.core.methods.response.IntegerStackItem;
import io.neow3j.protocol.core.methods.response.MapStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.utils.Numeric;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlockchainTest extends ContractTest {

    private static final String firstTxHash =
            "45ccc78c902009bc557e8f71c931d99ec8d18723e65979b7b7be2b801a28c210";
    private static final String scriptOfFirstTx = "41123e7fe8";
    private static final String senderAddressOfFirstTx = "NeN4xPMn4kHoj7G8Lciq9oorgLTvqt4qi1";

    private static final String firstBlockHash =
            "8af604166e33ec77d67b03d4e3c4a9c199ba064f07df3e4eb44e58a5771495ed";
    private static final String neoHash = "de5f57d430d3dece511cf975a8d37848cb9e0525";

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(BlockchainTestContract.class.getName());
    }

    @Test
    public void getNeoContractManifest() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.hash160(new ScriptHash(neoHash)));

        MapStackItem map = response.getInvocationResult().getStack().get(0).asMap();
        assertThat(map.get("groups").asArray().size(), is(0));
        assertThat(map.get("supportedstandards").asArray().get(0).asByteString().getAsString(),
                is("NEP-5"));
        assertThat(map.get("abi").asMap().size(), is(3));
        assertThat(map.get("permissions").asArray().getValue(), notNullValue());
        assertThat(map.get("trusts").asArray().getValue(), notNullValue());
        assertThat(map.get("safemethods").asArray().getValue(), notNullValue());
        assertThat(map.get("extra").asAny().getValue(), nullValue());
    }

    @Test
    public void getNeoContractScript() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(
                ContractParameter.hash160(new ScriptHash(neoHash)));

        ByteStringStackItem s = response.getInvocationResult().getStack().get(0).asByteString();
        assertThat(s.getAsHexString(), is("0c034e454f416b67780b"));
    }

    @Test
    public void getTransactionHeight() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(firstTxHash));
        assertThat(
                response.getInvocationResult().getStack().get(0).asInteger().getValue().intValue(),
                is(0));
    }

    @Test
    public void getTransactionFromBlock() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(integer(0), integer(0));
        ArrayStackItem tx = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(tx.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(firstTxHash)));
        assertThat(tx.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(tx.get(2).asInteger().getValue().intValue(), is(0)); // nonce
        assertThat(tx.get(3).asByteString().getAsAddress(), is(senderAddressOfFirstTx)); // sender
        assertThat(tx.get(4).asInteger().getValue().intValue(), is(0)); // system fee
        assertThat(tx.get(5).asInteger().getValue().intValue(), is(0)); // network fee
        assertThat(tx.get(6).asInteger().getValue().intValue(), is(0)); // valid until block
        assertThat(tx.get(7).asByteString().getAsHexString(), is(scriptOfFirstTx)); // script
    }

    @Test
    public void getTransactionFromBlockWithBlockHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(firstBlockHash), integer(0));
        ArrayStackItem tx = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(tx.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(firstTxHash)));
        assertThat(tx.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(tx.get(2).asInteger().getValue().intValue(), is(0)); // nonce
        assertThat(tx.get(3).asByteString().getAsAddress(), is(senderAddressOfFirstTx)); // sender
        assertThat(tx.get(4).asInteger().getValue().intValue(), is(0)); // system fee
        assertThat(tx.get(5).asInteger().getValue().intValue(), is(0)); // network fee
        assertThat(tx.get(6).asInteger().getValue().intValue(), is(0)); // valid until block
        assertThat(tx.get(7).asByteString().getAsHexString(), is(scriptOfFirstTx)); // script
    }

    @Test
    public void getTransaction() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(firstTxHash));
        ArrayStackItem tx = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(tx.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(firstTxHash)));
        assertThat(tx.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(tx.get(2).asInteger().getValue().intValue(), is(0)); // nonce
        assertThat(tx.get(3).asByteString().getAsAddress(), is(senderAddressOfFirstTx)); // sender
        assertThat(tx.get(4).asInteger().getValue().intValue(), is(0)); // system fee
        assertThat(tx.get(5).asInteger().getValue().intValue(), is(0)); // network fee
        assertThat(tx.get(6).asInteger().getValue().intValue(), is(0)); // valid until block
        assertThat(tx.get(7).asByteString().getAsHexString(), is(scriptOfFirstTx)); // script
    }

    @Test
    public void getBlockWithBlockHash() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(hash256(firstBlockHash));
        ArrayStackItem block = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(block.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(firstBlockHash))); // block hash
        assertThat(block.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(block.get(2).asByteString().getAsHexString(), // previous block hash
                is("0000000000000000000000000000000000000000000000000000000000000000"));
        assertThat(block.get(3).asByteString().getAsHexString(), // merkle root
                is(Numeric.reverseHexString(
                        "e5a19ba16bacedbca421cc7dedb804a5d57cbfb8a58b924007fcb898068a0b2e")));
        assertThat(block.get(4).asInteger().getValue().longValue(), is(1468595301000L)); // timestamp
        assertThat(block.get(5).asInteger().getValue().intValue(), is(0)); // tx count
        assertThat(block.get(6).asByteString().getAsAddress(), // next consensus
                is("NX8GreRFGFK5wpGMWetpX93HmtrezGogzk"));
        assertThat(block.get(7).asInteger().getValue().intValue(), is(1)); // index
    }

    @Test
    public void getBlockWithBlockNumber() throws IOException {
        NeoInvokeFunction response = callInvokeFunction(integer(0));
        ArrayStackItem block = response.getInvocationResult().getStack().get(0).asArray();
        assertThat(block.get(0).asByteString().getAsHexString(),
                is(Numeric.reverseHexString(firstBlockHash))); // block hash
        assertThat(block.get(1).asInteger().getValue().intValue(), is(0)); // version
        assertThat(block.get(2).asByteString().getAsHexString(), // previous block hash
                is("0000000000000000000000000000000000000000000000000000000000000000"));
        assertThat(block.get(3).asByteString().getAsHexString(), // merkle root
                is(Numeric.reverseHexString(
                        "e5a19ba16bacedbca421cc7dedb804a5d57cbfb8a58b924007fcb898068a0b2e")));
        assertThat(block.get(4).asInteger().getValue().longValue(), is(1468595301000L)); // timestamp
        assertThat(block.get(5).asInteger().getValue().intValue(), is(0)); // tx count
        assertThat(block.get(6).asByteString().getAsAddress(), // next consensus
                is("NX8GreRFGFK5wpGMWetpX93HmtrezGogzk"));
        assertThat(block.get(7).asInteger().getValue().intValue(), is(1)); // index
    }

    @Test
    public void getHeight() throws IOException {
        NeoInvokeFunction response = callInvokeFunction();
        IntegerStackItem height = response.getInvocationResult().getStack().get(0).asInteger();
        assertThat(height.getValue().intValue(), greaterThanOrEqualTo(0));
    }

    static class BlockchainTestContract {

        public static Object getNeoContractManifest(byte[] hash) {
            Contract c = Blockchain.getContract(hash);
            return Json.deserialize(c.manifest);
        }

        public static byte[] getNeoContractScript(byte[] hash) {
            Contract c = Blockchain.getContract(hash);
            return c.script;
        }

        public static int getTransactionHeight(byte[] blockHash) {
            return Blockchain.getTransactionHeight(blockHash);
        }

        public static Object getTransactionFromBlock(int blockNr, int txNr) {
            return Blockchain.getTransactionFromBlock(blockNr, txNr);
        }

        public static Object getTransactionFromBlockWithBlockHash(byte[] blockHash, int txNr) {
            return Blockchain.getTransactionFromBlock(blockHash, txNr);
        }

        public static Object getTransaction(byte[] txHash) {
            return Blockchain.getTransaction(txHash);
        }

        public static Object getBlockWithBlockHash(byte[] blockHash) {
            return Blockchain.getBlock(blockHash);
        }

        public static Object getBlockWithBlockNumber(int blockNr) {
            return Blockchain.getBlock(blockNr);
        }

        public static int getHeight() {
            return Blockchain.getHeight();
        }
    }
}
