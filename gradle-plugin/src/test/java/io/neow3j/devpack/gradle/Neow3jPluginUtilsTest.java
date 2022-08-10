package io.neow3j.devpack.gradle;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.compiler.DebugInfo;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.neow3j.devpack.gradle.Neow3jPluginUtils.DEBUG_JSON_SUFFIX;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.NEFDBGNFO_SUFFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

public class Neow3jPluginUtilsTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testWriteToFile() throws IOException {
        Path tempFile = Files.createTempFile("test", "neow3jPlugin");
        byte[] bytesToBeWritten = new byte[]{0x00, 0x01, 0x02};
        Neow3jPluginUtils.writeToFile(tempFile.toFile(), bytesToBeWritten);
        byte[] readBytes = Files.readAllBytes(tempFile);
        assertThat(readBytes, is(bytesToBeWritten));
    }

    @Test
    public void writeDebugInfoZipShouldGenerateCorrectZipFileWithCorrectEntry() throws IOException {
        String contractName = "contract";
        DebugInfo dbgnfo = new DebugInfo();
        Path outDir = Files.createTempDirectory("debuginfo");
        outDir.toFile().deleteOnExit();

        String zipFilePath = Neow3jPluginUtils.writeDebugInfoZip(dbgnfo, contractName, outDir);
        assertThat(zipFilePath,
                is(outDir.toFile().getAbsolutePath() + File.separator + contractName + NEFDBGNFO_SUFFIX));
        ZipFile generatedZipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = generatedZipFile.entries();
        if (!entries.hasMoreElements()) {
            fail("Zip file didn't contain an entry.");
        }
        ZipEntry entry = entries.nextElement();
        assertThat(entry.getName(), is(contractName + DEBUG_JSON_SUFFIX));
        objectMapper.readValue(generatedZipFile.getInputStream(entry), DebugInfo.class);
        if (entries.hasMoreElements()) {
            fail("Zip file contained more than one entry.");
        }

    }

}
