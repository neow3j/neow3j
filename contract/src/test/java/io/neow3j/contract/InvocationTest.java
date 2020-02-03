package io.neow3j.contract;

import static io.neow3j.constants.NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT;
import static io.neow3j.contract.ContractTestUtils.CONTRACT_1_SCRIPT_HASH;
import static io.neow3j.contract.ContractTestUtils.GETBLOCKCOUNT_RESPONSE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        // This is needed because the builder should fetch the current block number.
        ContractTestUtils.setUpWireMockForGetBlockCount();
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        Account acc = Account.createAccount();
        Invocation i = new InvocationBuilder(neow, sh, method).withAccount(acc).build();
        assertThat(
                i.getTransaction().getValidUntilBlock(),
                is((long) MAX_VALID_UNTIL_BLOCK_INCREMENT + GETBLOCKCOUNT_RESPONSE)
        );
    }

    @Test
    public void testCreationOfTheScript() throws IOException {
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        Account acc = Account.createAccount();
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withAccount(acc)
                .validUntilBlock(1000)
                .build();

        String expectedScript = "" +
                OpCode.PUSH0.toString() +
                OpCode.PACK.toString() +
                OpCode.PUSHBYTES4.toString() +
                "6e616d65" +                                // method: "name"
                OpCode.PUSHBYTES20.toString() +
                Numeric.toHexStringNoPrefix(sh.toArray()) + // ScriptHash in little-endian format
                OpCode.SYSCALL.toString() +
                InteropServiceCode.SYSTEM_CONTRACT_CALL.toString();

        assertThat(Numeric.toHexStringNoPrefix(i.getTransaction().getScript()), is(expectedScript));
    }

    @Test
    public void testAutomaticSettingOfSystemFee() throws IOException {
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        Account acc = Account.createAccount();
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withAccount(acc)
                .validUntilBlock(1000)
                .build();

        assertThat(i.getTransaction().getSystemFee(), is(3_700_000L));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithSingleSigAccount() throws Exception {
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        Account acc = Account.createAccount();
        long additionalFee = 100_000_000;
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withAccount(acc)
                .validUntilBlock(1000)
                .withAdditionalNetworkFee(additionalFee) // Additional fee of 1 GAS.
                .build();

        int signedTxSize = i.sign().getTransaction().getSize();
        long sizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        long verificationFee = OpCode.PUSHBYTES64.getPrice() +
                OpCode.PUSHBYTES33.getPrice() +
                InteropServiceCode.NEO_CRYPTO_CHECKSIG.getPrice();

        assertThat(i.getTransaction().getNetworkFee(),
                is(sizeFee + verificationFee + additionalFee));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithMultiSigAccount() throws Exception {
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        ScriptHash sh = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        ECKeyPair keyPair1 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair2 = ECKeyPair.createEcKeyPair();
        List<ECPublicKey> keys = Arrays.asList(keyPair1.getPublicKey2(), keyPair2.getPublicKey2());
        int signingThreshold = 2;
        Account multiSigAcc = Account.fromMultiSigKeys(keys, signingThreshold).build();
        long additionalFee = 100_000_000;
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withAccount(multiSigAcc)
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
        long verificationFee = OpCode.PUSHBYTES64.getPrice() * signingThreshold +
                OpCode.PUSH2.getPrice() +
                OpCode.PUSHBYTES33.getPrice() * keys.size() +
                OpCode.PUSH2.getPrice() +
                InteropServiceCode.NEO_CRYPTO_CHECKSIG.getPrice() * keys.size();

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
                .withAccount(multiSigAcc)
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
                .withAccount(multiSigAcc)
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
                .withAccount(multiSigAcc)
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
        Account sender = Account.fromECKeyPair(keyPair).build();
        ScriptHash sh = new ScriptHash(NEOAsset.HASH_ID);
        Invocation i = new InvocationBuilder(neow, sh, method)
                .withAccount(sender)
                .withNonce(1348080909)
                .validUntilBlock(2102660)
                .withParameters(
                        ContractParameter.byteArrayFromAddress(sender.getAddress()),
                        ContractParameter
                                .byteArrayFromAddress("PPULnCTke8Cu8Yyaggg2BCMLKc8k7QdvWr"),
                        ContractParameter.integer(10))
                .build();
        i.sign();

//        The network fee should be 1257240, although the script that was created by the
//        private net node has one byte too much at the end, which I don't know what it does.
    }
}