package io.neow3j.wallet;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvoke;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        return this.invokeContract("totalSupply").getInvocationResult().getStack().get(1).asByteArray().getAsNumber();
    }

    public String name() throws IOException {
        return this.invokeContract("name").getInvocationResult().getStack().get(1).asByteArray().getAsString();
    }

    public String symbol() throws IOException {
        return this.invokeContract("symbol").getInvocationResult().getStack().get(1).asByteArray().getAsString();
    }

    public BigInteger decimals() throws IOException {
        return this.invokeContract("decimals").getInvocationResult().getStack().get(1).asInteger().getValue();
    }

    public BigInteger balanceOf(byte[] account) throws IOException {
        return this.invokeContract("balanceOf", account).getInvocationResult().getStack().get(1).asByteArray().getAsNumber();
    }

    public NeoInvoke invokeContract(String nep5Method) throws IOException {
        Optional.ofNullable(nep5Method).orElseThrow(() -> new IllegalStateException("Method is not set"));
        List<ContractParameter> contractParameters = Arrays.asList(
                ContractParameter.string(nep5Method),
                ContractParameter.array());
        return neow3j.invoke(this.fromContractScriptHash.toString(), contractParameters).send();
    }

    public NeoInvoke invokeContract(String nep5Method, byte[] param) throws IOException {
        Optional.ofNullable(nep5Method).orElseThrow(() -> new IllegalStateException("Method is not set"));
        Optional.ofNullable(param).orElseThrow(() -> new IllegalStateException("required params is not set"));
        List<ContractParameter> contractParameters = Arrays.asList(
                ContractParameter.string(nep5Method),
                ContractParameter.array(ContractParameter.byteArray(param)));
        return neow3j.invoke(this.fromContractScriptHash.toString(), contractParameters).send();
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
            Optional.ofNullable(neow3j).orElseThrow(() -> new IllegalStateException("Neow3j not set."));
            Optional.ofNullable(fromContractScriptHash).orElseThrow(() -> new IllegalStateException("Contract not set."));
            return new Nep5(this);
        }
    }

}
