package io.neow3j.contract;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.neow3j.protocol.core.response.ContractManifest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("unchecked")
public class ContractUtils {

    static final String MANIFEST_FILENAME_SUFFIX = "manifest.json";
    static final String NEF_SUFFIX = ".nef";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    /**
     * Writes given NEF to "{@code <outdir>/<contractName>.nef}".
     *
     * @param nef          the contract NEF file to write.
     * @param contractName the contract's name
     * @param outDir       the directory to which to write to.
     * @return the absolute path of the written file.
     * @throws IOException if an error occurs when writting to file.
     */
    public static String writeNefFile(NefFile nef, String contractName, Path outDir) throws IOException {
        File nefFile = outDir.resolve(contractName + NEF_SUFFIX).toFile();
        try (FileOutputStream outputStream = new FileOutputStream(nefFile)) {
            outputStream.write(nef.toArray());
        }
        return nefFile.getAbsolutePath();
    }

    public static String writeContractManifestFile(ContractManifest manifest, Path outDir) throws IOException {
        String fileName = getContractManifestFilename(manifest);
        return writeContractManifestFile(manifest, fileName, outDir);
    }

    public static String writeContractManifestFile(ContractManifest manifest, String fileName, Path outDir)
            throws IOException {

        File destination = new File(outDir.toString(), fileName);
        objectMapper.writeValue(destination, manifest);

        return destination.getAbsolutePath();
    }

    public static ContractManifest loadContractManifestFile(String absoluteFilePath) throws IOException {
        return objectMapper.readValue(new FileInputStream(absoluteFilePath), ContractManifest.class);
    }

    public static String getContractManifestFilename(ContractManifest manifest) {
        if (manifest.getName() != null && !manifest.getName().equals("")) {
            return getContractManifestFilename(manifest.getName());
        }
        return MANIFEST_FILENAME_SUFFIX;
    }

    public static String getContractManifestFilename(String contractName) {
        return contractName + "." + MANIFEST_FILENAME_SUFFIX;
    }

}
