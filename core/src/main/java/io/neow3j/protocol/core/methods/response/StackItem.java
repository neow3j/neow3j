package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.neow3j.model.types.StackItemType;

@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = AnyStackItem.class, name = StackItemType.ANY_VALUE),
//        @JsonSubTypes.Type(value = PointerStackItem.class, name = StackItemType.POINTER_VALUE),
        @JsonSubTypes.Type(value = BooleanStackItem.class, name = StackItemType.BOOLEAN_VALUE),
        @JsonSubTypes.Type(value = IntegerStackItem.class, name = StackItemType.INTEGER_VALUE),
        @JsonSubTypes.Type(value = ByteStringStackItem.class, name = StackItemType.BYTE_STRING_VALUE),
//        @JsonSubTypes.Type(value = BufferStackItem.class, name = StackItemType.BUFFER_VALUE),
        @JsonSubTypes.Type(value = ArrayStackItem.class, name = StackItemType.ARRAY_VALUE),
        @JsonSubTypes.Type(value = StructStackItem.class, name = StackItemType.STRUCT_VALUE),
        @JsonSubTypes.Type(value = MapStackItem.class, name = StackItemType.MAP_VALUE),
        @JsonSubTypes.Type(value = InteropInterfaceStackItem.class, name = StackItemType.INTEROP_INTERFACE_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StackItem {

    @JsonProperty("type")
    protected StackItemType type;

    public StackItem() {
    }

    public StackItem(StackItemType type) {
        this.type = type;
    }

    /**
     * Returns the type of this stack item.
     *
     * @return the type of this stack item.
     */
    public StackItemType getType() {
        return type;
    }

    /**
     * Casts this stack item to a {@link ByteStringStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link ByteStringStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link ByteStringStackItem}.
     */
    @JsonIgnore
    public ByteStringStackItem asByteString() {
        if (this instanceof ByteStringStackItem) {
            return (ByteStringStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.BYTE_STRING.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link BooleanStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link BooleanStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link BooleanStackItem}.
     */
    @JsonIgnore
    public BooleanStackItem asBoolean() {
        if (this instanceof BooleanStackItem) {
            return (BooleanStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.BOOLEAN.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link IntegerStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link IntegerStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link IntegerStackItem}.
     */
    @JsonIgnore
    public IntegerStackItem asInteger() {
        if (this instanceof IntegerStackItem) {
            return (IntegerStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.INTEGER.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link ArrayStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link ArrayStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link ArrayStackItem}.
     */
    @JsonIgnore
    public ArrayStackItem asArray() {
        if (this instanceof ArrayStackItem) {
            return (ArrayStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.ARRAY.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link MapStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link MapStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link MapStackItem}.
     */
    @JsonIgnore
    public MapStackItem asMap() {
        if (this instanceof MapStackItem) {
            return (MapStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.MAP.jsonValue() + " but of " + this.type.jsonValue());
    }

    /**
     * Casts this stack item to a {@link StructStackItem}, if possible, and returns it.
     *
     * @return this stack item as a {@link StructStackItem}.
     * @throws IllegalStateException if this stack item is not an instance of
     *                               {@link StructStackItem}.
     */
    @JsonIgnore
    public StructStackItem asStruct() {
        if (this instanceof StructStackItem) {
            return (StructStackItem) this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.STRUCT.jsonValue() + " but of " + this.type.jsonValue());
    }

    @JsonIgnore
    public Object asAny() {
        if (this instanceof AnyStackItem) {
            return this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.ANY.jsonValue() + " but of " + this.type.jsonValue());
    }

    @JsonIgnore
    public Object asInteropInterface() {
        if (this instanceof InteropInterfaceStackItem) {
            return this;
        }
        throw new IllegalStateException("This stack item is not of type " +
                StackItemType.INTEROP_INTERFACE.jsonValue() + " but of " + this.type.jsonValue());
    }
}
