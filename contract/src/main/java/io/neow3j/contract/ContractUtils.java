package io.neow3j.contract;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class ContractUtils {

    static final String MANIFEST_FILENAME_SUFFIX = "manifest.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public static String generateContractManifestFile(
            ContractManifest manifest, File destinationDirectory) throws IOException {

        String fileName = getContractManifestFilename(manifest);
        return generateContractManifestFile(manifest, fileName, destinationDirectory);
    }

    public static String generateContractManifestFile(
            ContractManifest manifest, String fileName, File destinationDirectory)
            throws IOException {

        File destination = new File(destinationDirectory, fileName);
        objectMapper.writeValue(destination, manifest);

        return destination.getAbsolutePath();
    }

    public static ContractManifest loadContractManifestFile(String absoluteFilePath)
            throws IOException {
        return objectMapper.readValue(new FileInputStream(absoluteFilePath),
                ContractManifest.class);
    }

    public static String getContractManifestFilename(ContractManifest manifest) {
        if (manifest.getName() != null && !manifest.getName().equals("")) {
            return manifest.getName() + "." + MANIFEST_FILENAME_SUFFIX;
        }
        return MANIFEST_FILENAME_SUFFIX;
    }
}
