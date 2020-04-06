package io.neow3j.contract;

import static io.neow3j.contract.ContractTestUtils.CONTRACT_1_SCRIPT_HASH;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
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
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Cosigner;
import io.neow3j.transaction.Witness;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
        assertThat(i.getTransaction().getValidUntilBlock(),
                is((long) NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT
                        + ContractTestUtils.GETBLOCKCOUNT_RESPONSE));
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
        Wallet wallet = new Wallet.Builder().accounts(multiSigAcc).build();
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
    public void addDefaultAccountCosignerIfNotExplicitlySetAndNoOtherCosignerIsSet()
            throws IOException {

        Wallet wallet = Wallet.createWallet();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .validUntilBlock(1000)
                .build();

        Cosigner expected = Cosigner.calledByEntry(wallet.getDefaultAccount().getScriptHash());
        assertThat(i.getTransaction().getCosigners(), hasSize(1));
        assertThat(i.getTransaction().getCosigners().get(0), is(expected));
    }

    @Test
    public void addDefaultAccountCosignerIfNotExplicitlySetAndAnotherCosignerIsSet()
            throws IOException {

        Wallet wallet = Wallet.createWallet();
        Account other = Account.createAccount();
        wallet.addAccount(other);
        Cosigner cosigner = Cosigner.calledByEntry(other.getScriptHash());
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .withCosigners(cosigner)
                .validUntilBlock(1000)
                .build();

        Cosigner expected = Cosigner.calledByEntry(wallet.getDefaultAccount().getScriptHash());
        assertThat(i.getTransaction().getCosigners(), hasSize(2));
        assertThat(i.getTransaction().getCosigners(), containsInAnyOrder(expected, cosigner));
    }

    @Test
    public void dontAddDuplicateDefaultAccountCosignerIfAlreadySetExplicitly() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account acc = Account.fromECKeyPair(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)))
                .isDefault(true).build();
        Wallet wallet = new Wallet.Builder().accounts(acc).build();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .withCosigners(Cosigner.calledByEntry(acc.getScriptHash()))
                .validUntilBlock(1000)
                .build();

        Cosigner expected = Cosigner.calledByEntry(acc.getScriptHash());
        assertThat(i.getTransaction().getCosigners(), hasSize(1));
        assertThat(i.getTransaction().getCosigners().get(0), is(expected));
    }

    @Test
    public void addSenderCosignerIfNotExplicitlySetAndNoOtherCosignerIsSet() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = Account.fromECKeyPair(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)))
                .isDefault(true).build();
        Wallet wallet = Wallet.createWallet();
        wallet.addAccount(senderAcc);
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .withSender(senderAcc.getScriptHash())
                .validUntilBlock(1000)
                .build();

        Cosigner expected = Cosigner.calledByEntry(senderAcc.getScriptHash());
        assertThat(i.getTransaction().getCosigners(), hasSize(1));
        assertThat(i.getTransaction().getCosigners().get(0), is(expected));
    }

    @Test
    public void addSenderCosignerIfNotExplicitlySetAndAnotherCosignerIsSet()
            throws IOException {

        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = Account.fromECKeyPair(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)))
                .isDefault(true).build();
        Wallet wallet = Wallet.createWallet();
        wallet.addAccount(senderAcc);
        Account other = Account.createAccount();
        wallet.addAccount(other);
        Cosigner cosigner = Cosigner.calledByEntry(other.getScriptHash());
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .withSender(senderAcc.getScriptHash())
                .withCosigners(cosigner)
                .validUntilBlock(1000)
                .build();

        Cosigner expected = Cosigner.calledByEntry(senderAcc.getScriptHash());
        assertThat(i.getTransaction().getCosigners(), hasSize(2));
        assertThat(i.getTransaction().getCosigners(), containsInAnyOrder(expected, cosigner));
    }

    @Test
    public void dontAddDuplicateSenderCosignerIfAlreadySetExplicitly() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = Account.fromECKeyPair(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)))
                .isDefault(true).build();

        Wallet wallet = Wallet.createWallet();
        wallet.addAccount(senderAcc);
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .withSender(senderAcc.getScriptHash())
                .withCosigners(Cosigner.calledByEntry(senderAcc.getScriptHash()))
                .validUntilBlock(1000)
                .build();

        Cosigner expected = Cosigner.calledByEntry(senderAcc.getScriptHash());
        assertThat(i.getTransaction().getCosigners(), hasSize(1));
        assertThat(i.getTransaction().getCosigners().get(0), is(expected));
    }

    @Test
    public void signTransactionWithAdditionalCosigners() throws IOException {
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Wallet w = Wallet.createWallet();
        Account cosigner = Account.createAccount();
        w.addAccount(cosigner);
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(w)
                .withCosigners(Cosigner.calledByEntry(cosigner.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build()
                .sign();

        List<Witness> witnesses = i.getTransaction().getWitnesses();
        assertThat(witnesses, hasSize(2));
        List<ECPublicKey> signers = witnesses.stream()
                .map(wit -> wit.getVerificationScript().getPublicKeys().get(0))
                .collect(Collectors.toList());
        assertThat(signers, containsInAnyOrder(
                w.getDefaultAccount().getPublicKey2(), cosigner.getPublicKey2()));
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failBuildingInvocationBecauseWalletDoesntContainCosignerAccount()
            throws IOException {

        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Wallet w = Wallet.createWallet();
        Account cosigner = Account.createAccount();
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(w)
                .withCosigners(Cosigner.calledByEntry(cosigner.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build();
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failSigningInvocationBecauseWalletDoesntContainCosignerAccount()
            throws IOException {

        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Wallet w = Wallet.createWallet();
        Account cosigner = Account.createAccount();
        w.addAccount(cosigner);
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(w)
                .withCosigners(Cosigner.calledByEntry(cosigner.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build();
        w.removeAccount(cosigner.getScriptHash());
        i.sign();
    }

    @Test
    @Ignore("Test is ignored because the neo-core is not stable and therefore no valid reference "
            + "transaction can be produced.")
    public void testNeoTransfer() throws IOException {
        // Used address version 23 (0x17)
        // Reference transaction created with neo-node.
        byte[] expectedTx = Numeric.hexStringToByteArray(
                "004f211c3fbff3224963185dd8767494f8b7cd201346f0131400e1f50500000000064b130000000000891420000001bff3224963185dd8767494f8b7cd201346f013140155110c140fac870f5f898f68b2b769da8c2c9fd156618e0f0c14bff3224963185dd8767494f8b7cd201346f0131413c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c4057a75d45013a32d758a10f5355668367761a5d5968d9a19a923fca5e6893fb6036825e9ca87abcde22f65f289434625066121c16e3002d4c5a8f7e24934d2a56290c21027824fe1f368614d83fbce6cfb84b068113cb08e45181741d29f83acacfb79a890b410a906ad4");
        ContractTestUtils.setUpWireMockForInvokeFunction("transfer",
                "invokefunction_transfer.json");
        String senderWif = "KzaTU6vRwLCYfAcWScBWX6sMMfZ7tdfk4SmTNgXSBkUgbaRGJqGz";
        ECKeyPair senderPair = ECKeyPair.create(WIF.getPrivateKeyFromWIF(senderWif));
        Account sender = Account.fromECKeyPair(senderPair).isDefault(true).build();
        Wallet w = new Wallet.Builder().accounts(sender).build();
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