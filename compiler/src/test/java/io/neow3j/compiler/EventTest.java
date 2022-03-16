package io.neow3j.compiler;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event1Arg;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.types.ContractParameterType;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class EventTest {

    @Test
    public void testFiringEventWithMethodCallWithDevpackMethodCall() throws IOException {
        CompilationUnit res = new Compiler().compile(FireEventWithMethodCallThatCallsDevpackMethod.class.getName());

        List<ContractManifest.ContractABI.ContractEvent> events = res.getManifest().getAbi().getEvents();
        assertThat(events, hasSize(1));
        assertThat(events.get(0).getName(), is("onCall"));
        assertThat(events.get(0).getParameters(), hasSize(1));
        assertThat(events.get(0).getParameters().get(0).getType(), is(ContractParameterType.HASH160));
    }

    static class FireEventWithMethodCallThatCallsDevpackMethod {

        public static Hash160 value = StringLiteralHelper.addressToScriptHash("NXq2KbEeaSGaKcjkMgErcpWspGZqkSTWVA");

        private static Hash160 contractOwner() {
            return value;
        }

        @DisplayName("onCall")
        public static Event1Arg<Hash160> onCall;

        public static void fireEvent() {
            onCall.fire(contractOwner());
        }
    }

}
