package io.neow3j.transaction;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.Hash160;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializableInterface;
import io.neow3j.io.exceptions.DeserializationException;
import io.neow3j.transaction.Signer.Builder;
import io.neow3j.transaction.exceptions.SignerConfigurationException;
import io.neow3j.utils.Numeric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class SignerTest {

    private Hash160 accScriptHash;
    private Hash160 contract1;
    private Hash160 contract2;
    private ECPublicKey groupPubKey1;
    private ECPublicKey groupPubKey2;

    @Before
    public void setUp() {
        accScriptHash = Hash160.fromAddress("NZNos2WqTbu5oCgyfss9kUJgBXJqhuYAaj");
        contract1 = Hash160.fromScript(Numeric.hexStringToByteArray("d802a401"));
        contract2 = Hash160.fromScript(Numeric.hexStringToByteArray("c503b112"));
        String encPubKey1 = "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7";
        String encPubKey2 = "02958ab88e4cea7ae1848047daeb8883daf5fdf5c1301dbbfe973f0a29fe75de60";
        groupPubKey1 = new ECKeyPair.ECPublicKey(Numeric.hexStringToByteArray(encPubKey1));
        groupPubKey2 = new ECKeyPair.ECPublicKey(Numeric.hexStringToByteArray(encPubKey2));
    }

    @Test
    public void createSignerWithCallByEntryWitnessScope() {
        Signer signer = Signer.calledByEntry(accScriptHash);
        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(1));
        assertThat(signer.getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertTrue(signer.getAllowedContracts().isEmpty());
        assertTrue(signer.getAllowedGroups().isEmpty());
    }

    @Test
    public void createSignerWithGlobalWitnessScope() {
        Signer signer = Signer.global(accScriptHash);
        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(1));
        assertThat(signer.getScopes(), contains(WitnessScope.GLOBAL));
        assertTrue(signer.getAllowedContracts().isEmpty());
        assertTrue(signer.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidSigner1() {
        Signer signer = new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS, WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(contract1, contract2)
                .build();

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
        Signer signer = new Signer.Builder()
                .account(accScriptHash)
                // the allowed contracts scope is added automatically.
                .allowedContracts(contract1, contract2)
                .build();

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
        Signer signer = new Signer.Builder()
                .account(accScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .build();

        assertThat(signer.getScriptHash(), is(accScriptHash));
        assertThat(signer.getScopes(), hasSize(1));
        assertThat(signer.getScopes(), containsInAnyOrder(WitnessScope.CUSTOM_GROUPS));
        assertThat(signer.getAllowedGroups(), hasSize(2));
        assertThat(signer.getAllowedGroups(),
                containsInAnyOrder(groupPubKey1, groupPubKey2));
        assertTrue(signer.getAllowedContracts().isEmpty());
    }


    @Test(expected = SignerConfigurationException.class)
    public void failBuildingEmptySigner() {
        new Signer.Builder().build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithoutScopes() {
        new Signer.Builder()
                .account(accScriptHash)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithoutAccount() {
        new Signer.Builder()
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithGlobalAndAnyOtherScope() {
        try {
            new Signer.Builder()
                    .account(accScriptHash)
                    .scopes(WitnessScope.GLOBAL)
                    .scopes(WitnessScope.CUSTOM_CONTRACTS)
                    .build();
        } catch (SignerConfigurationException e) {
            // continue
        }

        try {
            new Signer.Builder()
                    .account(accScriptHash)
                    .scopes(WitnessScope.GLOBAL)
                    .scopes(WitnessScope.CUSTOM_GROUPS)
                    .build();
        } catch (SignerConfigurationException e) {
            // continue
        }

        new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.GLOBAL)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingCustomContractsSignerWithoutSpecifyingAllowedContracts() {
        new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingCustomGroupsSignerWithoutSpecifyingAllowedContracts() {
        new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.CUSTOM_GROUPS)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithTooManyContracts() {
        Hash160[] contracts = new Hash160[17];
        for (int i = 0; i <= 16; i++) {
            contracts[i] = new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88");
        }
        new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .allowedContracts(contracts)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithTooManyContractsAddedSeparately() {
        Builder b = new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .allowedContracts(new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88"));
        Hash160[] contracts = new Hash160[16];
        for (int i = 0; i <= 15; i++) {
            contracts[i] = new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88");
        }
        b.allowedContracts(contracts).build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithTooManyGroups() {
        ECPublicKey publicKey = new ECPublicKey(Numeric.hexStringToByteArray(
                "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7"));
        ECPublicKey[] groups = new ECPublicKey[17];
        for (int i = 0; i <= 16; i++) {
            groups[i] = publicKey;
        }
        new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .allowedGroups(groups)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithTooManyGroupsAddedSeparately() {
        ECPublicKey publicKey = new ECPublicKey(Numeric.hexStringToByteArray(
                "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7"));
        Builder b = new Signer.Builder()
                .account(accScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .allowedGroups(publicKey);
        ECPublicKey[] groups = new ECPublicKey[16];
        for (int i = 0; i <= 15; i++) {
            groups[i] = publicKey;
        }
        b.allowedGroups(groups).build();
    }

    @Test
    public void serializeGlobalScope() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        Signer.global(accScriptHash).serialize(writer);
        byte[] actual = outStream.toByteArray();
        String expected = ""
                + Numeric.reverseHexString(accScriptHash.toString())
                + Numeric.toHexStringNoPrefix(WitnessScope.GLOBAL.byteValue());
        assertThat(Numeric.toHexStringNoPrefix(actual), is(expected));
    }

    @Test
    public void serializingWithCustomContractsScopeProducesCorrectByteArray() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        Signer s = new Signer.Builder()
                .account(accScriptHash)
                .allowedContracts(contract1, contract2)
                .build();
        byte[] actual = s.toArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + Numeric.reverseHexString(accScriptHash.toString())
                + Numeric.toHexStringNoPrefix(WitnessScope.CUSTOM_CONTRACTS.byteValue())
                + "02" // array length 2
                + Numeric.reverseHexString(contract1.toString())
                + Numeric.reverseHexString(contract2.toString()));
        assertArrayEquals(expected, actual);
    }


    @Test
    public void serializeCustomGroupsScope() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Signer.Builder()
                .account(accScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .build()
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + Numeric.reverseHexString(accScriptHash.toString())
                + Numeric.toHexStringNoPrefix(WitnessScope.CUSTOM_GROUPS.byteValue())
                + "02" // array length 2
                + Numeric.toHexStringNoPrefix(groupPubKey1.toArray())
                + Numeric.toHexStringNoPrefix(groupPubKey2.toArray()));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void serializeWithMultipleScopesContractsAndGroups() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Signer.Builder()
                .account(accScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .allowedContracts(contract1, contract2)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build()
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + Numeric.reverseHexString(accScriptHash.toString())
                + "31" // calledByEntry, custom contracts and custom groups scope
                + "02" // array length 2
                + Numeric.reverseHexString(contract1.toString())
                + Numeric.reverseHexString(contract2.toString())
                + "02" // array length 2
                + Numeric.toHexStringNoPrefix(groupPubKey1.toArray())
                + Numeric.toHexStringNoPrefix(groupPubKey2.toArray()));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = Numeric.hexStringToByteArray(""
                + Numeric.reverseHexString(accScriptHash.toString())
                + "31" // calledByEntry, custom contracts and custom groups scope
                + "02" // array length 2
                + Numeric.reverseHexString(contract1.toString())
                + Numeric.reverseHexString(contract2.toString())
                + "02" // array length 2
                + Numeric.toHexStringNoPrefix(groupPubKey1.toArray())
                + Numeric.toHexStringNoPrefix(groupPubKey2.toArray()));

        Signer c = NeoSerializableInterface.from(data, Signer.class);
        assertThat(c.getScriptHash(), is(accScriptHash));
        assertThat(c.getScopes(), containsInAnyOrder(
                WitnessScope.CUSTOM_CONTRACTS,
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_GROUPS));
        assertThat(c.getAllowedContracts(), containsInAnyOrder(contract1, contract2));
        assertThat(c.getAllowedGroups(), containsInAnyOrder(groupPubKey1, groupPubKey2));
    }

    @Test(expected = DeserializationException.class)
    public void failDeserializingWithTooManyContracts() throws DeserializationException {
        StringBuilder serialized = new StringBuilder(""
                + Numeric.reverseHexString(accScriptHash.toString())
                + "11" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract script hashes.
        for (int i = 0; i <= 17; i++) {
            serialized.append(Numeric.reverseHexString(contract1.toString()));
        }
        byte[] serializedBytes = Numeric.hexStringToByteArray(serialized.toString());
        NeoSerializableInterface.from(serializedBytes, Signer.class);
    }

    @Test(expected = DeserializationException.class)
    public void failDeserializingWithTooManyContractGroups() throws DeserializationException {
        StringBuilder serialized = new StringBuilder(""
                + Numeric.reverseHexString(accScriptHash.toString())
                + "21" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract group public keys.
        for (int i = 0; i <= 17; i++) {
            serialized.append(Numeric.toHexStringNoPrefix(groupPubKey1.toArray()));
        }
        byte[] serializedBytes = Numeric.hexStringToByteArray(serialized.toString());
        NeoSerializableInterface.from(serializedBytes, Signer.class);
    }

    @Test
    public void getSize() {
        Signer signer = new Signer.Builder()
                .account(accScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .allowedContracts(contract1, contract2)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();

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
        Signer signer1 = Signer.calledByEntry(accScriptHash);
        Signer signer2 = Signer.calledByEntry(accScriptHash);
        assertThat(signer1, equalTo(signer2));

        signer1 = new Signer.Builder()
                .account(accScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .allowedContracts(contract1, contract2)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();

        signer2 = new Signer.Builder()
                .account(accScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .allowedContracts(contract1, contract2)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();

        assertThat(signer1, equalTo(signer2));
    }

}
