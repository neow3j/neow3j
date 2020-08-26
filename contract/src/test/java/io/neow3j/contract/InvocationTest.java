package io.neow3j.contract;

import static io.neow3j.contract.ContractTestHelper.setUpWireMockForBalanceOf;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.constants.NeoConstants;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.Invocation.Builder;
import io.neow3j.contract.exceptions.InvocationConfigurationException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.WIF;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.WitnessScope;
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

    private static final ScriptHash NEO_TOKEN_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final String NEP5_TRANSFER = "transfer";

    private static final String SCRIPT = Numeric.toHexStringNoPrefix(
            new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH, "name", new ArrayList<>())
                    .toArray());

    private Account account1;
    private Account account2;
    private Account multiSigAcc;
    private ScriptHash recipient;

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
        account1 = new Account(ECKeyPair.create(Numeric.hexStringToByteArray(
                "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3")));
        account2 = new Account(ECKeyPair.create(Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9")));
        multiSigAcc = Account.createMultiSigAccount(Arrays.asList(
                account1.getECKeyPair().getPublicKey(),
                account2.getECKeyPair().getPublicKey()),
                2);
        recipient = new ScriptHash("969a77db482f74ce27105f760efa139223431394");
    }

    @Test
    public void failWithoutSettingSenderAccount() {
        exceptionRule.expect(IllegalArgumentException.class);
        new Invocation.Builder(null);
    }

    @Test
    public void failWithoutSettingWallet() throws IOException {
        Invocation.Builder b = new Invocation.Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withFunction("name");
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
                .withContract(NEO_TOKEN_SCRIPT_HASH)
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
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Wallet wallet = Wallet.withAccounts(account1);
        long additionalFee = 100_000_000; // Additional fee of 1 GAS.
        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withValidUntilBlock(1000)
                .withAdditionalNetworkFee(additionalFee)
                .build();

        int signedTxSize = i.sign().getTransaction().getSize();
        long sizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        // PUSHDATA1 + PUSHDATA1 + PUSHNULL + ECDsaVerify
        long expectedVerificationFee = 180 + 180 + 30 + 1_000_000;
        long expectedFee = sizeFee + expectedVerificationFee + additionalFee;
        assertThat(i.getTransaction().getNetworkFee(), is(expectedFee));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithMultiSigAccount() throws Exception {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        Wallet wallet = Wallet.withAccounts(multiSigAcc, account1, account2);
        long additionalFee = 100_000_000;
        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withSigners(Signer.calledByEntry(multiSigAcc.getScriptHash()))
                .withValidUntilBlock(1000)
                .withAdditionalNetworkFee(additionalFee) // Additional fee of 1 GAS.
                .build()
                .sign();

        int signedTxSize = i.getTransaction().getSize();
        long expectedSizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        int m = 2;
        int n = 2;
        // PUSHDATA1 * m + PUSH2 + PUSHDATA1 * n + PUSH2 + PUSHNULL + ECDsaVerify * n
        long expectedVerificationFee = (180 * m) + 30 + (180 * n) + 30 + 30 + (1_000_000 * n);
        long expectedFee = expectedSizeFee + expectedVerificationFee + additionalFee;
        assertThat(i.getTransaction().getNetworkFee(), is(expectedFee));
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
    public void addDefaultAccountSignerIfNotExplicitlySet() throws IOException {

        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withValidUntilBlock(1000)
                .build();

        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0).getScriptHash(),
                is(wallet.getDefaultAccount().getScriptHash()));
        assertThat(i.getTransaction().getSigners().get(0).getScopes(),
                contains(WitnessScope.FEE_ONLY));
    }

    @Test
    public void dontAddDuplicateDefaultAccountSignerIfAlreadySetExplicitly() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account acc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(acc);
        Signer signer = Signer.calledByEntry(acc.getScriptHash());
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(wallet)
                .withSigners(signer)
                .withValidUntilBlock(1000)
                .build();

        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0).getScriptHash(), is(acc.getScriptHash()));
        assertThat(i.getTransaction().getSigners().get(0).getScopes(),
                contains(WitnessScope.CALLED_BY_ENTRY));
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

        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(i.getTransaction().getSigners().get(0).getScopes(),
                contains(WitnessScope.FEE_ONLY));
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
                .withSigners(signer)
                .withValidUntilBlock(1000)
                .build();

        assertThat(i.getTransaction().getSigners(), hasSize(2));
        assertThat(i.getTransaction().getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(i.getTransaction().getSigners().get(0).getScopes(),
                contains(WitnessScope.FEE_ONLY));
        assertThat(i.getTransaction().getSigners().get(1).getScriptHash(),
                is(signer.getScriptHash()));
        assertThat(i.getTransaction().getSigners().get(1).getScopes(), is(signer.getScopes()));
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
                .withSigners(Signer.calledByEntry(senderAcc.getScriptHash()))
                .withValidUntilBlock(1000)
                .build();

        assertThat(i.getTransaction().getSigners(), hasSize(1));
        assertThat(i.getTransaction().getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(i.getTransaction().getSigners().get(0).getScopes(),
                contains(WitnessScope.CALLED_BY_ENTRY));
    }

    @Test
    public void signTransactionWithAdditionalSigners() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Wallet w = Wallet.withAccounts(account1, account2);
        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .withSigners(Signer.calledByEntry(account1.getScriptHash()),
                        Signer.calledByEntry(account2.getScriptHash()))
                .withValidUntilBlock(1000)
                .build()
                .sign();

        List<Witness> witnesses = i.getTransaction().getWitnesses();
        assertThat(witnesses, hasSize(2));
        List<ECPublicKey> signers = witnesses.stream()
                .map(wit -> wit.getVerificationScript().getPublicKeys().get(0))
                .collect(Collectors.toList());
        assertThat(signers, containsInAnyOrder(
                account1.getECKeyPair().getPublicKey(),
                account2.getECKeyPair().getPublicKey()));
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
                .withSigners(Signer.calledByEntry(signer.getScriptHash()))
                .withValidUntilBlock(1000); // Setting explicitly so that no RPC call is necessary.
        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(
                new StringContains("Wallet does not contain the account for signer" +
                        " with script hash " + signer.getScriptHash()));
        b.build();
    }

    @Test
    public void failSigningInvocationBecauseWalletDoesntContainSignerAccount()
            throws IOException {

        Wallet w = Wallet.createWallet();
        Account account = Account.createAccount();
        w.addAccounts(account);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Invocation i = new Invocation.Builder(neow)
                .withScript(Numeric.hexStringToByteArray(SCRIPT))
                .withWallet(w)
                .withSigners(Signer.calledByEntry(account.getScriptHash()))
                .withValidUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build();
        w.removeAccount(account.getScriptHash());
        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("Can't create transaction "
                + "signature. Wallet does not contain the signer account with script "
                + "hash " + account.getScriptHash()));
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
                .withSigners(Signer.calledByEntry(signer.getScriptHash()))
                .withValidUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .build();

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("The transaction does not have a signature"
                + " for each of its signers"));
        i.send();
    }

    @Test
    public void sendInvokeFunction() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Wallet w = Wallet.withAccounts(account1);
        NeoSendRawTransaction i = new Invocation.Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withFunction(NEP5_TRANSFER)
                .withWallet(w)
                .withParameters(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
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
    public void transferNeoFromNormalAccount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5)))
                .opCode(OpCode.ASSERT).toArray();
        byte[] expectedVerificationScript = account1.getVerificationScript().getScript();

        Wallet w = Wallet.withAccounts(account1);
        Invocation i = new Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withFunction(NEP5_TRANSFER)
                .withWallet(w)
                .withValidUntilBlock(100)
                .withParameters(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))
                .failOnFalse()
                .build().sign();

        Transaction tx = i.getTransaction();
        assertThat(tx.getScript(), is(expectedScript));
        List<Witness> witnesses = tx.getWitnesses();
        assertThat(witnesses, hasSize(1));
        assertThat(witnesses.get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
    }

    // Tests if the script and the verification script are correctly produced when an invocation
    // with a transfer from a multi-sig account is made.
    @Test
    public void transferNeoWithMultiSigAccount() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP5_TRANSFER, Arrays.asList(
                        ContractParameter.hash160(multiSigAcc.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(1))) // 1 NEO
                .opCode(OpCode.ASSERT).toArray();
        byte[] expectedVerificationScript = multiSigAcc.getVerificationScript().getScript();

        Wallet w = Wallet.withAccounts(multiSigAcc, account1, account2);
        Invocation i = new Invocation.Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withFunction(NEP5_TRANSFER)
                .withWallet(w)
                .withValidUntilBlock(100)
                .withParameters(
                        ContractParameter.hash160(multiSigAcc.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(1))  // 1 NEO
                .failOnFalse()
                .build()
                .sign();

        Transaction tx = i.getTransaction();
        assertThat(tx.getScript(), is(expectedScript));
        List<Witness> witnesses = tx.getWitnesses();
        assertThat(witnesses, hasSize(1));
        assertThat(witnesses.get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
    }

    // This tests if the `invokeFunction()` method produces the right request.
    @Test
    public void invokingWithParamsShouldProduceTheCorrectRequest() throws IOException {
        setUpWireMockForCall("invokefunction", "invokefunction_transfer_neo.json",
                NEO_TOKEN_SCRIPT_HASH.toString(), NEP5_TRANSFER,
                account1.getScriptHash().toString(), recipient.toString(), "5"); // the params

        NeoInvokeFunction i = new Invocation.Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withFunction(NEP5_TRANSFER)
                .withParameters(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))
                .invokeFunction();

        // The script that's in the `invokefunction_transfer_neo.json` response file.
        String scriptInResponse =
                "150c14c8172ea3b405bf8bfc57c33a8410116b843e13df0c14941343239213fa0e765f1027ce742f48db779a9613c00c087472616e736665720c1425059ecb4878d3a875f91c51ceded330d4575fde41627d5b5238";
        assertThat(i.getResult().getScript(), is(scriptInResponse));
    }

    @Test
    public void callWithoutParams() throws IOException {
        setUpWireMockForCall("invokefunction",
                "invokefunction_name.json",
                NEO_TOKEN_SCRIPT_HASH.toString(),
                "name",
                "[\"721e1376b75fe93889023d47832c160fcc5d4a06\"]"
        );
        Wallet w = Wallet.withAccounts(account1);

        NeoInvokeFunction i = new Invocation.Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withFunction("name")
                .withWallet(w)
                .failOnFalse()
                .invokeFunction();

        assertThat(i.getResult().getStack().get(0).asByteString().getAsString(), is("NEO"));
        assertThat(i.getResult().getScript(), is(SCRIPT));
    }

    @Test
    public void doIfSenderCannotCoverFees() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_1000000.json");

        Wallet w = Wallet.withAccounts(account1);
        long netFee = 1248390;
        // The system fee found in the `invokescript_transfer_with_fixed_sysfee.json` file.
        long sysFee = 9007990;
        BigInteger expectedFees = BigInteger.valueOf(netFee + sysFee);
        BigInteger expectedBalance = BigInteger.valueOf(1_000_000L);
        AtomicBoolean tested = new AtomicBoolean(false);
        new Builder(neow)
                .withContract(GasToken.SCRIPT_HASH)
                .withFunction(NEP5_TRANSFER)
                .withWallet(w)
                .withValidUntilBlock(2000000)
                .failOnFalse()
                .withParameters(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(2_000_000))
                .build()
                .doIfSenderCannotCoverFees((fee, balance) -> {
                    assertThat(fee, is(expectedFees));
                    assertThat(balance, is(expectedBalance));
                    tested.set(true);
                });
        assertTrue(tested.get()); // Assert that the test actually called the lambda function.
    }

    @Test
    public void throwIfSenderCannotCoverFees() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_1000000.json",
                "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc",
                "balanceOf",
                "721e1376b75fe93889023d47832c160fcc5d4a06");

        Wallet w = Wallet.withAccounts(account1);

        Invocation i = new Invocation.Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withFunction(NEP5_TRANSFER)
                .withWallet(w)
                .withValidUntilBlock(2000000)
                .withParameters(
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))
                .failOnFalse()
                .build();

        exceptionRule.expect(IllegalStateException.class);
        i.throwIfSenderCannotCoverFees(IllegalStateException::new);
    }

    @Test
    public void invokeScript() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT,
                "[\"721e1376b75fe93889023d47832c160fcc5d4a06\"]"); // witness (sender script hash)
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
                "[\"721e1376b75fe93889023d47832c160fcc5d4a06\"]"); // witness (sender script hash)
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
                .withFunction(NEP5_TRANSFER)
                .withWallet(Wallet.createWallet());

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage("contract");
        b.invokeFunction();
    }

    @Test
    public void invokeFunctionWithoutSettingFunction() throws IOException {
        Invocation.Builder b = new Invocation.Builder(neow)
                .withContract(NEO_TOKEN_SCRIPT_HASH)
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
                .withContract(NEO_TOKEN_SCRIPT_HASH)
                .withWallet(Wallet.createWallet())
                .withValidUntilBlock(1000);

        exceptionRule.expect(InvocationConfigurationException.class);
        exceptionRule.expectMessage(new StringContainsInOrder(Arrays.asList("contract",
                "function")));
        b.build();
    }
}
