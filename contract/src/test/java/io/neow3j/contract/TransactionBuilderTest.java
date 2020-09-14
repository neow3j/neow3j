package io.neow3j.contract;

import static io.neow3j.contract.ContractTestHelper.setUpWireMockForBalanceOf;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TransactionBuilderTest {

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
    public void failWithoutSettingWallet() throws IOException {
        TransactionBuilder b = new TransactionBuilder(neow);
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("wallet");
        b.buildTransaction();
    }

    @Test
    public void testAutomaticSettingOfValidUntilBlockVariable() throws IOException {
        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .buildTransaction();

        assertThat(tx.getValidUntilBlock(),
                is((long) NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT + 1000 - 1));
    }

    // TODO: 09.09.20 Michael: Move to SmartContractTest
//    @Test
//    public void testCreationOfTheScript() throws IOException {
//        Wallet wallet = Wallet.createWallet();
//        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
//
//        Transaction tx = new TransactionBuilder(neow)
//                .contract(NEO_TOKEN_SCRIPT_HASH)
//                .function("name")
//                .wallet(wallet)
//                .validUntilBlock(1000)
//                .build();
//
//        assertThat(Numeric.toHexStringNoPrefix(tx.getScript()), is(SCRIPT));
//    }

    @Test
    public void testAutomaticSettingOfSystemFee() throws IOException {
        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSystemFee(), is(1007390L));

        // With fail on false.
        String script = "10c00c046e616d650c14897720d8cd76f4f00abfa37c0edd889c208fde9b41627d5b5238";
        setUpWireMockForCall("invokescript", "invokescript_name_neo_fail_on_false.json", script);
        tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(script))
                .wallet(wallet)
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSystemFee(), is(1007420L));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithSingleSigAccount() throws Exception {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Wallet wallet = Wallet.withAccounts(account1);
        long additionalFee = 100_000_000; // Additional fee of 1 GAS.
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .validUntilBlock(1000)
                .additionalNetworkFee(additionalFee);
        Transaction tx = b.buildTransaction();

        int signedTxSize = b.sign().getSize();
        long sizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        // PUSHDATA1 + PUSHDATA1 + PUSHNULL + ECDsaVerify
        long expectedVerificationFee = 180 + 180 + 30 + 1_000_000;
        long expectedFee = sizeFee + expectedVerificationFee + additionalFee;

        assertThat(tx.getNetworkFee(), is(expectedFee));
    }

    @Test
    public void testAutomaticSettingOfNetworkFeeWithMultiSigAccount() throws Exception {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        Wallet wallet = Wallet.withAccounts(multiSigAcc, account1, account2);
        long additionalFee = 100_000_000;
        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .signers(Signer.calledByEntry(multiSigAcc.getScriptHash()))
                .validUntilBlock(1000)
                .additionalNetworkFee(additionalFee) // Additional fee of 1 GAS.
                .sign();

        int signedTxSize = tx.getSize();
        long expectedSizeFee = signedTxSize * NeoConstants.GAS_PER_BYTE;
        int m = 2;
        int n = 2;
        // PUSHDATA1 * m + PUSH2 + PUSHDATA1 * n + PUSH2 + PUSHNULL + ECDsaVerify * n
        long expectedVerificationFee = (180 * m) + 30 + (180 * n) + 30 + 30 + (1_000_000 * n);
        long expectedFee = expectedSizeFee + expectedVerificationFee + additionalFee;

        assertThat(tx.getNetworkFee(), is(expectedFee));
    }

    @Test
    public void failTryingToSignInvocationWithAccountMissingAPrivateKey() throws Exception {
        Wallet w = Wallet.createWallet("neo");
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        TransactionBuilder builder = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .sender(w.getAccounts().get(0).getScriptHash())
                .wallet(w)
                .validUntilBlock(1000);

        exceptionRule.expect(TransactionConfigurationException.class);
        builder.sign();
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

        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(w)
                .sender(multiSigAcc.getScriptHash())
                .validUntilBlock(1000);

        exceptionRule.expect(TransactionConfigurationException.class);
        b.sign();
    }

    @Test
    public void addDefaultAccountSignerIfNotExplicitlySet() throws IOException {

        Wallet wallet = Wallet.createWallet();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(wallet.getDefaultAccount().getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
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

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .signers(signer)
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(), is(acc.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
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

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .sender(senderAcc.getScriptHash())
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
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

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .sender(senderAcc.getScriptHash())
                .signers(signer)
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(2));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
                contains(WitnessScope.FEE_ONLY));
        assertThat(tx.getSigners().get(1).getScriptHash(),
                is(signer.getScriptHash()));
        assertThat(tx.getSigners().get(1).getScopes(), is(signer.getScopes()));
    }

    @Test
    public void dontAddDuplicateSenderSignerIfAlreadySetExplicitly() throws IOException {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(senderAcc);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(wallet)
                .sender(senderAcc.getScriptHash())
                .signers(Signer.calledByEntry(senderAcc.getScriptHash()))
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
                contains(WitnessScope.CALLED_BY_ENTRY));
    }

    @Test
    public void signTransactionWithAdditionalSigners() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Wallet w = Wallet.withAccounts(account1, account2);
        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(w)
                .signers(Signer.calledByEntry(account1.getScriptHash()),
                        Signer.calledByEntry(account2.getScriptHash()))
                .validUntilBlock(1000)
                .sign();

        List<Witness> witnesses = tx.getWitnesses();
        assertThat(witnesses, hasSize(2));
        List<ECPublicKey> signers = witnesses.stream()
                .map(wit -> wit.getVerificationScript().getPublicKeys().get(0))
                .collect(Collectors.toList());
        assertThat(signers, containsInAnyOrder(
                account1.getECKeyPair().getPublicKey(),
                account2.getECKeyPair().getPublicKey()));
    }

    // TODO: 14.09.20 Michael: Check design. Do we need to throw, when building or only when trying to sign?
    @Test
    public void failBuildingTransactionBecauseWalletDoesntContainSignerAccount()
            throws IOException {

        Wallet w = Wallet.createWallet();
        Account signer = Account.createAccount();
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(w)
                .signers(Signer.calledByEntry(signer.getScriptHash()))
                .validUntilBlock(1000); // Setting explicitly so that no RPC call is necessary.
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage(
                new StringContains("Wallet does not contain the account for signer" +
                        " with script hash " + signer.getScriptHash()));
        b.buildTransaction();
    }

    @Test
    public void failSigningTransactionBecauseWalletDoesntContainSignerAccount()
            throws IOException {

        Wallet w = Wallet.createWallet();
        Account account = Account.createAccount();
        w.addAccounts(account);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(w)
                .signers(Signer.calledByEntry(account.getScriptHash()))
                .validUntilBlock(1000); // Setting explicitly so that no RPC call is necessary.
        w.removeAccount(account.getScriptHash());
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("Can't create transaction "
                + "signature. Wallet does not contain the signer account with script "
                + "hash " + account.getScriptHash()));
        b.sign();
    }

    @Test
    public void failSendingTransactionBecauseItDoesntContainSignaturesForAllSigners()
            throws IOException {

        Wallet w = Wallet.createWallet();
        Account signer = Account.createAccount();
        w.addAccounts(signer);
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT);

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(w)
                .signers(Signer.calledByEntry(signer.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .buildTransaction();

        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("The transaction does not have a signature"
                + " for each of its signers"));
        tx.send();
    }

    @Test
    public void sendInvokeFunction() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new NeoToken(neow)
                .invokeFunction(NEP5_TRANSFER,
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))
                .wallet(w)
                .sign();

        NeoSendRawTransaction response = tx.send();

        assertThat(response.getError(), nullValue());
        // This is not the actual transaction id of the above built transaction but merely the
        // one used in the file `responses/sendrawtransaction.json`.
        assertThat(response.getSendRawTransaction().getHash(), is(
                "0x830816f0c801bcabf919dfa1a90d7b9a4f867482cb4d18d0631a5aa6daefab6a"));
    }

    // TODO: 14.09.20 Michael: fix test. Expected script contains INITSSLOT Opcode, whereas the true does not.
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
        Transaction tx = new NeoToken(neow)
                .invokeFunction(NEP5_TRANSFER,
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))
                .wallet(w)
                .validUntilBlock(100)
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
        List<Witness> witnesses = tx.getWitnesses();
        assertThat(witnesses, hasSize(1));
        assertThat(witnesses.get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
    }

    // TODO: 14.09.20 Michael: fix test. Expected script contains INITSSLOT Opcode, whereas the true does not.
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
        Transaction tx = new NeoToken(neow)
                .invokeFunction(NEP5_TRANSFER,
                        ContractParameter.hash160(multiSigAcc.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(1))
                .wallet(w)
                .validUntilBlock(100)
                .sign();

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

        NeoInvokeFunction i = new NeoToken(neow)
                .callInvokeFunction(NEP5_TRANSFER,
                        Arrays.asList(
                                ContractParameter.hash160(account1.getScriptHash()),
                                ContractParameter.hash160(recipient),
                                ContractParameter.integer(5)
                        )
                );

//        NeoInvokeFunction i = new TransactionBuilder(neow)
//                .contract(NEO_TOKEN_SCRIPT_HASH)
//                .function(NEP5_TRANSFER)
//                .parameters(
//                        ContractParameter.hash160(account1.getScriptHash()),
//                        ContractParameter.hash160(recipient),
//                        ContractParameter.integer(5))
//                .invokeFunction();

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

        NeoInvokeFunction i = new NeoToken(neow)
                .callInvokeFunction("name");

//        NeoInvokeFunction i = new TransactionBuilder(neow)
//                .contract(NEO_TOKEN_SCRIPT_HASH)
//                .function("name")
//                .wallet(w)
//                .invokeFunction();

        assertThat(i.getResult().getStack().get(0).asByteString().getAsString(), is("NEO"));
        assertThat(i.getResult().getScript(), is(SCRIPT));
    }

    // TODO: 14.09.20 Michael: fix test. See comment in TransactionBuilder.java in method
    //  doIfSenderCannotCoverFees.
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

        new GasToken(neow)
                .invokeFunction(NEP5_TRANSFER,
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(2_000_000))
                .wallet(w)
                .validUntilBlock(2000000)
                .doIfSenderCannotCoverFees((fee, balance) -> {
                    assertThat(fee, is(expectedFees));
                    assertThat(balance, is(expectedBalance));
                    tested.set(true);
                })
                .buildTransaction();

        assertTrue(tested.get()); // Assert that the test actually called the lambda function.

