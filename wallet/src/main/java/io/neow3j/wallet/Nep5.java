package io.neow3j.wallet;

import io.neow3j.contract.ContractParameter;
import io.neow3j.contract.ScriptHash;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NeoInvoke;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Nep5 {
    private Neow3j neow3j;
    private ScriptHash fromContractScriptHash;

    private Nep5(Builder builder) {
        this.neow3j = builder.neow3j;
        this.fromContractScriptHash = builder.fromContractScriptHash;
    }


    public BigInteger totalSupply() throws IOException {

        List<ContractParameter> params = Arrays.asList(
                ContractParameter.string("totalSupply"),
                ContractParameter.array());
        NeoInvoke send = neow3j.invoke(this.fromContractScriptHash.toString(), params).send();
        return send.getInvocationResult().getStack().get(1).asByteArray().getAsNumber();
    }


    public String name() throws IOException {

        List<ContractParameter> params = Arrays.asList(
                ContractParameter.string("name"),
                ContractParameter.array());
        NeoInvoke send = neow3j.invoke(this.fromContractScriptHash.toString(), params).send();
        return send.getInvocationResult().getStack().get(1).asByteArray().getAsString();

    }


    public String symbol() throws IOException {

        List<ContractParameter> params = Arrays.asList(
                ContractParameter.string("symbol"),
                ContractParameter.array());
        NeoInvoke send = neow3j.invoke(this.fromContractScriptHash.toString(), params).send();
        return send.getInvocationResult().getStack().get(1).asByteArray().getAsString();

    }


    public BigInteger decimals() throws IOException {

        List<ContractParameter> params = Arrays.asList(
                ContractParameter.string("decimals"),
                ContractParameter.array());
        NeoInvoke send = neow3j.invoke(this.fromContractScriptHash.toString(), params).send();
        return (BigInteger) send.getInvocationResult().getStack().get(1).getValue();

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
            if (neow3j == null) throw new IllegalStateException("Neow3j not set");
            return new Nep5(this);
        }
    }
}
