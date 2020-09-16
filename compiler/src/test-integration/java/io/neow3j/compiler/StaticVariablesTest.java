package io.neow3j.compiler;

import static org.junit.Assert.assertThat;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.annotations.Features;
import io.neow3j.devpack.neo.Runtime;
import io.neow3j.devpack.neo.Storage;
import io.neow3j.devpack.neo.StorageMap;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.BeforeClass;
import org.junit.Test;

public class StaticVariablesTest extends CompilerTest {

    @BeforeClass
    public static void setUp() throws Exception {
        setUp(StaticVariables.class.getName());
    }

    @Test
    public void putToStaticStorageMap() throws IOException {
        String txHash = sendInvokeTransaction(
                getTestName(),
                ContractParameter.string("key"),
                ContractParameter.string("value"));
        waitUntilTransactionIsExecuted(txHash);
        assertVMExitedWithHalt(txHash);
        assertStorageContains("data" + "key", "value");
    }

    @Test
    public void stringConcatWithSyscall() throws IOException {
        NeoInvokeFunction response = contract.invokeFunction(getTestName());
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                StringStartsWith.startsWith("The platform: "));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                StringEndsWith.endsWith("NEO"));
    }
}

@Features(hasStorage = true)
class StaticVariables {

    private static final StorageMap map = Storage.getStorageContext().createMap("data");
    private static final String platform = "The platform: " + Runtime.getPlatform() ;

    public static void putToStaticStorageMap(String key, String value) {
        map.put(key, value);
    }

    public static String stringConcatWithSyscall() {
        return platform;
    }

}
