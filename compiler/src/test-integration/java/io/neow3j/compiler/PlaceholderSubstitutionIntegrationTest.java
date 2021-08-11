package io.neow3j.compiler;

import io.neow3j.contract.SmartContract;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PlaceholderSubstitutionIntegrationTest {

    @ClassRule
    public static ContractTestRule ct = new ContractTestRule();

    @Test
    public void testReplacementInMethodBody() throws Throwable {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("<PLACEHODLER1>", "Hello, ");
        replaceMap.put("<PLACEHODLER2>", "world!");
        CompilationUnit res = new Compiler().compile(
                PlaceholderSubstitutionIntegrationTestContract.class.getName(),
                replaceMap
        );
        SmartContract c = ct.deployContract(res.getNefFile(), res.getManifest());
        assertThat(c.callFuncReturningString("main"), is("Hello, world!"));
    }

    static class PlaceholderSubstitutionIntegrationTestContract {

        public static final String s1 = "<PLACEHODLER1>";

        public static String main() {
            String s2 = "<PLACEHODLER2>";
            return s1 + s2;
        }

    }
}
