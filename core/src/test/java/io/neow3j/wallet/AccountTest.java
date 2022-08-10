package io.neow3j.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPrivateKey;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.script.VerificationScript;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.exceptions.AccountStateException;
import io.neow3j.wallet.nep6.NEP6Account;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.neow3j.test.TestProperties.committeeAccountAddress;
import static io.neow3j.test.TestProperties.committeeAccountVerificationScript;
import static io.neow3j.test.TestProperties.defaultAccountAddress;
import static io.neow3j.test.TestProperties.defaultAccountEncryptedPrivateKey;
import static io.neow3j.test.TestProperties.defaultAccountPassword;
import static io.neow3j.test.TestProperties.defaultAccountPrivateKey;
import static io.neow3j.test.TestProperties.defaultAccountPublicKey;
import static io.neow3j.test.TestProperties.defaultAccountScriptHash;
import static io.neow3j.test.TestProperties.defaultAccountVerificationScript;
import static io.neow3j.test.TestProperties.defaultAccountWIF;
import static io.neow3j.test.TestProperties.gasTokenHash;
import static io.neow3j.test.TestProperties.neoTokenHash;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

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
    public void testBuildAccountFromExistingKeyPair() {
        ECKeyPair pair = ECKeyPair.create(hexStringToByteArray(defaultAccountPrivateKey()));
        Account a = new Account(pair);

        assertThat(a.isMultiSig(), is(false));
        assertThat(a.getECKeyPair(), is(pair));
        assertThat(a.getAddress(), is(defaultAccountAddress()));
        assertThat(a.getLabel(), is(defaultAccountAddress()));
        assertThat(a.getVerificationScript().getScript(),
                is(hexStringToByteArray(defaultAccountVerificationScript())));
    }

    @Test
    public void testFromVerificationScript() {
        Account account = Account.fromVerificationScript(
                new VerificationScript(
                        hexStringToByteArray(
                                "0x0c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b600b4195440d78"
                        )));

        assertThat(account.getAddress(), is("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj"));
        assertThat(account.getVerificationScript().getScript(),
                is(hexStringToByteArray(
                        "0x0c2102163946a133e3d2e0d987fb90cb01b060ed1780f1718e2da28edf13b965fd2b600b4195440d78")));
    }

    @Test
    public void testFromPublicKey() {
        ECPublicKey publicKey = new ECPublicKey(defaultAccountPublicKey());
        Account account = Account.fromPublicKey(publicKey);

        assertThat(account.getAddress(), is(defaultAccountAddress()));
        assertThat(account.getVerificationScript().getScript(),
                is(hexStringToByteArray(defaultAccountVerificationScript())));
    }

    @Test
    public void testCreateMultiSigAccountFromPublicKeys() {
        ECPublicKey pubKey = new ECPublicKey(defaultAccountPublicKey());
        Account a = Account.createMultiSigAccount(singletonList(pubKey), 1);

        assertThat(a.isMultiSig(), is(true));
        assertThat(a.getAddress(), is(committeeAccountAddress()));
        assertThat(a.getLabel(), is(committeeAccountAddress()));
        assertThat(a.getVerificationScript().getScript(),
                is(hexStringToByteArray(committeeAccountVerificationScript())));
    }

    @Test
    public void testCreateMultiSigAccountWithAddress() {
        Account a = Account.createMultiSigAccount(committeeAccountAddress(), 4, 7);

        assertThat(a.getSigningThreshold(), is(4));
        assertThat(a.getNrOfParticipants(), is(7));
        assertThat(a.getAddress(), is(committeeAccountAddress()));
        assertThat(a.isMultiSig(), is(true));
        assertThat(a.getLabel(), is(committeeAccountAddress()));
        assertThat(a.getVerificationScript(), is(nullValue()));
    }

    @Test
    public void testCreateMultiSigAccountFromVerificationScript() {
        Account a = Account.fromVerificationScript(new VerificationScript(
                hexStringToByteArray(committeeAccountVerificationScript())));

        assertThat(a.isMultiSig(), is(true));
        assertThat(a.getAddress(), is(committeeAccountAddress()));
        assertThat(a.getLabel(), is(committeeAccountAddress()));
        assertThat(a.getVerificationScript().getScript(),
                is(hexStringToByteArray(committeeAccountVerificationScript())));
    }

    @Test
    public void testEncryptPrivateKey() throws CipherException {
        ECKeyPair keyPair = ECKeyPair.create(
                hexStringToByteArray(defaultAccountPrivateKey()));
        Account a = new Account(keyPair);
        a.encryptPrivateKey(defaultAccountPassword());

        assertThat(a.getEncryptedPrivateKey(), is(defaultAccountEncryptedPrivateKey()));
    }

    @Test
    public void failEncryptAccountWithoutPrivateKey() {
        Account a = Account.fromAddress(defaultAccountAddress());

        AccountStateException thrown = assertThrows(AccountStateException.class, () -> a.encryptPrivateKey("pwd"));
        assertThat(thrown.getMessage(), is("The account does not hold a decrypted private key."));
    }

    @Test
    public void decryptWithStandardScryptParams() throws NEP2InvalidFormat, CipherException,
            NEP2InvalidPassphrase {

        final ECPrivateKey privateKey =
                new ECPrivateKey(hexStringToByteArray(defaultAccountPrivateKey()));
        NEP6Account nep6Acct = new NEP6Account("", "", true, false,
                defaultAccountEncryptedPrivateKey(), null, null);
        Account a = Account.fromNEP6Account(nep6Acct);
        a.decryptPrivateKey(defaultAccountPassword());
        assertThat(a.getECKeyPair().getPrivateKey(), is(privateKey));
        a.decryptPrivateKey(defaultAccountPassword()); // This shouldn't do or change anything.
        assertThat(a.getECKeyPair().getPrivateKey(), is(privateKey));
    }

    @Test
    public void failDecryptingAccountWithoutDecryptedPrivateKey() {
        Account a = Account.fromAddress(defaultAccountAddress());

        AccountStateException thrown =
                assertThrows(AccountStateException.class, () -> a.decryptPrivateKey(defaultAccountPassword()));
        assertThat(thrown.getMessage(), is("The account does not hold an encrypted private key."));
    }

    @Test
    public void loadAccountFromNEP6() throws URISyntaxException, IOException {
        URL nep6AccountFileUrl =
                AccountTest.class.getClassLoader().getResource("wallet/account.json");
        FileInputStream stream = new FileInputStream(new File(nep6AccountFileUrl.toURI()));
        NEP6Account nep6Acc = new ObjectMapper().readValue(stream, NEP6Account.class);
        Account a = Account.fromNEP6Account(nep6Acc);

        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertThat(a.getAddress(), is(defaultAccountAddress()));
        assertThat(a.getEncryptedPrivateKey(), is(defaultAccountEncryptedPrivateKey()));
        assertThat(a.getVerificationScript().getScript(),
                is(hexStringToByteArray(defaultAccountVerificationScript())));
    }

    @Test
    public void loadMultiSigAccountFromNEP6() throws IOException {
        InputStream s = this.getClass().getClassLoader()
                .getResourceAsStream("wallet/multiSigAccount.json");
        NEP6Account nep6Acc = new ObjectMapper().readValue(s, NEP6Account.class);
        Account a = Account.fromNEP6Account(nep6Acc);

        assertFalse(a.isDefault());
        assertFalse(a.isLocked());
        assertThat(a.getAddress(), is(committeeAccountAddress()));
        assertThat(a.getVerificationScript().getScript(),
                is(hexStringToByteArray(committeeAccountVerificationScript())));
        assertThat(a.getNrOfParticipants(), is(1));
        assertThat(a.getSigningThreshold(), is(1));
    }

    @Test
    public void toNep6AccountWithOnlyAnAddress() {
        Account a = Account.fromAddress(defaultAccountAddress());
        NEP6Account nep6 = a.toNEP6Account();

        assertThat(nep6.getContract(), nullValue());
        assertFalse(nep6.getDefault());
        assertFalse(nep6.getLock());
        assertThat(nep6.getAddress(), is(defaultAccountAddress()));
        assertThat(nep6.getLabel(), is(defaultAccountAddress()));
        assertThat(nep6.getKey(), is(nullValue()));
    }

    @Test
    public void toNep6AccountWithUnencryptedPrivateKey() {
        Account a = Account.fromWIF(defaultAccountWIF());

        AccountStateException thrown = assertThrows(AccountStateException.class, a::toNEP6Account);
        assertThat(thrown.getMessage(), is("Account private key is available but not encrypted."));
    }

    @Test
    public void toNep6AccountWithEncryptedPrivateKey() throws CipherException {
        Account a = Account.fromWIF(defaultAccountWIF());
        a.encryptPrivateKey("neo");
        NEP6Account nep6 = a.toNEP6Account();

        assertThat(nep6.getContract().getScript(),
                is(Base64.encode(defaultAccountVerificationScript())));
        assertThat(nep6.getKey(), is(defaultAccountEncryptedPrivateKey()));
        assertFalse(nep6.getDefault());
        assertFalse(nep6.getLock());
        assertThat(nep6.getAddress(), is(defaultAccountAddress()));
        assertThat(nep6.getLabel(), is(defaultAccountAddress()));
    }

    @Test
    public void toNep6AccountWithMultiSigAccount() {
        ECPublicKey key = new ECPublicKey(hexStringToByteArray(defaultAccountPublicKey()));
        Account a = Account.createMultiSigAccount(singletonList(key), 1);
        NEP6Account nep6 = a.toNEP6Account();

        assertThat(nep6.getContract().getScript(),
                is(Base64.encode(committeeAccountVerificationScript())));
        assertFalse(nep6.getDefault());
        assertFalse(nep6.getLock());
        assertThat(nep6.getAddress(), is(committeeAccountAddress()));
        assertThat(nep6.getLabel(), is(committeeAccountAddress()));
        assertThat(nep6.getKey(), is(nullValue()));
        assertThat(nep6.getContract().getParameters().get(0).getParamName(), is("signature0"));
        assertThat(nep6.getContract().getParameters().get(0).getParamType(), is(
                ContractParameterType.SIGNATURE));
    }

    @Test
    public void createAccountFromWIF() {
        Account a = Account.fromWIF(defaultAccountWIF());
        byte[] expectedPrivKey = hexStringToByteArray(defaultAccountPrivateKey());
        ECKeyPair expectedKeyPair = ECKeyPair.create(expectedPrivKey);

        assertThat(a.getECKeyPair(), is(expectedKeyPair));
        assertThat(a.getAddress(), is(defaultAccountAddress()));
        assertThat(a.getLabel(), is(defaultAccountAddress()));
        assertThat(a.getEncryptedPrivateKey(), is(nullValue()));
        assertThat(a.getScriptHash().toString(), is(defaultAccountScriptHash()));
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
        assertThat(a.getVerificationScript().getScript(),
                is(hexStringToByteArray(defaultAccountVerificationScript())));
    }

    @Test
    public void createAccountFromAddress() {
        Account a = Account.fromAddress(defaultAccountAddress());

        assertThat(a.getAddress(), is(defaultAccountAddress()));
        assertThat(a.getLabel(), is(defaultAccountAddress()));
        assertThat(a.getScriptHash().toString(), is(defaultAccountScriptHash()));
        assertThat(a.isDefault(), is(false));
        assertThat(a.isLocked(), is(false));
        assertThat(a.getVerificationScript(), is(nullValue()));
    }

    @Test
    public void getNep17Balances() throws IOException {
        int port = wireMockExtension.getPort();
        WireMock.configureFor(port);
        Neow3j neow = Neow3j.build(new HttpService("http://127.0.0.1:" + port));
        Account a = Account.fromAddress(defaultAccountAddress());
        WalletTestHelper.setUpWireMockForCall("getnep17balances",
                "getnep17balances_ofDefaultAccount.json",
                defaultAccountAddress());
        Map<Hash160, BigInteger> balances = a.getNep17Balances(neow);

        assertThat(balances.keySet(), containsInAnyOrder(
                new Hash160(gasTokenHash()), new Hash160(neoTokenHash())));
        assertThat(balances.values(), containsInAnyOrder(
                new BigInteger("300000000"),
                new BigInteger("5")));
    }

    @Test
    public void testIsMultiSig() {
        Account a = Account.fromAddress(defaultAccountAddress());
        assertFalse(a.isMultiSig());

        a = Account.createMultiSigAccount(committeeAccountAddress(), 1, 1);
        assertTrue(a.isMultiSig());

        a = Account.fromVerificationScript(new VerificationScript(
                hexStringToByteArray(committeeAccountVerificationScript())));
        assertTrue(a.isMultiSig());

        a = Account.fromVerificationScript(new VerificationScript(
                hexStringToByteArray(defaultAccountVerificationScript())));
        assertFalse(a.isMultiSig());

        ECPublicKey pubKey = new ECPublicKey(defaultAccountPublicKey());
        a = Account.createMultiSigAccount(asList(pubKey), 1);
        assertTrue(a.isMultiSig());
    }

    @Test
    public void testUnsetLocked() {
        Account a = Account.fromAddress(defaultAccountAddress()).lock();
        assertTrue(a.isLocked());

        a.unlock();
        assertFalse(a.isLocked());
    }

    @Test
    public void testIsDefault() {
        Account a = Account.fromAddress(defaultAccountAddress());
        Wallet wallet = Wallet.create().addAccounts(a);

        assertFalse(a.isDefault());

        wallet.defaultAccount(a.getScriptHash());
        assertTrue(a.isDefault());
    }

    @Test
    public void testWalletLink() {
        Account a = Account.fromAddress(defaultAccountAddress());
        Wallet wallet = Wallet.create();

        assertNull(a.getWallet());

        wallet.addAccounts(a);
        assertNotNull(a.getWallet());
        assertEquals(wallet, a.getWallet());
    }

    @Test
    public void callingGetSigningThresholdWithSingleSigShouldFail() {
        Account a = Account.fromAddress(defaultAccountAddress());

        AccountStateException thrown = assertThrows(AccountStateException.class, a::getSigningThreshold);
        assertThat(thrown.getMessage(),
                containsString("Cannot get signing threshold from account " + defaultAccountAddress()));
    }

    @Test
    public void callingGetNrOfParticipantsWithSingleSigShouldFail() {
        Account a = Account.fromAddress(defaultAccountAddress());

        AccountStateException thrown = assertThrows(AccountStateException.class, a::getNrOfParticipants);
        assertThat(thrown.getMessage(),
                Matchers.containsString("Cannot get number of participants from account " + defaultAccountAddress()));
    }

}
