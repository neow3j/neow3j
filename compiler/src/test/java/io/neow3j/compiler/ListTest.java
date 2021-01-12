package io.neow3j.compiler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.DebugInfo.Method;
import io.neow3j.devpack.List;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import java.io.IOException;
import org.junit.Test;

public class ListTest {

    @Test
    public void listsShouldBeRepresentedAsArraysInTheContractManifest() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(ListTestContract.class.getName());
        java.util.List<ContractMethod> methods = unit.getManifest().getAbi().getMethods();

        ContractMethod method = methods.stream().filter(m -> m.getName().equals("returnList"))
                .findFirst().get();
        assertThat(method.getReturnType(), is(ContractParameterType.ARRAY));

        method = methods.stream().filter(m -> m.getName().equals("listAsParameter"))
                .findFirst().get();
        assertThat(method.getParameters().get(0).getParamType(), is(ContractParameterType.ARRAY));
        assertThat(method.getParameters().get(1).getParamType(), is(ContractParameterType.ARRAY));

        java.util.List<ContractEvent> events = unit.getManifest().getAbi().getEvents();
        assertThat(events.get(0).getName(), is("event"));
        assertThat(events.get(0).getParameters().get(0).getParamType(),
                is(ContractParameterType.ARRAY));
        assertThat(events.get(0).getParameters().get(1).getParamType(),
                is(ContractParameterType.ARRAY));
    }

    @Test
    public void listsShouldBeRepresentedAsArraysInTheDebugInfo() throws IOException {
        CompilationUnit unit = new Compiler().compileClass(ListTestContract.class.getName(),
                "/path/to/src/file/io/neow3j/compiler/ListTest$ListTestContract.java");
        java.util.List<Method> methods = unit.getDebugInfo().getMethods();
        DebugInfo.Method method = methods.stream().filter(m -> m.getName().contains("returnList"))
                .findFirst().get();
        assertThat(method.getReturnType(), is(StackItemType.ARRAY.jsonValue()));

        method = methods.stream().filter(m -> m.getName().contains("listAsParameter"))
                .findFirst().get();
        assertThat(method.getParams().get(0), containsString(StackItemType.ARRAY.jsonValue()));
        assertThat(method.getParams().get(1), containsString(StackItemType.ARRAY.jsonValue()));

        java.util.List<DebugInfo.Event> events = unit.getDebugInfo().getEvents();
        assertThat(events.get(0).getParams().get(0),
                containsString(StackItemType.ARRAY.jsonValue()));
        assertThat(events.get(0).getParams().get(1),
                containsString(StackItemType.ARRAY.jsonValue()));
    }

    static class ListTestContract {

        private static Event2Args<List<Byte>, List<Integer>> event;

        public static List<Byte> returnList() {
            return new List<>();
        }

        public static boolean listAsParameter(List<Byte> list1, List<Integer> list2) {
            return true;
        }

    }
}
