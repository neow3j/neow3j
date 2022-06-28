package io.neow3j.protocol.core.stackitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.neow3j.protocol.exceptions.StackItemCastException;
import io.neow3j.types.StackItemType;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * A stack item represents a value from the neo-vm stack. The possible types of stack items are enumerated in
 * {@link StackItemType}.
 * <p>
 * When obtaining stack items as a result from a contract invocation, the value of the item can be retrieved with one
 * of the getter methods, e.g., {@link StackItem#getInteger()} without the need to cast the item to its specific type.
 */
@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes(value = {@JsonSubTypes.Type(value = AnyStackItem.class, name = StackItemType.ANY_VALUE),
        @JsonSubTypes.Type(value = PointerStackItem.class, name = StackItemType.POINTER_VALUE),
        @JsonSubTypes.Type(value = BooleanStackItem.class, name = StackItemType.BOOLEAN_VALUE),
        @JsonSubTypes.Type(value = IntegerStackItem.class, name = StackItemType.INTEGER_VALUE),
        @JsonSubTypes.Type(value = ByteStringStackItem.class, name = StackItemType.BYTE_STRING_VALUE),
        @JsonSubTypes.Type(value = BufferStackItem.class, name = StackItemType.BUFFER_VALUE),
        @JsonSubTypes.Type(value = ArrayStackItem.class, name = StackItemType.ARRAY_VALUE),
        @JsonSubTypes.Type(value = StructStackItem.class, name = StackItemType.STRUCT_VALUE),
        @JsonSubTypes.Type(value = MapStackItem.class, name = StackItemType.MAP_VALUE),
        @JsonSubTypes.Type(value = InteropInterfaceStackItem.class, name = StackItemType.INTEROP_INTERFACE_VALUE)})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StackItem {

    protected final static int MAX_VALUE_STRING_LENGTH = 80;

    @JsonProperty("type")
    protected StackItemType type;

    public StackItem() {
    }

    public abstract Object getValue();

    public StackItem(StackItemType type) {
        this.type = type;
    }

    /**
     * @return the type of this stack item.
     */
    public StackItemType getType() {
        return type;
    }

    /**
     * @return this stack item's value formatted as a string.
     */
    protected abstract String valueToString();

    public String toString() {
        String valueString;
        if (getValue() == null) {
            valueString = "null";
        } else {
            valueString = valueToString();
        }
        if (valueString.length() > MAX_VALUE_STRING_LENGTH) {
            valueString = valueString.substring(0, MAX_VALUE_STRING_LENGTH) + "...";
        }
        return type.getValue() + "{value='" + valueString + "'}";
    }

    /**
     * @return this item as a boolean.
     * @throws StackItemCastException if the stack item cannot be converted to a boolean or if its value is null.
     */
    @JsonIgnore
    public boolean getBoolean() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a boolean.", toString()));
    }

    /**
     * @return this item as an integer.
     * @throws StackItemCastException if the stack item cannot be converted to a integer or if its value is null.
     */
    @JsonIgnore
    public BigInteger getInteger() {
        throw new StackItemCastException(format("Cannot cast stack item %s to an integer.", toString()));
    }

    /**
     * @return this item as a Neo address.
     * @throws StackItemCastException if the stack item cannot be converted to a valid address or if its value is null.
     */
    @JsonIgnore
    public String getAddress() {
        throw new StackItemCastException(format("Cannot cast stack item %s to an address.", toString()));
    }

    /**
     * @return this item as a string.
     * @throws StackItemCastException if the stack item cannot be converted to a string or if its value is null.
     */
    @JsonIgnore
    public String getString() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a string.", toString()));
    }

    /**
     * Gets this item as a hex string. I.e., if the underlying value is a byte array that array is converted to its
     * hex string representation.
     *
     * @return the hex string.
     * @throws StackItemCastException if the stack item cannot be converted to a hex string or if its value is null.
     */
    @JsonIgnore
    public String getHexString() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a hex string.", toString()));
    }

    /**
     * @return this item as a byte array.
     * @throws StackItemCastException if the stack item cannot be converted to a byte array or if its value is null.
     */
    @JsonIgnore
    public byte[] getByteArray() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a byte array.", toString()));
    }

    /**
     * Gets this item as a list of stack items.
     * <p>
     * This can be used if the expected stack item type is {@link StackItemType#ARRAY} or {@link StackItemType#STRUCT}.
     *
     * @return the list of stack items.
     * @throws StackItemCastException if the stack item cannot be converted to a list of stack items of if its value
     *                                is null.
     */
    @JsonIgnore
    public List<StackItem> getList() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a list.", toString()));
    }

    /**
     * Gets this item as a map of stack items.
     * <p>
     * This can be used if the expected stack item type is {@link StackItemType#MAP}.
     *
     * @return the map of stack items.
     * @throws StackItemCastException if the stack item cannot be converted to a map of stack items or if its value
     *                                is null.
     */
    @JsonIgnore
    public Map<StackItem, StackItem> getMap() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a map.", toString()));
    }

    /**
     * Gets this item as a pointer.
     * <p>
     * This can only be used if the expected stack item type is {@link StackItemType#POINTER}.
     *
     * @return the pointer.
     * @throws StackItemCastException if the stack item cannot be converted to a pointer or if its value is null.
     */
    @JsonIgnore
    public BigInteger getPointer() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a neo-vm pointer.", toString()));
    }

    /**
     * Gets the iterator id of this item.
     * <p>
     * This can only be used if the expected stack item type is {@link StackItemType#INTEROP_INTERFACE}.
     *
     * @return the iterator id.
     * @throws StackItemCastException if the stack item cannot be converted to an {@code InteropInterface} or if its
     *                                value is null.
     */
    @JsonIgnore
    public String getIteratorId() {
        throw new StackItemCastException(format("Cannot cast stack item %s to a neo-vm session id.", toString()));
    }

    protected void nullCheck() {
        if (getValue() == null) {
            throw new StackItemCastException("Cannot cast stack item because its value is null");
        }
    }

}
