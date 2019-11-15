package io.neow3j.wallet;

import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.transaction.RawScript;
import io.neow3j.transaction.RawTransactionOutput;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.model.types.TransactionAttributeUsageType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.NeoGetContractState;
import io.neow3j.protocol.core.methods.response.NeoGetContractState.ContractState;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents.Balance;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents.UnspentTransaction;
import io.neow3j.protocol.core.methods.response.NeoGetUnspents.Unspents;
import io.neow3j.protocol.exceptions.ErrorResponseException;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Balances.AssetBalance;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class AssetTransferTest {

    // A Neow3j instance which doesn't actually have a connection to a RPC node.
    private Neow3j neow3j;

    // Account with address AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y
    private Account acct;

    private static final String ALT_ADDR = "AJQ6FoaSXDFzA6wLnyZ1nFN7SGSN2oNTc3";

    @Before
    public void setUp() {
        this.neow3j = Neow3j.build(new HttpService(""));
        this.acct = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
    }

    /*
     * This test uses a raw transaction string generated with neo-python. The UTXO used as input has
     * to be mocked.
     */
    @Test
    public void transferNeoWithoutFees() throws IOException, ErrorResponseException {
        Utxo utxo = new Utxo(NEOAsset.HASH_ID, "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0, BigDecimal.valueOf(100000000));
        Account spyAcct = mockAccountBalances(this.acct, utxo);

        AssetTransfer at = new AssetTransfer.Builder(this.neow3j)
                .account(spyAcct)
                .output(NEOAsset.HASH_ID, "1", ALT_ADDR)
                // This attribute is automatically added by neo-python, so we need to add it as well
                // to get a matching transaction.
                .attribute(TransactionAttributeUsageType.SCRIPT, spyAcct.getScriptHash().toArray())
                .build()
                .sign();

        String expectedTxHex = "8000012023ba2703c53263e8d6e522dc32203339dcd8eee901ff8c509a090d440" +
                "c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000029b7cffdaa674beae0f930ebe6" +
                "085af9093e5fe56b34a5c220ccdcf6efc336fc500e1f505000000001cc9c05cefffe6cdd7b182816" +
                "a9152ec218d2ec09b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc50" +
                "01fcb69f286230023ba2703c53263e8d6e522dc32203339dcd8eee90141405355d70f13718659993" +
                "3fb7df0b93f19f8a60ac01148780480eff8497e66e34b234cdb7ad668271579f6e268f01b8103bef" +
                "ec12c17bb255a6f58ac38e1d5fb2b2321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc" +
                "2b7548ca2a46c4fcf4aac";

        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));

        // Test the same transaction with alternative builder methods.
        at = new AssetTransfer.Builder(this.neow3j)
                .account(spyAcct)
                .toAddress(ALT_ADDR)
                .amount(1)
                .asset(NEOAsset.HASH_ID)
                .attribute(TransactionAttributeUsageType.SCRIPT, spyAcct.getScriptHash().toArray())
                .build()
                .sign();

        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));
    }

    /*
     * This test uses a raw transaction that was generated with neow3j itself and successfully
     * tested on a private network. The UTXOs used as inputs have to be mocked.
     */
    @Test
    public void transferNeoWithGasFee() throws IOException, ErrorResponseException {
        Utxo utxo1 = new Utxo(NEOAsset.HASH_ID, "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0, 100000000);
        Utxo utxo2 = new Utxo(GASAsset.HASH_ID, "803ec81b9ddb7dec5c914793a9e61bf556deafb561216473ad7a8ee7a91979cc", 0, 40);
        Account spyAcct = mockAccountBalances(this.acct, utxo1, utxo2);

        AssetTransfer at = new AssetTransfer.Builder(this.neow3j)
                .account(spyAcct)
                .output(NEOAsset.HASH_ID, "1", ALT_ADDR)
                .networkFee(0.1)
                .build()
                .sign();

        String expectedTxHex = "80000002cc7919a9e78e7aad73642161b5afde56f51be6a99347915cec7ddb9d1bc83e800000ff8c509a090d440c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000039b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500e1f505000000001cc9c05cefffe6cdd7b182816a9152ec218d2ec0e72d286979ee6cb1b7e65dfddfb2e384100b8d148e7758de42e4168b71792c608091d2ed0000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc5001fcb69f286230023ba2703c53263e8d6e522dc32203339dcd8eee9014140732311635cdd61f4cad0eaf85fab4dcb4074d9b879e64e07c7897ee6aac052aa7753931aabd66dfee7b3d17035ce125b4132a9b8da8361dc5146cbba3468e8862321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc2b7548ca2a46c4fcf4aac";

        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));
    }

    /*
     * This test uses a raw transaction string generated with neo-python. The required inputs have
     * to be mocked.
     */
    @Test
    public void transferNeoWithoutFeesWithMultipleInputsNeeded() {
        // Account with address APLJBPhtRg2XLhtpxEHd6aRNL7YSLGH2ZL
        Account acct = Account.fromWIF("L56SWKLsdynnXTHScMdNjsJRgbtqcf9p5TUgSAHq242L2yD8NyrA").build();

        Utxo utxo1 = new Utxo(NEOAsset.HASH_ID, "ea8f4ea77370f317c3ea1529e10c60869d7ac9193b953e903a91e3dbeb188ac5", 0, 10);
        Utxo utxo2 = new Utxo(NEOAsset.HASH_ID, "fdd33c5ee319101311dd0485950a902eb286eff4d3cd164c13337e0be154e268", 0, 10);
        Utxo utxo3 = new Utxo(NEOAsset.HASH_ID, "3d0f63349fb23387652d639bfdc9d1a247f6c7ada46a87722d9938e4ef5c45dc", 0, 10);
        Account spyAcct = mockAccountBalances(acct, utxo1, utxo2, utxo3);
        AssetTransfer at = new AssetTransfer.Builder(this.neow3j)
                .account(spyAcct)
                .output(NEOAsset.HASH_ID, "25", ALT_ADDR)
                // This attribute is automatically added by neo-python, so we need to add it as well
                // to get a matching transaction.
                .attribute(TransactionAttributeUsageType.SCRIPT, this.acct.getScriptHash().toArray())
                .build()
                .sign();

        String expectedTxHex = "8000012023ba2703c53263e8d6e522dc32203339dcd8eee903c58a18ebdbe3913a903e953b19c97a9d86600ce12915eac317f37073a74e8fea000068e254e10b7e33134c16cdd3f4ef86b22e900a958504dd11131019e35e3cd3fd0000dc455cefe438992d72876aa4adc7f647a2d1c9fd9b632d658733b29f34630f3d0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500f90295000000001cc9c05cefffe6cdd7b182816a9152ec218d2ec09b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc50065cd1d0000000052eaab8b2aab608902c651912db34de36e7a2b0f01414027420055dea9b299270ce41ff1d4492febc8f83cf4a19892d78577a2be0afac6406dbeb9daf47820ff89e8f5e1bd2afe4b3931e001a4aae8744781cf14eea4b52321036245f426b4522e8a2901be6ccc1f71e37dc376726cc6665d80c5997e240568fbac";

        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));
    }

    /*
     * This test uses a raw transaction string generated with neo-python. The UTXOs used for input
     * are manually added. The attribute that is added is not really necessary but neo-python adds
     * it automatically and so we need to do it here as well, to arrive at the same transaction byte
     * array.
     */
    @Test
    public void transferNeoWithoutFeesWithManuallyAddedUtxo() {
        AssetTransfer at = new AssetTransfer.Builder(this.neow3j)
                .account(this.acct)
                .output(NEOAsset.HASH_ID, "1", ALT_ADDR)
                .utxo(NEOAsset.HASH_ID, "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0, 100000000)
                // This attribute is automatically added by neo-python, so we need to add it as well
                // to get a matching transaction.
                .attribute(TransactionAttributeUsageType.SCRIPT, this.acct.getScriptHash().toArray())
                .build()
                .sign();

        String expectedTxHex = "8000012023ba2703c53263e8d6e522dc32203339dcd8eee901ff8c509a090d440" +
                "c0e3471709ef536f8e8d32caa2488ed8c64c6f7acf1d1a44b0000029b7cffdaa674beae0f930ebe6" +
                "085af9093e5fe56b34a5c220ccdcf6efc336fc500e1f505000000001cc9c05cefffe6cdd7b182816" +
                "a9152ec218d2ec09b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc50" +
                "01fcb69f286230023ba2703c53263e8d6e522dc32203339dcd8eee90141405355d70f13718659993" +
                "3fb7df0b93f19f8a60ac01148780480eff8497e66e34b234cdb7ad668271579f6e268f01b8103bef" +
                "ec12c17bb255a6f58ac38e1d5fb2b2321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f7fc" +
                "2b7548ca2a46c4fcf4aac";

        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));

        // Test the same transaction with alternative builder methods.
        at = new AssetTransfer.Builder(this.neow3j)
                .account(this.acct)
                .toAddress(ALT_ADDR)
                .amount(1)
                .asset(NEOAsset.HASH_ID)
                .utxo(NEOAsset.HASH_ID, "4ba4d1f1acf7c6648ced8824aa2cd3e8f836f59e7071340e0c440d099a508cff", 0, 100000000)
                .attribute(TransactionAttributeUsageType.SCRIPT, this.acct.getScriptHash().toArray())
                .build()
                .sign();

        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));
    }

    /*
     * This test uses a raw transaction string generated with neo-python. It is a simple asset
     * transfer from one to another address. The UTXOs used for input are manually added.
     * The attribute that is added is not really necessary but neo-python adds it automatically and
     * so we need to do it here as well, to arrive at the same transaction byte array.
     */
    @Test
    public void transferNeoWithoutFeesWithManuallyAddedUtxos() {
        // Account with address APLJBPhtRg2XLhtpxEHd6aRNL7YSLGH2ZL
        Account acct = Account.fromWIF("L56SWKLsdynnXTHScMdNjsJRgbtqcf9p5TUgSAHq242L2yD8NyrA").build();

        Utxo utxo1 = new Utxo(NEOAsset.HASH_ID, "ea8f4ea77370f317c3ea1529e10c60869d7ac9193b953e903a91e3dbeb188ac5", 0, 10);
        Utxo utxo2 = new Utxo(NEOAsset.HASH_ID, "fdd33c5ee319101311dd0485950a902eb286eff4d3cd164c13337e0be154e268", 0, 10);
        Utxo utxo3 = new Utxo(NEOAsset.HASH_ID, "3d0f63349fb23387652d639bfdc9d1a247f6c7ada46a87722d9938e4ef5c45dc", 0, 10);
        AssetTransfer at = new AssetTransfer.Builder(this.neow3j)
                .account(acct)
                .output(NEOAsset.HASH_ID, "25", ALT_ADDR)
                .attribute(TransactionAttributeUsageType.SCRIPT, this.acct.getScriptHash().toArray())
                .utxos(utxo1, utxo2, utxo3)
                .build()
                .sign();

        String expectedTxHex = "8000012023ba2703c53263e8d6e522dc32203339dcd8eee903c58a18ebdbe3913a903e953b19c97a9d86600ce12915eac317f37073a74e8fea000068e254e10b7e33134c16cdd3f4ef86b22e900a958504dd11131019e35e3cd3fd0000dc455cefe438992d72876aa4adc7f647a2d1c9fd9b632d658733b29f34630f3d0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500f90295000000001cc9c05cefffe6cdd7b182816a9152ec218d2ec09b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc50065cd1d0000000052eaab8b2aab608902c651912db34de36e7a2b0f01414027420055dea9b299270ce41ff1d4492febc8f83cf4a19892d78577a2be0afac6406dbeb9daf47820ff89e8f5e1bd2afe4b3931e001a4aae8744781cf14eea4b52321036245f426b4522e8a2901be6ccc1f71e37dc376726cc6665d80c5997e240568fbac";

        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));
    }

    /*
     * This test uses a raw transaction string generated with neon-js. The transaction makes an
     * asset transfer of one NEO from a contract's balance to a normal address. The transaction
     * input, i.e. the contract's UTXO has to be mocked. As well as the contract state, of which we
     * only need the number of input parameters.
     */
    @Test
    public void transferFromContract() throws IOException {
        ScriptHash contractScriptHash = new ScriptHash("d994605e4f3960ba8d7422c4c8b1e94d48960a8d");
        Account contractAccount = Account.fromAddress(contractScriptHash.toAddress()).build();

        // Mock the Unspents response for the UTXO of the contract.
        UnspentTransaction utxo = new UnspentTransaction(
                "47cc41bfc0ad504032a73de8e3082a20172984730496ed98336e895c9a54b8b3", 0, BigDecimal.TEN);
        Balance balance = new Balance(Arrays.asList(utxo), NEOAsset.HASH_ID, NEOAsset.NAME,
                NEOAsset.NAME, BigDecimal.TEN);
        Unspents unspents = new Unspents(Arrays.asList(balance), contractScriptHash.toAddress());
        NeoGetUnspents unspentsResponse = new NeoGetUnspents();
        unspentsResponse.setResult(unspents);
        Request<?, NeoGetUnspents> unspentsRequestSpy = spy(new Request<>());
        doReturn(unspentsResponse).when(unspentsRequestSpy).send();

        // Mock the contract state response for the contract state.
        ContractState contractState = new ContractState(0, null, null,
                Arrays.asList(ContractParameterType.STRING), null, null, null, null, null, null, null);
        NeoGetContractState stateResponse = new NeoGetContractState();
        stateResponse.setResult(contractState);
        Request<?, NeoGetContractState> stateRequestSpy = spy(new Request<>());
        doReturn(stateResponse).when(stateRequestSpy).send();

        Neow3j neow3jSpy = spy(this.neow3j);
        doReturn(unspentsRequestSpy).when(neow3jSpy).getUnspents(contractAccount.getAddress());
        doReturn(stateRequestSpy).when(neow3jSpy).getContractState(contractScriptHash.toString());

        AssetTransfer at = new AssetTransfer.Builder(neow3jSpy)
                .account(this.acct)
                .amount("1")
                .asset(NEOAsset.HASH_ID)
                .toAddress(this.acct.getAddress())
                .fromContract(contractScriptHash)
                .build()
                .sign();

        byte[] txBytes = at.getTransaction().toArray();
        String expectedTx = "8000012023ba2703c53263e8d6e522dc32203339dcd8eee901b3b8549a5c896e3398" +
                "ed960473842917202a08e3e83da7324050adc0bf41cc470000029b7cffdaa674beae0f930ebe6085" +
                "af9093e5fe56b34a5c220ccdcf6efc336fc500e1f5050000000023ba2703c53263e8d6e522dc3220" +
                "3339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500e9" +
                "a435000000008d0a96484de9b1c8c422748dba60394f5e6094d9020100004140bda6a86e1d1e325d" +
                "a2a10cec2ff7792c3bde45852b578ecf024a87183bc98304ce8ff1e491978140b7e29cc0f5dc05b8" +
                "5eab3e6cd9a72b13cee72082befba0022321031a6c6fbbdf02ca351745fa86b9ba5a9452d785ac4f" +
                "7fc2b7548ca2a46c4fcf4aac";

        assertEquals(expectedTx, Numeric.toHexStringNoPrefix(txBytes));
    }

    @Test
    public void transferFromMultiSigAddress() throws IOException, ErrorResponseException {
        Neow3j neow3j = Neow3j.build(new HttpService("http://nucbox.axlabs.com:30333"));
        List<BigInteger> keys = Arrays.asList(SampleKeys.PUBLIC_KEY_1, SampleKeys.PUBLIC_KEY_2);
        // This account has the address "ATcWffQV1A7NMEsqQ1RmKfS7AbSqcAp2hd"
        Account multiSigAcct = Account.fromMultiSigKeys(keys, 2).build();
        // The account could also be instantiated from the address directly
        // Account multiSigAcct = Account.fromAddress("ATcWffQV1A7NMEsqQ1RmKfS7AbSqcAp2hd").build();
        Utxo utxo1 = new Utxo(NEOAsset.HASH_ID, "3f39ecb7d1583b563e5764a80b6089f8100d6101596b8a8ec08ef6864155d3c9", 0, 100);
        Account spyAcct = mockAccountBalances(multiSigAcct, utxo1);
        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(spyAcct)
                .output(NEOAsset.HASH_ID, 1, "AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y")
                .build();

        byte[] unsignedTxHex = at.getTransaction().toArrayWithoutScripts();
        SignatureData sig1 = Sign.signMessage(unsignedTxHex, SampleKeys.KEY_PAIR_1);
        SignatureData sig2 = Sign.signMessage(unsignedTxHex, SampleKeys.KEY_PAIR_2);
        RawScript witness = RawScript.createMultiSigWitness(2, Arrays.asList(sig1, sig2), keys);
        at.addWitness(witness);

        String expectedTxHex = "80000001c9d3554186f68ec08e8a6b5901610d10f889600ba864573e563b58d1b7ec393f0000029b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc500e1f5050000000023ba2703c53263e8d6e522dc32203339dcd8eee99b7cffdaa674beae0f930ebe6085af9093e5fe56b34a5c220ccdcf6efc336fc50003164e0200000081dc40aa001a388671254601a0593197d7474bc6018240cb10f5cda6cf3adcfa35f67e29d0fec3b96dbfdac079912cd554175b80d923c2decbd47ecb410bc1806eabd4285eea54608a0f18b8c7fef6cb6f2eb39c24aa14408174af3c64a304a1e586694493e448b3c435c1c5391ef7bed9b768a92fddc8e23419b04eee404e656503fa30a057b35befcb200c162a70a5dc78178f75d285734752210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d621025dd091303c62a683fab1278349c3475c958f4152292495350571d3e998611d4352ae";
        assertEquals(expectedTxHex, Numeric.toHexStringNoPrefix(at.getTransaction().toArray()));
    }

    @Test(expected = IllegalStateException.class)
    public void test_missing_neow3j() {
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(null)
                .account(a)
                .output(output)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_missing_account() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .output(output)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_missing_outputs() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_partially_missing_output1() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .asset(NEOAsset.HASH_ID)
                .amount("1")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_partially_missing_output2() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .asset(NEOAsset.HASH_ID)
                .toAddress(ALT_ADDR)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_partially_missing_output3() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .amount(1)
                .toAddress(ALT_ADDR)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_erroneously_add_outputs1() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .asset(NEOAsset.HASH_ID)
                .output(output)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void test_erroneously_add_outputs2() {
        Neow3j neow3j = ResponseInterceptor.createNeow3jWithInceptor(null);
        Account a = Account.fromWIF("KxDgvEKzgSBPPfuVfw67oPQBSjidEiqTHURKSDL1R7yGaGYAeYnr").build();
        RawTransactionOutput output = new RawTransactionOutput(NEOAsset.HASH_ID, "1", ALT_ADDR);

        AssetTransfer at = new AssetTransfer.Builder(neow3j)
                .account(a)
                .output(output)
                .amount("1")
                .build();
    }

    private Account mockAccountBalances(Account acct, Utxo... utxos) {
        List<Utxo> gasUtxos = Arrays.stream(utxos)
                .filter(u -> u.getAssetId().equals(GASAsset.HASH_ID))
                .collect(Collectors.toList());
        List<Utxo> neoUtxos = Arrays.stream(utxos)
                .filter(u -> u.getAssetId().equals(NEOAsset.HASH_ID))
                .collect(Collectors.toList());

        Account spyAcct = spy(acct);
        if (!gasUtxos.isEmpty()) {
            BigDecimal sum = gasUtxos.stream().map(Utxo::getValue).reduce(BigDecimal::add).get();
            AssetBalance mockBalance = new AssetBalance(gasUtxos, sum);
            doReturn(mockBalance).when(spyAcct).getAssetBalance(GASAsset.HASH_ID);
        }
        if (!neoUtxos.isEmpty()) {
            BigDecimal sum = neoUtxos.stream().map(Utxo::getValue).reduce(BigDecimal::add).get();
            AssetBalance mockBalance = new AssetBalance(neoUtxos, sum);
            doReturn(mockBalance).when(spyAcct).getAssetBalance(NEOAsset.HASH_ID);
        }
        return spyAcct;
    }

}
