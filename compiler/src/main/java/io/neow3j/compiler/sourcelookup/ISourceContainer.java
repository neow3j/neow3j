package io.neow3j.compiler.sourcelookup;

import java.io.File;

public interface ISourceContainer {

    /**
     * Searches this container for the source file with the given name. Returns the first match.
     *
     * @param name the name of the file.
     * @return a source file matching the given name or null if no match was found.
     */
    File findSourceFile(String name);

}
