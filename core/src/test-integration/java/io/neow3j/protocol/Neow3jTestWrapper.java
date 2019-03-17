package io.neow3j.protocol;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.KeyUtils;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.transaction.RawInvocationScript;
import io.neow3j.crypto.transaction.RawTransaction;
import io.neow3j.crypto.transaction.RawTransactionInput;
import io.neow3j.crypto.transaction.RawTransactionOutput;
import io.neow3j.crypto.transaction.RawVerificationScript;
import io.neow3j.model.types.AssetType;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.model.types.TransactionType;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoBlockHash;
import io.neow3j.protocol.core.methods.response.NeoConnectionCount;
import io.neow3j.protocol.core.methods.response.NeoDumpPrivKey;
import io.neow3j.protocol.core.methods.response.NeoGetAccountState;
import io.neow3j.protocol.core.methods.response.NeoGetAssetState;
import io.neow3j.protocol.core.methods.response.NeoGetBalance;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlockSysFee;
import io.neow3j.protocol.core.methods.response.NeoGetNewAddress;
import io.neow3j.protocol.core.methods.response.NeoGetPeers;
import io.neow3j.protocol.core.methods.response.NeoGetRawBlock;
import io.neow3j.protocol.core.methods.response.NeoGetRawMemPool;
import io.neow3j.protocol.core.methods.response.NeoGetRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetTxOut;
import io.neow3j.protocol.core.methods.response.NeoGetValidators;
import io.neow3j.protocol.core.methods.response.NeoGetVersion;
import io.neow3j.protocol.core.methods.response.NeoGetWalletHeight;
import io.neow3j.protocol.core.methods.response.NeoListAddress;
import io.neow3j.protocol.core.methods.response.NeoSendMany;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.core.methods.response.NeoSendToAddress;
import io.neow3j.protocol.core.methods.response.NeoValidateAddress;
import io.neow3j.protocol.core.methods.response.Script;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.neow3j.protocol.core.methods.response.TransactionAttribute;
import io.neow3j.protocol.core.methods.response.TransactionInput;
import io.neow3j.protocol.core.methods.response.TransactionOutput;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static io.neow3j.protocol.jsonrpc.JsonRpcErrorConstants.INVALID_PARAMS_CODE;
import static io.neow3j.protocol.jsonrpc.JsonRpcErrorConstants.INVALID_PARAMS_MESSAGE;
import static io.neow3j.utils.Numeric.prependHexPrefix;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Neow3jTestWrapper implements InterfaceCoreIT {

    private static int BLOCK_HASH_LENGTH_WITH_PREFIX = 66;

    private Neow3j neow3j;

    public Neow3jTestWrapper(Neow3j neow3j) {
        this.neow3j = neow3j;
    }

    public void testGetVersion() throws IOException {
        NeoGetVersion version = neow3j.getVersion().send();
        NeoGetVersion.Result versionResult = version.getVersion();
        assertNotNull(versionResult);
        assertThat(versionResult.getUserAgent(), not(isEmptyString()));
        assertThat(versionResult.getNonce(), is(greaterThanOrEqualTo(0L)));
        assertThat(versionResult.getPort(), is(greaterThanOrEqualTo(0)));
    }

    public void testGetBestBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = neow3j.getBestBlockHash().send();
        String blockHash = getBestBlockHash.getBlockHash();
        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    public void testGetBlockHash() throws IOException {
        NeoBlockHash getBestBlockHash = neow3j.getBlockHash(new BlockParameterIndex(1)).send();
        String blockHash = getBestBlockHash.getBlockHash();
        assertNotNull(blockHash);
        assertThat(blockHash.length(), is(BLOCK_HASH_LENGTH_WITH_PREFIX));
    }

    public void testGetConnectionCount() throws IOException {
        NeoConnectionCount getConnectionCount = neow3j.getConnectionCount().send();
        Integer connectionCount = getConnectionCount.getCount();
        assertNotNull(connectionCount);
        assertThat(connectionCount, greaterThanOrEqualTo(1));
    }

    public void testListAddress() throws IOException {
        NeoListAddress listAddress = neow3j.listAddress().send();
        List<NeoListAddress.Address> addresses = listAddress.getAddresses();
        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    public void testGetPeers() throws IOException {
        NeoGetPeers getPeers = neow3j.getPeers().send();
        NeoGetPeers.Peers peers = getPeers.getPeers();
        assertNotNull(peers);
        assertThat(peers.getBad(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getConnected(), hasSize(greaterThanOrEqualTo(0)));
        assertThat(peers.getUnconnected(), hasSize(greaterThanOrEqualTo(0)));
    }

    public void testGetRawMemPool() throws IOException {
        NeoGetRawMemPool getRawMemPool = neow3j.getRawMemPool().send();
        List<String> addresses = getRawMemPool.getAddresses();
        assertNotNull(addresses);
        assertThat(addresses, hasSize(greaterThanOrEqualTo(0)));
    }

    public void testGetValidators() throws IOException {
        NeoGetValidators getValidators = neow3j.getValidators().send();
        List<NeoGetValidators.Validator> validators = getValidators.getValidators();
        assertNotNull(validators);
        assertThat(validators, hasSize(greaterThanOrEqualTo(0)));
    }

    public void testValidateAddress() throws IOException {
        NeoValidateAddress validateAddress = neow3j.validateAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y").send();
        NeoValidateAddress.Result validation = validateAddress.getValidation();
        assertNotNull(validation);
        assertThat(validation.getValid(), is(true));
        assertThat(validation.getAddress(), is("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"));
    }

    public void testGetBlock_Index_fullTransactionObjects() throws IOException {
        NeoGetBlock neoGetBlock = neow3j
                .getBlock(new BlockParameterIndex(2001L), true)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), equalTo(2001L));
        assertThat(block.getTransactions(), not(empty()));
    }

    public void testGetBlock_Index() throws IOException {
        NeoGetBlock neoGetBlock = neow3j
                .getBlock(new BlockParameterIndex(2001L), false)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), equalTo(2001L));
        assertThat(block.getTransactions(), is(nullValue()));
    }

    public void testGetRawBlock_Index() throws IOException {
        NeoGetRawBlock neoGetRawBlock = neow3j
                .getRawBlock(new BlockParameterIndex(2001L))
                .send();
        String rawBlock = neoGetRawBlock.getRawBlock();
        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        NeoGetBlock neoGetBlock = neow3j
                .getBlock("0xf34dccda9df17f070e0bad1f4bc176124dc733fef6800bee4d917c4858da4175", true)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), equalTo(2001L));
        assertThat(block.getTransactions(), not(empty()));
    }

    public void testGetBlock_Hash() throws IOException {
        NeoGetBlock neoGetBlock = neow3j
                .getBlock("0xf34dccda9df17f070e0bad1f4bc176124dc733fef6800bee4d917c4858da4175", false)
                .send();
        NeoBlock block = neoGetBlock.getBlock();
        assertNotNull(block);
        assertThat(block.getIndex(), greaterThanOrEqualTo(2001L));
        assertThat(block.getTransactions(), is(nullValue()));
    }

    public void testGetRawBlock_Hash() throws IOException {
        NeoGetRawBlock neoGetRawBlock = neow3j
                .getRawBlock("0xf34dccda9df17f070e0bad1f4bc176124dc733fef6800bee4d917c4858da4175")
                .send();
        String rawBlock = neoGetRawBlock.getRawBlock();
        assertThat(rawBlock, not(isEmptyOrNullString()));
    }

    public void testGetBlockCount() throws Exception {
        NeoBlockCount neoBlockCount = neow3j.getBlockCount().send();
        BigInteger blockIndex = neoBlockCount.getBlockIndex();
        assertNotNull(blockIndex);
        assertThat(neoBlockCount.getBlockIndex(), greaterThan(BigInteger.valueOf(0)));
    }

    public void testGetAccountState() throws IOException {
        NeoGetAccountState getAccountState = neow3j
                .getAccountState("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y").send();
        NeoGetAccountState.State accountState = getAccountState.getAccountState();
        assertNotNull(accountState);
        assertThat(accountState.getVotes(), is(empty()));
        assertThat(accountState.getFrozen(), is(false));
        assertThat(accountState.getVersion(), is(0));
        assertThat(accountState.getBalances(), hasSize(2));
        assertThat(accountState.getBalances(), hasItem(new NeoGetAccountState.Balance(prependHexPrefix(NEOAsset.HASH_ID), "100000000")));
        assertThat(accountState.getBalances().get(1).getAssetAddress(), is(prependHexPrefix(GASAsset.HASH_ID)));
        assertThat(accountState.getBalances().get(1).getValue(), is(notNullValue()));
    }

    public void testGetBlockHeader_Hash() throws IOException {
        NeoBlock block = getNeoBlockHeaderHash("0xf34dccda9df17f070e0bad1f4bc176124dc733fef6800bee4d917c4858da4175").getBlock();
        assertThat(block.getTransactions(), is(nullValue()));
        assertThat(block.getIndex(), is(notNullValue()));
        assertThat(block.getHash(), is("0xf34dccda9df17f070e0bad1f4bc176124dc733fef6800bee4d917c4858da4175"));
    }

    public void testGetBlockHeader_Index() throws IOException {
        NeoBlock block = getNeoBlockHeaderIndex(BigInteger.valueOf(2001)).getBlock();
        assertThat(block.getTransactions(), is(nullValue()));
        assertThat(block.getIndex(), is(2001L));
        assertThat(block.getHash(), is("0xf34dccda9df17f070e0bad1f4bc176124dc733fef6800bee4d917c4858da4175"));
    }

    public void testGetRawBlockHeader_Hash() throws IOException {
        NeoGetRawBlock getRawBlockHeader = neow3j.getRawBlockHeader("0x7300105346d8d0ec7b81975ec72f32ccbd10a123e558e1ae89239f8487d80b4f").send();
        assertThat(getRawBlockHeader.getRawBlock(), is(notNullValue()));
        assertThat(getRawBlockHeader.getRawBlock(), is("0000000090f323184e1b84b9e974bba16f20a304ff7a5830891bb7df06e3359c10e2d9aa030f8d692a72b48303d6db66dcfaff12ed585305df5415b872663605a6387c277bf2435cd7070000f0288813af014b7cbe48d3a3f5d10013ab9ffee489706078714f1ea201c340c3f9fb8d1593f0de732b957e9f5a7ad3d88c80d1e8a5a7fd4fe616c670fbb27fbb8b03cac9300e9f68a4513c21f7599c34a615e851123392bcf4b00e8e61aebd40da662abb761aeab165c3b48a5f7ae1ec565cc2cfad65044c29f022c6b3f1fc03b4b124579d0629d4a0c9f9bab70a57e3a88d20157d7aaab20a5f6f7842f3cff2405d1f775650d84c9a59f57c7a2947324b8565174ff13c9f7ca3a890b50d1aea32635dfea7b42fe39487c9c8b1157418aae57a97db5c3b9fcf5286f88a001856378b532102103a7f7dd016558597f7960d27c516a4394fd968b9e65155eb4b013e4040406e2102a7bc55fe8684e0119768d104ba30795bdcc86619e864add26156723ed185cd622102b3622bf4017bdfe317c58aed5f4c753f206b7db896046fa7d774bbc4bf7f8dc22103d90c07df63e690ce77912e10ab51acc944b66860237b608c4f8f8309e71ee69954ae00"));
    }

    public void testGetRawBlockHeader_Index() throws IOException {
        NeoGetRawBlock getRawBlockHeader = neow3j.getRawBlockHeader(new BlockParameterIndex(2007L)).send();
        assertThat(getRawBlockHeader.getRawBlock(), is(notNullValue()));
        assertThat(getRawBlockHeader.getRawBlock(), is("0000000090f323184e1b84b9e974bba16f20a304ff7a5830891bb7df06e3359c10e2d9aa030f8d692a72b48303d6db66dcfaff12ed585305df5415b872663605a6387c277bf2435cd7070000f0288813af014b7cbe48d3a3f5d10013ab9ffee489706078714f1ea201c340c3f9fb8d1593f0de732b957e9f5a7ad3d88c80d1e8a5a7fd4fe616c670fbb27fbb8b03cac9300e9f68a4513c21f7599c34a615e851123392bcf4b00e8e61aebd40da662abb761aeab165c3b48a5f7ae1ec565cc2cfad65044c29f022c6b3f1fc03b4b124579d0629d4a0c9f9bab70a57e3a88d20157d7aaab20a5f6f7842f3cff2405d1f775650d84c9a59f57c7a2947324b8565174ff13c9f7ca3a890b50d1aea32635dfea7b42fe39487c9c8b1157418aae57a97db5c3b9fcf5286f88a001856378b532102103a7f7dd016558597f7960d27c516a4394fd968b9e65155eb4b013e4040406e2102a7bc55fe8684e0119768d104ba30795bdcc86619e864add26156723ed185cd622102b3622bf4017bdfe317c58aed5f4c753f206b7db896046fa7d774bbc4bf7f8dc22103d90c07df63e690ce77912e10ab51acc944b66860237b608c4f8f8309e71ee69954ae00"));
    }

    public void testGetNewAddress() throws IOException {
        NeoGetNewAddress getNewAddress = getNewAddress();
        String address = getNewAddress.getAddress();
        assertNotNull(address);
        assertThat(address.length(), is(KeyUtils.ADDRESS_SIZE));
    }

    public void testGetWalletHeight() throws IOException {
        NeoGetWalletHeight getWalletHeight = neow3j.getWalletHeight().send();
        BigInteger height = getWalletHeight.getHeight();
        assertNotNull(height);
        assertTrue(height.longValueExact() > 0);
    }

    public void testGetBlockSysFee() throws IOException {
        NeoGetBlockSysFee getBlockSysFee = neow3j
                .getBlockSysFee(new BlockParameterIndex(BigInteger.ONE)).send();
        String fee = getBlockSysFee.getFee();
        assertThat(fee, not(isEmptyOrNullString()));
    }

    public void testGetTxOut() throws IOException {
        NeoGetTxOut getTxOut = neow3j
                .getTxOut("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0)
                .send();
        TransactionOutput tx = getTxOut.getTransaction();
        assertNotNull(tx);
        assertThat(tx.getIndex(), is(0));
        assertThat(tx.getAssetId(), is(prependHexPrefix(NEOAsset.HASH_ID)));
        assertThat(tx.getAddress(), not(isEmptyOrNullString()));
        assertThat(tx.getValue(), not(isEmptyOrNullString()));
    }

    public void testSendRawTransaction() throws IOException {
        NeoSendRawTransaction neoSendRawTransaction = neow3j
                .sendRawTransaction("80000001ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500ca9a3b00000000295f83f83fc439f56e6e1fb062d89c6f538263d79b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500362634f286230023ba2703c53263e8d6e522dc32203339dcd8eee90141402c797867e0a4d670876fc11c5ba09ea0690ae1d8622e16e6466fcdc7a9689aff8c1b364ab0774d19c64c4349bc2f4cc8c46e40816636f5cb24efb24e92d81b312321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac")
                .send();
        Boolean sendRawTransaction = neoSendRawTransaction.getSendRawTransaction();
        assertThat(sendRawTransaction, is(true));
    }

    public void testSendToAddress() throws Exception {
        NeoSendToAddress neoSendToAddress = neow3j
                .sendToAddress(NEOAsset.HASH_ID, "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", "10")
                .send();
        Transaction sendToAddress = neoSendToAddress.getSendToAddress();
        assertNotNull(sendToAddress);
        assertThat(
                sendToAddress.getOutputs(),
                hasItem(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "10", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ")
                )
        );
    }

    public void testSendToAddress_Fee() throws IOException {
        NeoSendToAddress neoSendToAddress = neow3j
                .sendToAddress(NEOAsset.HASH_ID, "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ", "10", "0.1")
                .send();
        Transaction sendToAddress = neoSendToAddress.getSendToAddress();
        assertNotNull(sendToAddress);
        assertThat(
                sendToAddress.getOutputs(),
                hasItem(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "10", "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ")
                )
        );
        assertThat(
                sendToAddress.getNetFee(),
                is("0.1")
        );
    }

    public void testSendToAddress_Fee_And_ChangeAddress() throws IOException {
        NeoGetNewAddress neoGetNewAddress = getNewAddress();
        String newChangeAddress = neoGetNewAddress.getAddress();
        NeoSendToAddress neoSendToAddress = neow3j
                .sendToAddress(NEOAsset.HASH_ID, "AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ",
                        "10", "0.1", newChangeAddress)
                .send();
        Transaction sendToAddress = neoSendToAddress.getSendToAddress();
        assertNotNull(sendToAddress);
        assertThat(sendToAddress.getOutputs().size(), greaterThanOrEqualTo(2));
        assertThat(sendToAddress.getOutputs().get(0).getValue(), is("10"));
        assertThat(sendToAddress.getOutputs().get(0).getAddress(), is("AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ"));
        assertThat(sendToAddress.getOutputs().get(1).getValue(), notNullValue());
        assertThat(sendToAddress.getOutputs().get(1).getAddress(), is(newChangeAddress));
        assertThat(
                sendToAddress.getNetFee(),
                is("0.1")
        );
    }

    public void testGetTransaction() throws IOException {
        NeoGetTransaction getTransaction = neow3j.getTransaction("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff").send();
        assertThat(getTransaction.getTransaction(), is(notNullValue()));
        assertThat(
                getTransaction.getTransaction().getTransactionId(),
                is("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff")
        );
        assertThat(
                getTransaction.getTransaction().getSize(),
                is(223L)
        );
        assertThat(
                getTransaction.getTransaction().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
        assertThat(
                getTransaction.getTransaction().getVersion(),
                is(0)
        );
        assertThat(
                getTransaction.getTransaction().getAttributes(),
                hasItem(
                        new TransactionAttribute(TransactionAttributeUsageType.SCRIPT, "23ba2703c53263e8d6e522dc32203339dcd8eee9")
                )
        );
        assertThat(
                getTransaction.getTransaction().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100000000", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
                )
        );
        assertThat(
                getTransaction.getTransaction().getInputs(),
                hasItem(
                        new TransactionInput("0x83df8bd085fcb60b2789f7d0a9f876e5f3908567f7877fcba835e899b9dea0b5", 0)
                )
        );
        assertThat(
                getTransaction.getTransaction().getSysFee(),
                is("0")
        );
        assertThat(
                getTransaction.getTransaction().getNetFee(),
                is("0")
        );
        assertThat(
                getTransaction.getTransaction().getScripts(),
                hasItems(
                        new Script("40a3799c78dec17823fde75233793a7039bf2b1dbca4383a6eef1ac829460ba14c1e6a50ab1f2174e689bebfc0bb7accc965a6fe3e46d517b317bba1325b7fdaca", "21031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac")
                )
        );
        assertThat(
                getTransaction.getTransaction().getBlockHash(),
                is("0x7300105346d8d0ec7b81975ec72f32ccbd10a123e558e1ae89239f8487d80b4f")
        );
        assertThat(
                getTransaction.getTransaction().getConfirmations(),
                greaterThanOrEqualTo(1L)
        );
        assertThat(
                getTransaction.getTransaction().getBlockTime(),
                greaterThanOrEqualTo(1547956859L)
        );
    }

    public void testGetRawTransaction() throws IOException {
        NeoGetRawTransaction getRawTransaction = neow3j.getRawTransaction("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff").send();
        assertThat(getRawTransaction.getRawTransaction(), is("8000012023ba2703c53263e8d6e522dc32203339dcd8eee901b5a0deb999e835a8cb7f87f7678590f3e576f8a9d0f789270bb6fc85d08bdf830000019b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc50000c16ff286230023ba2703c53263e8d6e522dc32203339dcd8eee9014140a3799c78dec17823fde75233793a7039bf2b1dbca4383a6eef1ac829460ba14c1e6a50ab1f2174e689bebfc0bb7accc965a6fe3e46d517b317bba1325b7fdaca2321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac"));
    }

    public void testGetBalance() throws IOException {
        NeoGetBalance getBalance = getBalance();
        assertThat(getBalance.getBalance(), is(notNullValue()));
        assertThat(getBalance.getBalance().getConfirmed(), is("100000000"));
        assertThat(getBalance.getBalance().getBalance(), is("100000000"));
    }

    public void testGetAssetState() throws IOException {
        NeoGetAssetState getAssetState = neow3j.getAssetState(NEOAsset.HASH_ID).send();
        assertThat(getAssetState.getAssetState(), CoreMatchers.is(CoreMatchers.notNullValue()));
        assertThat(
                getAssetState.getAssetState().getVersion(),
                is(0)
        );
        assertThat(
                getAssetState.getAssetState().getId(),
                is("0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b")
        );
        assertThat(
                getAssetState.getAssetState().getType(),
                is(AssetType.GOVERNING_TOKEN)
        );
        assertThat(
                getAssetState.getAssetState().getNames(),
                hasItems(
                        new NeoGetAssetState.AssetName("zh-CN", "小蚁股"),
                        new NeoGetAssetState.AssetName("en", "AntShare")
                )
        );
        assertThat(
                getAssetState.getAssetState().getAmount(),
                is("100000000")
        );
        assertThat(
                getAssetState.getAssetState().getAvailable(),
                is("100000000")
        );
        assertThat(
                getAssetState.getAssetState().getPrecision(),
                is(0)
        );
        assertThat(
                getAssetState.getAssetState().getFee(),
                is(IsNull.nullValue())
        );
        assertThat(
                getAssetState.getAssetState().getAddress(),
                is(IsNull.nullValue())
        );
        assertThat(
                getAssetState.getAssetState().getOwner(),
                is("00")
        );
        assertThat(
                getAssetState.getAssetState().getAdmin(),
                is("Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt")
        );
        assertThat(
                getAssetState.getAssetState().getIssuer(),
                is("Abf2qMs1pzQb8kYk9RuxtUb9jtRKJVuBJt")
        );
        assertThat(
                getAssetState.getAssetState().getExpiration(),
                is(4000000L)
        );
        assertThat(
                getAssetState.getAssetState().getFrozen(),
                is(false)
        );
    }

    public void testSendMany() throws IOException {
        NeoSendMany sendMany = neow3j.sendMany(
                Arrays.asList(
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2")
                )
        ).send();
        assertThat(sendMany.getSendMany(), is(notNullValue()));
        assertThat(
                sendMany.getSendMany().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        // instead of "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", the address "AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs" is also part of the wallet -- default address
                        new TransactionOutput(2, prependHexPrefix(NEOAsset.HASH_ID), "99999890", "AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs")
                )
        );
        assertThat(
                sendMany.getSendMany().getInputs(),
                hasItem(
                        new TransactionInput("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0)
                )
        );
        assertThat(
                sendMany.getSendMany().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
    }

    public void testSendMany_Empty_Transaction() throws IOException {
        NeoSendMany sendMany = neow3j.sendMany(Arrays.asList()).send();
        assertThat(sendMany.getSendMany(), is(nullValue()));
        assertThat(sendMany.getError(), is(notNullValue()));
        assertThat(sendMany.getError().getCode(), is(INVALID_PARAMS_CODE));
        assertThat(sendMany.getError().getMessage(), is(INVALID_PARAMS_MESSAGE));
    }

    public void testSendMany_Fee() throws IOException {
        NeoSendMany sendMany = neow3j.sendMany(
                Arrays.asList(
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2")
                ),
                "50"
        ).send();
        assertThat(sendMany.getSendMany(), is(notNullValue()));
        assertThat(
                sendMany.getSendMany().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        // instead of "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y", the address "AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs" is also part of the wallet -- default address
                        new TransactionOutput(2, prependHexPrefix(NEOAsset.HASH_ID), "99999890", "AKkkumHbBipZ46UMZJoFynJMXzSRnBvKcs")
                )
        );
        assertThat(
                sendMany.getSendMany().getInputs(),
                hasItem(
                        new TransactionInput("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0)
                )
        );
        assertThat(
                sendMany.getSendMany().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
        assertThat(
                sendMany.getSendMany().getNetFee(),
                is("50")
        );
    }

    public void testSendMany_Fee_And_ChangeAddress() throws IOException {
        NeoSendMany sendMany = neow3j.sendMany(
                Arrays.asList(
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput("c56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b", "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2")
                ),
                "50",
                "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y"
        ).send();
        assertThat(sendMany.getSendMany(), is(notNullValue()));
        assertThat(
                sendMany.getSendMany().getOutputs(),
                hasItems(
                        new TransactionOutput(0, prependHexPrefix(NEOAsset.HASH_ID), "100", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput(1, prependHexPrefix(NEOAsset.HASH_ID), "10", "AbRTHXb9zqdqn5sVh4EYpQHGZ536FgwCx2"),
                        new TransactionOutput(2, prependHexPrefix(NEOAsset.HASH_ID), "99999890", "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
                )
        );
        assertThat(
                sendMany.getSendMany().getInputs(),
                hasItem(
                        new TransactionInput("0x4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0)
                )
        );
        assertThat(
                sendMany.getSendMany().getType(),
                is(TransactionType.CONTRACT_TRANSACTION)
        );
        assertThat(
                sendMany.getSendMany().getNetFee(),
                is("50")
        );
    }

    public void testDumpPrivKey() throws IOException {
        NeoDumpPrivKey neoDumpPrivKey = neow3j.dumpPrivKey("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y").send();
        String privKey = neoDumpPrivKey.getDumpPrivKey();
        assertThat(privKey, not(isEmptyOrNullString()));
        assertThat(privKey, is("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr"));
    }

    public void testGetStorage() throws IOException {
        // TODO: 2019-02-28 Guil:
        // to be implemented
    }

    public void testInvoke() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    public void testInvokeFunction() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    public void testInvokeScript() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    public void testGetContractState() throws IOException {
        // TODO: 2019-03-17 Guil:
        // to be implemented
    }

    public NeoGetBalance getBalance() throws IOException {
        return neow3j.getBalance(NEOAsset.HASH_ID).send();
    }

    public NeoConnectionCount getConnectionCount() throws IOException {
        return neow3j.getConnectionCount().send();
    }

    private NeoBlockHash getNeoBestBlockHash() throws IOException {
        return neow3j
                .getBestBlockHash()
                .send();
    }

    private NeoGetBlock getNeoBlockHeaderHash(String blockHash) throws IOException {
        return neow3j
                .getBlockHeader(blockHash)
                .send();
    }

    private NeoGetBlock getNeoBlockHeaderIndex(BigInteger index) throws IOException {
        return neow3j
                .getBlockHeader(new BlockParameterIndex(index))
                .send();
    }

    public NeoGetNewAddress getNewAddress() throws IOException {
        return neow3j.getNewAddress().send();
    }

    public void waitUntilWalletHasBalanceGreaterThanOrEqualToOne() {
        waitUntilWalletHasBalance(greaterThanOrEqualTo(BigDecimal.ONE));
    }

    public void waitUntilWalletHasBalance(Matcher matcher) {
        waitUntil(callableGetBalance(), matcher);
    }

    public void waitUntilConnectionCountIsGreaterThanOrEqualToOne() {
        waitUntilConnectionCountHasCount(greaterThanOrEqualTo(1));
    }

    public void waitUntilConnectionCountHasCount(Matcher matcher) {
        waitUntil(callableGetConnectionCount(), matcher);
    }

    public void waitUntil(Callable<? extends Object> callable, Matcher matcher) {
        await().timeout(15, TimeUnit.SECONDS).until(callable, matcher);
    }

    private Callable<BigDecimal> callableGetBalance() {
        return () -> new BigDecimal(getBalance().getBalance().getBalance());
    }

    private Callable<Integer> callableGetConnectionCount() {
        return () -> getConnectionCount().getCount();
    }

    private byte[] createContractTransaction(ECKeyPair keyPair, String inputHash, int inputIndex, BigDecimal amountToSend, String addressToSend, BigDecimal amountAsChange, String changeAddress) {
        RawTransaction tUnsigned = RawTransaction.createContractTransaction(
                null,
                null,
                Arrays.asList(
                        new RawTransactionInput(inputHash, inputIndex)
                ),
                Arrays.asList(
                        new RawTransactionOutput(0, NEOAsset.HASH_ID, amountToSend.toPlainString(), addressToSend),
                        new RawTransactionOutput(1, NEOAsset.HASH_ID, amountAsChange.toPlainString(), changeAddress)
                )
        );

        byte[] tUnsignedArray = tUnsigned.toArray();
        byte[] signature = Sign.signMessage(tUnsignedArray, keyPair).getConcatenated();
        tUnsigned.addScript(
                Arrays.asList(new RawInvocationScript(signature)),
                new RawVerificationScript(Arrays.asList(keyPair.getPublicKey()), 1)
        );

        return tUnsigned.toArray();
    }

}
