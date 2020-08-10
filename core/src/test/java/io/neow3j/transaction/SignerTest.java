package io.neow3j.transaction;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.neow3j.contract.ScriptHash;
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

    private ScriptHash acctScriptHash;
    private ScriptHash contract1;
    private ScriptHash contract2;
    private ECKeyPair.ECPublicKey groupPubKey1;
    private ECKeyPair.ECPublicKey groupPubKey2;

    @Before
    public void setUp() {
        acctScriptHash = ScriptHash.fromAddress("AK2nJJpJr6o664CWJKi1QRXjqeic2zRp8y");
        contract1 = ScriptHash.fromScript(Numeric.hexStringToByteArray("d802a401"));
        contract2 = ScriptHash.fromScript(Numeric.hexStringToByteArray("c503b112"));
        String encPubKey1 = "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7";
        String encPubKey2 = "02958ab88e4cea7ae1848047daeb8883daf5fdf5c1301dbbfe973f0a29fe75de60";
        groupPubKey1 = new ECKeyPair.ECPublicKey(Numeric.hexStringToByteArray(encPubKey1));
        groupPubKey2 = new ECKeyPair.ECPublicKey(Numeric.hexStringToByteArray(encPubKey2));
    }

    @Test
    public void createSignerWithCallByEntryWitnessScope() {
        Signer cos = Signer.calledByEntry(acctScriptHash);
        assertThat(cos.getScriptHash(), is(acctScriptHash));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), contains(WitnessScope.CALLED_BY_ENTRY));
        assertTrue(cos.getAllowedContracts().isEmpty());
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void createSignerWithGlobalWitnessScope() {
        Signer cos = Signer.global(acctScriptHash);
        assertThat(cos.getScriptHash(), is(acctScriptHash));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), contains(WitnessScope.GLOBAL));
        assertTrue(cos.getAllowedContracts().isEmpty());
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidSigner1() {
        Signer cos = new Signer.Builder()
                .account(this.acctScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS, WitnessScope.CALLED_BY_ENTRY)
                .allowedContracts(this.contract1, this.contract2)
                .build();

        assertThat(cos.getScriptHash(), is(this.acctScriptHash));
        assertThat(cos.getScopes(), hasSize(2));
        assertThat(cos.getScopes(),
                containsInAnyOrder(WitnessScope.CUSTOM_CONTRACTS, WitnessScope.CALLED_BY_ENTRY));
        assertThat(cos.getAllowedContracts(), hasSize(2));
        assertThat(cos.getAllowedContracts(), containsInAnyOrder(this.contract1, this.contract2));
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidSigner2() {
        Signer cos = new Signer.Builder()
                .account(this.acctScriptHash)
                // the allowed contracts scope is added automatically.
                .allowedContracts(this.contract1, this.contract2)
                .build();

        assertThat(cos.getScriptHash(), is(this.acctScriptHash));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), containsInAnyOrder(WitnessScope.CUSTOM_CONTRACTS));
        assertThat(cos.getAllowedContracts(), hasSize(2));
        assertThat(cos.getAllowedContracts(),
                containsInAnyOrder(this.contract1, this.contract2));
        assertTrue(cos.getAllowedGroups().isEmpty());
    }

    @Test
    public void buildValidSigner3() {
        Signer cos = new Signer.Builder()
                .account(this.acctScriptHash)
                .allowedGroups(this.groupPubKey1, this.groupPubKey2)
                .build();

        assertThat(cos.getScriptHash(), is(this.acctScriptHash));
        assertThat(cos.getScopes(), hasSize(1));
        assertThat(cos.getScopes(), containsInAnyOrder(WitnessScope.CUSTOM_GROUPS));
        assertThat(cos.getAllowedGroups(), hasSize(2));
        assertThat(cos.getAllowedGroups(),
                containsInAnyOrder(this.groupPubKey1, this.groupPubKey2));
        assertTrue(cos.getAllowedContracts().isEmpty());
    }


    @Test(expected = SignerConfigurationException.class)
    public void failBuildingEmptySigner() {
        new Signer.Builder().build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithoutScopes() {
        new Signer.Builder()
                .account(this.acctScriptHash)
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
                    .account(this.acctScriptHash)
                    .scopes(WitnessScope.GLOBAL)
                    .scopes(WitnessScope.CUSTOM_CONTRACTS)
                    .build();
        } catch (SignerConfigurationException e) {
            // continue
        }

        try {
            new Signer.Builder()
                    .account(this.acctScriptHash)
                    .scopes(WitnessScope.GLOBAL)
                    .scopes(WitnessScope.CUSTOM_GROUPS)
                    .build();
        } catch (SignerConfigurationException e) {
            // continue
        }

        new Signer.Builder()
                .account(this.acctScriptHash)
                .scopes(WitnessScope.GLOBAL)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingCustomContractsSignerWithoutSpecifyingAllowedContracts() {
        new Signer.Builder()
                .account(this.acctScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingCustomGroupsSignerWithoutSpecifyingAllowedContracts() {
        new Signer.Builder()
                .account(this.acctScriptHash)
                .scopes(WitnessScope.CUSTOM_GROUPS)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithTooManyContracts() {
        ScriptHash[] contracts = new ScriptHash[17];
        for (int i = 0; i <= 16; i++) {
            contracts[i] = new ScriptHash("3ab0be8672e25cf475219d018ded961ec684ca88");
        }
        new Signer.Builder()
                .account(this.acctScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .allowedContracts(contracts)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithTooManyContractsAddedSeparately() {
        Builder b = new Signer.Builder()
                .account(this.acctScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .allowedContracts(new ScriptHash("3ab0be8672e25cf475219d018ded961ec684ca88"));
        ScriptHash[] contracts = new ScriptHash[16];
        for (int i = 0; i <= 15; i++) {
            contracts[i] = new ScriptHash("3ab0be8672e25cf475219d018ded961ec684ca88");
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
                .account(this.acctScriptHash)
                .scopes(WitnessScope.CUSTOM_CONTRACTS)
                .allowedGroups(groups)
                .build();
    }

    @Test(expected = SignerConfigurationException.class)
    public void failBuildingSignerWithTooManyGroupsAddedSeparately() {
        ECPublicKey publicKey = new ECPublicKey(Numeric.hexStringToByteArray(
                "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7"));
        Builder b = new Signer.Builder()
                .account(this.acctScriptHash)
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
        Signer.global(acctScriptHash).serialize(writer);
        byte[] actual = outStream.toByteArray();
        String expected = ""
                + "01" // attribute type: signer
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9" // script hash LE
                + "00"; // global scope
        assertThat(Numeric.toHexStringNoPrefix(actual), is(expected));
    }

    @Test
    public void serializeCustomContractsScope() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Signer.Builder()
                .account(acctScriptHash)
                .allowedContracts(contract1, contract2)
                .build()
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + "01" // attribute type: signer
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash LE
                + "10" // custom contracts scope
                + "02" // array length 2
                + "47efccbc2c12df2935b39044b507eae270110288" // contract 1 script hash LE
                + "3ab0be8672e25cf475219d018ded961ec684ca88"); // contract 2 script hash LE
        assertArrayEquals(expected, actual);
    }


    @Test
    public void serializeCustomGroupsScope() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Signer.Builder()
                .account(acctScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .build()
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + "01" // attribute type: signer
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash LE
                + "20" // custom groups scope
                + "02" // array length 2
                + "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7" // group 1
                + "02958ab88e4cea7ae1848047daeb8883daf5fdf5c1301dbbfe973f0a29fe75de60"); // group 2
        assertArrayEquals(expected, actual);
    }

    @Test
    public void serializeWithMultipleScopesContractsAndGroups() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new Signer.Builder()
                .account(acctScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .allowedContracts(contract1, contract2)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build()
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = Numeric.hexStringToByteArray(""
                + "01" // attribute type: signer
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash LE
                + "31" // calledByEntry, custom contracts and custom groups scope
                + "02" // array length 2
                + "47efccbc2c12df2935b39044b507eae270110288" // contract 1 script hash LE
                + "3ab0be8672e25cf475219d018ded961ec684ca88" // contract 2 script hash LE
                + "02" // array length 2
                + "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7" // group 1
                + "02958ab88e4cea7ae1848047daeb8883daf5fdf5c1301dbbfe973f0a29fe75de60"); // group 2
        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = Numeric.hexStringToByteArray(""
                + "01" // attribute type: signer
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash LE
                + "31" // calledByEntry, custom contracts and custom groups scope
                + "02" // array length 2
                + "47efccbc2c12df2935b39044b507eae270110288" // contract 1 script hash LE
                + "3ab0be8672e25cf475219d018ded961ec684ca88" // contract 2 script hash LE
                + "02" // array length 2
                + "0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7" // group 1
                + "02958ab88e4cea7ae1848047daeb8883daf5fdf5c1301dbbfe973f0a29fe75de60"); // group 2
        Signer c = NeoSerializableInterface.from(data, Signer.class);
        assertThat(c.getScriptHash(), is(acctScriptHash));
        assertThat(c.getScopes(), containsInAnyOrder(
                WitnessScope.CUSTOM_CONTRACTS,
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_GROUPS));
        assertThat(c.getAllowedContracts(), containsInAnyOrder(contract1, contract2));
        assertThat(c.getAllowedGroups(), containsInAnyOrder(groupPubKey1, groupPubKey2));
    }

    @Test(expected = DeserializationException.class)
    public void failDeserializingWithTooManyContracts() throws DeserializationException {
        StringBuilder serialized = new StringBuilder("01" // attribute type: signer
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash LE
                + "11" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract script hashes.
        for (int i = 0; i <= 17; i++) {
            serialized.append("3ab0be8672e25cf475219d018ded961ec684ca88"); // contract
        }
        byte[] serializedBytes = Numeric.hexStringToByteArray(serialized.toString());
        NeoSerializableInterface.from(serializedBytes, Signer.class);
    }

    @Test(expected = DeserializationException.class)
    public void failDeserializingWithTooManyContractGroups() throws DeserializationException {
        StringBuilder serialized = new StringBuilder("01" // attribute type: signer
                + "23ba2703c53263e8d6e522dc32203339dcd8eee9"// account script hash LE
                + "21" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract group public keys.
        for (int i = 0; i <= 17; i++) {
            serialized.append("0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7");
        }
        byte[] serializedBytes = Numeric.hexStringToByteArray(serialized.toString());
        NeoSerializableInterface.from(serializedBytes, Signer.class);
    }

    @Test
    public void getSize() {
        Signer c = new Signer.Builder()
                .account(acctScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .allowedContracts(contract1, contract2)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();

        int expectedSize = 1 // attribute type
                + 20 // Account script hash
                + 1 // Scope byte
                + 1 // length byte of allowed contracts list
                + 20 + 20 // Script hashes of two allowed contracts
                + 1 // length byte of allowed groups list
                + 33 + 33; // Public keys of two allowed groups

        assertThat(c.getSize(), is(expectedSize));
    }

    @Test
    public void equals() {
        Signer c1 = Signer.calledByEntry(acctScriptHash);
        Signer c2 = Signer.calledByEntry(acctScriptHash);
        assertThat(c1, equalTo(c2));

        Signer c = new Signer.Builder()
                .account(acctScriptHash)
                .allowedGroups(groupPubKey1, groupPubKey2)
                .allowedContracts(contract1, contract2)
                .scopes(WitnessScope.CALLED_BY_ENTRY)
                .build();
    }
}