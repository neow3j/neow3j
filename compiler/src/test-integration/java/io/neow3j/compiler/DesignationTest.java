package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.integer;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.ByteArrayTest.ByteArrays;
import io.neow3j.devpack.neo.DesignateRole;
import io.neow3j.devpack.neo.Designation;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class DesignationTest extends ContractTest {

    @BeforeClass
    public static void setUp() throws Throwable {
        setUp(DesignationTestContract.class.getName());
    }

    @Test
    public void getName() throws IOException {
        // TODO: Test when preview4 privatenet docker image is ready.
        NeoInvokeFunction response = callInvokeFunction();
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("Designation"));
    }

    @Test
    public void getDesingates() throws IOException {
        // TODO: Test when preview4 privatenet docker image is ready.
        //  How can we add new designates?
        NeoInvokeFunction response = callInvokeFunction(integer(DesignateRole.STATE_VALIDATOR));
        assertThat(response.getInvocationResult().getStack().get(0).asByteString().getAsString(),
                is("Designation"));
    }

    static class DesignationTestContract {

        public static String getName() {
            return Designation.name();
        }

        public static byte[][] getDesignates(byte role) {
            return Designation.getDesignatedByRole(role);
        }
    }
}
