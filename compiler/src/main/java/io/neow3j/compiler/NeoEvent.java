package io.neow3j.compiler;

import io.neow3j.types.ContractParameter;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.utils.ClassUtils;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import static io.neow3j.compiler.AsmHelper.extractTypeParametersFromSignature;

public class NeoEvent {

    private final static String PARAM_NAME_PREFIX = "arg";

    private final String displayName;
    private final List<Param> params;
    private final String namespace;
    private final FieldNode asmVariable;

    public NeoEvent(FieldNode asmVariable, ClassNode owner) {
        Optional<AnnotationNode> annOpt = AsmHelper.getAnnotationNode(asmVariable, DisplayName.class);
        if (annOpt.isPresent()) {
            displayName = (String) annOpt.get().values.get(1);
        } else {
            displayName = asmVariable.name;
        }

        AtomicInteger argNr = new AtomicInteger(1);
        params = extractTypeParametersFromSignature(asmVariable).stream()
                .map(t -> new Param(PARAM_NAME_PREFIX + argNr.getAndIncrement(), Type.getType(t)))
                .collect(Collectors.toList());

        namespace = ClassUtils.getFullyQualifiedNameForInternalName(owner.name);
        this.asmVariable = asmVariable;
    }

    public FieldNode getAsmVariable() {
        return asmVariable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return namespace + "#" + asmVariable.name;
    }

    public int getNumberOfParams() {
        return params.size();
    }

    public ContractEvent getAsContractManifestEvent() {
        List<ContractParameter> contractParameters = params.stream()
                .map(Param::getAsContractParameter)
                .collect(Collectors.toList());
        return new ContractEvent(displayName, contractParameters);
    }

    public DebugInfo.Event getAsDebugInfoEvent() {
        List<String> contractParameters = params.stream()
                .map(Param::getAsStringForDebugInfo)
                .collect(Collectors.toList());
        String name = namespace.substring(namespace.lastIndexOf(".") + 1) + "," + displayName;
        return new DebugInfo.Event(getId(), name, contractParameters);
    }

    private static class Param {

        private final String name;
        private final Type type;

        private Param(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        private String getAsStringForDebugInfo() {
            return name + "," + Compiler.mapTypeToParameterType(type).jsonValue();
        }

        private ContractParameter getAsContractParameter() {
            return new ContractParameter(name, Compiler.mapTypeToParameterType(type));
        }

    }

}
