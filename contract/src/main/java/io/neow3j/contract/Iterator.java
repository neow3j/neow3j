package io.neow3j.contract;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.stackitem.StackItem;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class Iterator<T> {

    private final Neow3j neow3j;
    private final String sessionId;
    private final String iteratorId;
    private final Function<StackItem, T> mapper;

    public Iterator(Neow3j neow3j, String sessionId, String iteratorId) {
        this(neow3j, sessionId, iteratorId, i -> (T) i);
    }

    public Iterator(Neow3j neow3j, String sessionId, String iteratorId, Function<StackItem, T> mapper) {
        this.neow3j = neow3j;
        this.sessionId = sessionId;
        this.iteratorId = iteratorId;
        this.mapper = mapper;
    }

    public List<T> traverse(int count) throws IOException {
        return neow3j.traverseIterator(sessionId, iteratorId, count).send().getTraverseIterator()
                .stream().map(mapper).collect(Collectors.toList());
    }

    public void terminateSession() throws IOException {
        neow3j.terminateSession(sessionId).send();
    }

    public Neow3j getNeow3j() {
        return neow3j;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getIteratorId() {
        return iteratorId;
    }

    public Function<StackItem, T> getMapper() {
        return mapper;
    }

}
