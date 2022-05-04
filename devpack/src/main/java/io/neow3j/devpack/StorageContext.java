package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.script.OpCode;

import static io.neow3j.script.InteropService.SYSTEM_STORAGE_ASREADONLY;

/**
 * A {@code StorageContext} is the gateway to a contract's storage. It can be passed to other contracts as an
 * argument, allowing them to perform read/write operations on the persistent store of the current contract. It is
 * required in all {@link Storage} methods.
 */
public class StorageContext implements InteropInterface {

    private StorageContext() {
    }

    /**
     * Gets this {@code StorageContext} in read-only mode, meaning that after calling this method, write access to
     * the contract's storage is denied.
     *
     * @return this {@code StorageContext}.
     */
    @Instruction(interopService = SYSTEM_STORAGE_ASREADONLY)
    public native StorageContext asReadOnly();

    /**
     * Compares this context to the given object. The comparison happens by reference only.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same storage context. False, otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
