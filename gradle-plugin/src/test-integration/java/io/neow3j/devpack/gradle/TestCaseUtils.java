package io.neow3j.devpack.gradle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TestCaseUtils {

    protected static void appendFile(File destination, String content) throws IOException {
        TestCaseUtils.writeFile(destination, content, true);
    }

    protected static void writeFile(File destination, String content) throws IOException {
        TestCaseUtils.writeFile(destination, content, false);
    }

    protected static void writeFile(File destination, String content, boolean append)
            throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination, append));
            if (append) {
                output.append(content);
            } else {
                output.write(content);
            }
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    protected static void appendFile(File destination, InputStream stream) throws IOException {
        appendFile(destination, getContentAsString(stream));
    }

    protected static void writeFile(File destination, InputStream stream) throws IOException {
        writeFile(destination, getContentAsString(stream));
    }

    protected static String getContentAsString(InputStream stream) {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8));
        return in.lines().collect(Collectors.joining());
    }

}
