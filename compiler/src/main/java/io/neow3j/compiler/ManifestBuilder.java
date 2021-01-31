package io.neow3j.compiler;

import static io.neow3j.compiler.AsmHelper.getAnnotationNode;
import static io.neow3j.compiler.AsmHelper.hasAnnotations;
import static java.util.Optional.ofNullable;

import io.neow3j.contract.ContractParameter;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.Group;
import io.neow3j.devpack.annotations.Group.Groups;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.ManifestExtra.ManifestExtras;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.annotations.Permission.Permissions;
import io.neow3j.devpack.annotations.Safe;
import io.neow3j.devpack.annotations.SupportedStandards;
import io.neow3j.devpack.annotations.Trust;
import io.neow3j.devpack.annotations.Trust.Trusts;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractGroup;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import io.neow3j.utils.ClassUtils;
import java.util.ArrayList;
import java.util.Collections;
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
@SuppressWarnings("unchecked")
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

        List<ContractGroup> groups = buildGroups(compUnit.getContractClass());
        List<ContractPermission> permissions = buildPermissions(compUnit.getContractClass());
        List<String> trusts = buildTrusts(compUnit.getContractClass());

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
            boolean isSafe = hasAnnotations(neoMethod.getAsmMethod(), Safe.class);
            methods.add(new ContractMethod(neoMethod.getName(), contractParams,
                    neoMethod.getStartAddress(), paramType, isSafe));
        }
        return new ContractABI(methods, events);
    }

    private static Map<String, String> buildManifestExtra(ClassNode classNode) {
        List<AnnotationNode> annotations = checkForSingleOrMultipleAnnotations(classNode,
                ManifestExtras.class, ManifestExtra.class);

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
        return getAnnotationNode(asmClass, SupportedStandards.class)
                .flatMap(ManifestBuilder::transformAnnotationNodeStringValue)
                .orElse(new ArrayList<>());
    }

    private static List<ContractGroup> buildGroups(ClassNode asmClass) {
        return checkForSingleOrMultipleAnnotations(asmClass, Groups.class, Group.class)
                .stream()
                .map(ManifestBuilder::getContractGroup)
                .collect(Collectors.toList());
    }

    public static List<ContractPermission> buildPermissions(ClassNode asmClass) {
        List<ContractPermission> permissions = checkForSingleOrMultipleAnnotations(asmClass,
                Permissions.class, Permission.class)
                .stream()
                .map(ManifestBuilder::getContractPermission)
                .collect(Collectors.toList());

        if (permissions.isEmpty()) {
            ContractPermission contractPermission = new ContractPermission("*",
                    Collections.singletonList("*"));
            return Collections.singletonList(contractPermission);
        } else {
            return permissions;
        }
    }

    private static List<String> buildTrusts(ClassNode asmClass) {
        return checkForSingleOrMultipleAnnotations(asmClass,
                Trusts.class, Trust.class)
                .stream()
                .map(ManifestBuilder::getContractTrust)
                .collect(Collectors.toList());
    }

    private static Optional<List<String>> transformAnnotationNodeStringValue(
            AnnotationNode annotationNode) {

        return ofNullable(annotationNode)
                .map(ann -> {
                    List<String> values = new ArrayList<>();
                    for (Object value : (List<?>) ann.values.get(1)) {
                        values.add((String) value);
                    }
                    return values;
                });
    }

    private static ContractPermission getContractPermission(AnnotationNode ann) {
        int i = ann.values.indexOf("contract");
        String contract = (String) ann.values.get(i + 1);

        i = ann.values.indexOf("methods");
        List<String> methods = new ArrayList<>();
        // if 'methods' is not found, it means we need to add a "wildcard"
        // to that manifest
        if (i < 0) {
            methods.add("*");
        } else {
            List<?> methodsValues = (List<?>) ann.values.get(i + 1);
            // this is required since we want to create an ArrayList of new
            // String objects, and not rely on what ASM provides us
            methods.addAll((List<String>) methodsValues);
        }

        return new ContractPermission(contract, methods);
    }

    private static ContractGroup getContractGroup(AnnotationNode ann) {
        int i = ann.values.indexOf("pubKey");
        String pubkey = (String) ann.values.get(i + 1);
        i = ann.values.indexOf("signature");
        String signature = (String) ann.values.get(i + 1);
        return new ContractGroup(pubkey, signature);
    }

    private static String getContractTrust(AnnotationNode ann) {
        int i = ann.values.indexOf("value");
        return (String) ann.values.get(i + 1);
    }

    private static List<AnnotationNode> checkForSingleOrMultipleAnnotations(ClassNode asmClass,
            Class<?> multipleAnnotationType, Class<?> singleAnnotationType) {

        Optional<AnnotationNode> annotation = getAnnotationNode(asmClass,
                multipleAnnotationType);

        return annotation
                .map(a -> (List<AnnotationNode>) a.values.get(1))
                .orElseGet(() -> {
                    // For example:
                    // If there is no @ManifestExtras, there could still be a single @ManifestExtra.
                    // We check for this, here.
                    Optional<AnnotationNode> ann = getAnnotationNode(asmClass,
                            singleAnnotationType);
                    List<AnnotationNode> annotations = new ArrayList<>();
                    ann.map(annotations::add);
                    return annotations;
                });
    }

}
