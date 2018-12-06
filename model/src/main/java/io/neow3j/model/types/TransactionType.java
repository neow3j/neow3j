package io.neow3j.model.types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {

    MINER_TRANSACTION("MinerTransaction", 0x00),
    ISSUE_TRANSACTION("IssueTransaction", 0x01),
    CLAIM_TRANSACTION("ClaimTransaction", 0x02),
    DATA_FILE("DataFile", 0x12),
    ENROLLMENT_TRANSACTION("EnrollmentTransaction", 0x20),
    REGISTER_TRANSACTION("RegisterTransaction", 0x40),
    CONTRACT_TRANSACTION("ContractTransaction", 0x80),
    RECORD_TRANSACTION("RecordTransaction", 0x81),
    STATE_UPDATE_TRANSACTION("StateUpdateTransaction", 0x90),
    STATE_UPDATER_TRANSACTION("StateUpdaterTransaction", 0x91),
    DESTROY_TRANSACTION("DestroyTransaction", 0x18),
    PUBLISH_TRANSACTION("PublishTransaction", 0xd0),
    INVOCATION_TRANSACTION("InvocationTransaction", 0xd1);

    private String jsonValue;
    private byte byteValue;

    TransactionType(String jsonValue, int v) {
        this.jsonValue = jsonValue;
        this.byteValue = (byte) v;
    }

    @JsonValue
    public String jsonValue() {
        return this.jsonValue;
    }

    public byte byteValue() {
        return this.byteValue;
    }

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
