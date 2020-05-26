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
        ContractTestUtils.setUpWireMockForGetBlockCount(1000);
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .build();
        assertThat(i.getTransaction().getValidUntilBlock(),
                is((long) NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT + 1000 - 1));
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
        ScriptHash neoToken = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, neoToken, method)
                .withWallet(wallet)
                .validUntilBlock(1000)
                .build();

        assertThat(i.getTransaction().getSystemFee(), is(1007390L));

        i = new InvocationBuilder(neow, neoToken, method)
                .withWallet(wallet)
                .validUntilBlock(1000)
                .failOnFalse()
                .build();

        assertThat(i.getTransaction().getSystemFee(), is(1007420L));
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
        Account multiSigAcc = Account.fromMultiSigKeys(keys, m).isDefault().build();
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
        Witness w = Witness.createMultiSigWitness(m, sigs,
                Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey()));
        i.addWitnesses(w);
        int signedTxSize = i.getTransaction().getSize();
        long sizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        // PUSHDATA1 * m + PUSH2 + PUSHDATA1 * n + PUSH2 + PUSHNULL + ECDsaVerify * n
        long verificationFee = (180 * m) + 30 + (180 * n) + 30 + 30 + (1_000_000 * n);

        assertThat(i.getTransaction().getNetworkFee(),
                is(sizeFee + verificationFee + additionalFee));
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
                .withAttributes(cosigner)
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
                .isDefault().build();
        Wallet wallet = new Wallet.Builder().accounts(acc).build();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .withAttributes(Cosigner.calledByEntry(acc.getScriptHash()))
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
                .isDefault().build();
        Wallet wallet = new Wallet.Builder().accounts(senderAcc).build();
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
                .isDefault().build();
        Wallet wallet = new Wallet.Builder().accounts(senderAcc).build();
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
                .withAttributes(cosigner)
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
                .isDefault().build();

        Wallet wallet = new Wallet.Builder().accounts(senderAcc).build();
        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Invocation i = new InvocationBuilder(neow, contract, method)
                .withWallet(wallet)
                .withSender(senderAcc.getScriptHash())
                .withAttributes(Cosigner.calledByEntry(senderAcc.getScriptHash()))
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
                .withAttributes(Cosigner.calledByEntry(cosigner.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build()
                .sign();

        List<Witness> witnesses = i.getTransaction().getWitnesses();
        assertThat(witnesses, hasSize(2));
        List<ECPublicKey> signers = witnesses.stream()
                .map(wit -> wit.getVerificationScript().getPublicKeys().get(0))
                .collect(Collectors.toList());
        assertThat(signers, containsInAnyOrder(
                w.getDefaultAccount().getECKeyPair().getPublicKey(),
                cosigner.getECKeyPair().getPublicKey()));
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
                .withAttributes(Cosigner.calledByEntry(cosigner.getScriptHash()))
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
                .withAttributes(Cosigner.calledByEntry(cosigner.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build();
        w.removeAccount(cosigner.getScriptHash());
        i.sign();
    }

    @Test(expected = InvocationConfigurationException.class)
    public void failSendingInvocationBecauseItDoesntContainSignaturesForAllCosigners()
            throws IOException {

        ScriptHash contract = new ScriptHash(CONTRACT_1_SCRIPT_HASH);
        String method = "name";
        // This is needed because the builder will invoke the contract for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction(method, "invokefunction_name.json");
        Wallet w = Wallet.createWallet();
        Account cosigner = Account.createAccount();
        w.addAccount(cosigner);
        new InvocationBuilder(neow, contract, method)
                .withWallet(w)
                .withAttributes(Cosigner.calledByEntry(cosigner.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build()
                .send();
    }

    @Test
    public void transferNeoWithNormalAccount() throws IOException {
        // Reference transaction created with address version 0x17. The signature produced by
        // neo-core was replaced by the signature created by neow3j because neo-core doesn't
        // produce deterministic signatures.
        byte[] expectedTx = Numeric.hexStringToByteArray(
                "00c0f5586b941343239213fa0e765f1027ce742f48db779a96c272890000000000064b1300000000003f2720000101941343239213fa0e765f1027ce742f48db779a960155150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c408283bd3ef1d925c135fc44cb87e7213920fdff7bcf98d76718729937b07217df306806927173a86a0136b386aa306f3aa70cfc0658a238c9855806e226892059290c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        // Required for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction("transfer",
                "invokefunction_transfer_neo.json");

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = Account.fromECKeyPair(senderPair).isDefault().build();
        Wallet w = new Wallet.Builder().accounts(sender).build();
        ScriptHash neo = new ScriptHash("9bde8f209c88dd0e7ca3bf0af0f476cdd8207789");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");

        Invocation i = new InvocationBuilder(neow, neo, "transfer")
                .withWallet(w)
                .withNonce(1800992192)
                .validUntilBlock(2107199)
                .withParameters(
                        ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        ContractParameter.integer(5))
                .failOnFalse()
                .build();
        i.sign();

        assertThat(i.getTransaction().getNonce(), is(1800992192L));
        assertThat(i.getTransaction().getValidUntilBlock(), is(2107199L));
        assertThat(i.getTransaction().getNetworkFee(), is(1264390L));
        assertThat(i.getTransaction().getSystemFee(), is(9007810L));
        byte[] expectedScript = Numeric.hexStringToByteArray(
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238");
        assertThat(i.getTransaction().getScript(), is(expectedScript));
        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                "0c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
        assertArrayEquals(expectedTx, i.getTransaction().toArray());
    }

    @Test
    public void transferNeoWithMutliSigAccount() throws IOException {
        // Reference transaction created with address version 0x17. The signature produced by
        // neo-core was replaced by the signature created by neow3j because neo-core doesn't
        // produce deterministic signatures.
        byte[] expectedTx = Numeric.hexStringToByteArray(
                "00ea02536400fea46931b5c22a99277a25233ff431d642b855c272890000000000b26213000000000024152000010100fea46931b5c22a99277a25233ff431d642b85501590200e1f5050c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c1400fea46931b5c22a99277a25233ff431d642b85513c00c087472616e736665720c143b7d3711c6f0ccf9b1dca903d1bfa1d896f1238c41627d5b523801420c406fded85ee546f0283e4dfd8c70c4d514139b0516de6d8a2d569b73e6da8468c21c2e8c18a1d3c8a7d5160960cf89d48fc433df7ddafb602f716ca11043eccb8e2b110c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238110b41c330181e");
        // Required for fetching the system fee.
        ContractTestUtils.setUpWireMockForInvokeFunction("transfer",
                "invokefunction_transfer_neo.json");

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = Account.fromMultiSigKeys(Arrays.asList(senderPair.getPublicKey()), 1)
                .isDefault().build();
        Account singleSigAcc = Account.fromECKeyPair(senderPair).build();
        Wallet w = new Wallet.Builder().accounts(sender, singleSigAcc).build();
        ScriptHash neo = new ScriptHash("8c23f196d8a1bfd103a9dcb1f9ccf0c611377d3b");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");

        Invocation i = new InvocationBuilder(neow, neo, "transfer")
                .withWallet(w)
                .withNonce(1683161834)
                .validUntilBlock(2102564)
                .withParameters(
                        ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        // The GAS (1 GAS) amount needs to be specified in fractions.
                        ContractParameter.integer(100000000))
                .failOnFalse()
                .build();
        i.sign();

        assertThat(i.getTransaction().getNonce(), is(1683161834L));
        assertThat(i.getTransaction().getValidUntilBlock(), is(2102564L));
        assertThat(i.getTransaction().getNetworkFee(), is(1270450L));
        assertThat(i.getTransaction().getSystemFee(), is(9007810L));
        byte[] expectedScript = Numeric.hexStringToByteArray(
                "0200e1f5050c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c1400fea46931b5c22a99277a25233ff431d642b85513c00c087472616e736665720c143b7d3711c6f0ccf9b1dca903d1bfa1d896f1238c41627d5b5238");
        assertThat(i.getTransaction().getScript(), is(expectedScript));
        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                "110c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238110b41c330181e");
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
        assertArrayEquals(expectedTx, i.getTransaction().toArray());
    }
}