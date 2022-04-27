package io.neow3j.compiler.sourcelookup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A source container that is a simple directory, its contents and subdirectories.
 */
public class DirectorySourceContainer extends CompositeSourceContainer {

    private File directory;
    private boolean searchSubDirs;

    /**
     * Constructs a new source container with the given directory as its root directory.
     *
     * @param directory     the directory in the local file system
     * @param searchSubDirs whether subdirectories within the root directory should be searched for source files.
     */
    public DirectorySourceContainer(File directory, boolean searchSubDirs) {
        this.directory = directory;
        this.searchSubDirs = searchSubDirs;
    }

    /**
     * Looks for the given file name in this container's root directory and subdirectories (depending on
     * configuration). Only returns the first occurrence of a match.
     * <p>
     * The name can be a single file name or a path, e.g., a package structure like {@code io/neow3j/compiler
     * /Compiler.java}. In the latter case, the whole path will be matched.
     *
     * @param name the name of the source file to search for.
     * @return the list of found source files matching the name.
     */
    @Override
    public File findSourceFile(String name) {
        File file = new File(directory, name);
        if (file.exists() && file.isFile()) {
            return file;
        }
        if (searchSubDirs) {
            for (ISourceContainer container : getSourceContainers()) {
                file = container.findSourceFile(name);
                if (file != null) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * If this container is configured to search through subdirectories, this method returns new {@code
     * DirectorySourceContainer}s for each of its subdirectories.
     *
     * @return the list of source containers made up of this container's subdirectories.
     */
    @Override
    protected List<ISourceContainer> createSourceContainers() {
        List<ISourceContainer> dirs = new ArrayList<>();
        if (searchSubDirs) {
            String[] files = directory.list();
            if (files != null) {
                for (String name : files) {
                    File file = new File(directory, name);
                    if (file.exists() && file.isDirectory()) {
                        dirs.add(new DirectorySourceContainer(file, true));
                    }
                }
            }
        }
        return dirs;
    }

}
