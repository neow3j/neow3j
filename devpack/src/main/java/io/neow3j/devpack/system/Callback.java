package io.neow3j.devpack.system;

import static io.neow3j.constants.InteropServiceCode.SYSTEM_CALLBACK_CREATEFROMMETHOD;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CALLBACK_CREATEFROMSYSCALL;
import static io.neow3j.constants.InteropServiceCode.SYSTEM_CALLBACK_INVOKE;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.Syscall;

public class Callback {

    /**
     * Invokes this callback function with the given arguments.
     *
     * @param args The arguments to pass to this callback function.
     * @return the result of the callback function invocation.
     */
    @Syscall(SYSTEM_CALLBACK_INVOKE)
    public native Object invoke(Object[] args);

//    @Instruction(opcode = OpCode.PUSH0)
//    @Instruction(opcode = OpCode.SWAP)
//    @Syscall(SYSTEM_CALLBACK_CREATE)
//    public static native <TResult> Callback create(Func<TResult> func);
//
//    @Instruction(opcode = OpCode.PUSH1)
//    @Instruction(opcode = OpCode.SWAP)
//    @Syscall(SYSTEM_CALLBACK_CREATE)
//    public static native <T, TResult> Callback create(Func<T, TResult> func);

//    ...How do we support callback functions with increasing numbers of parameters?...

    /**
     * Creates a callback function from the given method in the contract with the given script
     * hash.
     *
     * @param scriptHash The contract's script hash.
     * @param method     The method to call.
     * @return the callback.
     */
    @Syscall(SYSTEM_CALLBACK_CREATEFROMMETHOD)
    public static native Callback createFromMethod(Hash160 scriptHash, String method);

    /**
     * Creates a call back from the given syscall.
     * <p>
     * Only the syscalls specified in {@link SyscallCallback} can be used as callbacks. Use the
     * constants from there as parameters in this method.
     *
     * @param syscall The syscall to get as a callback.
     * @return the callback.
     */
    @Syscall(SYSTEM_CALLBACK_CREATEFROMSYSCALL)
    public static native Callback createFromSyscall(int syscall);

}