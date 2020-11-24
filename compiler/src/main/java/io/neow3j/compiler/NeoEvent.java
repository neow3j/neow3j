package io.neow3j.compiler;

import io.neow3j.contract.ContractParameter;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.utils.ClassUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class NeoEvent {

    private String displayName;
    private List<Param> params = new ArrayList<>();
    private String namespace;
    private FieldNode asmVariable;

    public NeoEvent(FieldNode asmVariable, ClassNode owner) {
        // TODO: Extract the display name.
        // TODO: Extract the parameter types.
        namespace = ClassUtils.getFullyQualifiedNameForInternalName(owner.name);
        this.asmVariable = asmVariable;
    }

    public String getId() {
        return namespace + "#" + asmVariable.name;
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
        return new DebugInfo.Event(getId(), displayName, contractParameters);
    }

    private static class Param {

        private final String name;
        private final Type type;

        Param(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        String getAsStringForDebugInfo() {
            return name + "," + Compiler.mapTypeToParameterType(type).jsonValue();
        }

        ContractParameter getAsContractParameter() {
            return new ContractParameter(name, Compiler.mapTypeToParameterType(type), null);
        }

    }

}
