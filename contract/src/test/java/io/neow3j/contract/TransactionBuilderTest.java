package io.neow3j.contract;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.neow3j.contract.ContractParameter.any;
import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.integer;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForBalanceOf;
import static io.neow3j.contract.ContractTestHelper.setUpWireMockForCall;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.WIF;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.methods.response.NeoApplicationLog;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.NeoInvokeScript;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.HighPriorityAttribute;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionAttribute;
import io.neow3j.transaction.TransactionAttributeType;
import io.neow3j.transaction.Witness;
import io.neow3j.transaction.WitnessScope;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.reactivex.Observable;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class TransactionBuilderTest {

    private static final Hash160 NEO_TOKEN_SCRIPT_HASH = NeoToken.SCRIPT_HASH;
    private static final String NEP17_TRANSFER = "transfer";

    private static final String SCRIPT_NEO_INVOKEFUNCTION_SYMBOL = Numeric.toHexStringNoPrefix(
            new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH, "symbol", new ArrayList<>())
                    .toArray());

    private Account account1;
    private Account account2;
    private Account multiSigAcc;
    private Hash160 recipient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private Neow3j neow;

    @Before
    public void setUp() {
        // Configuring WireMock to use default host and the dynamic port set in WireMockRule.
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);
        neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        neow.setNetworkMagicNumber(769);
        account1 = new Account(ECKeyPair.create(Numeric.hexStringToByteArray(
                "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3")));
        account2 = new Account(ECKeyPair.create(Numeric.hexStringToByteArray(
                "b4b2b579cac270125259f08a5f414e9235817e7637b9a66cfeb3b77d90c8e7f9")));
        multiSigAcc = Account.createMultiSigAccount(asList(
                account1.getECKeyPair().getPublicKey(),
                account2.getECKeyPair().getPublicKey()),
                2);
        recipient = new Hash160("969a77db482f74ce27105f760efa139223431394");
    }

    @Test
    public void buildTransactionWithCorrectNonce() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_necessary_mock.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Long nonce = ThreadLocalRandom.current().nextLong((long) Math.pow(2, 32));
        TransactionBuilder b = new TransactionBuilder(neow)
                .validUntilBlock(1L)
                .wallet(Wallet.withAccounts(account1))
                .script(new byte[]{1, 2, 3})
                .signers(Signer.calledByEntry(account1.getScriptHash()));

        Transaction t = b.nonce(nonce).buildTransaction();
        assertThat(t.getNonce(), is(nonce));

        nonce = 0L;
        t = b.nonce(0L).buildTransaction();
        assertThat(t.getNonce(), is(nonce));

        nonce = (long) Math.pow(2, 32) - 1;
        t = b.nonce(nonce).buildTransaction();
        assertThat(t.getNonce(), is(nonce));

        nonce = Integer.toUnsignedLong(-1);
        t = b.nonce(nonce).buildTransaction();
        assertThat(t.getNonce(), is(nonce));
    }

    @Test
    public void failBuildingTransactionWithIncorrectNonce() {
        TransactionBuilder b = new TransactionBuilder(neow)
                .validUntilBlock(1L)
                .wallet(Wallet.withAccounts(account1))
                .script(new byte[]{1, 2, 3})
                .signers(Signer.calledByEntry(account1.getScriptHash()));
        try {
            Long nonce = Integer.toUnsignedLong(-1) + 1;
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {
        }

        try {
            Long nonce = (long) Math.pow(2, 32);
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {
        }

        try {
            Long nonce = -1L;
            b.nonce(nonce);
            fail();
        } catch (TransactionConfigurationException ignored) {
        }
    }

    @Test
    public void failBuildingTransactionWithNegativeValidUntilBlockNumber() {
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("cannot be less than zero");
        new TransactionBuilder(neow)
                .validUntilBlock(-1L)
                .wallet(Wallet.withAccounts(account1))
                .script(new byte[]{1, 2, 3})
                .signers(Signer.calledByEntry(account1.getScriptHash()));
    }

    @Test
    public void failBuildingTransactionWithTooHighValidUntilBlockNumber() {
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("cannot be less than zero or more than 2^32");
        new TransactionBuilder(neow)
                .validUntilBlock((long) Math.pow(2, 32))
                .wallet(Wallet.withAccounts(account1))
                .script(new byte[]{1, 2, 3})
                .signers(Signer.calledByEntry(account1.getScriptHash()));
    }

    @Test
    public void automaticallySetNonce() throws Throwable {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokescript", "invokescript_necessary_mock.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Transaction transaction = new TransactionBuilder(neow)
                .wallet(Wallet.withAccounts(account1))
                .script(new byte[]{1, 2, 3})
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .buildTransaction();

        assertThat(transaction.getNonce(), greaterThanOrEqualTo(0L));
        assertThat(transaction.getNonce(), lessThanOrEqualTo((long) Math.pow(2, 32)));
    }

    @Test
    public void failBuildingTxWithoutAnySigner() throws Throwable {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Can't create a transaction without signers.");
        new TransactionBuilder(neow)
                .validUntilBlock(100L)
                .wallet(Wallet.withAccounts(account1))
                .script(new byte[]{1, 2, 3})
                .buildTransaction();
    }

    @Test
    public void failAddingMultipleSignersConcerningTheSameAccount() {
        TransactionBuilder b = new TransactionBuilder(neow);
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("concerning the same account");
        b.signers(Signer.global(account1.getScriptHash()),
                Signer.calledByEntry(account1.getScriptHash()));
    }

    @Test
    public void overrideSigner() {
        TransactionBuilder b = new TransactionBuilder(neow);
        b.signers(Signer.global(account1.getScriptHash()));
        assertThat(b.getSigners(), hasSize(1));
        assertThat(b.getSigners().get(0), is(Signer.global(account1.getScriptHash())));

        b.signers(Signer.calledByEntry(account2.getScriptHash()));
        assertThat(b.getSigners(), hasSize(1));
        assertThat(b.getSigners().get(0), is(Signer.calledByEntry(account2.getScriptHash())));
    }

    @Test
    public void failAddingMultipleFeeOnlySigners() {
        TransactionBuilder b = new TransactionBuilder(neow);
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage(
                new StringContains("Can't add multiple signers with the fee-only witness scope"));
        b.signers(Signer.feeOnly(account1.getScriptHash()),
                Signer.feeOnly(account2.getScriptHash()));
    }

    @Test
    public void attributes_highPriority() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForCall("getcommittee", "getcommittee.json");
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);

        Wallet wallet = Wallet.withAccounts(account1);
        HighPriorityAttribute attr = new HighPriorityAttribute();

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .attributes(attr)
                .signers(Signer.feeOnly(wallet.getDefaultAccount().getScriptHash()))
                .buildTransaction();

        assertThat(tx.getAttributes(), hasSize(1));
        assertThat(tx.getAttributes().get(0).getType(),
                is(TransactionAttributeType.HIGH_PRIORITY));
    }

    @Test
    public void attributes_highPriority_multiSigContainingCommitteeMember() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForCall("getcommittee", "getcommittee.json");
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);

        Account multiSigAccount = Account.createMultiSigAccount(
                asList(account2.getECKeyPair().getPublicKey(),
                        account1.getECKeyPair().getPublicKey()),
                1);
        Wallet wallet = Wallet.withAccounts(multiSigAccount, account1);
        HighPriorityAttribute attr = new HighPriorityAttribute();

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .attributes(attr)
                .signers(Signer.feeOnly(multiSigAccount.getScriptHash()))
                .buildTransaction();

        assertThat(tx.getAttributes(), hasSize(1));
        assertThat(tx.getAttributes().get(0).getType(),
                is(TransactionAttributeType.HIGH_PRIORITY));
    }

    @Test
    public void attributes_highPriority_noCommitteeMember() throws Throwable {
        setUpWireMockForCall("getcommittee", "getcommittee.json");
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);

        Wallet wallet = Wallet.withAccounts(account2);
        HighPriorityAttribute attr = new HighPriorityAttribute();

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Only committee members can send transactions with high " +
                                    "priority.");

        new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .attributes(attr)
                .signers(Signer.feeOnly(wallet.getDefaultAccount().getScriptHash()))
                .buildTransaction();
    }

    @Test
    public void attributes_highPriority_onlyAddedOnce() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForCall("getcommittee", "getcommittee.json");
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);

        Wallet wallet = Wallet.withAccounts(account1);
        HighPriorityAttribute attr1 = new HighPriorityAttribute();
        HighPriorityAttribute attr2 = new HighPriorityAttribute();

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .signers(Signer.feeOnly(wallet.getDefaultAccount().getScriptHash()))
                .wallet(wallet)
                .attributes(attr1)
                .attributes(attr2)
                .buildTransaction();

        assertThat(tx.getAttributes(), hasSize(1));
    }

    @Test
    public void attributes_failAddingMoreThanMaxToTxBuilder() {
        List<TransactionAttribute> attrs = new ArrayList<>();
        for (int i = 0; i <= NeoConstants.MAX_TRANSACTION_ATTRIBUTES; i++) {
            attrs.add(new HighPriorityAttribute());
        }
        TransactionAttribute[] attrArray = attrs.toArray(new TransactionAttribute[0]);

        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("A transaction cannot have " +
                                    "more than " + NeoConstants.MAX_TRANSACTION_ATTRIBUTES +
                                    " attributes.");

        new TransactionBuilder(neow).attributes(attrArray);
    }

    @Test
    public void failWithoutSettingWallet() throws Throwable {
        TransactionBuilder b = new TransactionBuilder(neow);
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("wallet");
        b.buildTransaction();
    }

    @Test
    public void testAutomaticSettingOfValidUntilBlockVariable() throws Throwable {
        Wallet wallet = Wallet.create();
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        ContractTestHelper.setUpWireMockForGetBlockCount(1000);

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .signers(Signer.feeOnly(wallet.getDefaultAccount().getScriptHash()))
                .buildTransaction();

        assertThat(tx.getValidUntilBlock(),
                is((long) NeoConstants.MAX_VALID_UNTIL_BLOCK_INCREMENT + 1000 - 1));
    }

    @Test
    public void testAutomaticSettingOfSystemFeeAndNetworkFee() throws Throwable {
        Wallet wallet = Wallet.create();
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .signers(Signer.feeOnly(wallet.getDefaultAccount().getScriptHash()))
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSystemFee(), is(984060L));
        assertThat(tx.getNetworkFee(), is(1230610L));
    }

    @Test
    public void failTryingToSignTransactionWithAccountMissingAPrivateKey() throws Throwable {
        Wallet w = Wallet.create("neo");
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        TransactionBuilder builder = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .signers(Signer.feeOnly(w.getAccounts().get(0).getScriptHash()))
                .wallet(w)
                .validUntilBlock(1000);

        exceptionRule.expect(TransactionConfigurationException.class);
        builder.sign();
    }

    @Test
    public void failTryingToSignTransaction_multiSig_withoutEnoughSigningAccounts()
            throws Throwable {
        Wallet w = Wallet.create();
        Account a2 = Account.create();
        List<ECPublicKey> keys = asList(w.getAccounts().get(0).getECKeyPair().getPublicKey(),
                a2.getECKeyPair().getPublicKey());
        Account multiSigAcc = Account.createMultiSigAccount(keys, 2);
        w.addAccounts(a2);
        w.addAccounts(multiSigAcc);
        a2.encryptPrivateKey("neo");
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(w)
                .signers(Signer.feeOnly(multiSigAcc.getScriptHash()))
                .validUntilBlock(1000);

        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("Wallet does not contain enough accounts (with decrypted " +
                                    "private keys)");
        b.sign();
    }

    @Test
    public void signMultiSigTransaction_continueAfterNotFindingFirstSigningAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        // Dummy multi-sig that only requires one signature.
        Account dummyMultiSig = Account.createMultiSigAccount(asList(
                account1.getECKeyPair().getPublicKey(),
                account2.getECKeyPair().getPublicKey()),
                1);
        Wallet w = Wallet.withAccounts(dummyMultiSig, account2);
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(w)
                .signers(Signer.feeOnly(dummyMultiSig.getScriptHash()));
        // The first signing account for the multi-sig is not in the wallet.
        // The sign method should execute normally and ignore the absence.
        b.sign();
        assertThat(b.transaction.getWitnesses().get(0).getScriptHash(),
                is(dummyMultiSig.getScriptHash()));
        assertThat(Numeric.toHexStringNoPrefix(b.transaction.getScript()),
                is(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL));
    }

    @Test
    public void addDefaultAccountSignerIfNotExplicitlySet() throws Throwable {
        Wallet wallet = Wallet.create();
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .signers(Signer.feeOnly(wallet.getDefaultAccount().getScriptHash()))
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(wallet.getDefaultAccount().getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
                contains(WitnessScope.NONE));
    }

    @Test
    public void dontAddDuplicateDefaultAccountSignerIfAlreadySetExplicitly() throws Throwable {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account acc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(acc);
        Signer signer = Signer.calledByEntry(acc.getScriptHash());
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
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
    public void addSenderSignerIfNotExplicitlySetAndNoOtherSignerIsSet() throws Throwable {
        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(senderAcc);
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .signers(Signer.feeOnly(senderAcc.getScriptHash()))
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
                contains(WitnessScope.NONE));
    }

    @Test
    public void addSenderSignerIfNotExplicitlySetAndAnotherSignerIsSet()
            throws Throwable {

        // WIF from key 000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f.
        final String wif = "KwDidQJHSE67VJ6MWRvbBKAxhD3F48DvqRT6JRqrjd7MHLBjGF7V";
        Account senderAcc = new Account(ECKeyPair.create(WIF.getPrivateKeyFromWIF(wif)));
        Wallet wallet = Wallet.withAccounts(senderAcc);
        Account other = Account.create();
        wallet.addAccounts(other);
        Signer signer = Signer.calledByEntry(other.getScriptHash());
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(wallet)
                .signers(Signer.feeOnly(senderAcc.getScriptHash()),
                        signer)
                .validUntilBlock(1000)
                .buildTransaction();

        assertThat(tx.getSigners(), hasSize(2));
        assertThat(tx.getSigners().get(0).getScriptHash(),
                is(senderAcc.getScriptHash()));
        assertThat(tx.getSigners().get(0).getScopes(),
                contains(WitnessScope.NONE));
        assertThat(tx.getSigners().get(1).getScriptHash(),
                is(signer.getScriptHash()));
        assertThat(tx.getSigners().get(1).getScopes(), is(signer.getScopes()));
    }

    @Test
    public void signTransactionWithAdditionalSigners() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Wallet w = Wallet.withAccounts(account1, account2);
        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
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

    @Test
    public void failBuildingTransactionBecauseWalletDoesntContainAnySignerAccount()
            throws Throwable {
        Wallet w = Wallet.create();
        Account signer = Account.create();
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(w)
                .signers(Signer.calledByEntry(signer.getScriptHash()))
                .validUntilBlock(1000); // Setting explicitly so that no RPC call is necessary.
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("No signers were set for which an account " +
                "with verification script exists in the wallet"));
        b.buildTransaction();
    }

    @Test
    public void failSendingTransactionBecauseItDoesntContainTheRightNumberOfWitnesses()
            throws Throwable {

        Wallet w = Wallet.create();
        Account signer = Account.create();
        w.addAccounts(signer);
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Transaction tx = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(w)
                .signers(Signer.calledByEntry(signer.getScriptHash()))
                .validUntilBlock(1000) // Setting explicitly so that no RPC call is necessary.
                .buildTransaction();
        // Don't add any witnesses, so it has one signer but no witness.
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage(new StringContains("The transaction does not have the same " +
                "number of signers and witnesses."));
        tx.send();
    }

    @Test
    public void sendInvokeFunction() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new NeoToken(neow)
                .invokeFunction(NEP17_TRANSFER,
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5),
                        any(null))
                .wallet(w)
                .signers(Signer.feeOnly(w.getDefaultAccount().getScriptHash()))
                .sign();

        NeoSendRawTransaction response = tx.send();

        assertThat(response.getError(), nullValue());
        // This is not the actual transaction id of the above built transaction but merely the
        // one used in the file `responses/sendrawtransaction.json`.
        assertThat(response.getSendRawTransaction().getHash(), is(
                "0x830816f0c801bcabf919dfa1a90d7b9a4f867482cb4d18d0631a5aa6daefab6a"));
    }

    @Test
    public void transferNeoFromNormalAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP17_TRANSFER, asList(
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5),
                        any(null)))
                .toArray();
        byte[] expectedVerificationScript = account1.getVerificationScript().getScript();

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new NeoToken(neow)
                .invokeFunction(NEP17_TRANSFER,
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5),
                        any(null))
                .wallet(w)
                .signers(Signer.feeOnly(w.getDefaultAccount().getScriptHash()))
                .validUntilBlock(100)
                .sign();

        assertThat(tx.getScript(), is(expectedScript));
        List<Witness> witnesses = tx.getWitnesses();
        assertThat(witnesses, hasSize(1));
        assertThat(witnesses.get(0).getVerificationScript().getScript(),
                is(expectedVerificationScript));
    }

    // Tests if the script and the verification script are correctly produced when an invocation
    // with a transfer from a multi-sig account is made.
    @Test
    public void transferNeoWithMultiSigAccount() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        byte[] expectedScript = new ScriptBuilder().contractCall(NEO_TOKEN_SCRIPT_HASH,
                NEP17_TRANSFER, asList(
                        hash160(multiSigAcc.getScriptHash()),
                        hash160(recipient),
                        integer(1),
                        any(null)))
                .toArray();
        byte[] expectedVerificationScript = multiSigAcc.getVerificationScript().getScript();

        Wallet w = Wallet.withAccounts(multiSigAcc, account1, account2);
        Transaction tx = new NeoToken(neow)
                .invokeFunction(NEP17_TRANSFER,
                        hash160(multiSigAcc.getScriptHash()),
                        hash160(recipient),
                        integer(1),
                        any(null))
                .wallet(w)
                .signers(Signer.feeOnly(w.getDefaultAccount().getScriptHash()))
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
                NEO_TOKEN_SCRIPT_HASH.toString(), NEP17_TRANSFER,
                account1.getScriptHash().toString(), recipient.toString(), "5"); // the params

        NeoInvokeFunction i = new NeoToken(neow)
                .callInvokeFunction(NEP17_TRANSFER,
                        asList(
                                hash160(account1.getScriptHash()),
                                hash160(recipient),
                                integer(5),
                                any(null)));

        // The script that's in the `invokefunction_transfer_neo.json` response file.
        String scriptInResponse =
                "CxUMFJQTQyOSE/oOdl8QJ850L0jbd5qWDBQGSl3MDxYsg0c9Aok46V" +
                "+3dhMechTAHwwIdHJhbnNmZXIMFIOrBnmtVcBQoTrUP1k26nP16x72QWJ9W1I=";
        assertThat(i.getResult().getScript(), is(scriptInResponse));
    }

    @Test
    public void doIfSenderCannotCoverFees() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_1000000.json");

        Wallet w = Wallet.withAccounts(account1);
        long netFee = 1230610;
        // The system fee found in the `invokescript_transfer_with_fixed_sysfee.json` file.
        long sysFee = 9999510;
        BigInteger expectedFees = BigInteger.valueOf(netFee + sysFee);
        BigInteger expectedBalance = BigInteger.valueOf(1_000_000L);
        AtomicBoolean tested = new AtomicBoolean(false);

        new GasToken(neow)
                .invokeFunction(NEP17_TRANSFER,
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(2_000_000),
                        any(null))
                .wallet(w)
                .signers(Signer.calledByEntry(w.getDefaultAccount().getScriptHash()))
                .validUntilBlock(2000000)
                .doIfSenderCannotCoverFees((fee, balance) -> {
                    assertThat(fee, is(expectedFees));
                    assertThat(balance, is(expectedBalance));
                    tested.set(true);
                })
                .buildTransaction();

        assertTrue(tested.get()); // Assert that the test actually called the lambda function.
    }

    @Test
    public void doIfSenderCannotCoverFees_alreadySpecifiedASupplier() {
        TransactionBuilder b = new TransactionBuilder(neow)
                .throwIfSenderCannotCoverFees(IllegalStateException::new);
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Can't handle a consumer for this case, since an exception");
        b.doIfSenderCannotCoverFees((fee, balance) -> System.out.println(fee));
    }

    @Test
    public void throwIfSenderCannotCoverFees() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("invokefunction", "invokefunction_balanceOf_1000000.json",
                "70e2301955bf1e74cbb31d18c2f96972abadb328",
                "balanceOf",
                "721e1376b75fe93889023d47832c160fcc5d4a06");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Wallet w = Wallet.withAccounts(account1);

        TransactionBuilder b = new NeoToken(neow)
                .invokeFunction(NEP17_TRANSFER,
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5),
                        any(null))
                .wallet(w)
                .validUntilBlock(2000000)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .throwIfSenderCannotCoverFees(
                        () -> new IllegalStateException("test throwIfSenderCannotCoverFees"));

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("test throwIfSenderCannotCoverFees");
        b.buildTransaction();
    }

    @Test
    public void throwIfSenderCannotCoverFees_alreadySpecifiedAConsumer() {
        TransactionBuilder b = new TransactionBuilder(neow)
                .doIfSenderCannotCoverFees((fee, balance) -> System.out.println(fee));
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Can't handle a supplier for this case, since a consumer");
        b.throwIfSenderCannotCoverFees(IllegalStateException::new);
    }

    @Test
    public void invokeScript() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json",
                SCRIPT_NEO_INVOKEFUNCTION_SYMBOL,
                "[\"721e1376b75fe93889023d47832c160fcc5d4a06\"]"); // witness (sender script hash)
        String privateKey = "e6e919577dd7b8e97805151c05ae07ff4f752654d6d8797597aca989c02c4cb3";
        ECKeyPair senderPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey));
        Account sender = new Account(senderPair);
        Wallet w = Wallet.withAccounts(sender);

        NeoInvokeScript response = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(w)
                .callInvokeScript();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("NEO"));
    }

    @Test
    public void invokeScriptWithoutSettingScript() throws IOException {
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json",
                SCRIPT_NEO_INVOKEFUNCTION_SYMBOL,
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
    public void buildWithoutSettingScript() throws Throwable {
        TransactionBuilder b = new TransactionBuilder(neow)
                .wallet(Wallet.create());

        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("script");
        b.buildTransaction();
    }

    @Test
    public void buildWithInvalidScript() throws Throwable {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokescript",
                "invokescript_invalidscript.json",
                "DAASDBSTrRVy");
        TransactionBuilder b = new TransactionBuilder(neow)
                .wallet(Wallet.withAccounts(account1))
                .script(Numeric.hexStringToByteArray("0c00120c1493ad1572"))
                .signers(Signer.calledByEntry(account1.getScriptHash()));
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("Instruction out of bounds.");
        b.buildTransaction();
    }

    @Test
    public void buildWithScript_vmFaults() throws Throwable {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokescript",
                "invokescript_exception.json",
                "DA5PcmFjbGVDb250cmFjdEEa93tn");
        TransactionBuilder b = new TransactionBuilder(neow)
                .wallet(Wallet.create())
                .script(Numeric.hexStringToByteArray("0c0e4f7261636c65436f6e7472616374411af77b67"))
                .signers(Signer.calledByEntry(account1.getScriptHash()));
        exceptionRule.expect(TransactionConfigurationException.class);
        exceptionRule.expectMessage("The vm exited due to the following exception: Value was " +
                                    "either too large or too small for an Int32.");
        b.buildTransaction();
    }

    @Test
    public void testGetUnsignedTransaction() throws Throwable {
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokescript", "invokescript_symbol_neo.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new TransactionBuilder(neow)
                .wallet(w)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .getUnsignedTransaction();

        assertThat(tx.getVersion(), is((byte) 0));
        assertThat(tx.getSigners(), hasSize(1));
        assertThat(tx.getSigners().get(0), is(Signer.calledByEntry(account1.getScriptHash())));
        assertThat(tx.getWitnesses(), hasSize(0));
    }

    @Test
    public void testVersion() {
        TransactionBuilder b = new TransactionBuilder(neow)
                .version((byte) 1);
        assertThat(b.getVersion(), is((byte) 1));
    }

    @Test
    public void testSetFirstSigner() {
        Signer s1 = Signer.global(account1.getScriptHash());
        Signer s2 = Signer.calledByEntry(account2.getScriptHash());
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(Wallet.create())
                .signers(s1, s2);
        assertThat(b.getSigners().get(0), is(s1));
        assertThat(b.getSigners().get(1), is(s2));

        b.firstSigner(s2.getScriptHash());
        assertThat(b.getSigners().get(0), is(s2));
        assertThat(b.getSigners().get(1), is(s1));
    }

    @Test
    public void testSetFirstSigner_account() {
        Signer s1 = Signer.global(account1.getScriptHash());
        Signer s2 = Signer.calledByEntry(account2.getScriptHash());
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(Wallet.create())
                .signers(s1, s2);
        assertThat(b.getSigners().get(0), is(s1));
        assertThat(b.getSigners().get(1), is(s2));

        b.firstSigner(account2);
        assertThat(b.getSigners().get(0), is(s2));
        assertThat(b.getSigners().get(1), is(s1));
    }

    @Test
    public void testSetFirstSigner_feeOnlyPresent() {
        Signer s1 = Signer.feeOnly(account1.getScriptHash());
        Signer s2 = Signer.calledByEntry(account2.getScriptHash());
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(Wallet.create())
                .signers(s1, s2);
        assertThat(b.getSigners().get(0), is(s1));
        assertThat(b.getSigners().get(1), is(s2));

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("contains a signer with fee-only witness scope");
        b.firstSigner(s2.getScriptHash());
    }

    @Test
    public void testSetFirstSigner_notPresent() {
        Signer s1 = Signer.global(account1.getScriptHash());
        TransactionBuilder b = new TransactionBuilder(neow)
                .script(Numeric.hexStringToByteArray(SCRIPT_NEO_INVOKEFUNCTION_SYMBOL))
                .wallet(Wallet.create())
                .signers(s1);
        assertThat(b.getSigners().get(0), is(s1));

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Could not find a signer with script hash");
        b.firstSigner(account2.getScriptHash());
    }

    @Test
    public void trackingTransactionShouldReturnCorrectBlock() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Wallet w = Wallet.withAccounts(account1);
        Neow3j neowSpy = Mockito.spy(neow);
        String txHash = "efd2ef6d0a68e01c2170110aab9d621df3e129dccae14e7ffc2b7b5822563885";
        neowSpy = Mockito.when(neowSpy.catchUpToLatestAndSubscribeToNewBlocksObservable(
                Mockito.any(BlockParameterIndex.class), Mockito.any(boolean.class)))
                .thenReturn(Observable.fromArray(createBlock(1000), createBlock(1001),
                        createBlock(1002, createTx(txHash)))).getMock();
        Transaction tx = new NeoToken(neowSpy)
                .invokeFunction(NEP17_TRANSFER,
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5),
                        any(null))
                .nonce(0L)
                .wallet(w)
                .signers(Signer.calledByEntry(w.getDefaultAccount().getScriptHash()))
                .sign();

        tx.send();
        CountDownLatch completedLatch = new CountDownLatch(1);
        AtomicLong receivedBlockNr = new AtomicLong();
        tx.track().subscribe(
                receivedBlockNr::set,
                throwable -> fail(throwable.getMessage()),
                completedLatch::countDown);

        completedLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(receivedBlockNr.get(), is(1002L));
    }

    @Test
    public void trackingTransaction_txNotSent() throws Throwable {
        setUpWireMockForCall("invokescript", "invokescript_transfer_with_fixed_sysfee.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");

        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new NeoToken(neow)
                .invokeFunction(NEP17_TRANSFER,
                        hash160(account1.getScriptHash()),
                        hash160(recipient),
                        integer(5),
                        any(null))
                .nonce(0L)
                .wallet(w)
                .signers(Signer.feeOnly(w.getDefaultAccount().getScriptHash()))
                .sign();

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("subscribe before transaction has been sent");
        tx.track();
    }

    private NeoGetBlock createBlock(int number) {
        NeoGetBlock neoGetBlock = new NeoGetBlock();
        NeoBlock block = new NeoBlock("", 0L, 0, "", "", 123456789, number, "nonce", null, null,
                new ArrayList<>(), 1, "next");
        neoGetBlock.setResult(block);
        return neoGetBlock;
    }

    private NeoGetBlock createBlock(int number,
            io.neow3j.protocol.core.methods.response.Transaction tx) {

        NeoGetBlock neoGetBlock = new NeoGetBlock();
        NeoBlock block = new NeoBlock("", 0L, 0, "", "", 123456789, number, "nonce", null, null,
                singletonList(tx), 1, "next");
        neoGetBlock.setResult(block);
        return neoGetBlock;
    }

    private io.neow3j.protocol.core.methods.response.Transaction createTx(String txHash) {
        return new io.neow3j.protocol.core.methods.response.Transaction(txHash, 0, 0, 0L, "", "",
                "", 0L, null, null, null, null);
    }

    @Test
    public void getApplicationLog() throws Throwable {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_1000000.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("getapplicationlog", "getapplicationlog.json");
        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new NeoToken(neow)
                .transfer(w, account1.getScriptHash(), BigDecimal.ONE)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .wallet(w)
                .sign();
        tx.send();
        NeoApplicationLog applicationLog = tx.getApplicationLog();
        assertThat(applicationLog.getTransactionId(),
                is("0xeb52f99ae5cf923d8905bdd91c4160e2207d20c0cb42f8062f31c6743770e4d1"));
    }

    @Test
    public void getApplicationLog_txNotSent() throws Throwable {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_1000000.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new NeoToken(neow)
                .transfer(w, account1.getScriptHash(), BigDecimal.ONE)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .wallet(w)
                .sign();

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("application log before transaction has been sent");
        tx.getApplicationLog();
    }

    @Test
    public void getApplicationLog_notExisting() throws Throwable {
        setUpWireMockForBalanceOf(account1.getScriptHash(),
                "invokefunction_balanceOf_1000000.json");
        setUpWireMockForCall("getblockcount", "getblockcount_1000.json");
        setUpWireMockForCall("invokescript", "invokescript_transfer.json");
        setUpWireMockForCall("calculatenetworkfee", "calculatenetworkfee.json");
        setUpWireMockForCall("sendrawtransaction", "sendrawtransaction.json");
        setUpWireMockForCall("getapplicationlog", "getapplicationlog_unknowntx.json");
        Wallet w = Wallet.withAccounts(account1);
        Transaction tx = new NeoToken(neow)
                .transfer(w, account1.getScriptHash(), BigDecimal.ONE)
                .signers(Signer.calledByEntry(account1.getScriptHash()))
                .wallet(w)
                .sign();
        tx.send();

        assertNull(tx.getApplicationLog());
    }

}
