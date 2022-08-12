package io.neow3j.compiler.sourcelookup;

import java.io.File;

public class MockSourceContainer implements ISourceContainer {

    private final File fileToReturn;

    public MockSourceContainer(File fileToReturn) {
        this.fileToReturn = fileToReturn;
    }

    @Override
    public File findSourceFile(String name) {
        return fileToReturn;
    }

}
