package io.neow3j.protocol.core.witnessrule;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.exceptions.WitnessConditionCastException;
import io.neow3j.transaction.witnessrule.WitnessAction;
import io.neow3j.transaction.witnessrule.WitnessConditionType;
import io.neow3j.types.Hash160;
import io.neow3j.protocol.ResponseTester;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WitnessRuleTest extends ResponseTester {

    @Test
    public void testBooleanCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Allow\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"Boolean\",\n" +
                "        \"expression\": \"false\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.ALLOW));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.BOOLEAN));
        assertFalse(witnessRule.getCondition().getBooleanExpression());

        BooleanCondition expected = new BooleanCondition(false);
        assertEquals(expected, witnessRule.getCondition());

        WitnessRule expectedRule = new WitnessRule(WitnessAction.ALLOW, new BooleanCondition(false));
        assertEquals(expectedRule, witnessRule);
        assertEquals(expectedRule.toString(), witnessRule.toString());
    }

    @Test
    public void testNotCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Allow\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"Not\",\n" +
                "        \"expression\": {\n" +
                "            \"type\": \"Not\",\n" +
                "            \"expression\": {\n" +
                "                \"type\": \"CalledByEntry\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.ALLOW));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.NOT));
        WitnessCondition expression1 = witnessRule.getCondition().getExpression();
        assertThat(expression1.getType(), is(WitnessConditionType.NOT));
        WitnessCondition expression2 = expression1.getExpression();
        assertThat(expression2.getType(), is(WitnessConditionType.CALLED_BY_ENTRY));

        NotCondition expression = new NotCondition(new CalledByEntryCondition());
        NotCondition expected = new NotCondition(expression);
        assertEquals(expected, witnessRule.getCondition());
        assertEquals(expected.toString(), witnessRule.getCondition().toString());
    }

    @Test
    public void testAndCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Allow\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"And\",\n" +
                "        \"expressions\": [\n" +
                "            {\n" +
                "                \"type\": \"CalledByEntry\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": \"Group\",\n" +
                "                \"group\": \"021821807f923a3da004fb73871509d7635bcc05f41edef2a3ca5c941d8bbc1231\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": \"Boolean\",\n" +
                "                \"expression\": \"true\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.ALLOW));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.AND));
        List<WitnessCondition> expressionList = witnessRule.getCondition().getExpressionList();
        assertThat(expressionList, hasSize(3));
        assertThat(expressionList.get(0).getType(), is(WitnessConditionType.CALLED_BY_ENTRY));
        assertThat(expressionList.get(1).getType(), is(WitnessConditionType.GROUP));
        ECKeyPair.ECPublicKey pubKey =
                new ECKeyPair.ECPublicKey("021821807f923a3da004fb73871509d7635bcc05f41edef2a3ca5c941d8bbc1231");
        assertThat(expressionList.get(1).getGroup(), is(pubKey));
        assertThat(expressionList.get(2).getType(), is(WitnessConditionType.BOOLEAN));
        assertTrue(expressionList.get(2).getBooleanExpression());

        ArrayList<WitnessCondition> expressions = new ArrayList<>();
        expressions.add(new CalledByEntryCondition());
        expressions.add(new GroupCondition(pubKey));
        expressions.add(new BooleanCondition(true));
        AndCondition expected = new AndCondition(expressions);
        assertEquals(expected, witnessRule.getCondition());
    }

    @Test
    public void testOrCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"Or\",\n" +
                "        \"expressions\": [\n" +
                "            {\n" +
                "                \"type\": \"Group\",\n" +
                "                \"group\": \"023be7b6742268f4faca4835718f3232ddc976855d5ef273524cea36f0e8d102f3\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": \"CalledByEntry\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.DENY));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.OR));
        List<WitnessCondition> expressionList = witnessRule.getCondition().getExpressionList();
        assertThat(expressionList, hasSize(2));
        assertThat(expressionList.get(0).getType(), is(WitnessConditionType.GROUP));
        ECKeyPair.ECPublicKey pubKey =
                new ECKeyPair.ECPublicKey("023be7b6742268f4faca4835718f3232ddc976855d5ef273524cea36f0e8d102f3");
        assertThat(expressionList.get(0).getGroup(), is(pubKey));
        assertThat(expressionList.get(1).getType(), is(WitnessConditionType.CALLED_BY_ENTRY));

        ArrayList<WitnessCondition> expressions = new ArrayList<>();
        expressions.add(new GroupCondition(pubKey));
        expressions.add(new CalledByEntryCondition());
        OrCondition expected = new OrCondition(expressions);
        assertEquals(expected, witnessRule.getCondition());
        assertEquals(expected.toString(), witnessRule.getCondition().toString());
    }

    @Test
    public void testScriptHashCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Allow\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"ScriptHash\",\n" +
                "        \"hash\": \"0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5\"\n" +
                "    }" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.ALLOW));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.SCRIPT_HASH));
        Hash160 hash = new Hash160("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5");
        assertThat(witnessRule.getCondition().getScriptHash(), is(hash));

        ScriptHashCondition expected = new ScriptHashCondition(hash);
        assertEquals(expected, witnessRule.getCondition());
        assertEquals(expected.toString(), witnessRule.getCondition().toString());
    }

    @Test
    public void testGroupCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Allow\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"Group\",\n" +
                "        \"group\": \"0352321377ac7b4e1c4c2ebfe28f4d82fa3c213f7ccfcc9dac62da37fb9b433f0c\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.ALLOW));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.GROUP));
        ECKeyPair.ECPublicKey pubKey =
                new ECKeyPair.ECPublicKey("0352321377ac7b4e1c4c2ebfe28f4d82fa3c213f7ccfcc9dac62da37fb9b433f0c");
        assertThat(witnessRule.getCondition().getGroup(), is(pubKey));

        GroupCondition expected = new GroupCondition(pubKey);
        assertEquals(expected, witnessRule.getCondition());
    }

    @Test
    public void testCalledByEntryCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"CalledByEntry\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.DENY));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.CALLED_BY_ENTRY));

        CalledByEntryCondition expected = new CalledByEntryCondition();
        assertEquals(expected, witnessRule.getCondition());
    }

    @Test
    public void testCalledByContractCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Allow\",\n" +
                "    \"condition\": {\n" +
                "        \"type\": \"CalledByContract\",\n" +
                "        \"hash\": \"0xef4073a0f2b305a38ec4050e4d3d28bc40ea63e4\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.ALLOW));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.CALLED_BY_CONTRACT));
        Hash160 hash = new Hash160("0xef4073a0f2b305a38ec4050e4d3d28bc40ea63e4");
        assertThat(witnessRule.getCondition().getScriptHash(), is(hash));

        CalledByContractCondition expected = new CalledByContractCondition(hash);
        assertEquals(expected, witnessRule.getCondition());
        assertEquals(expected.toString(), witnessRule.getCondition().toString());
    }

    @Test
    public void testCalledByGroupCondition() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\":\"CalledByGroup\",\n" +
                "        \"group\":\"035a1ced7ae274a881c3f479452c8bca774c89f653d54c5c5959a01371a8c696fd\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        assertThat(witnessRule.getAction(), is(WitnessAction.DENY));
        assertThat(witnessRule.getCondition().getType(), is(WitnessConditionType.CALLED_BY_GROUP));
        ECKeyPair.ECPublicKey pubKey =
                new ECKeyPair.ECPublicKey("035a1ced7ae274a881c3f479452c8bca774c89f653d54c5c5959a01371a8c696fd");
        assertThat(witnessRule.getCondition().getGroup(), is(pubKey));

        CalledByGroupCondition expected = new CalledByGroupCondition(pubKey);
        assertEquals(expected, witnessRule.getCondition());
    }

    @Test
    public void throwNoBoolean() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\":\"CalledByGroup\",\n" +
                "        \"group\":\"035a1ced7ae274a881c3f479452c8bca774c89f653d54c5c5959a01371a8c696fd\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        WitnessConditionCastException thrown = assertThrows(WitnessConditionCastException.class,
                () -> witnessRule.getCondition().getBooleanExpression());
        assertThat(thrown.getMessage(), containsString("Cannot retrieve a boolean"));
    }

    @Test
    public void throwNoExpression() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\":\"CalledByGroup\",\n" +
                "        \"group\":\"035a1ced7ae274a881c3f479452c8bca774c89f653d54c5c5959a01371a8c696fd\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        WitnessConditionCastException thrown = assertThrows(WitnessConditionCastException.class,
                () -> witnessRule.getCondition().getExpression());
        assertThat(thrown.getMessage(), containsString("Cannot retrieve a condition"));
    }

    @Test
    public void throwNoExpressionList() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\":\"Boolean\",\n" +
                "        \"expression\":\"false\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        WitnessConditionCastException thrown = assertThrows(WitnessConditionCastException.class,
                () -> witnessRule.getCondition().getExpressionList());
        assertThat(thrown.getMessage(), containsString("Cannot retrieve a list of witness conditions"));
    }

    @Test
    public void throwNoScriptHash() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\":\"And\",\n" +
                "        \"expressions\": [\n" +
                "            {\n" +
                "                \"type\": \"CalledByEntry\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": \"Group\",\n" +
                "                \"group\": \"021821807f923a3da004fb73871509d7635bcc05f41edef2a3ca5c941d8bbc1231\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        WitnessConditionCastException thrown = assertThrows(WitnessConditionCastException.class,
                () -> witnessRule.getCondition().getScriptHash());
        assertThat(thrown.getMessage(), containsString("Cannot retrieve a Hash160"));
    }

    @Test
    public void throwNoGroup() throws JsonProcessingException {
        String json = "{" +
                "    \"action\": \"Deny\",\n" +
                "    \"condition\": {\n" +
                "        \"type\":\"CalledByEntry\"\n" +
                "    }\n" +
                "}";
        WitnessRule witnessRule = getObjectMapper().readValue(json, WitnessRule.class);

        WitnessConditionCastException thrown = assertThrows(WitnessConditionCastException.class,
                () -> witnessRule.getCondition().getGroup());
        assertThat(thrown.getMessage(), containsString("Cannot retrieve a ECPublicKey"));
    }

}
