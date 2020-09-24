package io.neow3j.wallet;

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

    private static final String ADDRESS = "AZt9DgwW8PKSEQsa9QLX86SyE1DSNjSbsS";
    private static final String ACCOUNT_JSON_ADDRESS = "AJunErzotcQTNWP2qktA7LgkXZVdHea97H";
    private static final String ACCOUNT_JSON_KEY =
            "6PYLykbKcbwnCuTJiQQ5PYu5uH9NgwGYLoMyTUabRxRJUsiA9GP8NgorUV";
    private static final String WIF = "L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR";
    private static final ScriptHash NEO_SCRIPT_HASH = new ScriptHash(
            "de5f57d430d3dece511cf975a8d37848cb9e0525");
    private static final ScriptHash GAS_SCRIPT_HASH = new ScriptHash(
            "668e0c1f9d7b70a99dd9e06eadd4c784d641afbc");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCreateGenericAccount() {
        Account a = Account.createAccount();
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
        String expectedAdr = "AXJxLU79D795wMRMmGq9SWaqCqnmpGw9uq";
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
    public void testFromMultiSigKeys() {
        // Used neo-core with address version 0x17 to generate test data.
        String adr = "AFvT3wZSFywrag5K1Nw3es2UieuRc9s3dj";
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
        assertThat(a.getECKeyPair(), is(nullValue()));
        assertThat(a.getLabel(), is(adr));
        assertThat(a.getVerificationScript().getScript(), is(verScript));
    }

    @Test
    public void testEncryptPrivateKey() throws CipherException {
        String privKeyString = "7fe9d4b69f85c1fe15387a76e79d2b95c4c9e3fe756de3435afbc077d99d5346";
        ECKeyPair keyPair = ECKeyPair.create(Numeric.hexStringToByteArray(privKeyString));
        String password = "neo";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String expectedNep2Encrypted = "6PYLykbKcbwnCuTJiQQ5PYu5uH9NgwGYLoMyTUabRxRJUsiA9GP8NgorUV";
        Account a = new Account(keyPair);
        a.encryptPrivateKey(password);
        assertThat(a.getEncryptedPrivateKey(), is(expectedNep2Encrypted));
    }

    @Test
    public void failEncryptAccountWithoutPrivateKey() throws CipherException {
        Account a = Account.fromAddress(ACCOUNT_JSON_ADDRESS);
        expectedException.expect(AccountStateException.class);
        a.encryptPrivateKey("pwd");
    }

    @Test
    public void decryptWithStandardScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        final ECPrivateKey privateKey = new ECPrivateKey(Numeric.toBigInt(
                "7fe9d4b69f85c1fe15387a76e79d2b95c4c9e3fe756de3435afbc077d99d5346"));
        String password = "neo";
        // Used neo-core with address version 0x17 to generate the encrypted key.
        String nep2Encrypted = "6PYLykbKcbwnCuTJiQQ5PYu5uH9NgwGYLoMyTUabRxRJUsiA9GP8NgorUV";

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
        expectedException.expect(AccountStateException.class);
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
                        "026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d"))
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
        expectedException.expect(AccountStateException.class);
        expectedException.expectMessage(new StringContains("private key"));
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
                        "026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d"))
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
        String publicKey = "026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d";
        ECPublicKey key = new ECPublicKey(Numeric.hexStringToByteArray(publicKey));
        Account a = Account.createMultiSigAccount(Arrays.asList(key), 1);
        NEP6Account nep6 = a.toNEP6Account();

        ScriptBuilder scriptBuilder = new ScriptBuilder();
        String expectedScript = Base64.encode(scriptBuilder
                .pushInteger(1)
                .pushData(Numeric.hexStringToByteArray(
                        "026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d"))
                .pushInteger(1)
                .opCode(OpCode.PUSHNULL)
                .sysCall(InteropServiceCode.NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1)
                .toArray());
        assertThat(nep6.getContract().getScript(), is(expectedScript));
        assertFalse(nep6.getDefault());
        assertFalse(nep6.getLock());
        assertThat(nep6.getAddress(), is("AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN"));
        assertThat(nep6.getLabel(), is("AGZLEiwUyCC4wiL5sRZA3LbxWPs9WrZeyN"));
        assertThat(nep6.getKey(), is(nullValue()));
        assertThat(nep6.getContract().getParameters().get(0).getParamName(), is("signature0"));
        assertThat(nep6.getContract().getParameters().get(0).getParamType(), is(
                ContractParameterType.SIGNATURE));
    }

    @Test
    public void createAccountFromWIF() {
        Account a = Account.fromWIF(WIF);
        byte[] expectedPrivKey = Numeric.hexStringToByteArray(
                "7fe9d4b69f85c1fe15387a76e79d2b95c4c9e3fe756de3435afbc077d99d5346");
        ECKeyPair expectedKeyPair = ECKeyPair.create(expectedPrivKey);
        assertThat(a.getECKeyPair(), is(expectedKeyPair));
        assertThat(a.getAddress(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getLabel(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getScriptHash().toString(), is("cc45cc8987b0e35371f5685431e3c8eeea306722"));
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
        ScriptBuilder scriptBuilder = new ScriptBuilder();
        byte[] verifScript = scriptBuilder
                .pushData(Numeric.hexStringToByteArray(
                        "026aa8fe6b4360a67a530e23c08c6a72525afde34719c5436f9d3ced759f939a3d"))
                .opCode(OpCode.PUSHNULL)
                .sysCall(InteropServiceCode.NEO_CRYPTO_VERIFYWITHECDSASECP256R1)
                .toArray();
        assertThat(a.getVerificationScript().getScript(), is(verifScript));
    }

    @Test
    public void createAccountFromAddress() {
        Account a = Account.fromAddress(ACCOUNT_JSON_ADDRESS);
        assertThat(a.getECKeyPair(), is(nullValue()));
        assertThat(a.getAddress(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getLabel(), is(ACCOUNT_JSON_ADDRESS));
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getScriptHash().toString(), is("cc45cc8987b0e35371f5685431e3c8eeea306722"));
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
        assertThat(a.getVerificationScript(), is(nullValue()));
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void getNep5Balances() throws IOException {
        WireMock.configure();
        Neow3j neow = Neow3j.build(new HttpService("http://localhost:8080"));
        Account a = Account.fromAddress(ADDRESS);
        WalletTestHelper.setUpWireMockForCall("getnep5balances",
                "getnep5balances_AZt9DgwW8PKSEQsa9QLX86SyE1DSNjSbsS.json",
                ADDRESS);
        Map<ScriptHash, BigInteger> balances = a.getNep5Balances(neow);
        assertThat(balances.keySet(), contains(GAS_SCRIPT_HASH, NEO_SCRIPT_HASH));
        assertThat(balances.values(), contains(
                new BigInteger("300000000"),
                new BigInteger("5")));
    }

    @Test
    public void isMultiSig() {
        Account a = Account.fromAddress(ADDRESS);
        expectedException.expect(AccountStateException.class);
        expectedException.expectMessage("verification script");
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
        Wallet wallet = Wallet.createWallet().addAccounts(a);

        assertFalse(a.isDefault());

        wallet.defaultAccount(a.getScriptHash());
        assertTrue(a.isDefault());
    }

    @Test
    public void testWalletLink() {
        Account a = Account.fromAddress(ADDRESS);
        Wallet wallet = Wallet.createWallet();

        assertNull(a.getWallet());

        wallet.addAccounts(a);
        assertNotNull(a.getWallet());
        assertEquals(wallet, a.getWallet());
    }
}
