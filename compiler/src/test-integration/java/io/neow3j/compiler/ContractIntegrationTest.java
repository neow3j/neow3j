package io.neow3j.compiler;

import static io.neow3j.contract.ContractParameter.hash160;
import static io.neow3j.contract.ContractParameter.string;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.contract.NeoToken;
import io.neow3j.devpack.neo.CallFlags;
import io.neow3j.devpack.neo.Contract;
import io.neow3j.protocol.core.methods.response.NeoInvokeFunction;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContractIntegrationTest extends ContractTest {

   @BeforeClass
   public static void setUp() throws Throwable {
      setUp(ContractIntegrationTestContract.class.getName());
   }

   @Test
   public void callWithoutCallFlagsWithoutArguments() throws IOException {
       NeoInvokeFunction resp = callInvokeFunction(hash160(NeoToken.SCRIPT_HASH), string(
               "symbol"));
       assertThat(resp.getInvocationResult().getStack().get(0).asByteString().getAsString(),
               is("NEO"));
   }

    @Test
    public void callWithoutCallFlagsWithArgument() throws IOException {
        NeoInvokeFunction resp = callInvokeFunction(hash160(NeoToken.SCRIPT_HASH), string(
                "balanceOf"), hash160(committee.getScriptHash()));
        assertThat(resp.getInvocationResult().getStack().get(0).asInteger().getValue().intValue(),
                is(100_000_000));
    }

    @Test
    public void callWitCallFlagsWithArgument() throws IOException {
        NeoInvokeFunction resp = callInvokeFunction(hash160(NeoToken.SCRIPT_HASH), string(
                "balanceOf"), hash160(committee.getScriptHash()));
        assertThat(resp.getInvocationResult().getStack().get(0).asInteger().getValue().intValue(),
                is(100_000_000));
    }

   @Test
   public void getCallFlags() throws IOException {
       NeoInvokeFunction resp = callInvokeFunction();
       assertThat(resp.getInvocationResult().getStack().get(0).asInteger().getValue().intValue(),
               is(15)); // CallFlag ALL
   }

   static class ContractIntegrationTestContract {

       public static Object callWithoutCallFlagsWithoutArguments(byte[] hash, String method) {
           return Contract.call(hash, method, new Object[]{});
       }

       public static Object callWithoutCallFlagsWithArgument(byte[] hash, String method,
               byte[] scriptHash) {
           return Contract.call(hash, method, new Object[]{scriptHash});
       }

       public static Object callWitCallFlagsWithArgument(byte[] hash, String method,
               byte[] scriptHash) {
           return Contract.call(hash, method, new Object[]{scriptHash}, CallFlags.ALL);
       }

       public static byte getCallFlags() {
          return Contract.getCallFlags();
       }
   }
}
