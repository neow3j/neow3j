package io.neow3j.compiler.sourcelookup;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;

public class MockSourceContainer implements ISourceContainer {

    private final File fileToReturn;

    public MockSourceContainer(File fileToReturn) {
        this.fileToReturn = fileToReturn;
    }

    @Override
    public List<File> findSourceElements(String name) {
        return asList(fileToReturn);
    }
}
