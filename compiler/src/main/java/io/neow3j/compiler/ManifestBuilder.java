package io.neow3j.compiler;

import static java.lang.String.format;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.devpack.annotations.Features;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.ManifestExtra.ManifestExtras;
import io.neow3j.devpack.annotations.SupportedStandards;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractFeatures;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractGroup;
import io.neow3j.protocol.core.methods.response.ContractManifest.ContractPermission;
import io.neow3j.utils.Numeric;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

/**
 * Contains all functionality required to build a contract manifest from a compilation unit.
 */
public class ManifestBuilder {

    public static ContractManifest buildManifest(CompilationUnit compUnit, ScriptHash scriptHash) {
        List<ContractGroup> groups = new ArrayList<>();
        ContractFeatures features = new ContractFeatures(false, false);
        Map<String, String> extras = new HashMap<>();
        List<String> supportedStandards = new ArrayList<>();
        Optional<ClassNode> annotatedClass = getClassWithAnnotations(compUnit.getContractClasses());
        if (annotatedClass.isPresent()) {
            features = buildContractFeatures(annotatedClass.get());
            extras = buildManifestExtra(annotatedClass.get());
            supportedStandards = buildSupportedStandards(annotatedClass.get());
        }
        ContractABI abi = buildABI(compUnit.getNeoModule(), scriptHash);
        // TODO: Fill the remaining manifest fields below.
        List<ContractPermission> permissions = Arrays.asList(
                new ContractPermission("*", Arrays.asList("*")));
        List<String> trusts = new ArrayList<>();
        List<String> safeMethods = new ArrayList<>();
        return new ContractManifest(groups, features, supportedStandards, abi, permissions, trusts,
                safeMethods, extras);
    }

    // Throws an exception if multiple classes have the contract annotations.
    private static Optional<ClassNode> getClassWithAnnotations(Set<ClassNode> asmClasses) {
        Optional<ClassNode> annotatedClass = Optional.empty();
        for (ClassNode asmClass : asmClasses) {
            if (AsmHelper.hasAnnotations(asmClass, Features.class, ManifestExtra.class,
                    ManifestExtras.class, SupportedStandards.class)) {
                if (annotatedClass.isPresent()) {
                    throw new CompilerException(format("Make sure that the annotations %s, %s and "
                                    + "%s are only used on one class of a multi-file contract.",
                            Features.class.getName(), ManifestExtra.class.getName(),
                            SupportedStandards.class.getName()));
                }
                annotatedClass = Optional.of(asmClass);
            }
        }
        return annotatedClass;
    }

    private static ContractABI buildABI(NeoModule neoModule, ScriptHash scriptHash) {
        List<ContractMethod> methods = new ArrayList<>();
        // TODO: Fill events list.
        List<ContractEvent> events = new ArrayList<>();
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
            methods.add(new ContractMethod(neoMethod.getName(), contractParams, paramType,
                    neoMethod.getStartAddress()));
        }
        return new ContractABI(Numeric.prependHexPrefix(scriptHash.toString()), methods, events);
    }

    private static ContractFeatures buildContractFeatures(ClassNode classNode) {
        Optional<AnnotationNode> opt = classNode.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(Features.class)))
                .findFirst();
        boolean payable = false;
        boolean hasStorage = false;
        if (opt.isPresent()) {
            AnnotationNode ann = opt.get();
            int i = ann.values.indexOf("payable");
            payable = i != -1 && (boolean) ann.values.get(i + 1);
            i = ann.values.indexOf("hasStorage");
            hasStorage = i != -1 && (boolean) ann.values.get(i + 1);
        }
        return new ContractFeatures(hasStorage, payable);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> buildManifestExtra(ClassNode classNode) {
        List<AnnotationNode> annotations = new ArrayList<>();
        // First check if multiple @ManifestExtra where added to the contract. In this case the
        // expected annotation is a @ManifestExtras (plural).
        Optional<AnnotationNode> annotation = classNode.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(ManifestExtras.class)))
                .findFirst();
        if (annotation.isPresent()) {
            annotations = (List<AnnotationNode>) annotation.get().values.get(1);
        } else {
            // If there is no @ManifestExtras, there could still be a single @ManifestExtra.
            annotation = classNode.invisibleAnnotations.stream()
                    .filter(a -> a.desc.equals(Type.getDescriptor(ManifestExtra.class)))
                    .findFirst();
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
        Optional<AnnotationNode> opt = asmClass.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals(Type.getDescriptor(SupportedStandards.class)))
                .findFirst();
        if (!opt.isPresent()) {
            return new ArrayList<>();
        }
        AnnotationNode ann = opt.get();
        List<String> standards = new ArrayList<>();
        for (Object standard : (List<?>) ann.values.get(1)) {
            standards.add((String) standard);
        }
        return standards;
    }

}
