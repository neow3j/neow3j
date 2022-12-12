package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.StackItemType;

import java.util.List;
import java.util.Objects;

public class RecordState {

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "recordType", required = true)
    private RecordType recordType;

    @JsonProperty(value = "data")
    private String data;

    public RecordState() {
    }

    public RecordState(String name, RecordType recordType, String data) {
        this.name = name;
        this.recordType = recordType;
        this.data = data;
    }

    /**
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the record type.
     */
    public RecordType getRecordType() {
        return recordType;
    }

    /**
     * @return the data of the record.
     */
    public String getData() {
        return data;
    }

    public static RecordState fromStackItem(StackItem stackItem) {
        if (!stackItem.getType().equals(StackItemType.ARRAY)) {
            throw new IllegalArgumentException("Could not deserialise RecordState from the stack item.");
        }
        List<StackItem> list = stackItem.getList();
        String name = list.get(0).getString();
        RecordType recordType = RecordType.valueOf(list.get(1).getInteger().byteValue());
        String data = list.get(2).getString();
        return new RecordState(name, recordType, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecordState)) {
            return false;
        }
        RecordState that = (RecordState) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getRecordType(), that.getRecordType()) &&
                Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getRecordType(), getData());
    }

    @Override
    public String toString() {
        return "RecordState{" +
                "name='" + getName() + '\'' +
                ", recordType=" + recordType +
                ", data='" + getData() +
                "}";
    }

}