//        new TransactionBuilder(neow)
//                .contract(GasToken.SCRIPT_HASH)
//                .function(NEP5_TRANSFER)
//                .wallet(w)
//                .validUntilBlock(2000000)
//                .failOnFalse()
//                .parameters(
//                        ContractParameter.hash160(account1.getScriptHash()),
//                        ContractParameter.hash160(recipient),
//                        ContractParameter.integer(2_000_000))
//                .doIfSenderCannotCoverFees((fee, balance) -> {
//                    assertThat(fee, is(expectedFees));
//                    assertThat(balance, is(expectedBalance));
//                    tested.set(true);
//                })
//                .build();
//        assertTrue(tested.get()); // Assert that the test actually called the lambda function.
    }

    // TODO: 14.09.20 Michael: fix test. See comment in TransactionBuilder.java in method
    //  throwIfSenderCannotCoverFees.
    @Test
    public void throwIfSenderCannotCoverFees() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_1000000.json",
                "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc",
                "balanceOf",
                "721e1376b75fe93889023d47832c160fcc5d4a06");

        Wallet w = Wallet.withAccounts(account1);

        TransactionBuilder b = new NeoToken(neow)
                .invokeFunction(NEP5_TRANSFER,
                        ContractParameter.hash160(account1.getScriptHash()),
                        ContractParameter.hash160(recipient),
                        ContractParameter.integer(5))
                .wallet(w)
                .validUntilBlock(2000000);

