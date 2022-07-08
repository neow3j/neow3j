package io.neow3j.compiler;

import io.neow3j.constants.NeoConstants;
import io.neow3j.crypto.Base64;
import io.neow3j.crypto.ECKeyPair.ECPublicKey;
import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.ManifestExtra.ManifestExtras;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.annotations.Permission.Permissions;
import io.neow3j.devpack.annotations.Safe;
import io.neow3j.devpack.annotations.SupportedStandard;
import io.neow3j.devpack.annotations.SupportedStandard.SupportedStandards;
import io.neow3j.devpack.annotations.Trust;
import io.neow3j.devpack.annotations.Trust.Trusts;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.constants.NeoStandard;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractEvent;
import io.neow3j.protocol.core.response.ContractManifest.ContractABI.ContractMethod;
import io.neow3j.protocol.core.response.ContractManifest.ContractPermission;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.types.Hash160;
import io.neow3j.utils.ClassUtils;
import io.neow3j.utils.Numeric;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.neow3j.compiler.AsmHelper.getAnnotationNode;
import static io.neow3j.compiler.AsmHelper.hasAnnotations;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Contains all functionality required to build a contract manifest from a compilation unit.
 */
@SuppressWarnings("unchecked")
public class ManifestBuilder {

    public static ContractManifest buildManifest(CompilationUnit compUnit) {
        Optional<AnnotationNode> annotationNode = getAnnotationNode(compUnit.getContractClass(), DisplayName.class);
        String name = ClassUtils.getClassNameForInternalName(compUnit.getContractClass().name);
        if (annotationNode.isPresent()) {
            name = (String) annotationNode.get().values.get(1);
        }
        List<String> supportedStandards = buildSupportedStandards(compUnit.getContractClass());
        ContractABI abi = buildABI(compUnit.getNeoModule());
        List<ContractPermission> permissions = buildPermissions(compUnit.getContractClass());
        List<String> trusts = buildTrusts(compUnit.getContractClass());
        Map<String, String> extras = buildManifestExtra(compUnit.getContractClass());

        return new ContractManifest(name, null, null, supportedStandards, abi, permissions, trusts, extras);
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
                        Compiler.mapTypeToParameterType(Type.getType(var.getDescriptor()))));
            }
            ContractParameterType paramType = Compiler.mapTypeToParameterType(
                    Type.getMethodType(neoMethod.getAsmMethod().desc).getReturnType());
            boolean isSafe = hasAnnotations(neoMethod.getAsmMethod(), Safe.class);
            methods.add(new ContractMethod(neoMethod.getName(), contractParams, neoMethod.getStartAddress(),
                    paramType, isSafe));
        }
        return new ContractABI(methods, events);
    }

    private static Map<String, String> buildManifestExtra(ClassNode classNode) {
        List<AnnotationNode> annotations = checkForSingleOrMultipleAnnotations(classNode, ManifestExtras.class,
                ManifestExtra.class);

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
        return checkForSingleOrMultipleAnnotations(asmClass, SupportedStandards.class, SupportedStandard.class)
                .stream()
                .map(ManifestBuilder::getSupportedStandard)
                .collect(Collectors.toList());
    }

    private static List<ContractPermission> buildPermissions(ClassNode asmClass) {
        List<ContractPermission> permissions = checkForSingleOrMultipleAnnotations(asmClass, Permissions.class,
                Permission.class)
                .stream()
                .map(ManifestBuilder::getContractPermission)
                .collect(Collectors.toList());

        if (permissions.isEmpty()) {
            return new ArrayList<>();
        }
        return permissions;
    }

    private static List<String> buildTrusts(ClassNode asmClass) {
        return checkForSingleOrMultipleAnnotations(asmClass, Trusts.class, Trust.class)
                .stream()
                .map(ManifestBuilder::getContractTrust)
                .collect(Collectors.toList());
    }

    private static String getSupportedStandard(AnnotationNode ann) {
        int neoStandardIndex = ann.values.indexOf("neoStandard");
        int customStandardIndex = ann.values.indexOf("customStandard");
        boolean bothPresent = neoStandardIndex != -1 && customStandardIndex != -1;
        if (bothPresent) {
            throw new CompilerException("A @SupportedStandard annotation must only have one of the attributes " +
                    "'neoStandard' or 'customStandard' set.");
        }
        if (neoStandardIndex != -1) {
            NeoStandard neoStandard = NeoStandard.valueOf(
                    asList((String[]) ann.values.get(neoStandardIndex + 1)).get(1));
            return neoStandard.getStandard();
        }
        return (String) ann.values.get(customStandardIndex + 1);
    }

    private static ContractPermission getContractPermission(AnnotationNode ann) {
        String hashOrPubKey = getHashOrPubKey(ann);
        int i = ann.values.indexOf("methods");
        List<String> methods = new ArrayList<>();
        // if 'methods' is not found, it means we need to add a "wildcard" to that manifest
        if (i < 0) {
            methods.add("*");
        } else {
            List<?> methodsValues = (List<?>) ann.values.get(i + 1);
            // this is required since we want to create an ArrayList of new String objects, and not rely on what ASM
            // provides us
            methods.addAll((List<String>) methodsValues);
        }

        return new ContractPermission(hashOrPubKey, methods);
    }

    private static String getContractTrust(AnnotationNode ann) {
        return getHashOrPubKey(ann);
    }

    // Retrieves the hash or public key from annotations which allow both fields 'contract' and 'nativeContract'.
    private static String getHashOrPubKey(AnnotationNode ann) {
        if (ann.values == null) {
            throw new CompilerException("This annotation requires either the attribute 'contract' or 'nativeContract'" +
                    " to be set.");
        }
        int contractIndex = ann.values.indexOf("contract");
        int nativeContractIndex = ann.values.indexOf("nativeContract");
        boolean bothPresent = contractIndex != -1 && nativeContractIndex != -1;
        boolean bothAbsent = contractIndex == nativeContractIndex;
        if (bothPresent || bothAbsent) {
            throw new CompilerException("A @Permission or @Trust annotation must either have the attribute 'contract'" +
                    " or 'nativeContract' set but not both at the same time.");
        }
        String hashOrPubKey;
        if (contractIndex != -1) {
            hashOrPubKey = (String) ann.values.get(contractIndex + 1);
            throwIfNotValidContractHashOrPubKeyOrWildcard(hashOrPubKey);
            hashOrPubKey = addOrClearHexPrefix(hashOrPubKey);
        } else {
            NativeContract nativeContract = NativeContract.valueOf(
                    asList((String[]) ann.values.get(nativeContractIndex + 1)).get(1));
            throwIfNotValidNativeContract(nativeContract);
            hashOrPubKey = addOrClearHexPrefix(nativeContract.getContractHash().toString());
        }
        return hashOrPubKey;
    }

    /**
     * Adds missing prefixes for {@link Hash160} hashes and removes existing prefixes for public key values.
     *
     * @param hashOrPubKey the hash or public key.
     * @return hash with prefix or public key without prefix.
     */
    private static String addOrClearHexPrefix(String hashOrPubKey) {
        // Contract hashes need a '0x' prefix. Public keys must be without '0x' prefix.
        if (hashOrPubKey.length() == 2 * NeoConstants.HASH160_SIZE) {
            hashOrPubKey = Numeric.prependHexPrefix(hashOrPubKey);
        } else if (hashOrPubKey.length() == 2 * NeoConstants.PUBLIC_KEY_SIZE_COMPRESSED + 2) {
            hashOrPubKey = Numeric.cleanHexPrefix(hashOrPubKey);
        }
        return hashOrPubKey;
    }

    private static void throwIfNotValidNativeContract(NativeContract nativeContract) {
        if (nativeContract == NativeContract.None) {
            throw new CompilerException("The provided native contract does not exist. The None value exists for the " +
                    "sole purpose to serve as an internal default value and is not meant to be used.");
        }
    }

    private static void throwIfNotValidSignature(String signature) {
        try {
            Base64.decode(signature);
        } catch (Exception e) {
            throw new CompilerException(
                    format("Invalid signature: %s. Please, add a valid signature in base64 format.", signature)
            );
        }
    }

    private static void throwIfNotValidPubKey(String pubKey) {
        try {
            new ECPublicKey(pubKey);
        } catch (Exception e) {
            throw new CompilerException(format("Invalid public key: %s", pubKey)
            );
        }
    }

    private static void throwIfNotValidContractHash(String contractHash) {
        try {
            new Hash160(contractHash);
        } catch (Exception e) {
            throw new CompilerException(format("Invalid contract hash: %s", contractHash)
            );
        }
    }

    private static void throwIfNotValidContractHashOrPubKeyOrWildcard(String contract) {
        if (contract != null && contract.equals("*")) {
            return;
        }
        Exception notValidContractHash = null;
        try {
            throwIfNotValidContractHash(contract);
        } catch (Exception e) {
            notValidContractHash = e;
        }

        Exception notValidPubKey = null;
        try {
            throwIfNotValidPubKey(contract);
        } catch (Exception e) {
            notValidPubKey = e;
        }

        if (notValidContractHash != null && notValidPubKey != null) {
            // we can't evaluate which one is not valid, so, we raise an exception with a message specifying both.
            throw new CompilerException(format("Invalid contract hash or public key: %s", contract));
        }
    }

    private static List<AnnotationNode> checkForSingleOrMultipleAnnotations(ClassNode asmClass,
            Class<?> multipleAnnotationType, Class<?> singleAnnotationType) {

        Optional<AnnotationNode> annotation = getAnnotationNode(asmClass, multipleAnnotationType);

        return annotation
                .map(a -> (List<AnnotationNode>) a.values.get(1))
                .orElseGet(() -> {
                    // For example:
                    // If there is no @ManifestExtras, there could still be a single @ManifestExtra.
                    // We check for this, here.
                    Optional<AnnotationNode> ann = getAnnotationNode(asmClass, singleAnnotationType);
                    List<AnnotationNode> annotations = new ArrayList<>();
                    ann.map(annotations::add);
                    return annotations;
                });
    }

}
