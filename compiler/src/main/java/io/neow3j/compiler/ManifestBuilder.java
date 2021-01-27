package io.neow3j.compiler;

import static io.neow3j.compiler.AsmHelper.getAnnotationNode;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.ManifestExtra.ManifestExtras;
import io.neow3j.devpack.annotations.Safe;
import io.neow3j.devpack.annotations.SupportedStandards;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractGroup;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import io.neow3j.utils.ClassUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

/**
 * Contains all functionality required to build a contract manifest from a compilation unit.
 */
public class ManifestBuilder {

    public static ContractManifest buildManifest(CompilationUnit compUnit) {
        Optional<AnnotationNode> annotationNode = getAnnotationNode(
                compUnit.getContractClass(), DisplayName.class);
        String name = ClassUtils.getClassNameForInternalName(compUnit.getContractClass().name);
        if (annotationNode.isPresent()) {
            name = (String) annotationNode.get().values.get(1);
        }
        Map<String, String> extras = buildManifestExtra(compUnit.getContractClass());
        List<String> supportedStandards = buildSupportedStandards(compUnit.getContractClass());
        ContractABI abi = buildABI(compUnit.getNeoModule());
        // TODO: Fill the remaining manifest fields below.
        List<ContractGroup> groups = new ArrayList<>();
        List<ContractPermission> permissions = Arrays.asList(
                new ContractPermission("*", Arrays.asList("*")));
        List<String> trusts = new ArrayList<>();
        return new ContractManifest(name, groups, supportedStandards, abi, permissions, trusts,
                extras);
    }

    private static ContractABI buildABI(NeoModule neoModule) {
        List<ContractEvent> events = neoModule.getEvents().stream()
                .map(NeoEvent::getAsContractManifestEvent)
                .collect(Collectors.toList());

        List<ContractMethod> methods = new ArrayList<>();
        for (NeoMethod neoMethod : neoModule.getSortedMethods()) {
            if (!neoMethod.isAbiMethod()) {
                // TODO: This needs to change when enabling inheritance.
                continue; // Only add methods to the ABI that appear in the contract itself.
            }
            List<ContractParameter> contractParams = new ArrayList<>();
            for (NeoVariable var : neoMethod.getParametersByNeoIndex().values()) {
                contractParams.add(new ContractParameter(var.getName(),
                        Compiler.mapTypeToParameterType(Type.getType(var.getDescriptor())), null));
            }
            ContractParameterType paramType = Compiler.mapTypeToParameterType(
                    Type.getMethodType(neoMethod.getAsmMethod().desc).getReturnType());
            boolean isSafe = getAnnotationNode(neoMethod.getAsmMethod(), Safe.class).isPresent();
            methods.add(new ContractMethod(neoMethod.getName(), contractParams,
                    neoMethod.getStartAddress(), paramType, isSafe));
        }
        return new ContractABI(methods, events);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> buildManifestExtra(ClassNode classNode) {
        List<AnnotationNode> annotations = new ArrayList<>();
        // First check if multiple @ManifestExtra where added to the contract. In this case the
        // expected annotation is a @ManifestExtras (plural).
        Optional<AnnotationNode> annotation = getAnnotationNode(classNode,
                ManifestExtras.class);
        if (annotation.isPresent()) {
            annotations = (List<AnnotationNode>) annotation.get().values.get(1);
        } else {
            // If there is no @ManifestExtras, there could still be a single @ManifestExtra.
            annotation = getAnnotationNode(classNode, ManifestExtra.class);
            if (annotation.isPresent()) {
                annotations.add(annotation.get());
            }
        }
        Map<String, String> extras = new HashMap<>();
        for (AnnotationNode node : annotations) {
            int i = node.values.indexOf("key");
            String key = (String) node.values.get(i + 1);
            i = node.values.indexOf("value");
            String value = (String) node.values.get(i + 1);
            extras.put(key, value);
        }

        return extras;
    }

    private static List<String> buildSupportedStandards(ClassNode asmClass) {
        Optional<AnnotationNode> annotationNode = getAnnotationNode(asmClass,
                SupportedStandards.class);
        if (!annotationNode.isPresent()) {
            return new ArrayList<>();
        }
        AnnotationNode ann = annotationNode.get();
        List<String> standards = new ArrayList<>();
        for (Object standard : (List<?>) ann.values.get(1)) {
            standards.add((String) standard);
        }
        return standards;
    }

}
