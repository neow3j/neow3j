package io.neow3j.contract;

import io.neow3j.contract.abi.model.NeoContractEvent;
import io.neow3j.contract.abi.model.NeoContractFunction;
import io.neow3j.contract.abi.model.NeoContractInterface;
import io.neow3j.model.types.ContractParameterType;

import java.util.List;

import static io.neow3j.utils.Strings.isEmpty;

public class Contract {

    // TODO: 2019-07-03 Guil:
    // Maybe, should we use the NEP6Contract class?

    private final ScriptHash contractScriptHash;

    private NeoContractInterface abi;

    /**
     * Creates a new contract with the given script hash
     *
     * @param contractScriptHash Contract script hash in little-endian order.
     * @deprecated
     */
    @Deprecated
    public Contract(byte[] contractScriptHash) {
        this.contractScriptHash = new ScriptHash(contractScriptHash);
    }

    /**
     * Creates a new contract with the given script hash and ABI.
     *
     * @param contractScriptHash Contract script hash in little-endian order.
     * @param abi                The contract's ABI.
     * @deprecated
     */
    @Deprecated
    public Contract(byte[] contractScriptHash, NeoContractInterface abi) {
        this(contractScriptHash);
        this.abi = abi;
    }

    /**
     * Creates a new contract with the given script hash
     *
     * @param contractScriptHash Contract script hash.
     */
    public Contract(ScriptHash contractScriptHash) {
        this.contractScriptHash = contractScriptHash;
    }

    /**
     * Creates a new contract with the given script hash and ABI.
     *
     * @param contractScriptHash Contract script hash in little-endian order.
     * @param abi                The contract's ABI.
     */
    public Contract(ScriptHash contractScriptHash, NeoContractInterface abi) {
        this(contractScriptHash);
        this.abi = abi;
    }

    public ScriptHash getContractScriptHash() {
        return contractScriptHash;
    }

    public NeoContractInterface getAbi() {
        return abi;
    }

    public Contract abi(NeoContractInterface abi) {
        this.abi = abi;
        return this;
    }

    public List<ContractParameter> getEntryPointParameters() {
        return getFunctionParameters(abi.getEntryPoint());
    }

    public ContractParameterType getEntryPointReturnType() {
        return getFunctionReturnType(abi.getEntryPoint());
    }

    public List<NeoContractFunction> getFunctions() {
        return abi.getFunctions();
    }

    public List<NeoContractEvent> getEvents() {
        return abi.getEvents();
    }

    public List<ContractParameter> getFunctionParameters(final String functionName) {
        return abi.getFunctions()
                .stream()
                .filter(f -> isEmpty(f.getName()))
                .filter(f -> f.getName().equals(functionName))
                .findFirst()
                .map(NeoContractFunction::getParameters)
                .orElseThrow(() -> new IllegalArgumentException("No parameters found for the function (" + functionName + ")."));
    }

    public ContractParameterType getFunctionReturnType(final String functionName) {
        return abi.getFunctions()
                .stream()
                .filter(f -> isEmpty(f.getName()))
                .filter(f -> f.getName().equals(functionName))
                .findFirst()
                .map(NeoContractFunction::getReturnType)
                .orElseThrow(() -> new IllegalArgumentException("No returnType found for the function (" + functionName + ")."));
    }

    public List<ContractParameter> getEventParameters(final String eventName) {
        return abi.getEvents()
                .stream()
                .filter(e -> isEmpty((e.getName())))
                .filter(e -> e.getName().equals(eventName))
                .findFirst()
                .map(NeoContractEvent::getParameters)
                .orElseThrow(() -> new IllegalArgumentException("No parameters found for the event (" + eventName + ")."));
    }

}
