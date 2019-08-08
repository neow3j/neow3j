package io.neow3j.contract;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContractFunctionProperties extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(ContractFunctionProperties.class);

    private List<ContractParameterType> parameterTypes;

    private ContractParameterType returnType;

    private boolean needsStorage;

    private boolean needsDynamicInvoke;

    private boolean isPayable;

    public ContractFunctionProperties() {
    }

    public ContractFunctionProperties(List<ContractParameterType> parameterTypes, ContractParameterType returnType, boolean needsStorage, boolean needsDynamicInvoke, boolean isPayable) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.needsStorage = needsStorage;
        this.needsDynamicInvoke = needsDynamicInvoke;
        this.isPayable = isPayable;
    }

    public List<ContractParameterType> getParameterTypes() {
        return this.parameterTypes;
    }

    public ContractParameterType getReturnType() {
        return this.returnType;
    }

    public boolean getNeedsStorage() {
        return needsStorage;
    }

    public boolean getNeedsDynamicInvoke() {
        return needsDynamicInvoke;
    }

    public boolean getPayable() {
        return isPayable;
    }

    protected int packFlagsValue() {
        int flagsValue = 0;
        if (this.needsStorage) {
            flagsValue += (1 << 0);
        }
        if (this.needsDynamicInvoke) {
            flagsValue += (1 << 1);
        }
        if (this.isPayable) {
            flagsValue += (1 << 2);
        }
        return flagsValue;
    }

    protected static boolean unpackNeedsStorage(int value) {
        return (value & 1) == 1;
    }

    protected static boolean unpackNeedsDynamicInvoke(int value) {
        return (value & 2) == 2;
    }

    protected static boolean unpackIsPayable(int value) {
        return (value & 4) == 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractFunctionProperties)) return false;
        ContractFunctionProperties that = (ContractFunctionProperties) o;
        return getNeedsStorage() == that.getNeedsStorage() &&
                getNeedsDynamicInvoke() == that.getNeedsDynamicInvoke() &&
                getPayable() == that.getPayable() &&
                Objects.equals(getParameterTypes(), that.getParameterTypes()) &&
                Objects.equals(getReturnType(), that.getReturnType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParameterTypes(), getReturnType(), getNeedsStorage(), getNeedsDynamicInvoke(), getPayable());
    }

    @Override
    public String toString() {
        return "ContractFunctionProperties{" +
                "parameterTypes=" + parameterTypes +
                ", returnType=" + returnType +
                ", needsStorage=" + needsStorage +
                ", needsDynamicInvoke=" + needsDynamicInvoke +
                ", isPayable=" + isPayable +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        int functionProperties = reader.readPushInteger();
        this.needsStorage = unpackNeedsStorage(functionProperties);
        this.needsDynamicInvoke = unpackNeedsDynamicInvoke(functionProperties);
        this.isPayable = unpackIsPayable(functionProperties);
        this.returnType = ContractParameterType.valueOf((byte) reader.readPushInteger());
        byte[] parameters = reader.readPushData();
        this.parameterTypes = new ArrayList<>();
        for (byte parameter : parameters) {
            parameterTypes.add(ContractParameterType.valueOf(parameter));
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        Byte[] parameterTypesArray = new Byte[this.parameterTypes.size()];
        Byte[] bytes = this.parameterTypes
                .stream()
                .map(ContractParameterType::byteValue)
                .collect(Collectors.toList())
                .toArray(parameterTypesArray);
        byte[] bytesPrimitive = ArrayUtils.toPrimitive(bytes);

        writer.write(new ScriptBuilder()
                .pushInteger(packFlagsValue())
                .pushInteger(this.returnType.byteValue() & 0xff)
                .pushData(bytesPrimitive)
                .toArray());
    }
}
