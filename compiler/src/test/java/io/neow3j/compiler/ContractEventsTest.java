package io.neow3j.compiler;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.compiler.DebugInfo.Event;
import io.neow3j.compiler.sourcelookup.MockSourceContainer;
import io.neow3j.types.ContractParameter;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.devpack.events.Event5Args;
import io.neow3j.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class ContractEventsTest {

    @Test
    public void gettersOnANewEventShouldReturnExpectedValues() {
        String variableName = "onTransfer";
        String ownerClassName = "io.MyClass";
        String displayName = "transfer";
        String id = ownerClassName + "#" + variableName;

        FieldNode fieldNode = new FieldNode(0, variableName,
                "Lio/neow3j/devpack/events/Event2Args;",
                "Lio/neow3j/devpack/events/Event2Args<Ljava/lang/Integer;Ljava/lang/String;>;",
                null);
        AnnotationNode annNode = new AnnotationNode("Lio/neow3j/devpack/annotations/DisplayName;");
        annNode.values = new ArrayList<>();
        annNode.values.add("value");
        annNode.values.add(displayName);
        List<AnnotationNode> annotations = new ArrayList<>();
        annotations.add(annNode);
        fieldNode.invisibleAnnotations = annotations;

        ClassNode classNode = new ClassNode();
        classNode.name = ownerClassName;

        NeoEvent event = new NeoEvent(fieldNode, classNode);

        assertThat(event.getDisplayName(), is(displayName));
        assertThat(event.getId(), is(id));
        assertThat(event.getAsmVariable(), is(fieldNode));

        assertThat(event.getAsContractManifestEvent().getName(), is(displayName));
        assertThat(event.getAsContractManifestEvent().getParameters(), hasSize(2));
        assertThat(event.getAsContractManifestEvent().getParameters().get(0),
                is(new ContractParameter("arg1", ContractParameterType.INTEGER, null)));
        assertThat(event.getAsContractManifestEvent().getParameters().get(1),
                is(new ContractParameter("arg2", ContractParameterType.STRING, null)));

        assertThat(event.getAsDebugInfoEvent().getName(), is("MyClass," + displayName));
        assertThat(event.getAsDebugInfoEvent().getId(), is(id));
        assertThat(event.getAsDebugInfoEvent().getParams(), hasSize(2));
        assertThat(event.getAsDebugInfoEvent().getParams().get(0), is("arg1,Integer"));
        assertThat(event.getAsDebugInfoEvent().getParams().get(1), is("arg2,String"));
    }

    @Test
    public void newEventWithoutDisplayNameAnnotationShouldHaveVariableNameSetAsDisplayName() {
        String variableName = "onTransfer";
        String ownerClassName = "io.MyClass";
        String id = ownerClassName + "#" + variableName;

        FieldNode fieldNode = new FieldNode(0, variableName,
                "Lio/neow3j/devpack/events/Event2Args;",
                "Lio/neow3j/devpack/events/Event2Args<Ljava/lang/Integer;Ljava/lang/String;>;",
                null);

        ClassNode classNode = new ClassNode();
        classNode.name = ownerClassName;

        NeoEvent event = new NeoEvent(fieldNode, classNode);

        assertThat(event.getDisplayName(), is(variableName));
        assertThat(event.getId(), is(id));
        assertThat(event.getAsmVariable(), is(fieldNode));

        assertThat(event.getAsContractManifestEvent().getName(), is(variableName));
        assertThat(event.getAsDebugInfoEvent().getName(), is("MyClass," + variableName));
    }

    @Test
    public void eventNamesAndParametersShouldBeSetCorrectlyInManifest() throws IOException {
        CompilationUnit res = new Compiler().compile(ContractEventsTestContract.class.getName());

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
        CompilationUnit res = new Compiler().compile(ContractEventsTestContract.class.getName(),
                asList(new MockSourceContainer(new File("/path/to/src/file/SourceFile.java"))));

        List<Event> debugInfoEvents = res.getDebugInfo().getEvents();
        String fqClassName = ContractEventsTestContract.class.getName();
        String shortName = fqClassName.substring(fqClassName.lastIndexOf('.') + 1);

        assertThat(debugInfoEvents.get(0).getName(), is(shortName + ",event1"));
        assertThat(debugInfoEvents.get(0).getId(), is(fqClassName + "#event1"));
        String a1 = "arg1,String";
        String a2 = "arg2,Integer";
        assertThat(debugInfoEvents.get(0).getParams(), contains(a1, a2));

        assertThat(debugInfoEvents.get(1).getName(), is(shortName + ",displayName"));
        assertThat(debugInfoEvents.get(1).getId(), is(fqClassName + "#event2"));
        a1 = "arg1,String";
        a2 = "arg2,Integer";
        String a3 = "arg3,Boolean";
        String a4 = "arg4,String";
        String a5 = "arg5,Any";
        assertThat(debugInfoEvents.get(1).getParams(), contains(a1, a2, a3, a4, a5));
    }

    public static class ContractEventsTestContract {

        private static Event2Args<String, Integer> event1;

        @DisplayName("displayName")
        private static Event5Args<String, Integer, Boolean, String, Object> event2;

        public static void main() {
            event1.fire("notification", 0);
            event2.fire("notification", 0, false, "notification", "notification");
        }
    }
}
