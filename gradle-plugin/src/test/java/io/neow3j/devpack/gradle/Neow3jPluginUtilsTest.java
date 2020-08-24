package io.neow3j.devpack.gradle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class Neow3jPluginUtilsTest {

    @Test
    public void testGetClassNameHappyPath() {
        String simpleClassName = Neow3jPluginUtils.getClassName("io.neow3j.blah.Test");
        assertThat(simpleClassName, is("Test"));
    }

    @Test
    public void testGetClassNameNoClass() {
        String simpleClassName = Neow3jPluginUtils.getClassName("io.neow3j.blah.");
        assertThat(simpleClassName, is(nullValue()));
    }

    @Test
    public void testGetClassNameEmpty() {
        String simpleClassName = Neow3jPluginUtils.getClassName("");
        assertThat(simpleClassName, is(nullValue()));
    }

    @Test
    public void testWriteToFile() throws IOException {
        Path tempFile = Files.createTempFile("test", "neow3jPlugin");
        byte[] bytesToBeWritten = new byte[]{0x00, 0x01, 0x02};
        Neow3jPluginUtils.writeToFile(tempFile.toFile(), bytesToBeWritten);
        byte[] readBytes = Files.readAllBytes(tempFile);
        assertThat(readBytes, is(bytesToBeWritten));
    }

}
