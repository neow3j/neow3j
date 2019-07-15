package io.neow3j.transaction;

import io.neow3j.crypto.transaction.RawTransaction;
import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.model.types.TransactionType;
import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;

public class InvocationTransaction extends RawTransaction {

    private byte[] contractScript;
    private BigDecimal systemFee;

    public InvocationTransaction() { }

    protected InvocationTransaction(Builder builder) {
        super(builder);
        this.contractScript = builder.contractScript;
        this.systemFee = builder.systemFee;
    }

    @Override
    public void serializeExclusive(BinaryWriter writer) throws IOException {
        writer.writeVarBytes(contractScript);
        byte[] gas = Numeric.fromBigDecimalToFixed8Bytes(this.systemFee);
        writer.write(ArrayUtils.reverseArray(gas));
    }

    @Override
    public void deserializeExclusive(BinaryReader reader) throws IOException {
        this.contractScript = reader.readVarBytes();
        this.systemFee = Numeric.fromFixed8ToDecimal(
                ArrayUtils.reverseArray(reader.readBytes(8)));
    }

    public static class Builder extends RawTransaction.Builder<Builder> {

        private byte[] contractScript;
        private BigDecimal systemFee;

        public Builder() {
            super();
            transactionType(TransactionType.INVOCATION_TRANSACTION);
        }

        public Builder contractScript(byte[] script) {
            this.contractScript = script; return this;
        }

        public Builder systemFee(BigDecimal gas) {
            this.systemFee = gas; return this;
        }

        @Override
        public InvocationTransaction build() {
            return new InvocationTransaction(this);
        }
    }
}

