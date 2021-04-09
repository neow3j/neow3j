package io.neow3j.compiler.sourcelookup;

import java.util.List;

public abstract class CompositeSourceContainer implements ISourceContainer {

    private List<ISourceContainer> containers;

    public synchronized List<ISourceContainer> getSourceContainers() {
        if (containers == null) {
            containers = createSourceContainers();
        }
        return containers;
    }

    /**
     * Creates the source containers in this composite container. Subclasses should override this
     * methods.
     *
     * @return the array of {@link ISourceContainer}s
     */
    protected abstract List<ISourceContainer> createSourceContainers();

}
