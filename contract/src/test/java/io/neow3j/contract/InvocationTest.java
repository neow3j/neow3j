package io.neow3j.contract;

import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.Sign;
import io.neow3j.crypto.Sign.SignatureData;
import io.neow3j.crypto.WIF;
import io.neow3j.model.NeoConfig;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Witness;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.hamcrest.core.StringContains;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InvocationTest {

    private static final String SCRIPT = "10c00c046e616d650c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configure WireMock to use default host and port "localhost:8080".
        WireMock.configure();
        neow = Neow3j.build(new HttpService("http://localhost:8080"));
    }

    @Test
    public void failWithoutSettingSenderAccount() {
        exceptionRule.expect(IllegalArgumentException.class);
        new Invocation.Builder(null);
    }

    @Test
    public void failWithoutSettingWallet() throws IOException {
        ScriptHash neo = new ScriptHash("0xde5f57d430d3dece511cf975a8d37848cb9e0525");
        String method = "name";
        Invocation.Builder b = new Invocation.Builder(neow)
                .withContract(neo)
                .withFunction(method);
        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage("wallet");
        b.build();
    }

    @Test
    public void testAutomaticSettingOfValidUntilBlockVariable() throws IOException {
        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .build();
        assertThat(i.getTransaction().getValidUntilBlock(),
                is((long) NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT + 1000 - 1));
    }

    @Test
    public void testCreationOfTheScript() throws IOException {
        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withContract(new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"))
                .withFunction("name")
                .withWallet(wallet)
                .withValidUntilBlock(1000)
                .build();

        assertThat(Numeric.toHexStringNoPrefix(i.getTransaction().getScript()), is(SCRIPT));
    }

    @Test
    public void testAutomaticSettingOfSystemFee() throws IOException {
        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withValidUntilBlock(1000)
                .build();
        assertThat(i.getTransaction().getSystemFee(), is(1007390L));

        // With fail on false.
        String script = "10c00c046e616d650c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238";
        setUpWireMockForCall("invokescript", "invokescript_name_neo_fail_on_false.json", script);
        i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(script))
                .withWallet(wallet)
                .withValidUntilBlock(1000)
                .failOnFalse()
                .build();

        assertThat(i.getTransaction().getSystemFee(), is(1007420L));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithSingleSigAccount() throws Exception {
        Wallet wallet = Wallet.createWallet();
        long additionalFee = 100_000_000; // Additional fee of 1 GAS.
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withValidUntilBlock(1000)
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
        ECKeyPair keyPair1 = ECKeyPair.createEcKeyPair();
        ECKeyPair keyPair2 = ECKeyPair.createEcKeyPair();
        List<ECPublicKey> keys = Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey());
        int m = 2; // signingThreshold
        int n = 2; // total number of participating keys
        Account multiSigAcc = Account.createMultiSigAccount(keys, m);
        Wallet wallet = Wallet.withAccounts(multiSigAcc);
        long additionalFee = 100_000_000;
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withValidUntilBlock(1000)
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

    @Test
    public void failTryingToSignInvocationWithAccountMissingAPrivateKey() throws Exception {
        Wallet w = Wallet.createWallet("neo");
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withSender(w.getAccounts().get(0).getScriptHash())
                .withWallet(w)
                .withValidUntilBlock(1000)
                .build();

        exceptionRule.expect(InvocationConfigurationException.class);
        i.sign();
    }

    @Test
    public void failTryingToSignInvocationWithMultiSigAccountMissingAPrivateKey() throws Exception {
        Wallet w = Wallet.createWallet();
        Account a2 = Account.createAccount();
        List<ECPublicKey> keys = Arrays.asList(w.getAccounts().get(0).getECKeyPair().getPublicKey(),
                a2.getECKeyPair().getPublicKey());
        Account multiSigAcc = Account.createMultiSigAccount(keys, 2);
        w.addAccounts(a2);
        w.addAccounts(multiSigAcc);
        a2.encryptPrivateKey("neo");
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .withSender(multiSigAcc.getScriptHash())
                .withValidUntilBlock(1000)
                .build();

        exceptionRule.expect(InvocationConfigurationException.class);
        i.sign();
    }

    @Test
    public void addDefaultAccountSignerIfNotExplicitlySetAndNoOtherSignerIsSet()
            throws IOException {

        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withValidUntilBlock(1000)
                .build();

        Signer expected = Signer.calledByEntry(wallet.getDefaultAccount().getScriptHash());
        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0), is(expected));
    }

    @Test
    public void addDefaultAccountSignerIfNotExplicitlySetAndAnotherSignerIsSet()
            throws IOException {

        Wallet wallet = Wallet.createWallet();
        Account other = Account.createAccount();
        wallet.addAccounts(other);
        Signer signer = Signer.calledByEntry(other.getScriptHash());
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withAttributes(signer)
                .withValidUntilBlock(1000)
                .build();

        Signer expected = Signer.calledByEntry(wallet.getDefaultAccount().getScriptHash());
        assertThat(i.getTransaction().getSigners(), hasSize(2));
        assertThat(i.getTransaction().getSigners(), containsInAnyOrder(expected, signer));
    }

    @Test
    public void dontAddDuplicateDefaultAccountSignerIfAlreadySetExplicitly() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account acc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(acc);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withAttributes(Signer.calledByEntry(acc.getScriptHash()))
                .withValidUntilBlock(1000)
                .build();

        Signer expected = Signer.calledByEntry(acc.getScriptHash());
        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0), is(expected));
    }

    @Test
    public void addSenderSignerIfNotExplicitlySetAndNoOtherSignerIsSet() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(senderAcc);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json",
                SCRIPT); // expected script

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withSender(senderAcc.getScriptHash())
                .withValidUntilBlock(1000)
                .build();

        Signer expected = Signer.calledByEntry(senderAcc.getScriptHash());
        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0), is(expected));
    }

    @Test
    public void addSenderSignerIfNotExplicitlySetAndAnotherSignerIsSet()
            throws IOException {

        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(senderAcc);
        Account other = Account.createAccount();
        wallet.addAccounts(other);
        Signer signer = Signer.calledByEntry(other.getScriptHash());
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withSender(senderAcc.getScriptHash())
                .withAttributes(signer)
                .withValidUntilBlock(1000)
                .build();

        Signer expected = Signer.calledByEntry(senderAcc.getScriptHash());
        assertThat(i.getTransaction().getSigners(), hasSize(2));
        assertThat(i.getTransaction().getSigners(), containsInAnyOrder(expected, signer));
    }

    @Test
    public void dontAddDuplicateSenderSignerIfAlreadySetExplicitly() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(senderAcc);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withSender(senderAcc.getScriptHash())
                .withAttributes(Signer.calledByEntry(senderAcc.getScriptHash()))
                .withValidUntilBlock(1000)
                .build();

        Signer expected = Signer.calledByEntry(senderAcc.getScriptHash());
        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0), is(expected));
    }

    @Test
    public void signTransactionWithAdditionalSigners() throws IOException {
        Wallet w = Wallet.createWallet();
        Account signer = Account.createAccount();
        w.addAccounts(signer);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .withAttributes(Signer.calledByEntry(signer.getScriptHash()))
                .withValidUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build()
                .sign();

        List<Witness> witnesses = i.getTransaction().getWitnesses();
        assertThat(witnesses, hasSize(2));
        List<ECPublicKey> signers = witnesses.stream()
                .map(wit -> wit.getVerificationScript().getPublicKeys().get(0))
                .collect(Collectors.toList());
        assertThat(signers, containsInAnyOrder(
                w.getDefaultAccount().getECKeyPair().getPublicKey(),
                signer.getECKeyPair().getPublicKey()));
    }

    @Test
    public void failBuildingInvocationBecauseWalletDoesntContainSignerAccount()
            throws IOException {

        Wallet w = Wallet.createWallet();
        Account signer = Account.createAccount();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        Invocation.Builder b = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .withAttributes(Signer.calledByEntry(signer.getScriptHash()))
                .withValidUntilBlock(1000); // Setting explicitly so that no RPC call is necessary.
        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("Wallet does not contain the account for signer" +
                " with script hash " + signer.getScriptHash()));
        b.build();
    }

    @Test
    public void failSigningInvocationBecauseWalletDoesntContainSignerAccount()
            throws IOException {

        Wallet w = Wallet.createWallet();
        Account signer = Account.createAccount();
        w.addAccounts(signer);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .withAttributes(Signer.calledByEntry(signer.getScriptHash()))
                .withValidUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build();
        w.removeAccount(signer.getScriptHash());
        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("Can't create transaction "
                + "signature. Wallet does not contain the signer account with script "
                + "hash " + signer.getScriptHash()));
        i.sign();
    }

    @Test
    public void failSendingInvocationBecauseItDoesntContainSignaturesForAllSigners()
            throws IOException {

        Wallet w = Wallet.createWallet();
        Account signer = Account.createAccount();
        w.addAccounts(signer);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .withAttributes(Signer.calledByEntry(signer.getScriptHash()))
                .withValidUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build();

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("The transaction does not have a signature"
                + " for each of its signers"));
        i.send();
    }

    @Test
    public void sendInvokeFunction() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_5_neo.json",
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52",
                "[\"969a77db482f74ce27105f760efa139223431394\"]"); // witness (sender script hash)

        setUpWireMockForCall("sendrawtransaction",
                "sendrawtransaction.json",
                // verification script, part of the transaction hex.
                "0c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75"
        );
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");

        NeoSendRawTransaction i = new Invocation.Builder(neow)
                .withContract(neo)
                .withFunction("transfer")
                .withWallet(w)
                .withParameters(
                        ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        ContractParameter.integer(5))
                .build()
                .sign()
                .send();

        assertThat(i.getError(), nullValue());
        // This is not the actual transaction id of the above built transaction but merely the
        // one used in the file `responses/sendrawtransaction.json`.
        assertThat(i.getSendRawTransaction().getHash(), is(
                "0x830816f0c801bcabf919dfa1a90d7b9a4f867482cb4d18d0631a5aa6daefab6a"));
    }

    @Test
    public void transferNeoWithNormalAccount() throws IOException {
        NeoConfig.setMagicNumber(new byte[]{0x4e, 0x45, 0x4F, 0x00});
        // Reference transaction created with address version 0x17. The signature produced by
        // neo-core was replaced by the signature created by neow3j because neo-core doesn't
        // produce deterministic signatures.
        byte[] expectedTx = Numeric.hexStringToByteArray(
                "00c0f5586b941343239213fa0e765f1027ce742f48db779a96c272890000000000064b1300000000003f2720000101941343239213fa0e765f1027ce742f48db779a960155150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b523801420c408283bd3ef1d925c135fc44cb87e7213920fdff7bcf98d76718729937b07217df306806927173a86a0136b386aa306f3aa70cfc0658a238c9855806e226892059290c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        String script =
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238";
        setUpWireMockForCall("invokescript", "invokescript_transfer_5_neo.json", script,
                "969a77db482f74ce27105f760efa139223431394");

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");

        Invocation i = new Invocation.Builder(neow)
                .withContract(neo)
                .withFunction("transfer")
                .withWallet(w)
                .withNonce(1800992192)
                .withValidUntilBlock(2107199)
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
        assertThat(i.getTransaction().getScript(), is(Numeric.hexStringToByteArray(script)));
        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                "0c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f9562380b418a6b1e75");
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
        assertArrayEquals(expectedTx, i.getTransaction().toArray());
    }

    @Test
    public void transferNeoWithMultiSigAccount() throws IOException {
        NeoConfig.setMagicNumber(new byte[]{0x4e, 0x45, 0x4F, 0x00});
        // Reference transaction created with address version 0x17. The signature produced by
        // neo-core was replaced by the signature created by neow3j because neo-core doesn't
        // produce deterministic signatures.
        byte[] expectedTx = Numeric.hexStringToByteArray(
                "00ea02536400fea46931b5c22a99277a25233ff431d642b855c272890000000000b26213000000000024152000010100fea46931b5c22a99277a25233ff431d642b85501590200e1f5050c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c1400fea46931b5c22a99277a25233ff431d642b85513c00c087472616e736665720c143b7d3711c6f0ccf9b1dca903d1bfa1d896f1238c41627d5b523801420c406fded85ee546f0283e4dfd8c70c4d514139b0516de6d8a2d569b73e6da8468c21c2e8c18a1d3c8a7d5160960cf89d48fc433df7ddafb602f716ca11043eccb8e2b110c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238110b41c330181e");
        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = Account.createMultiSigAccount(Arrays.asList(senderPair.getPublicKey()), 1);
        Account singleSigAcc = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender, singleSigAcc);
        ScriptHash neo = new ScriptHash("668e0c1f9d7b70a99dd9e06eadd4c784d641afbc");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");
        String script =
                "0200e1f5050c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c1400fea46931b5c22a99277a25233ff431d642b85513c00c087472616e736665720c143b7d3711c6f0ccf9b1dca903d1bfa1d896f1238c41627d5b5238";
        setUpWireMockForCall("invokescript", "invokescript_transfer_1_gas_multisig_account.json",
                script, "55b842d631f43f23257a27992ac2b53169a4fe00");

        Invocation i = new Invocation.Builder(neow)
                .withContract(neo)
                .withFunction("transfer")
                .withWallet(w)
                .withNonce(1683161834)
                .withValidUntilBlock(2102564)
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
        assertThat(i.getTransaction().getScript(), is(Numeric.hexStringToByteArray(script)));
        byte[] expectedVerificationScript = Numeric.hexStringToByteArray(
                "110c2102c0b60c995bc092e866f15a37c176bb59b7ebacf069ba94c0ebf561cb8f956238110b41c330181e");
        assertThat(i.getTransaction().getWitnesses().get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
        assertArrayEquals(expectedTx, i.getTransaction().toArray());
    }

    @Test
    public void callWithParams() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_transfer_neo.json",
                "de5f57d430d3dece511cf975a8d37848cb9e0525",
                "transfer",
                "\"type\":\"Hash160\",\"value\":\"969a77db482f74ce27105f760efa139223431394\"",
                "\"type\":\"Hash160\",\"value\":\"df133e846b1110843ac357fc8bbf05b4a32e17c8\"",
                "\"type\":\"Integer\",\"value\":\"5\"",
                "[\"969a77db482f74ce27105f760efa139223431394\"]"
        );

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");

        NeoInvokeFunction i = new Invocation.Builder(neow)
                .withContract(neo)
                .withFunction(
                        "transfer")
                .withWallet(w)
                .withParameters(
                        ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        ContractParameter.integer(5))
                .failOnFalse()
                .invokeFunction();

        assertTrue(i.getResult().getStack().get(0).asBoolean().getValue());
        String expectedScript =
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238";
        assertThat(i.getResult().getScript(), is(expectedScript));
    }

    @Test
    public void callWithoutParams() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_name.json",
                "de5f57d430d3dece511cf975a8d37848cb9e0525",
                "name",
                "[\"969a77db482f74ce27105f760efa139223431394\"]"
        );

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");

        NeoInvokeFunction i = new Invocation.Builder(neow)
                .withContract(neo)
                .withFunction("name")
                .withWallet(w)
                .failOnFalse()
                .invokeFunction();

        assertThat(i.getResult().getStack().get(0).asByteString().getAsString(), is("NEO"));
        assertThat(i.getResult().getScript(), is(SCRIPT));
    }

    @Test
    public void doIfSenderCannotCoverFees() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_5_neo.json",
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52",
                "[\"969a77db482f74ce27105f760efa139223431394\"]"); // witness (sender script hash)

        setUpWireMockForCall("invokefunction",
                "invokefunction_balanceOf_0.01gas.json",
                "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc",
                "balanceOf",
                "969a77db482f74ce27105f760efa139223431394"
        );

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");

        long netFee = 1_264_390L;
        long sysFee = 9_007_810L;
        BigInteger expectedFees = BigInteger.valueOf(netFee + sysFee);
        BigInteger expectedBalance = BigInteger.valueOf(1_000_000L);
        AtomicBoolean tested = new AtomicBoolean(false);
        new Invocation.Builder(neow)
                .withContract(new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"))
                .withFunction("transfer")
                .withWallet(w)
                .withValidUntilBlock(2000000)
                .withParameters(
                        ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        ContractParameter.integer(5))
                .failOnFalse()
                .build()
                .doIfSenderCannotCoverFees((fee, balance) -> {
                    assertThat(fee, is(expectedFees));
                    assertThat(balance, is(expectedBalance));
                    tested.set(true);
                });
        assertTrue(tested.get());
    }

    @Test
    public void throwIfSenderCannotCoverFees() throws IOException {
        String script =
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b52";
        setUpWireMockForCall("invokescript", "invokescript_transfer_5_neo.json", script,
                "[\"969a77db482f74ce27105f760efa139223431394\"]"); // witness (sender script hash)
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_0.01gas.json",
                "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc",
                "balanceOf",
                "969a77db482f74ce27105f760efa139223431394");

        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);
        ScriptHash neo = new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525");
        ScriptHash receiver = new ScriptHash("df133e846b1110843ac357fc8bbf05b4a32e17c8");

        Invocation i = new Invocation.Builder(neow)
                .withContract(neo)
                .withFunction("transfer")
                .withWallet(w)
                .withValidUntilBlock(2000000)
                .withParameters(
                        ContractParameter.hash160(sender.getScriptHash()),
                        ContractParameter.hash160(receiver),
                        ContractParameter.integer(5))
                .failOnFalse()
                .build();

        exceptionRule.expect(IllegalStateException.class);
        i.throwIfSenderCannotCoverFees(IllegalStateException::new);
    }

    @Test
    public void invokeScript() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT,
                "[\"969a77db482f74ce27105f760efa139223431394\"]"); // witness (sender script hash)
        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);

        NeoInvokeScript response = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .invokeScript();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("NEO"));
    }

    @Test
    public void invokeScriptWithoutSettingScript() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT,
                "[\"969a77db482f74ce27105f760efa139223431394\"]"); // witness (sender script hash)
        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage("Cannot make an 'invokescript' call");
        new Invocation.Builder(neow)
                .withWallet(w)
                .invokeScript();
    }

    @Test
    public void invokeFunctionWithoutSettingContract() throws IOException {
        Invocation.Builder b = new Invocation.Builder(neow)
                .withFunction("transfer")
                .withWallet(Wallet.createWallet());

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage("contract");
        b.invokeFunction();
    }

    @Test
    public void invokeFunctionWithoutSettingFunction() throws IOException {
        Invocation.Builder b = new Invocation.Builder(neow)
                .withContract(new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"))
                .withWallet(Wallet.createWallet());

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage("function");
        b.invokeFunction();
    }

    @Test
    public void buildWithoutSettingScriptNorContract() throws IOException {
        Invocation.Builder b = new Invocation.Builder(neow)
                .withWallet(Wallet.createWallet())
                .withValidUntilBlock(1000);

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList("script", "contract")));
        b.build();
    }

    @Test
    public void buildWithSettingContractButNoFunction() throws IOException {
        Invocation.Builder b = new Invocation.Builder(neow)
                .withContract(new ScriptHash("de5f57d430d3dece511cf975a8d37848cb9e0525"))
                .withWallet(Wallet.createWallet())
                .withValidUntilBlock(1000);

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList("contract",
                "function")));
        b.build();
    }
}