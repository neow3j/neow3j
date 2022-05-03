package io.neow3j.compiler.sourcelookup;

import java.util.List;

/**
 * A source container consisting of multiple other containers.
 * <p>
 * Subclasses need to implement {@link CompositeSourceContainer#createSourceContainers()} where they create the
 * containers that this composite container is made up of.
 */
public abstract class CompositeSourceContainer implements ISourceContainer {

    private List<ISourceContainer> containers;

    /**
     * Gets the source containers of this composite container.
     *
     * @return the list of source containers.
     */
    public List<ISourceContainer> getSourceContainers() {
        if (containers == null) {
            containers = createSourceContainers();
        }
        return containers;
    }

    /**
     * Creates the source containers of this composite container.
     *
     * @return the list of source containers.
     */
    protected abstract List<ISourceContainer> createSourceContainers();

}
