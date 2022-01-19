package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.SignerConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.neow3j.constants.NeoConstants.MAX_SIGNER_SUBITEMS;
import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class SignerTest {

    private Account acc;
    private Hash160 accScriptHash;
    private Hash160 contract1;
    private Hash160 contract2;
    private ECPublicKey groupPubKey1;
    private ECPublicKey groupPubKey2;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        acc = Account.fromWIF("Kzt94tAAiZSgH7Yt4i25DW6jJFprZFPSqTgLr5dWmWgKDKCjXMfZ");
        accScriptHash = acc.getScriptHash();
        contract1 = Hash160.fromScript(hexStringToByteArray("d802a401"));
        contract2 = Hash160.fromScript(hexStringToByteArray("c503b112"));
        String encPubKey1 = "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7";
        String encPubKey2 = "02958ab88e4cea7ae1848047daeb8883daf5fdf5c1301dbbfe973f0a29fe75de60";
        groupPubKey1 = new ECKeyPair.ECPublicKey(hexStringToByteArray(encPubKey1));
        groupPubKey2 = new ECKeyPair.ECPublicKey(hexStringToByteArray(encPubKey2));
    }

    @Test
    public void createSignerWithCallByEntryWitnessScope() {
        Signer signer = calledByEntry(accScriptHash);
        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(1));
        assertThat(signer.getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertTrue(signer.getAllowedContracts().isEmpty());
        assertTrue(signer.getAllowedGroups().isEmpty());
    }

    @Test
    public void createSignerWithGlobalWitnessScope() {
        Signer signer = AccountSigner.global(accScriptHash);
        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(1));
        assertThat(signer.getScopes(), contains(WitnessScope.GLOBAL));
        assertTrue(signer.getAllowedContracts().isEmpty());
        assertTrue(signer.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidSigner1() {
        Signer signer = AccountSigner.calledByEntry(accScriptHash)
                .setAllowedContracts(contract1, contract2);

        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(2));
        assertThat(signer.getScopes(),
                containsInAnyOrder(WitnessScope.CUSTOM_CONTRACTS, WitnessScope.CALLED_BY_ENTRY));
        assertThat(signer.getAllowedContracts(), hasSize(2));
        assertThat(signer.getAllowedContracts(),
                containsInAnyOrder(contract1, contract2));
        assertTrue(signer.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidSigner2() {
        Signer signer = AccountSigner.none(accScriptHash)
                .setAllowedContracts(contract1, contract2);

        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(1));
        assertThat(signer.getScopes(), containsInAnyOrder(WitnessScope.CUSTOM_CONTRACTS));
        assertThat(signer.getAllowedContracts(), hasSize(2));
        assertThat(signer.getAllowedContracts(),
                containsInAnyOrder(contract1, contract2));
        assertTrue(signer.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidSigner3() {
        Signer signer = AccountSigner.none(accScriptHash)
                .setAllowedGroups(groupPubKey1, groupPubKey2);

        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(1));
        assertThat(signer.getScopes(), containsInAnyOrder(WitnessScope.CUSTOM_GROUPS));
        assertThat(signer.getAllowedGroups(), hasSize(2));
        assertThat(signer.getAllowedGroups(),
                containsInAnyOrder(groupPubKey1, groupPubKey2));
        assertTrue(signer.getAllowedContracts().isEmpty());
    }

    @Test
    public void failBuildingSignerWithGlobalScopeAndCustomContracts() {
        exceptionRule.expect(SignerConfigurationException.class);
        exceptionRule.expectMessage("Trying to set allowed contracts on a Signer with global " +
                "scope.");
        AccountSigner.global(accScriptHash).setAllowedContracts(contract1, contract2);
    }

    @Test
    public void failBuildingSignerWithGlobalScopeAndCustomGroups() {
        exceptionRule.expect(SignerConfigurationException.class);
        exceptionRule.expectMessage("Trying to set allowed contract groups on a Signer with " +
                "global scope.");
        AccountSigner.global(accScriptHash).setAllowedGroups(groupPubKey1, groupPubKey2);
    }

    @Test
    public void failBuildingSignerWithTooManyContracts() {
        Hash160[] contracts = new Hash160[17];
        for (int i = 0; i <= 16; i++) {
            contracts[i] = new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88");
        }
        exceptionRule.expect(SignerConfigurationException.class);
        exceptionRule.expectMessage("Tyring to set more than " + MAX_SIGNER_SUBITEMS
                + " allowed contracts on a signer.");
        AccountSigner.calledByEntry(accScriptHash).setAllowedContracts(contracts);
    }

    @Test
    public void failBuildingSignerWithTooManyContractsAddedSeparately() {
        Signer signer = AccountSigner.none(accScriptHash)
                .setAllowedContracts(new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88"));
        Hash160[] contracts = new Hash160[16];
        for (int i = 0; i <= 15; i++) {
            contracts[i] = new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88");
        }
        exceptionRule.expect(SignerConfigurationException.class);
        exceptionRule.expectMessage("Tyring to set more than " + MAX_SIGNER_SUBITEMS
                + " allowed contracts on a signer.");
        signer.setAllowedContracts(contracts);
    }

    @Test
    public void failBuildingSignerWithTooManyGroups() {
        ECPublicKey publicKey = new ECPublicKey(hexStringToByteArray(
                "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7"));
        ECPublicKey[] groups = new ECPublicKey[17];
        for (int i = 0; i <= 16; i++) {
            groups[i] = publicKey;
        }
        exceptionRule.expect(SignerConfigurationException.class);
        exceptionRule.expectMessage("Tyring to set more than " + MAX_SIGNER_SUBITEMS
                + " allowed contract groups on a signer.");
        AccountSigner.calledByEntry(accScriptHash).setAllowedGroups(groups);
    }

    @Test
    public void failBuildingSignerWithTooManyGroupsAddedSeparately() {
        ECPublicKey publicKey = new ECPublicKey(hexStringToByteArray(
                "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7"));
        Signer signer = AccountSigner.none(accScriptHash).setAllowedGroups(publicKey);
        ECPublicKey[] groups = new ECPublicKey[16];
        for (int i = 0; i <= 15; i++) {
            groups[i] = publicKey;
        }

        exceptionRule.expect(SignerConfigurationException.class);
        exceptionRule.expectMessage("Tyring to set more than " + MAX_SIGNER_SUBITEMS
                + " allowed contract groups on a signer.");
        signer.setAllowedGroups(groups);
    }

    @Test
    public void serializeGlobalScope() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        AccountSigner.global(accScriptHash).serialize(writer);
        byte[] actual = outStream.toByteArray();
        String expected = ""
                + reverseHexString(accScriptHash.toString())
                + toHexStringNoPrefix(WitnessScope.GLOBAL.byteValue());
        assertThat(toHexStringNoPrefix(actual), is(expected));
    }

    @Test
    public void serializingWithCustomContractsScopeProducesCorrectByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        Signer s = AccountSigner.none(accScriptHash).setAllowedContracts(contract1, contract2);
        byte[] actual = s.toArray();
        byte[] expected = hexStringToByteArray(""
                + reverseHexString(accScriptHash.toString())
                + toHexStringNoPrefix(WitnessScope.CUSTOM_CONTRACTS.byteValue())
                + "02" // array length 2
                + reverseHexString(contract1.toString())
                + reverseHexString(contract2.toString()));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void serializeCustomGroupsScope() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        AccountSigner.none(accScriptHash).setAllowedGroups(groupPubKey1, groupPubKey2)
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = hexStringToByteArray(""
                + reverseHexString(accScriptHash.toString())
                + toHexStringNoPrefix(WitnessScope.CUSTOM_GROUPS.byteValue())
                + "02" // array length 2
                + toHexStringNoPrefix(groupPubKey1.toArray())
                + toHexStringNoPrefix(groupPubKey2.toArray()));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void serializeWithMultipleScopesContractsAndGroups() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        AccountSigner.calledByEntry(accScriptHash)
                .setAllowedGroups(groupPubKey1, groupPubKey2)
                .setAllowedContracts(contract1, contract2)
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = hexStringToByteArray(""
                + reverseHexString(accScriptHash.toString())
                + "31" // calledByEntry, custom contracts and custom groups scope
                + "02" // array length 2
                + reverseHexString(contract1.toString())
                + reverseHexString(contract2.toString())
                + "02" // array length 2
                + toHexStringNoPrefix(groupPubKey1.toArray())
                + toHexStringNoPrefix(groupPubKey2.toArray()));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + reverseHexString(accScriptHash.toString())
                + "31" // calledByEntry, custom contracts and custom groups scope
                + "02" // array length 2
                + reverseHexString(contract1.toString())
                + reverseHexString(contract2.toString())
                + "02" // array length 2
                + toHexStringNoPrefix(groupPubKey1.toArray())
                + toHexStringNoPrefix(groupPubKey2.toArray()));

        Signer c = NeoSerializableInterface.from(data, Signer.class);
        assertThat(c.getScriptHash(), is(accScriptHash));
        assertThat(c.getScopes(), containsInAnyOrder(
                WitnessScope.CUSTOM_CONTRACTS,
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_GROUPS));
        assertThat(c.getAllowedContracts(), containsInAnyOrder(contract1, contract2));
        assertThat(c.getAllowedGroups(), containsInAnyOrder(groupPubKey1, groupPubKey2));
    }

    @Test
    public void failDeserializingWithTooManyContracts() throws DeserializationException {
        StringBuilder serialized = new StringBuilder(""
                + reverseHexString(accScriptHash.toString())
                + "11" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract script hashes.
        for (int i = 0; i <= 17; i++) {
            serialized.append(reverseHexString(contract1.toString()));
        }
        byte[] serializedBytes = hexStringToByteArray(serialized.toString());

        exceptionRule.expect(DeserializationException.class);
        exceptionRule.expectMessage(new StringContains("A signer's scope can only contain "
                + MAX_SIGNER_SUBITEMS + " allowed contracts."));
        NeoSerializableInterface.from(serializedBytes, Signer.class);
    }

    @Test
    public void failDeserializingWithTooManyContractGroups() throws DeserializationException {
        StringBuilder serialized = new StringBuilder(""
                + reverseHexString(accScriptHash.toString())
                + "21" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract group public keys.
        for (int i = 0; i <= 17; i++) {
            serialized.append(toHexStringNoPrefix(groupPubKey1.toArray()));
        }
        byte[] serializedBytes = hexStringToByteArray(serialized.toString());

        exceptionRule.expect(DeserializationException.class);
        exceptionRule.expectMessage(new StringContains("A signer's scope can only contain "
                + MAX_SIGNER_SUBITEMS + " allowed contract groups."));
        NeoSerializableInterface.from(serializedBytes, Signer.class);
    }

    @Test
    public void getSize() {
        Signer signer = AccountSigner.calledByEntry(accScriptHash)
                .setAllowedGroups(groupPubKey1, groupPubKey2)
                .setAllowedContracts(contract1, contract2);

        int expectedSize = 20 // Account script hash
                + 1 // Scope byte
                + 1 // length byte of allowed contracts list
                + 20 + 20 // Script hashes of two allowed contracts
                + 1 // length byte of allowed groups list
                + 33 + 33; // Public keys of two allowed groups

        assertThat(signer.getSize(), is(expectedSize));
    }

    @Test
    public void equals() {
        Signer signer1 = AccountSigner.global(accScriptHash);
        Signer signer2 = AccountSigner.global(accScriptHash);
        assertThat(signer1, equalTo(signer2));

        signer1 = ContractSigner.calledByEntry(accScriptHash);
        signer2 = ContractSigner.calledByEntry(accScriptHash);
        assertThat(signer1, equalTo(signer2));

        signer1 = AccountSigner.calledByEntry(accScriptHash)
                .setAllowedGroups(groupPubKey1, groupPubKey2)
                .setAllowedContracts(contract1, contract2);

        signer2 = AccountSigner.calledByEntry(accScriptHash)
                .setAllowedGroups(groupPubKey1, groupPubKey2)
                .setAllowedContracts(contract1, contract2);

        assertThat(signer1, equalTo(signer2));
    }

}
