package io.neow3j.wallet;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.neow3j.transaction.VerificationScript;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.neow3j.constants.InteropServiceCode;
import io.neow3j.constants.OpCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptHash;
import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AccountTest {

    private static final String ADDRESS = "NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke";
    private static final String ACCOUNT_JSON_ADDRESS = "NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj";
    private static final String ACCOUNT_JSON_KEY =
            "6PYNxavNrrWiCNgLtd5WJjerGUwJD7LPp5Pzt85azUo4nLHL9dUkJaYtAo";
    private static final String WIF = "L3kCZj6QbFPwbsVhxnB8nUERDy4mhCSrWJew4u5Qh5QmGMfnCTda";
    private static final ScriptHash NEO_SCRIPT_HASH = new ScriptHash(
            "de5f57d430d3dece511cf975a8d37848cb9e0525");
    private static final ScriptHash GAS_SCRIPT_HASH = new ScriptHash(
            "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testCreateGenericAccount() {
        Account a = Account.create();
        assertThat(a, notNullValue());
        assertThat(a.getAddress(), notNullValue());
        assertThat(a.getVerificationScript(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
    }

    @Test
    public void testThrowIfNoECKeyPair() {
        Account account = Account.fromVerificationScript(new VerificationScript(
                Numeric.hexStringToByteArray(
                        "027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b2")));

        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("account does not hold an EC key pair");

        account.getECKeyPair();
    }

    @Test
    public void testFromNewECKeyPair() {
        Account a = Account.fromNewECKeyPair()
                .label("example")
                .lock();

        assertThat(a, notNullValue());
        assertThat(a.getAddress(), notNullValue());
        assertThat(a.getVerificationScript(), notNullValue());
        assertThat(a.getECKeyPair(), notNullValue());
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getLabel(), is("example"));
        assertThat(a.getECKeyPair(), notNullValue());
        assertTrue(a.isLocked());
    }

    @Test
    public void testBuildAccountFromExistingKeyPair() {
        // Used neo-core with address version 0x17 to generate test data.
        String expectedAdr = "NbT3sj3nWX3NUMbxVrphzGkS5yX5BHgRAb";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        String verScript = Numeric.toHexString(ScriptBuilder.buildVerificationScript(
                Numeric.hexStringToByteArray(
                        "027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b2")));

        Account a = new Account(pair);
        assertThat(a.isMultiSig(), is(false));
        assertThat(a.getECKeyPair(), is(pair));
        assertThat(a.getAddress(), is(expectedAdr));
        assertThat(a.getLabel(), is(expectedAdr));
        assertThat(a.getVerificationScript().getScript(),
                is(Numeric.hexStringToByteArray(verScript)));
    }

    @Test
    public void testFromVerificationScript() {
        Account account = Account.fromVerificationScript(
                new VerificationScript(
                        Numeric.hexStringToByteArray("0x0c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b600b4195440d78")));

        assertThat(account.getAddress(), is("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));
        assertThat(account.getVerificationScript().getScript(),
                is(Numeric.hexStringToByteArray("0x0c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b600b4195440d78")));
    }

    @Test
    public void testFromPublicKey() {
        ECPublicKey publicKey = new ECPublicKey(
                "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60");
        Account account = Account.fromPublicKey(publicKey);

        assertThat(account.getAddress(), is("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));
        assertThat(account.getVerificationScript().getScript(),
                is(Numeric.hexStringToByteArray("0x0c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b600b4195440d78")));
    }

    @Test
    public void testFromMultiSigKeys() {
        // Used neo-core with address version 0x17 to generate test data.
        String adr = "NL4YbCW5ZPr97gFujxvcCdC5bnditu4aNg";
        ECKeyPair pair = ECKeyPair.create(Numeric.hexStringToByteArray(
                "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"));
        List<ECPublicKey> keys = Arrays.asList(pair.getPublicKey(), pair.getPublicKey());
        Account a = Account.createMultiSigAccount(keys, 2);
        byte[] verScript = ScriptBuilder.buildVerificationScript(
                Arrays.asList(
                        Numeric.hexStringToByteArray(
                                "027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b2"),
                        Numeric.hexStringToByteArray(
                                "027a593180860c4037c83c12749845c8ee1424dd297fadcb895e358255d2c7d2b2")),
                2);
        assertThat(a.isMultiSig(), is(true));
        assertThat(a.getAddress(), is(adr));
        assertThat(a.getLabel(), is(adr));
        assertThat(a.getVerificationScript().getScript(), is(verScript));
    }

    @Test
    public void testEncryptPrivateKey() throws CipherException {
        String privKeyString = "3d7f55bf3fd8bfdaa8c8dd36bc5b4e003f8c90a39da9916fcecf38c5be94bd1c";
        ECKeyPair keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(privKeyString));
        String password = "neo";
        // Used neo-core with address version 0x35 to generate the encrypted key.
        String expectedNep2Encrypted = "6PYSQWBqZE5oEFdMGCJ3xR7bz6ezz814oKE7GqwB9i5uhtUzkshe9B6YGB";
        Account a = new Account(keyPair);
        a.encryptPrivateKey(password);
        assertThat(a.getEncryptedPrivateKey(), is(expectedNep2Encrypted));
    }

    @Test
    public void failEncryptAccountWithoutPrivateKey() throws CipherException {
        Account a = Account.fromAddress(ACCOUNT_JSON_ADDRESS);
        exceptionRule.expect(AccountStateException.class);
        a.encryptPrivateKey("pwd");
    }

    @Test
    public void decryptWithStandardScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        final ECPrivateKey privateKey = new ECPrivateKey(Numeric.toBigInt(
                "c2b590be636cb7a2377d40bf13d948bed85fe45e155ecf839dba0df45e4a35f0"));
        String password = "neo";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String nep2Encrypted = "6PYNxavNrrWiCNgLtd5WJjerGUwJD7LPp5Pzt85azUo4nLHL9dUkJaYtAo";

        NEP6Account nep6Acct = new NEP6Account("", "", true, false, nep2Encrypted, null, null);
        Account a = Account.fromNEP6Account(nep6Acct);
        a.decryptPrivateKey(password);
        assertThat(a.getECKeyPair().getPrivateKey(), is(privateKey));
        a.decryptPrivateKey(password); // This shouldn't do or change anything.
        assertThat(a.getECKeyPair().getPrivateKey(), is(privateKey));
    }

    @Test
    public void failDecryptingAccountWithoutDecryptedPrivateKey()
            throws NEP2InvalidFormat, CipherException, NEP2InvalidPassphrase {

        Account a = Account.fromAddress(ADDRESS);
        exceptionRule.expect(AccountStateException.class);
        a.decryptPrivateKey("neo");
    }

    @Test
    public void loadAccountFromNEP6Account() throws URISyntaxException, IOException {
        URL nep6AccountFileUrl = AccountTest.class.getClassLoader().getResource("account.json");
        FileInputStream stream = new FileInputStream(new File(nep6AccountFileUrl.toURI()));
        NEP6Account nep6Acc = new ObjectMapper().readValue(stream, NEP6Account.class);
        Account a = Account.fromNEP6Account(nep6Acc);
        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertThat(a.getAddress(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getEncryptedPrivateKey(), is(ACCOUNT_JSON_KEY));
        ScriptBuilder scriptBuilder = new ScriptBuilder();
        byte[] expectedScript = scriptBuilder
                .pushData(Numeric.hexStringToByteArray(
                        "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60"))
                .opCode(OpCode.PUSHNULL)
                .sysCall(InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1)
                .toArray();
        assertThat(a.getVerificationScript().getScript(), is(expectedScript));
    }

    @Test
    public void toNep6AccountWithOnlyAnAddress() {
        Account a = Account.fromAddress(ACCOUNT_JSON_ADDRESS);
        NEP6Account nep6 = a.toNEP6Account();

        assertThat(nep6.getContract(), nullValue());
        assertFalse(nep6.getDefault());
        assertFalse(nep6.getLock());
        assertThat(nep6.getAddress(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(nep6.getLabel(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(nep6.getKey(), is(nullValue()));
    }

    @Test
    public void toNep6AccountWithUnencryptedPrivateKey() {
        Account a = Account.fromWIF(WIF);
        exceptionRule.expect(AccountStateException.class);
        exceptionRule.expectMessage(new StringContains("private key"));
        a.toNEP6Account();
    }

    @Test
    public void toNep6AccountWithEncryptedPrivateKey() throws CipherException {
        Account a = Account.fromWIF(WIF);
        a.encryptPrivateKey("neo");
        NEP6Account nep6 = a.toNEP6Account();

        ScriptBuilder scriptBuilder = new ScriptBuilder();
        String expectedScript = Base64.encode(scriptBuilder
                .pushData(Numeric.hexStringToByteArray(
                        "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60"))
                .opCode(OpCode.PUSHNULL)
                .sysCall(InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1)
                .toArray());
        assertThat(nep6.getContract().getScript(), is(expectedScript));
        assertThat(nep6.getKey(), is(ACCOUNT_JSON_KEY));
        assertFalse(nep6.getDefault());
        assertFalse(nep6.getLock());
        assertThat(nep6.getAddress(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(nep6.getLabel(), is(ACCOUNT_JSON_ADDRESS));
    }

    @Test
    public void toNep6AccountWithMultiSigAccount() {
        String publicKey = "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60";
        ECPublicKey key = new ECPublicKey(Numeric.hexStringToByteArray(publicKey));
        Account a = Account.createMultiSigAccount(Arrays.asList(key), 1);
        NEP6Account nep6 = a.toNEP6Account();

        ScriptBuilder scriptBuilder = new ScriptBuilder();
        String expectedScript = Base64.encode(scriptBuilder
                .pushInteger(1)
                .pushData(Numeric.hexStringToByteArray(
                        "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60"))
                .pushInteger(1)
                .opCode(OpCode.PUSHNULL)
                .sysCall(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1)
                .toArray());
        assertThat(nep6.getContract().getScript(), is(expectedScript));
        assertFalse(nep6.getDefault());
        assertFalse(nep6.getLock());
        assertThat(nep6.getAddress(), is("NX8GreRFGFK5wpGMWetpX93HmtrezGogzk"));
        assertThat(nep6.getLabel(), is("NX8GreRFGFK5wpGMWetpX93HmtrezGogzk"));
        assertThat(nep6.getKey(), is(nullValue()));
        assertThat(nep6.getContract().getParameters().get(0).getParamName(), is("signature0"));
        assertThat(nep6.getContract().getParameters().get(0).getParamType(), is(
                ContractParameterType.SIGNATURE));
    }

    @Test
    public void createAccountFromWIF() {
        Account a = Account.fromWIF(WIF);
        byte[] expectedPrivKey = Numeric.hexStringToByteArray(
                "c2b590be636cb7a2377d40bf13d948bed85fe45e155ecf839dba0df45e4a35f0");
        ECKeyPair expectedKeyPair = ECKeyPair.create(expectedPrivKey);
        assertThat(a.getECKeyPair(), is(expectedKeyPair));
        assertThat(a.getAddress(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getLabel(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getScriptHash().toString(), is("0f46dc4287b70117ce8354924b5cb3a47215ad93"));
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
        ScriptBuilder scriptBuilder = new ScriptBuilder();
        byte[] verifScript = scriptBuilder
                .pushData(Numeric.hexStringToByteArray(
                        "02163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b60"))
                .opCode(OpCode.PUSHNULL)
                .sysCall(InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1)
                .toArray();
        assertThat(a.getVerificationScript().getScript(), is(verifScript));
    }

    @Test
    public void createAccountFromAddress() {
        Account a = Account.fromAddress(ACCOUNT_JSON_ADDRESS);
        assertThat(a.getAddress(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getLabel(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getScriptHash().toString(), is("0f46dc4287b70117ce8354924b5cb3a47215ad93"));
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
        assertThat(a.getVerificationScript(), is(nullValue()));
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Test
    public void getNep17Balances() throws IOException {
        int port = this.wireMockRule.port();
        WireMock.configureFor(port);

        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        Account a = Account.fromAddress(ADDRESS);
        WalletTestHelper.setUpWireMockForCall("getnep17balances",
                "getnep17balances_NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke.json",
                ADDRESS);
        Map<ScriptHash, BigInteger> balances = a.getNep17Balances(neow);
        assertThat(balances.keySet(), contains(GAS_SCRIPT_HASH, NEO_SCRIPT_HASH));
        assertThat(balances.values(), contains(
                new BigInteger("300000000"),
                new BigInteger("5")));
    }

    @Test
    public void isMultiSig() {
        Account a = Account.fromAddress(ADDRESS);
        exceptionRule.expect(AccountStateException.class);
        exceptionRule.expectMessage("verification script");
        a.isMultiSig();
    }

    @Test
    public void testUnsetLocked() {
        Account a = Account.fromAddress(ADDRESS).lock();
        assertTrue(a.isLocked());

        a.unlock();
        assertFalse(a.isLocked());
    }

    @Test
    public void testIsDefault() {
        Account a = Account.fromAddress(ADDRESS);
        Wallet wallet = Wallet.create().addAccounts(a);

        assertFalse(a.isDefault());

        wallet.defaultAccount(a.getScriptHash());
        assertTrue(a.isDefault());
    }

    @Test
    public void testWalletLink() {
        Account a = Account.fromAddress(ADDRESS);
        Wallet wallet = Wallet.create();

        assertNull(a.getWallet());

        wallet.addAccounts(a);
        assertNotNull(a.getWallet());
        assertEquals(wallet, a.getWallet());
    }
}
