package io.neow3j.transaction;

public enum TransactionAttributeType {

    COSIGNER(0x01, Cosigner.class);

    private byte byteValue;
    private Class<? extends TransactionAttribute> clazz;

    TransactionAttributeType(int byteValue, Class<? extends TransactionAttribute> clazz) {
        this.byteValue = (byte) byteValue;
        this.clazz = clazz;
    }

    public byte byteValue() {
        return this.byteValue;
    }

    public Class<? extends TransactionAttribute> clazz() {
        return this.clazz;
    }

    public static TransactionAttributeType valueOf(byte byteValue) {
        for (TransactionAttributeType e : TransactionAttributeType.values()) {
            if (e.byteValue == byteValue) {
                return e;
            }
        }
        throw new IllegalArgumentException(String.format("%s value type not found.", TransactionAttributeType.class.getName()));
    }

}
