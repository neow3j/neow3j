package io.neow3j.protocol;

import io.neow3j.protocol.http.HttpService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.time.Duration;

public class CoreIT implements InterfaceCoreIT {

    private static int EXPOSED_INTERNAL_PORT_NEO_DOTNET = 30333;
    private static int EXPOSED_INTERNAL_PORT_NEO_PYTHON = 30337;

    @Rule
    public GenericContainer privateNetContainer
            = new GenericContainer("axlabs/neo-privatenet-openwallet-docker:latest")
            .withExposedPorts(EXPOSED_INTERNAL_PORT_NEO_DOTNET, EXPOSED_INTERNAL_PORT_NEO_PYTHON)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(1L));

    // TODO: 2019-02-24 Guil:
    // neo-python is not yet fully working on the docker container.
    // left it out for now.
    //private Neow3j neow3jNeoPython;
    private Neow3j neow3jNeoDotNet;

    private Neow3jTestWrapper neow3jTestWrapper;

    @Before
    public void setup() {
        //neow3jNeoPython = Neow3j.build(new HttpService(getPrivateNetHost(EXPOSED_INTERNAL_PORT_NEO_PYTHON)));
        neow3jNeoDotNet = Neow3j.build(new HttpService(getPrivateNetHost(EXPOSED_INTERNAL_PORT_NEO_DOTNET)));
        neow3jTestWrapper = new Neow3jTestWrapper(neow3jNeoDotNet);
        // ensure that the wallet with NEO/GAS is initialized for the tests
        neow3jTestWrapper.waitUntilWalletHasBalanceGreaterThanOrEqualToOne();
    }

    @Test
    public void testGetVersion() throws IOException {
        neow3jTestWrapper.testGetVersion();
    }

    @Test
    public void testGetBestBlockHash() throws IOException {
        neow3jTestWrapper.testGetBestBlockHash();
    }

    @Test
    public void testGetBlockHash() throws IOException {
        neow3jTestWrapper.testGetBlockHash();
    }

    @Test
    public void testGetConnectionCount() throws IOException {
        neow3jTestWrapper.waitUntilConnectionCountIsGreaterThanOrEqualToOne();
        neow3jTestWrapper.testGetConnectionCount();
    }

    @Test
    public void testListAddress() throws IOException {
        neow3jTestWrapper.testListAddress();
    }

    @Test
    public void testGetPeers() throws IOException {
        neow3jTestWrapper.testGetPeers();
    }

    @Test
    public void testGetRawMemPool() throws IOException {
        neow3jTestWrapper.testGetRawMemPool();
    }

    @Test
    public void testGetValidators() throws IOException {
        neow3jTestWrapper.testGetValidators();
    }

    @Test
    public void testValidateAddress() throws IOException {
        neow3jTestWrapper.testValidateAddress();
    }

    @Test
    public void testGetBlock_Index_fullTransactionObjects() throws IOException {
        neow3jTestWrapper.testGetBlock_Index_fullTransactionObjects();
    }

    @Test
    public void testGetBlock_Index() throws IOException {
        neow3jTestWrapper.testGetBlock_Index();
    }

    @Test
    public void testGetRawBlock_Index() throws IOException {
        neow3jTestWrapper.testGetRawBlock_Index();
    }

    @Test
    public void testGetBlock_Hash_fullTransactionObjects() throws IOException {
        neow3jTestWrapper.testGetBlock_Hash_fullTransactionObjects();
    }

    @Test
    public void testGetBlock_Hash() throws IOException {
        neow3jTestWrapper.testGetBlock_Hash();
    }

    @Test
    public void testGetRawBlock_Hash() throws IOException {
        neow3jTestWrapper.testGetRawBlock_Hash();
    }

    @Test
    public void testGetBlockCount() throws Exception {
        neow3jTestWrapper.testGetBlockCount();
    }

    @Test
    public void testGetAccountState() throws IOException {
        neow3jTestWrapper.testGetAccountState();
    }

    @Test
    public void testGetBlockHeader_Hash() throws IOException {
        neow3jTestWrapper.testGetBlockHeader_Hash();
    }

    @Test
    public void testGetBlockHeader_Index() throws IOException {
        neow3jTestWrapper.testGetBlockHeader_Index();
    }

    @Test
    public void testGetRawBlockHeader_Hash() throws IOException {
        neow3jTestWrapper.testGetRawBlockHeader_Hash();
    }

    @Test
    public void testGetRawBlockHeader_Index() throws IOException {
        neow3jTestWrapper.testGetRawBlockHeader_Index();
    }

    @Test
    public void testGetNewAddress() throws IOException {
        neow3jTestWrapper.testGetNewAddress();
    }

    @Test
    public void testGetWalletHeight() throws IOException {
        neow3jTestWrapper.testGetWalletHeight();
    }

    @Test
    public void testGetBlockSysFee() throws IOException {
        neow3jTestWrapper.testGetBlockSysFee();
    }

    @Test
    public void testGetTxOut() throws IOException {
        neow3jTestWrapper.testGetTxOut();
    }

    @Test
    public void testSendRawTransaction() throws IOException {
        neow3jTestWrapper.testSendRawTransaction();
    }

    @Test
    public void testSendToAddress() throws Exception {
        neow3jTestWrapper.testSendToAddress();
    }

    @Test
    public void testSendToAddress_Fee() throws IOException {
        neow3jTestWrapper.testSendToAddress_Fee();
    }

    @Test
    public void testSendToAddress_Fee_And_ChangeAddress() throws IOException {
        neow3jTestWrapper.testSendToAddress_Fee_And_ChangeAddress();
    }

    @Test
    public void testGetTransaction() throws IOException {
        neow3jTestWrapper.testGetTransaction();
    }

    @Test
    public void testGetRawTransaction() throws IOException {
        neow3jTestWrapper.testGetRawTransaction();
    }

    @Test
    public void testGetBalance() throws IOException {
        neow3jTestWrapper.testGetBalance();
    }

    @Test
    public void testGetAssetState() throws IOException {
        neow3jTestWrapper.testGetAssetState();
    }

    @Test
    public void testSendMany() throws IOException {
        neow3jTestWrapper.testSendMany();
    }

    @Test
    public void testSendMany_Empty_Transaction() throws IOException {
        neow3jTestWrapper.testSendMany_Empty_Transaction();
    }

    @Test
    public void testSendMany_Fee() throws IOException {
        neow3jTestWrapper.testSendMany_Fee();
    }

    @Test
    public void testSendMany_Fee_And_ChangeAddress() throws IOException {
        neow3jTestWrapper.testSendMany_Fee_And_ChangeAddress();
    }

    @Test
    public void testDumpPrivKey() throws IOException {
        this.neow3jTestWrapper.testDumpPrivKey();
    }

    @Ignore
    @Test
    public void testGetStorage() throws IOException {
        this.neow3jTestWrapper.testGetStorage();
    }

    @Ignore
    @Test
    public void testInvoke() throws IOException {
        this.neow3jTestWrapper.testInvoke();
    }

    @Ignore
    @Test
    public void testInvokeFunction() throws IOException {
        this.neow3jTestWrapper.testInvokeFunction();
    }

    @Ignore
    @Test
    public void testInvokeScript() throws IOException {
        this.neow3jTestWrapper.testInvokeScript();
    }

    @Ignore
    @Test
    public void testGetContractState() throws IOException {
        this.neow3jTestWrapper.testGetContractState();
    }

    @Ignore
    @Test
    public void testSubmitBlock() throws IOException {
        this.neow3jTestWrapper.testSubmitBlock();
    }

    @Ignore
    @Test
    public void testGetUnspents() throws IOException {
        this.neow3jTestWrapper.testGetUnspents();
    }

    private String getPrivateNetHost(int port) {
        return "http://"
                + privateNetContainer.getContainerIpAddress()
                + ":"
                + privateNetContainer.getMappedPort(port);
    }

}
