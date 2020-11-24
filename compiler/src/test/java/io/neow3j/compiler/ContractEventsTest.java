package io.neow3j.compiler;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.DebugInfo.Event;
import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.devpack.events.Event5Args;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class ContractEventsTest {

    @Test
    public void eventNamesAndParametersShouldBeSetCorrectlyInManifest() throws IOException {
        CompilationUnit res = new Compiler().compileClass(ContractEvents.class.getName(), "ignore");

        List<ContractEvent> manifestEvents = res.getManifest().getAbi().getEvents();
        assertThat(manifestEvents.get(0).getName(), is("event1"));
        ContractParameter arg1 = new ContractParameter("arg1", ContractParameterType.STRING, null);
        ContractParameter arg2 = new ContractParameter("arg2", ContractParameterType.INTEGER, null);
        assertThat(manifestEvents.get(0).getParameters(), contains(arg1, arg2));

        assertThat(manifestEvents.get(1).getName(), is("displayName"));
        arg1 = new ContractParameter("arg1", ContractParameterType.STRING, null);
        arg2 = new ContractParameter("arg2", ContractParameterType.INTEGER, null);
        ContractParameter arg3 = new ContractParameter("arg3", ContractParameterType.BOOLEAN, null);
        ContractParameter arg4 = new ContractParameter("arg4", ContractParameterType.STRING, null);
        ContractParameter arg5 = new ContractParameter("arg5", ContractParameterType.ANY, null);
        assertThat(manifestEvents.get(1).getParameters(), contains(arg1, arg2, arg3, arg4, arg5));
    }

    @Test
    public void eventNamesAndParametersShouldBeSetCorrectlyInDebugInfo() throws IOException {
        CompilationUnit res = new Compiler().compileClass(ContractEvents.class.getName(), "ignore");

        List<Event> debugInfoEvents = res.getDebugInfo().getEvents();
        assertThat(debugInfoEvents.get(0).getName(), is("EventNames,event1"));
        // TODO: What should the ID be?
        assertThat(debugInfoEvents.get(0).getId(), is("EventNames,event1"));
        String a1 = "arg1,String";
        String a2 = "arg2,Integer";
        assertThat(debugInfoEvents.get(0).getParams(), contains(a1, a2));

        assertThat(debugInfoEvents.get(1).getName(), is("EventNames,displayName"));
        // TODO: What should the ID be?
        assertThat(debugInfoEvents.get(1).getId(), is("EventNames,event1"));
        a1 = "arg1,String";
        a2 = "arg2,Integer";
        String a3 = "arg3,Boolean";
        String a4 = "arg4,String";
        String a5 = "arg5,Any";
        assertThat(debugInfoEvents.get(1).getParams(), contains(a1, a2, a3, a4, a5));
    }

    static class ContractEvents {

        Event2Args<String, Integer> event1;

        @DisplayName("displayName")
        Event5Args<String, Integer, Boolean, String, Object> event2;

        public static boolean main() {
            return true;
        }
    }

}
