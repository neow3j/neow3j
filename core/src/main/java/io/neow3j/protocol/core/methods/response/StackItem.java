package io.neow3j.protocol.core.methods.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.exceptions.StackItemCastException;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXISTING_PROPERTY)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = AnyStackItem.class, name = StackItemType.ANY_VALUE),
        @JsonSubTypes.Type(value = PointerStackItem.class, name = StackItemType.POINTER_VALUE),
        @JsonSubTypes.Type(value = BooleanStackItem.class, name = StackItemType.BOOLEAN_VALUE),
        @JsonSubTypes.Type(value = IntegerStackItem.class, name = StackItemType.INTEGER_VALUE),
        @JsonSubTypes.Type(value = ByteStringStackItem.class, name = StackItemType.BYTE_STRING_VALUE),
        @JsonSubTypes.Type(value = BufferStackItem.class, name = StackItemType.BUFFER_VALUE),
        @JsonSubTypes.Type(value = ArrayStackItem.class, name = StackItemType.ARRAY_VALUE),
        @JsonSubTypes.Type(value = StructStackItem.class, name = StackItemType.STRUCT_VALUE),
        @JsonSubTypes.Type(value = MapStackItem.class, name = StackItemType.MAP_VALUE),
        @JsonSubTypes.Type(value = InteropInterfaceStackItem.class, name = StackItemType.INTEROP_INTERFACE_VALUE)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StackItem {

    protected final static int MAX_VALUE_STRING_LENGTH = 80;

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
     * Gets this stack item's value formatted into a string.
     * @return the string.
     */
    public abstract String valueToString();

    public String toString() {
        String valueString = valueToString();
        if (valueString.length() > MAX_VALUE_STRING_LENGTH) {
            valueString = valueString.substring(0, MAX_VALUE_STRING_LENGTH) + "...";
        }
        return type.getValue() + "{value='" + valueString + "'}";
    }

    public boolean getBoolean() {
        throw new StackItemCastException(
                format("Cannot cast stack item %s to a boolean.", toString()));
    }

    public BigInteger getInteger() {
        throw new StackItemCastException(
                format("Cannot cast stack item %s to an integer.", toString()));
    }

    public String getAddress() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to an address.", type.getValue(), valueToString()));
    }

    public String getString() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to a string.", type.getValue(), valueToString()));
    }

    public String getHexString() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to a hex string.", type.getValue(), valueToString()));
    }

    public byte[] getByteArray() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to a byte array.", type.getValue(), valueToString()));
    }

    public StackItem[] getArray() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to a list.", type.getValue(), valueToString()));
    }

    public Map<StackItem, StackItem> getMap() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to a map.", type.getValue(), valueToString()));
    }

    public BigInteger getPointer() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to a neo-vm pointer.", type.getValue(), valueToString()));
    }

    public Object getInteropInterface() {
        throw new StackItemCastException(format("Cannot cast stack item of type %s with value %s " +
                "to a neo-vm interoperability interface.", type.getValue(), valueToString()));
    }

//    /**
//     * Casts this stack item to a {@link ByteStringStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link ByteStringStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link ByteStringStackItem}.
//     */
//    @JsonIgnore
//    public ByteStringStackItem asByteString() {
//        if (this instanceof ByteStringStackItem) {
//            return (ByteStringStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.BYTE_STRING.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    /**
//     * Casts this stack item to a {@link PointerStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link PointerStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link PointerStackItem}.
//     */
//    @JsonIgnore
//    public PointerStackItem asPointer() {
//        if (this instanceof PointerStackItem) {
//            return (PointerStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.POINTER.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    /**
//     * Casts this stack item to a {@link BooleanStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link BooleanStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link BooleanStackItem}.
//     */
//    @JsonIgnore
//    public BooleanStackItem asBoolean() {
//        if (this instanceof BooleanStackItem) {
//            return (BooleanStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.BOOLEAN.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    /**
//     * Casts this stack item to a {@link IntegerStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link IntegerStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link IntegerStackItem}.
//     */
//    @JsonIgnore
//    public IntegerStackItem asInteger() {
//        if (this instanceof IntegerStackItem) {
//            return (IntegerStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.INTEGER.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    /**
//     * Casts this stack item to a {@link BufferStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link BufferStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link BufferStackItem}.
//     */
//    @JsonIgnore
//    public BufferStackItem asBuffer() {
//        if (this instanceof BufferStackItem) {
//            return (BufferStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.BUFFER.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    /**
//     * Casts this stack item to a {@link ArrayStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link ArrayStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link ArrayStackItem}.
//     */
//    @JsonIgnore
//    public ArrayStackItem asArray() {
//        if (this instanceof ArrayStackItem) {
//            return (ArrayStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.ARRAY.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    /**
//     * Casts this stack item to a {@link MapStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link MapStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link MapStackItem}.
//     */
//    @JsonIgnore
//    public MapStackItem asMap() {
//        if (this instanceof MapStackItem) {
//            return (MapStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.MAP.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    /**
//     * Casts this stack item to a {@link StructStackItem}, if possible, and returns it.
//     *
//     * @return this stack item as a {@link StructStackItem}.
//     * @throws IllegalStateException if this stack item is not an instance of
//     *                               {@link StructStackItem}.
//     */
//    @JsonIgnore
//    public StructStackItem asStruct() {
//        if (this instanceof StructStackItem) {
//            return (StructStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.STRUCT.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    @JsonIgnore
//    public AnyStackItem asAny() {
//        if (this instanceof AnyStackItem) {
//            return (AnyStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.ANY.jsonValue() + " but of " + this.type.jsonValue());
//    }
//
//    @JsonIgnore
//    public InteropInterfaceStackItem asInteropInterface() {
//        if (this instanceof InteropInterfaceStackItem) {
//            return (InteropInterfaceStackItem) this;
//        }
//        throw new IllegalStateException("This stack item is not of type " +
//                StackItemType.INTEROP_INTERFACE.jsonValue() + " but of " + this.type.jsonValue());
//    }
}
