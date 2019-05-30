package io.neow3j.protocol;

public interface InterfaceCoreIT {

    // API 2.9.*

    void testGetVersion() throws Exception;

    void testGetBestBlockHash() throws Exception;

    void testGetBlockHash() throws Exception;

    void testGetConnectionCount() throws Exception;

    void testListAddress() throws Exception;

    void testGetPeers() throws Exception;

    void testGetRawMemPool() throws Exception;

    void testGetValidators() throws Exception;

    void testValidateAddress() throws Exception;

    void testGetBlock_Index_fullTransactionObjects() throws Exception;

    void testGetBlock_Index() throws Exception;

    void testGetBlock_Hash_fullTransactionObjects() throws Exception;

    void testGetBlock_Hash() throws Exception;

    void testGetRawBlock_Index() throws Exception;

    void testGetRawBlock_Hash() throws Exception;

    void testGetBlockCount() throws Exception;

    void testGetAccountState() throws Exception;

    void testGetBlockHeader_Hash() throws Exception;

    void testGetBlockHeader_Index() throws Exception;

    void testGetRawBlockHeader_Hash() throws Exception;

    void testGetRawBlockHeader_Index() throws Exception;

    void testGetNewAddress() throws Exception;

    void testGetWalletHeight() throws Exception;

    void testGetBlockSysFee() throws Exception;

    void testGetTxOut() throws Exception;

    void testSendRawTransaction() throws Exception;

    void testSendToAddress() throws Exception;

    void testSendToAddress_Fee() throws Exception;

    void testSendToAddress_Fee_And_ChangeAddress() throws Exception;

    void testGetTransaction() throws Exception;

    void testGetRawTransaction() throws Exception;

    void testGetBalance() throws Exception;

    void testGetAssetState() throws Exception;

    void testSendMany() throws Exception;

    void testSendMany_Empty_Transaction() throws Exception;

    void testSendMany_Fee() throws Exception;

    void testSendMany_Fee_And_ChangeAddress() throws Exception;

    void testDumpPrivKey() throws Exception;

    void testGetStorage() throws Exception;

    void testInvoke() throws Exception;

    void testInvokeFunction() throws Exception;

    void testInvokeScript() throws Exception;

    void testGetContractState() throws Exception;

    void testSubmitBlock() throws Exception;

    // API 2.10.*

    void testGetUnspents() throws Exception;

    void testGetNep5Balances() throws Exception;

}
