package io.neow3j.compiler;

import static io.neow3j.compiler.CompilerTest.deployContract;
import static io.neow3j.compiler.CompilerTest.getResultFilePath;
import static io.neow3j.compiler.CompilerTest.loadExpectedResultFile;
import static io.neow3j.protocol.ObjectMapperFactory.getObjectMapper;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import io.neow3j.protocol.core.methods.response.StackItem;
import java.io.IOException;
import java.io.InputStream;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class RelationalOperatorsTest {

    private static String CONTRACT_NAME = RelationalOperators.class.getSimpleName();
    private static SmartContract relationalOperatorsContract;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClass() throws Exception {
        CompilerTest.setUp();
        relationalOperatorsContract = deployContract("io.neow3j.compiler." + CONTRACT_NAME);
        CompilerTest.waitUntilContractIsDeployed(relationalOperatorsContract.getScriptHash());
    }

    @Test
    public void integerRelationalOperators() throws IOException, InterruptedException {
        String methodName = testName.getMethodName();
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                methodName,
                ContractParameter.integer(1),
                ContractParameter.integer(0));

        ArrayStackItem expected = loadExpectedResultFile(CONTRACT_NAME, methodName,
                ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

}

class RelationalOperators {

    @EntryPoint
    public static boolean[] integerRelationalOperators(int i, int j) {
        boolean[] b = new boolean[6];
        b[0] = i == j;
        b[1] = i != j;
        b[2] = i < j;
        b[3] = i <= j;
        b[4] = i > j;
        b[5] = i >= j;
        return b;
    }

}
