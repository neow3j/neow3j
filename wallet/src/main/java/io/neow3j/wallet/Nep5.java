package io.neow3j.wallet;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Nep5 {

    private Neow3j neow3j;
    private ScriptHash fromContractScriptHash;

    public Nep5() {
    }

    private Nep5(Builder builder) {
        this.neow3j = builder.neow3j;
        this.fromContractScriptHash = builder.fromContractScriptHash;
    }

    public BigInteger totalSupply() throws IOException {
        return invokeContract("totalSupply").getInvocationResult().getStack().get(1).asByteArray().getAsNumber();
    }

    public String name() throws IOException {
        return invokeContract("name").getInvocationResult().getStack().get(1).asByteArray().getAsString();
    }

    public String symbol() throws IOException {
        return invokeContract("symbol").getInvocationResult().getStack().get(1).asByteArray().getAsString();
    }

    public BigInteger decimals() throws IOException {
        return (BigInteger) invokeContract("decimals").getInvocationResult().getStack().get(1).getValue();
    }

    public BigInteger balanceOf(byte[] account) throws IOException {
        return invokeContract("balanceOf").getInvocationResult().getStack().get(1).asByteArray().getAsNumber();
    }

    public NeoInvoke invokeContract(String methodName) throws IOException {
        List<ContractParameter> contractParameters = null;
        if (Objects.nonNull(methodName)) {
            contractParameters = Arrays.asList(
                    ContractParameter.string(methodName),
                    ContractParameter.array());
            return neow3j.invoke(this.fromContractScriptHash.toString(), contractParameters).send();
        } else {
            throw new  IllegalStateException("Method name is not set");
        }
    }

    public static class Builder {
        private Neow3j neow3j;
        private ScriptHash fromContractScriptHash;
        public Builder(Neow3j neow3j) {
            this.neow3j = neow3j;
        }
        public Nep5.Builder fromContract(ScriptHash contractScriptHash) {
            this.fromContractScriptHash = contractScriptHash;
            return this;
        }

        public Nep5 build() {
            if (neow3j == null) throw new IllegalStateException("Neow3j not set.");
            else if (fromContractScriptHash == null) throw new IllegalStateException("Contract not set.");
            return new Nep5(this);
        }
    }

}
