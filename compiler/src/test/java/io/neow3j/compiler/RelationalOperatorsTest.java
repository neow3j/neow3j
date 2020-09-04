package io.neow3j.compiler;

import static io.neow3j.compiler.CompilerTest.deployContract;
import static io.neow3j.compiler.CompilerTest.loadExpectedResultFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.SmartContract;
import io.neow3j.devpack.framework.annotations.EntryPoint;
import io.neow3j.protocol.core.methods.response.ArrayStackItem;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class RelationalOperatorsTest {

    private static String CONTRACT_NAME = RelationalOperators.class.getSimpleName();
    private static SmartContract relationalOperatorsContract;

    // These are the names of the methods inside of the smart contract under test.
    private final static String INTEGERS_MTHD_NAME = "integers";
    private final static String LONGS_MTHD_NAME = "longs";
    private final static String BOOLEANS_MTHD_NAME = "booleans";


    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void beforeClass() throws Exception {
        CompilerTest.setUp();
        relationalOperatorsContract = deployContract("io.neow3j.compiler." + CONTRACT_NAME);
        CompilerTest.waitUntilContractIsDeployed(relationalOperatorsContract.getScriptHash());
    }

    @Test
    public void unequalSmallIntegers() throws IOException {
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                INTEGERS_MTHD_NAME,
                ContractParameter.integer(1),
                ContractParameter.integer(0));

        ArrayStackItem expected = loadExpectedResultFile(CONTRACT_NAME, testName.getMethodName(),
                ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void equalLargeIntegers() throws IOException, InterruptedException {
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                INTEGERS_MTHD_NAME,
                ContractParameter.integer(new BigInteger("100000000000000000000")),
                ContractParameter.integer(new BigInteger("100000000000000000000")));

        ArrayStackItem expected = loadExpectedResultFile(CONTRACT_NAME, testName.getMethodName(),
                ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void unequalSmallLongs() throws IOException {
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                LONGS_MTHD_NAME,
                ContractParameter.integer(1),
                ContractParameter.integer(0));

        ArrayStackItem expected = loadExpectedResultFile(CONTRACT_NAME, testName.getMethodName(),
                ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void equalLargeLongs() throws IOException {
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                LONGS_MTHD_NAME,
                ContractParameter.integer(new BigInteger("100000000000000000000")),
                ContractParameter.integer(new BigInteger("100000000000000000000")));

        ArrayStackItem expected = loadExpectedResultFile(CONTRACT_NAME, testName.getMethodName(),
                ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void equalBooleans() throws IOException {
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                BOOLEANS_MTHD_NAME,
                ContractParameter.bool(true),
                ContractParameter.bool(true));

        ArrayStackItem expected = loadExpectedResultFile(CONTRACT_NAME, testName.getMethodName(),
                ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }

    @Test
    public void unequalBooleans() throws IOException {
        NeoInvokeFunction response = relationalOperatorsContract.invokeFunction(
                BOOLEANS_MTHD_NAME,
                ContractParameter.bool(false),
                ContractParameter.bool(true));

        ArrayStackItem expected = loadExpectedResultFile(CONTRACT_NAME, testName.getMethodName(),
                ArrayStackItem.class);
        assertThat(response.getInvocationResult().getStack().get(0), is(expected));
    }
}

class RelationalOperators {

    @EntryPoint
    public static boolean[] integers(int i, int j) {
        boolean[] b = new boolean[6];
        b[0] = i == j;
        b[1] = i != j;
        b[2] = i < j;
        b[3] = i <= j;
        b[4] = i > j;
        b[5] = i >= j;
        return b;
    }

    public static boolean[] longs(long i, long j) {
        boolean[] b = new boolean[6];
        b[0] = i == j;
        b[1] = i != j;
        b[2] = i < j;
        b[3] = i <= j;
        b[4] = i > j;
        b[5] = i >= j;
        return b;
    }

    public static boolean[] booleans(boolean i, boolean j) {
        boolean[] b = new boolean[2];
        b[0] = i == j;
        b[1] = i != j;
        return b;
    }

}
