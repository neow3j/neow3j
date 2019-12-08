package io.neow3j.contract.abi;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.neow3j.contract.abi.exceptions.NEP3Exception;
import io.neow3j.contract.abi.exceptions.NEP3ParsingException;
import io.neow3j.contract.abi.model.NeoContractInterface;
import io.neow3j.utils.Numeric;
import io.neow3j.utils.Strings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility functions for working with ABI files,
 * based on NEP-3 (https://github.com/neo-project/proposals/blob/master/nep-3.mediawiki).
 */
public class NeoABIUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();

    static {
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        objectMapper.setDefaultPrettyPrinter(prettyPrinter);
    }

    public static NeoContractInterface loadABIFile(String absoluteFileName) throws NEP3Exception {
        return loadABIFile(new File(absoluteFileName));
    }

    public static NeoContractInterface loadABIFile(File source) throws NEP3Exception {
        try {
            return objectMapper.readValue(source, NeoContractInterface.class);
        } catch (Exception e) {
            throw new NEP3ParsingException("Could not load the ABI file in the parsing process.", e);
        }
    }

    public static NeoContractInterface loadABIFile(InputStream source) throws NEP3Exception {
        try {
            return objectMapper.readValue(source, NeoContractInterface.class);
        } catch (Exception e) {
            throw new NEP3ParsingException("Could not load the ABI file in the parsing process.", e);
        }
    }

    public static String generateNeoContractInterface(NeoContractInterface neoContractInfo, File destinationDirectory)
            throws IOException {

        String fileName = getABIFileName(neoContractInfo);
        File destination = new File(destinationDirectory, fileName);

        objectMapper.writeValue(destination, neoContractInfo);

        return fileName;
    }

    private static String getABIFileName(NeoContractInterface neoContractInfo) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                "'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        String abiName = "neow3j";
        if (Strings.isEmpty(neoContractInfo.getHash())) {
            abiName = Numeric.cleanHexPrefix(neoContractInfo.getHash());
        }
        return now.format(format) + abiName + ".abi";
    }

}
