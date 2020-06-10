package io.neow3j.devpack.framework;

public enum TriggerType {
    Verification(0x00),
    VerificationR(0x01),
    Application(0x10),
    ApplicationR(0x11);

    private byte value;

    private TriggerType(int value) {
        this.value = (byte) value;
    }

    public byte value() {
        return value;
    }
}