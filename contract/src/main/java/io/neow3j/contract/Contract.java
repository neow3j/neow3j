package io.neow3j.contract;

import io.neow3j.contract.abi.model.NeoContractEvent;
import io.neow3j.contract.abi.model.NeoContractFunction;
import io.neow3j.contract.abi.model.NeoContractInterface;
import io.neow3j.model.types.ContractParameterType;

import java.util.List;

import static io.neow3j.utils.Strings.isEmpty;

public class Contract {

    // TODO: 2019-07-24 Guil:
    // Apply the build pattern here?

    private final byte[] contractScriptHash;

    private ContractDeploymentScript deploymentScript;

    private NeoContractInterface abi;

    public Contract(ContractDeploymentScript deploymentScript) {
        this.deploymentScript = deploymentScript;
        this.contractScriptHash = deploymentScript.getScriptHash();
    }

    public Contract(ContractDeploymentScript deploymentScript, NeoContractInterface abi) {
        this.deploymentScript = deploymentScript;
        this.contractScriptHash = deploymentScript.getScriptHash();
        this.abi = abi;
    }

    public Contract(byte[] contractScriptHash) {
        this.contractScriptHash = contractScriptHash;
    }

    public Contract(byte[] contractScriptHash, NeoContractInterface abi) {
        this.contractScriptHash = contractScriptHash;
        this.abi = abi;
    }

    public byte[] getContractScriptHash() {
        return contractScriptHash;
    }

    public ContractDeploymentScript getDeploymentScript() {
        return deploymentScript;
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
        throwIfABINotSet();
        return abi.getFunctions()
                .stream()
                .filter(f -> isEmpty(f.getName()))
                .filter(f -> f.getName().equals(functionName))
                .findFirst()
                .map(NeoContractFunction::getParameters)
                .orElseThrow(() -> new IllegalArgumentException("No parameters found for the function (" + functionName + ")."));
    }

    public ContractParameterType getFunctionReturnType(final String functionName) {
        throwIfABINotSet();
        return abi.getFunctions()
                .stream()
                .filter(f -> isEmpty(f.getName()))
                .filter(f -> f.getName().equals(functionName))
                .findFirst()
                .map(NeoContractFunction::getReturnType)
                .orElseThrow(() -> new IllegalArgumentException("No returnType found for the function (" + functionName + ")."));
    }

    public List<ContractParameter> getEventParameters(final String eventName) {
        throwIfABINotSet();
        return abi.getEvents()
                .stream()
                .filter(e -> isEmpty((e.getName())))
                .filter(e -> e.getName().equals(eventName))
                .findFirst()
                .map(NeoContractEvent::getParameters)
                .orElseThrow(() -> new IllegalArgumentException("No parameters found for the event (" + eventName + ")."));
    }

    private void throwIfABINotSet() {
        if (abi == null) {
            throw new IllegalStateException("ABI should be set first.");
        }
    }

}
