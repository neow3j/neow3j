package io.neow3j.contract.types;

import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.types.Hash160;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * Represents a whitelist entry for a smart contract method with a fixed system fee.
 * <p>
 * Each whitelist entry is uniquely identified by the contract script hash, method name, and argument count, and
 * overrides the default system fee for that method.
 * <p>
 * The fixed system fee replaces both the execution fee (opcode and interop prices multiplied by the execution fee
 * factor) and the storage fee (storage prices multiplied by the storage fee factor).
 */
public class WhitelistFeeEntry {
    /**
     * The script hash of the contract.
     */
    private final Hash160 contract;

    /**
     * The method name.
     */
    private final String method;

    /**
     * The number of arguments.
     */
    private final int argCount;

    /**
     * The fixed fee.
     */
    private final BigInteger fixedFee;

    /**
     * Creates a new whitelist entry for a contract method with a fixed system fee.
     *
     * @param contract the script hash of the smart contract.
     * @param method   the name of the contract method.
     * @param argCount the number of arguments the method accepts.
     * @param fixedFee the fixed system fee applied to this method, overriding both the execution fee and the storage
     *                 fee.
     */
    public WhitelistFeeEntry(Hash160 contract, String method, int argCount, BigInteger fixedFee) {
        this.contract = contract;
        this.method = method;
        this.argCount = argCount;
        this.fixedFee = fixedFee;
    }

    /**
     * @return the script hash of the contract.
     */
    public Hash160 getContract() {
        return contract;
    }

    /**
     * @return the method name.
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the number of arguments.
     */
    public int getArgCount() {
        return argCount;
    }

    /**
     * @return the fixed fee.
     */
    public BigInteger getFixedFee() {
        return fixedFee;
    }

    /**
     * Converts a {@link StackItem} returned by the Neo node into a {@link WhitelistFeeEntry}.
     *
     * @param item the {@link StackItem} to convert.
     * @return the corresponding {@link WhitelistFeeEntry}.
     */
    public static WhitelistFeeEntry fromStackItem(StackItem item) {
        List<StackItem> list = item.getList();
        Hash160 contract = Hash160.fromAddress(list.get(0).getAddress());
        String method = list.get(1).getString();
        int argCount = list.get(2).getInteger().intValue();
        BigInteger fixedFee = list.get(3).getInteger();
        return new WhitelistFeeEntry(contract, method, argCount, fixedFee);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WhitelistFeeEntry that = (WhitelistFeeEntry) o;
        return Objects.equals(contract, that.getContract()) &&
                Objects.equals(method, that.getMethod()) &&
                Objects.equals(argCount, that.getArgCount()) &&
                Objects.equals(fixedFee, that.getFixedFee());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContract(), getMethod(), getArgCount(), getFixedFee());
    }

    @Override
    public String toString() {
        return "WhitelistFeeEntry{" +
                "contract=" + contract +
                ", method='" + method + '\'' +
                ", argCount=" + argCount +
                ", fixedFee=" + fixedFee +
                '}';
    }

}
