package io.neow3j.contract.exception;

import io.neow3j.constants.NeoVMState;
import io.neow3j.model.ContractParameter;

import java.util.List;

public class ContractInvocationException extends Exception {

    List<ContractParameter> stack;

    NeoVMState vmState;

    public ContractInvocationException(String message) {
        super(message);
    }

    public ContractInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContractInvocationException(Throwable cause) {
        super(cause);
    }

    public ContractInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ContractInvocationException(List<ContractParameter> stack) {
        this.stack = stack;
    }

    public ContractInvocationException(NeoVMState vmState, List<ContractParameter> stack) {
        this(stack);
        this.vmState = vmState;
    }


    public List<ContractParameter> getStack() {
        return this.stack;
    }

    public NeoVMState getVmState() {
        return this.vmState;
    }
}

