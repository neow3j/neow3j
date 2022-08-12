package io.neow3j.transaction.witnessrule;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.serialization.BinaryWriter;
import io.neow3j.serialization.NeoSerializableInterface;
import io.neow3j.serialization.exceptions.DeserializationException;
import io.neow3j.types.Hash160;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.neow3j.test.TestProperties.defaultAccountPublicKey;
import static io.neow3j.test.TestProperties.defaultAccountScriptHash;
import static io.neow3j.utils.Numeric.hexStringToByteArray;
import static io.neow3j.utils.Numeric.reverseHexString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WitnessConditionTest {

    @Test
    public void deserializeBooleanCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "00"
                + "01"); // value for "true"
        BooleanCondition c = NeoSerializableInterface.from(data, BooleanCondition.class);
        assertTrue(c.getExpression());
        assertThat(c.getType(), is(WitnessConditionType.BOOLEAN));

    }

    @Test
    public void serializeBooleanCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new BooleanCondition(true).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "00" // type
                + "01"); // value
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeNotCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "01"
                + "0001"); // a boolean condition in the not condition
        NotCondition c = NeoSerializableInterface.from(data, NotCondition.class);
        assertThat(c.getExpression().getType(), is(WitnessConditionType.BOOLEAN));
        assertThat(c.getType(), is(WitnessConditionType.NOT));
    }

    @Test
    public void serializeNotCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new NotCondition(new BooleanCondition(true)).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "01" // type
                + "0001"); // value
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeAndCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "02"
                + "02" // two sub-conditions
                + "0001" // a boolean condition
                + "0000"); // a boolean condition
        AndCondition c = NeoSerializableInterface.from(data, AndCondition.class);
        assertThat(c.getExpressions().get(0).getType(), is(WitnessConditionType.BOOLEAN));
        assertTrue(((BooleanCondition) c.getExpressions().get(0)).getExpression());
        assertThat(c.getExpressions().get(1).getType(), is(WitnessConditionType.BOOLEAN));
        assertFalse(((BooleanCondition) c.getExpressions().get(1)).getExpression());
        assertThat(c.getType(), is(WitnessConditionType.AND));
    }

    @Test
    public void serializeAndCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new AndCondition(new BooleanCondition(true), new BooleanCondition(false)).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "02"
                + "02" // two sub-conditions
                + "0001" // a boolean condition
                + "0000"); // a boolean condition
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeOrCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "03"
                + "02" // two sub-conditions
                + "0001" // a boolean condition
                + "0000"); // a boolean condition
        OrCondition c = NeoSerializableInterface.from(data, OrCondition.class);
        assertThat(c.getExpressions().get(0).getType(), is(WitnessConditionType.BOOLEAN));
        assertTrue(((BooleanCondition) c.getExpressions().get(0)).getExpression());
        assertThat(c.getExpressions().get(1).getType(), is(WitnessConditionType.BOOLEAN));
        assertFalse(((BooleanCondition) c.getExpressions().get(1)).getExpression());
        assertThat(c.getType(), is(WitnessConditionType.OR));
    }

    @Test
    public void serializeOrCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new OrCondition(new BooleanCondition(true), new BooleanCondition(false)).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "03"
                + "02" // two sub-conditions
                + "0001" // a boolean condition
                + "0000"); // a boolean condition
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeScriptHashCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "18"
                + reverseHexString(defaultAccountScriptHash()));
        ScriptHashCondition c = NeoSerializableInterface.from(data, ScriptHashCondition.class);
        assertThat(c.getScriptHash(), is(new Hash160(defaultAccountScriptHash())));
        assertThat(c.getType(), is(WitnessConditionType.SCRIPT_HASH));
    }

    @Test
    public void serializeScriptHashCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new ScriptHashCondition(new Hash160(defaultAccountScriptHash())).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "18"
                + reverseHexString(defaultAccountScriptHash()));
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeGroupCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "19"
                + defaultAccountPublicKey());
        GroupCondition c = NeoSerializableInterface.from(data, GroupCondition.class);
        assertThat(c.getGroup().getEncoded(true), is(hexStringToByteArray(defaultAccountPublicKey())));
        assertThat(c.getType(), is(WitnessConditionType.GROUP));
    }

    @Test
    public void serializeGroupCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new GroupCondition(new ECKeyPair.ECPublicKey(defaultAccountPublicKey())).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "19"
                + defaultAccountPublicKey());
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeCalledByEntryCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray("20");
        CalledByEntryCondition c = NeoSerializableInterface.from(data, CalledByEntryCondition.class);
        assertThat(c.getType(), is(WitnessConditionType.CALLED_BY_ENTRY));
    }

    @Test
    public void serializeCalledByEntryCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new CalledByEntryCondition().serialize(writer);
        byte[] expected = hexStringToByteArray("20");
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeCalledByContractCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "28"
                + reverseHexString(defaultAccountScriptHash()));
        CalledByContractCondition c = NeoSerializableInterface.from(data, CalledByContractCondition.class);
        assertThat(c.getScriptHash().toString(), is(defaultAccountScriptHash()));
        assertThat(c.getType(), is(WitnessConditionType.CALLED_BY_CONTRACT));
    }

    @Test
    public void serializeCalledByContractCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new CalledByContractCondition(new Hash160(defaultAccountScriptHash())).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "28"
                + reverseHexString(defaultAccountScriptHash()));
        assertArrayEquals(expected, outStream.toByteArray());
    }

    @Test
    public void deserializeCalledByGroupCondition() throws DeserializationException {
        byte[] data = hexStringToByteArray(""
                + "29"
                + defaultAccountPublicKey());
        CalledByGroupCondition c = NeoSerializableInterface.from(data, CalledByGroupCondition.class);
        assertThat(c.getGroup().getEncoded(true), is(hexStringToByteArray(defaultAccountPublicKey())));
        assertThat(c.getType(), is(WitnessConditionType.CALLED_BY_GROUP));
    }

    @Test
    public void serializeCalledByGroupCondition() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BinaryWriter writer = new BinaryWriter(outStream);
        new CalledByGroupCondition(new ECKeyPair.ECPublicKey(defaultAccountPublicKey())).serialize(writer);
        byte[] expected = hexStringToByteArray(""
                + "29"
                + defaultAccountPublicKey());
        assertArrayEquals(expected, outStream.toByteArray());
    }

}
