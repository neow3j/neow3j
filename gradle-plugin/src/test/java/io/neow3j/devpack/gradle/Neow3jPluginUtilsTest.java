package io.neow3j.devpack.gradle;

import static io.neow3j.devpack.gradle.Neow3jPluginUtils.DEBUG_JSON_SUFFIX;
import static io.neow3j.devpack.gradle.Neow3jPluginUtils.NEFDBGNFO_SUFFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.compiler.DebugInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.Test;

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
    public void testGetCompileOutputFileName() throws IOException {
        String outputFileName1 = Neow3jPluginUtils
                .getCompileOutputFileName("io.neow3j.blah.Test");
        assertThat(outputFileName1, is("Test.nef"));

        String outputFileName2 = Neow3jPluginUtils
                .getCompileOutputFileName("io.neow3j.blah.");
        assertThat(outputFileName2, is(Neow3jPluginUtils.DEFAULT_FILENAME));

        String outputFileName3 = Neow3jPluginUtils
                .getCompileOutputFileName("");
        assertThat(outputFileName3, is(Neow3jPluginUtils.DEFAULT_FILENAME));

        String outputFileName4 = Neow3jPluginUtils
                .getCompileOutputFileName(null);
        assertThat(outputFileName4, is(Neow3jPluginUtils.DEFAULT_FILENAME));
    }

    @Test
    public void generateDebugInfoZipShouldGenerateCorrectZipFileWithCorrectEntry()
            throws IOException {

        String zipFileName = "DbgnfoZip";
        DebugInfo dbgnfo = new DebugInfo();
        File tmpDir = Files.createTempDirectory("debuginfo").toFile();
        tmpDir.deleteOnExit();
        String zipFilePath = Neow3jPluginUtils.generateDebugInfoZip(dbgnfo,
                tmpDir.getAbsolutePath(), zipFileName);
        assertThat(zipFilePath, is(tmpDir.getAbsoluteFile() + "/" + zipFileName
                + NEFDBGNFO_SUFFIX));
        ZipFile generatedZipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = generatedZipFile.entries();
        if (!entries.hasMoreElements()) {
            fail("Zip file didn't contain an entry.");
        }
        ZipEntry entry = entries.nextElement();
        assertThat(entry.getName(), is(zipFileName + DEBUG_JSON_SUFFIX));
        DebugInfo debugInfo = objectMapper.readValue(generatedZipFile.getInputStream(entry),
                DebugInfo.class);
        if (entries.hasMoreElements()) {
            fail("Zip file contained more than one entry.");
        }
    }
}
