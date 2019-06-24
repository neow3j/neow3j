package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {

    MINER_TRANSACTION("MinerTransaction", 0x00, 0),
    ISSUE_TRANSACTION("IssueTransaction", 0x01, 0),
    CLAIM_TRANSACTION("ClaimTransaction", 0x02, 0),
    DATA_FILE("DataFile", 0x12, 0),
    ENROLLMENT_TRANSACTION("EnrollmentTransaction", 0x20, 0),
    REGISTER_TRANSACTION("RegisterTransaction", 0x40, 0),
    CONTRACT_TRANSACTION("ContractTransaction", 0x80, 0),
    RECORD_TRANSACTION("RecordTransaction", 0x81, 0),
    STATE_TRANSACTION("StateTransaction", 0x90, 0),
    STATE_UPDATE_TRANSACTION("StateUpdateTransaction", 0x90, 0),
    STATE_UPDATER_TRANSACTION("StateUpdaterTransaction", 0x91, 0),
    DESTROY_TRANSACTION("DestroyTransaction", 0x18, 0),
    PUBLISH_TRANSACTION("PublishTransaction", 0xd0, 0),
    INVOCATION_TRANSACTION("InvocationTransaction", 0xd1, 0);

    public static final byte DEFAULT_VERSION = 0;

    private String jsonValue;
    private byte byteValue;
    private byte version;

    TransactionType(String jsonValue, int v, int version) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) v;
        this.version = (byte) version;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public byte version() { return this.version; }

    public static TransactionType valueOf(byte byteValue) {
        for (TransactionType e : TransactionType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

    public static TransactionType fromJsonValue(String jsonValue) {
        for (TransactionType e : TransactionType.values()) {
            if (e.jsonValue.equals(jsonValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException();
    }

}
