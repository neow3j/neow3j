package io.neow3j.compiler;

import io.neow3j.devpack.annotations.EventParameterNames;
import io.neow3j.types.ContractParameter;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import static io.neow3j.compiler.AsmHelper.extractTypeParametersFromSignature;
import static java.lang.String.format;

public class NeoEvent {

    private final static String PARAM_NAME_PREFIX = "arg";

    private final String displayName;
    private final List<Param> params;
    private final String namespace;
    private final FieldNode asmVariable;

    public NeoEvent(FieldNode asmVariable, ClassNode owner) {
        Optional<AnnotationNode> displayNameAnnOpt = AsmHelper.getAnnotationNode(asmVariable, DisplayName.class);
        if (displayNameAnnOpt.isPresent()) {
            displayName = (String) displayNameAnnOpt.get().values.get(1);
        } else {
            displayName = asmVariable.name;
        }

        Optional<AnnotationNode> paramNamesAnnOpt = AsmHelper.getAnnotationNode(asmVariable, EventParameterNames.class);
        if (paramNamesAnnOpt.isPresent()) {
            List<Object> eventParamNameValues = paramNamesAnnOpt.get().values;
            assert eventParamNameValues.size() >= 2 : "The EventParameterNames annotation should never have less than" +
                    " 2 entries.";
            ArrayList paramNames = (ArrayList) eventParamNameValues.get(1);

            AtomicInteger paramNr = new AtomicInteger(0);
            params = extractTypeParametersFromSignature(asmVariable).stream()
                    .map(t -> {
                        // Check if there are enough parameter names provided for the event.
                        if (paramNames.size() < paramNr.get() + 1) {
                            throw new CompilerException(format("Not enough parameter names provided for event %s.",
                                    displayName));
                        }
                        return new Param((String) paramNames.get(paramNr.getAndIncrement()), Type.getType(t));
                    })
                    .collect(Collectors.toList());
            // After the lambda, all parameter names should have been used, i.e., the atomic int paramNr should now be
            // equal to the number of parameters.
            if (paramNames.size() != paramNr.get()) {
                throw new CompilerException(format("Too many parameter names provided for event %s.", displayName));
            }
        } else {
            // Add the parameters with default names based on their position in the parameter list.
            AtomicInteger argNr = new AtomicInteger(1);
            params = extractTypeParametersFromSignature(asmVariable).stream()
                    .map(t -> new Param(PARAM_NAME_PREFIX + argNr.getAndIncrement(), Type.getType(t)))
                    .collect(Collectors.toList());
        }

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
