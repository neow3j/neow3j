package io.neow3j.compiler.sourcelookup;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DirectorySourceContainer extends CompositeSourceContainer {

    // root directory
    private File directory;
    // whether to search sub-folders
    private boolean searchSubDirs = false;

    /**
     * Constructs an external folder container for the
     * directory identified by the given path.
     *
     * @param directory     path to a directory in the local file system
     * @param searchSubDirs whether folders within the root directory
     *                      should be searched for source elements
     */
    public DirectorySourceContainer(File directory, boolean searchSubDirs) {
        this.directory = directory;
        this.searchSubDirs = searchSubDirs;
    }

    /**
     * Looks for the given file name. The name can be a single file name or a path, e.g., a
     * package structure like {@code io/neow3j/compiler/Compiler.java}.
     *
     * @param name the name of the source element to search for.
     * @return
     */
    @Override
    public List<File> findSourceElements(String name) {
        ArrayList<File> sources = new ArrayList<>();
        File file = new File(directory, name);
        if (file.exists() && file.isFile()) {
            sources.add(file);
        }

        if (sources.isEmpty() && searchSubDirs) {
            List<ISourceContainer> containers = getSourceContainers();
            for (ISourceContainer container : containers) {
                List<File> nestedSources = container.findSourceElements(name);
                if (nestedSources == null || nestedSources.size() == 0) {
                    continue;
                }
                // Only add the first occurence that has been found.
                // TODO: Handle duplicates.
                sources.add(nestedSources.get(0));
            }
        }
        return sources;
    }

    /**
     * Recursively
     *
     * @return
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
