package io.neow3j.transaction;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.transaction.exceptions.SignerConfigurationException;
import io.neow3j.transaction.witnessrule.AndCondition;
import io.neow3j.transaction.witnessrule.BooleanCondition;
import io.neow3j.transaction.witnessrule.CalledByContractCondition;
import io.neow3j.transaction.witnessrule.NotCondition;
import io.neow3j.transaction.witnessrule.ScriptHashCondition;
import io.neow3j.transaction.witnessrule.WitnessAction;
import io.neow3j.transaction.witnessrule.WitnessCondition;
import io.neow3j.transaction.witnessrule.WitnessConditionType;
import io.neow3j.transaction.witnessrule.WitnessRule;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.neow3j.constants.NeoConstants.MAX_SIGNER_SUBITEMS;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static io.neow3j.utils.Numeric.toHexStringNoPrefix;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SignerTest {

    private Account acc;
    private Hash160 accScriptHash;
    private Hash160 contract1;
    private Hash160 contract2;
    private ECPublicKey groupPubKey1;
    private ECPublicKey groupPubKey2;

    @BeforeAll
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
        Signer signer = AccountSigner.calledByEntry(accScriptHash);
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
        SignerConfigurationException thrown = assertThrows(SignerConfigurationException.class,
                () -> AccountSigner.global(accScriptHash).setAllowedContracts(contract1, contract2));
        assertThat(thrown.getMessage(), is("Trying to set allowed contracts on a Signer with global scope."));
    }

    @Test
    public void failBuildingSignerWithGlobalScopeAndCustomGroups() {
        SignerConfigurationException thrown = assertThrows(SignerConfigurationException.class,
                () -> AccountSigner.global(accScriptHash).setAllowedGroups(groupPubKey1, groupPubKey2));
        assertThat(thrown.getMessage(), is("Trying to set allowed contract groups on a Signer with global scope."));
    }

    @Test
    public void failBuildingSignerWithTooManyContracts() {
        Hash160[] contracts = new Hash160[17];
        for (int i = 0; i <= 16; i++) {
            contracts[i] = new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88");
        }
        SignerConfigurationException thrown = assertThrows(SignerConfigurationException.class,
                () -> AccountSigner.calledByEntry(accScriptHash).setAllowedContracts(contracts));
        assertThat(thrown.getMessage(),
                is(format("Trying to set more than %s allowed contracts on a signer.", MAX_SIGNER_SUBITEMS)));
    }

    @Test
    public void failBuildingSignerWithTooManyContractsAddedSeparately() {
        Signer signer = AccountSigner.none(accScriptHash)
                .setAllowedContracts(new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88"));
        Hash160[] contracts = new Hash160[16];
        for (int i = 0; i <= 15; i++) {
            contracts[i] = new Hash160("3ab0be8672e25cf475219d018ded961ec684ca88");
        }

        SignerConfigurationException thrown =
                assertThrows(SignerConfigurationException.class, () -> signer.setAllowedContracts(contracts));
        assertThat(thrown.getMessage(),
                is(format("Trying to set more than %s allowed contracts on a signer.", MAX_SIGNER_SUBITEMS)));
    }

    @Test
    public void failBuildingSignerWithTooManyGroups() {
        ECPublicKey publicKey = new ECPublicKey(
                hexStringToByteArray("0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7"));
        ECPublicKey[] groups = new ECPublicKey[17];
        for (int i = 0; i <= 16; i++) {
            groups[i] = publicKey;
        }

        SignerConfigurationException thrown = assertThrows(SignerConfigurationException.class,
                () -> AccountSigner.calledByEntry(accScriptHash).setAllowedGroups(groups));
        assertThat(thrown.getMessage(),
                is(format("Trying to set more than %s allowed contract groups on a signer.", MAX_SIGNER_SUBITEMS)));
    }

    @Test
    public void failBuildingSignerWithTooManyGroupsAddedSeparately() {
        ECPublicKey publicKey = new ECPublicKey(
                hexStringToByteArray("0306d3e7f18e6dd477d34ce3cfeca172a877f3c907cc6c2b66c295d1fcc76ff8f7"));
        Signer signer = AccountSigner.none(accScriptHash).setAllowedGroups(publicKey);
        ECPublicKey[] groups = new ECPublicKey[16];
        for (int i = 0; i <= 15; i++) {
            groups[i] = publicKey;
        }

        SignerConfigurationException thrown =
                assertThrows(SignerConfigurationException.class, () -> signer.setAllowedGroups(groups));
        assertThat(thrown.getMessage(),
                is(format("Trying to set more than %s allowed contract groups on a signer.", MAX_SIGNER_SUBITEMS)));
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
    public void serializeWithMultipleScopesContractsGroupsAndRules() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        AccountSigner.calledByEntry(accScriptHash)
                .setAllowedGroups(groupPubKey1, groupPubKey2)
                .setAllowedContracts(contract1, contract2)
                .setRules(new WitnessRule(WitnessAction.ALLOW,
                        new CalledByContractCondition(contract1)))
                .serialize(writer);
        byte[] actual = outStream.toByteArray();
        byte[] expected = hexStringToByteArray(""
                + reverseHexString(accScriptHash.toString())
                + "71" // calledByEntry, custom contracts, custom groups and witness rule scopes
                + "02" // array length 2
                + reverseHexString(contract1.toString())
                + reverseHexString(contract2.toString())
                + "02" // array length 2
                + toHexStringNoPrefix(groupPubKey1.toArray())
                + toHexStringNoPrefix(groupPubKey2.toArray())
                + "01" // Rules list length 1
                + "01" // WitnessRuleAction "Allow"
                + "28" // CalleByContract WitnessConditionType
                + reverseHexString(contract1.toString()));
        assertArrayEquals(expected, actual);
    }

    @Test
    public void deserialize() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + reverseHexString(accScriptHash.toString())
                + "71" // calledByEntry, custom contracts and custom groups scope
                + "02" // array length 2
                + reverseHexString(contract1.toString())
                + reverseHexString(contract2.toString())
                + "02" // array length 2
                + toHexStringNoPrefix(groupPubKey1.toArray())
                + toHexStringNoPrefix(groupPubKey2.toArray())
                + "01" // Rules list length 1
                + "01" // WitnessRuleAction "Allow"
                + "28" // CalleByContract WitnessConditionType
                + reverseHexString(contract1.toString()));

        Signer c = NeoSerializableInterface.from(data, Signer.class);

        assertThat(c.getScriptHash(), is(accScriptHash));
        assertThat(c.getScopes(), containsInAnyOrder(
                WitnessScope.CUSTOM_CONTRACTS,
                WitnessScope.CALLED_BY_ENTRY,
                WitnessScope.CUSTOM_GROUPS,
                WitnessScope.WITNESS_RULES));
        assertThat(c.getAllowedContracts(), containsInAnyOrder(contract1, contract2));
        assertThat(c.getAllowedGroups(), containsInAnyOrder(groupPubKey1, groupPubKey2));
        WitnessRule rule = c.getRules().get(0);
        assertThat(rule.getAction(), is(WitnessAction.ALLOW));
        assertThat(rule.getCondition().getType(), is(WitnessConditionType.CALLED_BY_CONTRACT));
        assertThat(((CalledByContractCondition) rule.getCondition()).getScriptHash(),
                is(contract1));
    }

    @Test
    public void failDeserializingWithTooManyContracts() {
        StringBuilder serialized = new StringBuilder(""
                + reverseHexString(accScriptHash.toString())
                + "11" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract script hashes.
        for (int i = 0; i <= 17; i++) {
            serialized.append(reverseHexString(contract1.toString()));
        }
        byte[] serializedBytes = hexStringToByteArray(serialized.toString());

        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(serializedBytes, Signer.class));
        assertThat(thrown.getMessage(),
                containsString(format("A signer's scope can only contain %s allowed contracts.", MAX_SIGNER_SUBITEMS)));
    }

    @Test
    public void failDeserializingWithTooManyContractGroups() {
        StringBuilder serialized = new StringBuilder(""
                + reverseHexString(accScriptHash.toString())
                + "21" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract group public keys.
        for (int i = 0; i <= 17; i++) {
            serialized.append(toHexStringNoPrefix(groupPubKey1.toArray()));
        }
        byte[] serializedBytes = hexStringToByteArray(serialized.toString());

        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(serializedBytes, Signer.class));
        assertThat(thrown.getMessage(), containsString(
                format("A signer's scope can only contain %s allowed contract groups.", MAX_SIGNER_SUBITEMS)));
    }

    @Test
    public void failDeserializingWithTooManyRules() {
        StringBuilder serialized = new StringBuilder(""
                + reverseHexString(accScriptHash.toString())
                + "41" // calledByEntry, custom contracts
                + "11"); // array length 17 (0x11)
        // Add one too many contract group public keys.
        for (int i = 0; i <= 17; i++) {
            serialized.append("01") // WitnessRuleAction "Allow"
                    .append("28") // CalleByContract WitnessConditionType
                    .append(reverseHexString(contract1.toString()));
        }
        byte[] serializedBytes = hexStringToByteArray(serialized.toString());


        DeserializationException thrown = assertThrows(DeserializationException.class,
                () -> NeoSerializableInterface.from(serializedBytes, Signer.class));
        assertThat(thrown.getMessage(),
                containsString(format("A signer's scope can only contain %s rules.", MAX_SIGNER_SUBITEMS)));
    }

    @Test
    public void getSize() {
        WitnessRule rule = new WitnessRule(WitnessAction.ALLOW, new AndCondition(
                new BooleanCondition(true), new BooleanCondition(false)));

        Signer signer = AccountSigner.calledByEntry(accScriptHash)
                .setAllowedGroups(groupPubKey1, groupPubKey2)
                .setAllowedContracts(contract1, contract2)
                .setRules(rule, rule);

        int expectedSize = 20 // Account script hash
                + 1 // Scope byte
                + 1 // length byte of allowed contracts list
                + 20 + 20 // Script hashes of two allowed contracts
                + 1 // length byte of allowed groups list
                + 33 + 33 // Public keys of two allowed groups
                + 1 // length byte of rules list
                + 1 // byte for WitnessRuleAction Allow
                + 1 // byte for WitnessCondition type (AndCondition)
                + 1 // length of AND condition list
                + 1 // byte for WitnessCondition type (BooleanCondition)
                + 1 // byte for value of BooleanCondition
                + 1 // byte for WitnessCondition type (BooleanCondition)
                + 1 // byte for value of BooleanCondition
                + 1 // byte for WitnessRuleAction Allow
                + 1 // byte for WitnessCondition type (AndCondition)
                + 1 // length of AND condition list
                + 1 // byte for WitnessCondition type (BooleanCondition)
                + 1 // byte for value of BooleanCondition
                + 1 // byte for WitnessCondition type (BooleanCondition)
                + 1; // byte for value of BooleanCondition

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

    @Test
    public void failOnSteppingOverMaxConditionNestingDepth() {
        ScriptHashCondition cond = new ScriptHashCondition(accScriptHash);
        AndCondition and = new AndCondition(
                new AndCondition(
                        new AndCondition(
                                new NotCondition(cond))));

        WitnessRule rule = new WitnessRule(WitnessAction.ALLOW, and);

        SignerConfigurationException thrown =
                assertThrows(SignerConfigurationException.class, () -> AccountSigner.none(acc).setRules(rule));
        assertThat(thrown.getMessage(), is(format("A maximum nesting depth of %s is allowed for witness conditions.",
                WitnessCondition.MAX_NESTING_DEPTH)));
    }

    @Test
    public void failAddingRuleToGlobalSigner() {
        ScriptHashCondition cond = new ScriptHashCondition(accScriptHash);
        WitnessRule rule = new WitnessRule(WitnessAction.ALLOW, cond);
        SignerConfigurationException thrown = assertThrows(SignerConfigurationException.class,
                () -> AccountSigner.global(acc).setRules(rule));
        assertThat(thrown.getMessage(), is("Trying to set witness rules on a Signer with global scope."));
    }

    @Test
    public void failAddingTooManyRules() {
        ScriptHashCondition cond = new ScriptHashCondition(accScriptHash);
        WitnessRule rule = new WitnessRule(WitnessAction.ALLOW, cond);
        AccountSigner signer = AccountSigner.none(acc);
        for (int i = 0; i < MAX_SIGNER_SUBITEMS; i++) {
            signer.setRules(rule);
        }

        SignerConfigurationException thrown =
                assertThrows(SignerConfigurationException.class, () -> signer.setRules(rule));
        assertThat(thrown.getMessage(),
                is(format("Trying to set more than %s allowed witness rules on a signer.", MAX_SIGNER_SUBITEMS)));
    }

    @Test
    public void serialize_deserialize_max_nested_rules() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        WitnessRule rule = new WitnessRule(WitnessAction.ALLOW,
                new AndCondition(
                        new AndCondition(
                                new BooleanCondition(true))));

        AccountSigner.none(Hash160.ZERO).setRules(rule).serialize(writer);
        byte[] actual = outStream.toByteArray();

        byte[] expected = hexStringToByteArray(
                "0000000000000000000000000000000000000000400101020102010001");
        assertArrayEquals(expected, actual);
    }

}