//        TransactionBuilder b = new TransactionBuilder(neow)
//                .contract(NEO_TOKEN_SCRIPT_HASH)
//                .function(NEP5_TRANSFER)
//                .wallet(w)
//                .validUntilBlock(2000000)
//                .parameters(
//                        ContractParameter.hash160(account1.getScriptHash()),
//                        ContractParameter.hash160(recipient),
//                        ContractParameter.integer(5))
//                .failOnFalse();

        exceptionRule.expect(IllegalStateException.class);
        b.throwIfSenderCannotCoverFees(IllegalStateException::new);
    }

    @Test
    public void invokeScript() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_name_neo.json", SCRIPT,
                "[\"721e1376b75fe93889023d47832c160fcc5d4a06\"]"); // witness (sender script hash)
        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);

        NeoInvokeScript response = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT))
                .wallet(w)
                .callInvokeScript();
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

        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("Cannot make an 'invokescript' call");
        new TransactionBuilder(neow)
                .wallet(w)
                .callInvokeScript();
    }

    @Test
    public void invokeFunctionWithoutSettingFunction() throws IOException {
        SmartContract neo = new SmartContract(NEO_TOKEN_SCRIPT_HASH, neow);
        List<ContractParameter> params = new ArrayList<>();

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("function");
        neo.callInvokeFunction("", params);
    }

    @Test
    public void invokeFunctionWithoutSettingFunction_noParams() throws IOException {
        SmartContract neo = new SmartContract(NEO_TOKEN_SCRIPT_HASH, neow);

        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("function");
        neo.callInvokeFunction("");
    }

    @Test
    public void buildWithoutSettingScript() throws IOException {
        TransactionBuilder b = new TransactionBuilder(neow)
                .wallet(Wallet.createWallet());

        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("script");
        b.buildTransaction();
    }
}
