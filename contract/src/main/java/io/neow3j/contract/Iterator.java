package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.StackItemType;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represents an iterator for stack items of the type {@link StackItemType#INTEROP_INTERFACE}.
 *
 * @param <T> the type of the iterator items.
 */
@SuppressWarnings("unchecked")
public class Iterator<T> {

    private final Neow3j neow3j;
    private final String sessionId;
    private final String iteratorId;
    private final Function<StackItem, T> mapper;

    /**
     * Initializes an iterator without a mapper function.
     * <p>
     * Travering this iterator returns a list of the {@link StackItem}s in the iterator.
     *
     * @param neow3j     the neow3j instance.
     * @param sessionId  the session id.
     * @param iteratorId the iterator id.
     */
    public Iterator(Neow3j neow3j, String sessionId, String iteratorId) {
        this(neow3j, sessionId, iteratorId, i -> (T) i);
    }

    /**
     * Initializes an iterator with a mapper function.
     * <p>
     * Travering this iterator returns a list of the iterator items on which the provided {@code mapper} function
     * is applied.
     *
     * @param neow3j     the neow3j instance.
     * @param sessionId  the session id.
     * @param iteratorId the iterator id.
     * @param mapper     the mapper function to apply on the iterator items.
     */
    public Iterator(Neow3j neow3j, String sessionId, String iteratorId, Function<StackItem, T> mapper) {
        this.neow3j = neow3j;
        this.sessionId = sessionId;
        this.iteratorId = iteratorId;
        this.mapper = mapper;
    }

    /**
     * Sends a request to traverse this iterator and returns a maximum of {@code count} items per request.
     * <p>
     * Whenever this method is called, the next {@code count} items of the iterator are returned. If there are no
     * more items in the iterator, an empty list will be returned.
     * <p>
     * The maximum {@code count} value that can be used for traversing an iterator depends on the configuration of the
     * Neo node. Make sure, it's less than or equal to the Neo node's configured value. Otherwise, traversing this
     * iterator will fail.
     *
     * @param count the number of items per traverse request.
     * @return the list of the next {@code count} items of this iterator.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<T> traverse(int count) throws IOException {
        return neow3j.traverseIterator(sessionId, iteratorId, count).send().getTraverseIterator()
                .stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Terminates the session on the Neo node.
     *
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public void terminateSession() throws IOException {
        neow3j.terminateSession(sessionId).send();
    }

    /**
     * @return the neow3j instance.
     */
    public Neow3j getNeow3j() {
        return neow3j;
    }

    /**
     * @return the session id.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @return the iterator id.
     */
    public String getIteratorId() {
        return iteratorId;
    }

    /**
     * @return the mapper function that is applied on each item in the iterator when traversing.
     */
    public Function<StackItem, T> getMapper() {
        return mapper;
    }

}
