package io.neow3j.protocol.core.methods.response;

import io.neow3j.model.types.StackItemType;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Holds a map in which keys and values are StackItems.
 */
public class MapStackItem extends StackItem {

    public MapStackItem(Map<StackItem, StackItem> value) {
        super(StackItemType.MAP, value);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Map<StackItem, StackItem> getValue() {
        return (Map<StackItem, StackItem>) this.value;
    }

    /**
     * <p>Gets the item that corresponds to the given key.</p>
     * <br>
     * <p>This method only checks map entries which have a key of type {@link ByteArrayStackItem}
     * because this is the usual type of the keys in a map stack item.</p>
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which the given key is mapped, or null if this map stack item
     * contains no mapping for the key.
     */
    public StackItem get(String key) {
        for (Entry<StackItem, StackItem> e : getValue().entrySet()) {
            if (e.getKey() instanceof ByteArrayStackItem &&
                    e.getKey().asByteArray().getAsString().equals(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the stack item that corresponds to the given key.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which the given key is mapped, or null if this map stack item
     * contains no mapping for the key.
     */
    public StackItem get(StackItem key) {
        return getValue().get(key);
    }

    /**
     * Returns the number of key-value pairs in this map stack item.
     *
     * @return the number of key-value pairs in this map stack item.
     */
    public int size() {
        return getValue().size();
    }

    /**
     * Returns true if this map contains no entries.
     *
     * @return true if this map contains no entries. False, otherwise.
     */
    public boolean isEmpty() {
        return getValue().isEmpty();
    }
}
