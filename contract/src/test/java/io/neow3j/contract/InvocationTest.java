package io.neow3j.contract;

import static io.neow3j.constants.NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT;
import static io.neow3j.contract.ContractTestUtils.CONTRACT_1_SCRIPT_HASH;
import static io.neow3j.contract.ContractTestUtils.GETBLOCKCOUNT_RESPONSE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
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
import io.neow3j.io.exceptions.DeserializationException;
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
import org.junit.Ignore;
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
                Numeric.toHexStringNoPrefix(contract.toArray()) + // script hash, little-endian
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
        List<ECPublicKey> keys = Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey());
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
                keyPair1.getPublicKey(), keyPair2.getPublicKey(), keyPair3.getPublicKey());
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
                keyPair1.getPublicKey(), keyPair2.getPublicKey(), keyPair3.getPublicKey());
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
        List<ECPublicKey> keys = Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey());
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
    @Ignore("Test is ignored because the neo-core is not stable and therefore no valid reference "
            + "transaction can be produced.")
    public void testNeoTransfer() throws IOException, DeserializationException {
        // Used address version 23 (0x17)
        // Reference transaction created with neo-node.
        byte[] expectedTx = Numeric.hexStringToByteArray(
                "004f211c3fbff3224963185dd8767494f8b7cd201346f0131400e1f50500000000064b130000000000891420000001bff3224963185dd8767494f8b7cd201346f013140155110c140fac870f5f898f68b2b769da8c2c9fd156618e0f0c14bff3224963185dd8767494f8b7cd201346f0131413c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c4057a75d45013a32d758a10f5355668367761a5d5968d9a19a923fca5e6893fb6036825e9ca87abcde22f65f289434625066121c16e3002d4c5a8f7e24934d2a56290c21027824fe1f368614d83fbce6cfb84b068113cb08e45181741d29f83acacfb79a890b410a906ad4");
        ContractTestUtils.setUpWireMockForInvokeFunction("transfer",
                "invokefunction_transfer.json");
        String senderWif = "KzaTU6vRwLCYfAcWScBWX6sMMfZ7tdfk4SmTNgXSBkUgbaRGJqGz";
        ECKeyPair senderPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(senderWif));
        Account sender = Account.fromECKeyPair(senderPair).isDefault(true).build();
        Wallet w = new Wallet.Builder().account(sender).build();
        ScriptHash neo = ScriptHash.fromScript(
                new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_NEO).toArray());
        Invocation i = new InvocationBuilder(neow, neo, "transfer")
                .withWallet(w)
                .withNonce(1058808143)
                .validUntilBlock(2102409)
                .withParameters(
                        ContractParameter.byteArrayFromAddress(sender.getAddress()),
                        ContractParameter
                                .byteArrayFromAddress("AHCkToUT1eFMdf2fnXpRXygk8nhyhrRdZN"),
                        ContractParameter.integer(1))
                .failOnFalse()
                .build();
        i.sign();

        assertArrayEquals(expectedTx, i.getTransaction().toArray());
    }
}