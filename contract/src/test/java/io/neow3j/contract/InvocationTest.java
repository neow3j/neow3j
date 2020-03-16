package io.neow3j.contract;

import static io.neow3j.constants.NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT;
import static io.neow3j.contract.ContractTestUtils.CONTRACT_1_SCRIPT_HASH;
import static io.neow3j.contract.ContractTestUtils.GETBLOCKCOUNT_RESPONSE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.Invocation.InvocationBuilder;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.crypto.WIF;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class InvocationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configure WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failWithoutSettingSenderAccount() throws IOException {
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        InvocationBuilder b = new InvocationBuilder(neow, sh, method);
        b.build();
    }

    @Test
    public void testAutomaticSettingOfValidUntilBlockVariable() throws IOException {
        Wallet wallet = Wallet.createWallet();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        // This is needed because the builder should fetch the current block number.
        ContractTestUtils.setUpWireMockForGetBlockCount();
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .build();
        assertThat(
                i.getTransaction().getValidUntilBlock(),
                is((long) MAX_VALID_UNTIL_BLOCK_INCREMENT + GETBLOCKCOUNT_RESPONSE)
        );
    }

    @Test
    public void testCreationOfTheScript() throws IOException {
        Wallet wallet = Wallet.createWallet();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .validUntilBlock(1000)
                .build();

        String expectedScript = "" +
                OpCode.PUSH0.toString() +
                OpCode.PACK.toString() +
                OpCode.PUSHDATA1.toString() + "04" + // 4 bytes
                "6e616d65" +                                // method: "name"
                OpCode.PUSHDATA1.toString() + "14" + // 20 bytes
                Numeric.toHexStringNoPrefix(contract.toArray()) + // ScriptHash in little-endian format
                OpCode.SYSCALL.toString() +
                InteropServiceCode.SYSTEM_CONTRACT_CALL.getHash();

        assertThat(Numeric.toHexStringNoPrefix(i.getTransaction().getScript()), is(expectedScript));
    }

    @Test
    public void testAutomaticSettingOfSystemFee() throws IOException {
        Wallet wallet = Wallet.createWallet();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .validUntilBlock(1000)
                .build();

        assertThat(i.getTransaction().getSystemFee(), is(1_007_270L));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithSingleSigAccount() throws Exception {
        Wallet wallet = Wallet.createWallet();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        long additionalFee = 100_000_000; // Additional fee of 1 GAS.
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .validUntilBlock(1000)
                .withAdditionalNetworkFee(additionalFee)
                .build();

        int signedTxSize = i.sign().getTransaction().getSize();
        long sizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        // PUSHDATA1 + PUSHDATA1 + PUSHNULL + ECDsaVerify
        long verificationFee = 180 + 180 + 30 + 1_000_000;

        assertThat(i.getTransaction().getNetworkFee(),
                is(sizeFee + verificationFee + additionalFee));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithMultiSigAccount() throws Exception {
        String method = "name";
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        ECKeyPair keyPair1 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair2 = ECKeyPair.createEcKeyPair();
        List<ECPublicKey> keys = Arrays.asList(keyPair1.getPublicKey2(), keyPair2.getPublicKey2());
        int m = 2; // signingThreshold
        int n = 2; // total number of participating keys
        Account multiSigAcc = Account.fromMultiSigKeys(keys, m).isDefault(true).build();
        Wallet wallet = new Wallet.Builder().account(multiSigAcc).build();
        long additionalFee = 100_000_000;
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .validUntilBlock(1000)
                .withAdditionalNetworkFee(additionalFee) // Additional fee of 1 GAS.
                .build();

        byte[] txBytes = i.getTransactionForSigning();
        List<SignatureData> sigs = new ArrayList<>();
        sigs.add(Sign.signMessage(txBytes, keyPair1));
        sigs.add(Sign.signMessage(txBytes, keyPair2));
        i.addSignatures(sigs);
        int signedTxSize = i.getTransaction().getSize();
        long sizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        // PUSHDATA1 * m + PUSH2 + PUSHDATA1 * n + PUSH2 + PUSHNULL + ECDsaVerify * n
        long verificationFee = (180 * m) + 30 + (180 * n) + 30 + 30 + (1_000_000 * n);

        assertThat(i.getTransaction().getNetworkFee(),
                is(sizeFee + verificationFee + additionalFee));
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failTryingToAddTooManySignatures() throws Exception {
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        ECKeyPair keyPair1 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair2 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair3 = ECKeyPair.createEcKeyPair();
        List<ECPublicKey> keys = Arrays.asList(
                keyPair1.getPublicKey2(), keyPair2.getPublicKey2(), keyPair3.getPublicKey2());
        int signingThreshold = 2;
        Account multiSigAcc = Account.fromMultiSigKeys(keys, signingThreshold).build();
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withSender(multiSigAcc.getScriptHash())
                .validUntilBlock(1000)
                .build();

        byte[] txBytes = i.getTransactionForSigning();
        List<SignatureData> sigs = new ArrayList<>();
        sigs.add(Sign.signMessage(txBytes, keyPair1));
        sigs.add(Sign.signMessage(txBytes, keyPair2));
        sigs.add(Sign.signMessage(txBytes, keyPair3));
        i.addSignatures(sigs);
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failTryingToAddTooFewSignatures() throws Exception {
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        ECKeyPair keyPair1 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair2 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair3 = ECKeyPair.createEcKeyPair();
        List<ECPublicKey> keys = Arrays.asList(
                keyPair1.getPublicKey2(), keyPair2.getPublicKey2(), keyPair3.getPublicKey2());
        int signingThreshold = 3;
        Account multiSigAcc = Account.fromMultiSigKeys(keys, signingThreshold).build();
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withSender(multiSigAcc.getScriptHash())
                .validUntilBlock(1000)
                .build();

        byte[] txBytes = i.getTransactionForSigning();
        List<SignatureData> sigs = new ArrayList<>();
        sigs.add(Sign.signMessage(txBytes, keyPair1));
        sigs.add(Sign.signMessage(txBytes, keyPair2));
        i.addSignatures(sigs);
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failTryingToSignInvocationWithAccountMissingAPrivateKey() throws Exception {
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        ECKeyPair keyPair1 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair2 = ECKeyPair.createEcKeyPair();
        List<ECPublicKey> keys = Arrays.asList(keyPair1.getPublicKey2(), keyPair2.getPublicKey2());
        Account multiSigAcc = Account.fromMultiSigKeys(keys, 2).build();
        new InvocationBuilder(neow, sh, method)
                .withSender(multiSigAcc.getScriptHash())
                .validUntilBlock(1000)
                .build()
                .sign();
    }

    @Test
    public void test() {
        System.out.println(new ScriptHash("57e75368a3a9a7b8ccc4f541c8449bd95b023fa3").toAddress());
    }

    @Test
    public void testNeoTransfer()
            throws IOException, NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {
        //  WIF of example address PRivaTenetyWuqK7Gj7Vd747d77ssYeDhL of private net preview.
        //  See here https://github.com/hal0x2328/neo3-privatenet-tutorial
        final String method = "transfer";
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_transfer.json");
        ContractTestUtils.setUpWireMockForSendRawTransaction();

        String wif = "Kx6sh3EAsKQMY3PrqyhXTkNZdbBbs8Ya8D7VEssXkSb4DjfksTXF";
        ECKeyPair keyPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif));
        Account sender = Account.fromECKeyPair(keyPair).isDefault(true).build();
        Wallet w = new Wallet.Builder().account(sender).build();
        ScriptHash sh = new ScriptHash(NEOAsset.HASH_ID);
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withWallet(w)
                .withNonce(1348080909)
                .validUntilBlock(2102660)
                .withParameters(
                        ContractParameter.byteArrayFromAddress(sender.getAddress()),
                        ContractParameter
                                .byteArrayFromAddress("AHCkToUT1eFMdf2fnXpRXygk8nhyhrRdZN"),
                        ContractParameter.integer(10))
                .build();
        i.sign();
        fail();
//        The network fee should be 1257240, although the script that was created by the
//        private net node has one byte too much at the end, which I don't know what it does.
    }
}