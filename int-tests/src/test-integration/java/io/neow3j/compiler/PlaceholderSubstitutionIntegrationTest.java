package io.neow3j.compiler;

import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.events.Event1Arg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PlaceholderSubstitutionIntegrationTest {

    @RegisterExtension
    public static ContractTestExtension ct = new ContractTestExtension();

    @Test
    public void testReplacementInMethodBody() throws Throwable {
        Map<String, String> replaceMap = new HashMap<>();
        String ph1 = "Hello, ";
        String ph2 = "world!";
        String ph3 = "fffdc93764dbaddd97c48f252a53ea4643faa3fd";
        String ph4 = "someMethod";
        String ph5 = "acce6fd80d44e1796aa0c2c625e9e4e0ce39efc0";
        String ph6 = "anotherMethod";
        String ph7 = "MyEvent";
        String ph8 = "NM7Aky765FG8NhhwtxjXRx7jEL1cnw7PBP";
        replaceMap.put("<PLACEHODLER1>", ph1);
        replaceMap.put("<PLACEHODLER2>", ph2);
        replaceMap.put("<PLACEHODLER3>", ph3);
        replaceMap.put("<PLACEHODLER4>", ph4);
        replaceMap.put("<PLACEHODLER5>", ph5);
        replaceMap.put("<PLACEHODLER6>", ph6);
        replaceMap.put("<PLACEHODLER7>", ph7);
        replaceMap.put("<PLACEHODLER8>", ph8);
        CompilationUnit res = new Compiler().compile(
                PlaceholderSubstitutionIntegrationTestContract.class.getName(),
                replaceMap
        );
        SmartContract c = ct.deployContract(res.getNefFile(), res.getManifest());
        assertThat(c.callFunctionReturningString("method"), is(ph1+ph2));
        assertThat(c.getManifest().getPermissions().get(0).getContract(),is("0x" + ph3));
        assertThat(c.getManifest().getPermissions().get(0).getMethods().get(0),is(ph4));
        assertThat(c.getManifest().getPermissions().get(1).getContract(),is("0x" + ph5));
        assertThat(c.getManifest().getPermissions().get(1).getMethods().get(0),is(ph6));
        assertThat(c.getManifest().getAbi().getEvents().get(0).getName(), is(ph7));
        assertThat(c.callFunctionReturningScriptHash("getOwner").toAddress(), is(ph8));
    }

    @Permission(contract = "<PLACEHODLER3>", methods = "<PLACEHODLER4>")
    @Permission(contract = "<PLACEHODLER5>", methods = "<PLACEHODLER6>")
    static class PlaceholderSubstitutionIntegrationTestContract {

        static final String s1 = "<PLACEHODLER1>";
        static final Hash160 OWNER = StringLiteralHelper.addressToScriptHash("<PLACEHODLER8>");

        @DisplayName("<PLACEHODLER7>")
        static Event1Arg<String> event;

        public static String method() {
            String s2 = "<PLACEHODLER2>";
            return s1 + s2;
        }

        public static Hash160 getOwner() {
            return OWNER;
        }

    }

}
