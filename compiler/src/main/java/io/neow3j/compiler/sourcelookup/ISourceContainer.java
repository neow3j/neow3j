package io.neow3j.compiler.sourcelookup;

import java.io.File;
import java.util.List;


public interface ISourceContainer {

    /**
     * Searches this container for the source file(s) with the given name. Returns multiple files
     * if multiple items with the same name are found.
     *
     * @param name The name of the file.
     * @return a list of sources files found matching the given name.
     */
    File findSourceFile(String name);

}
