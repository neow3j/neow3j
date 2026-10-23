package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.constants.OpCode;

import static io.neow3j.devpack.constants.InteropService.SYSTEM_STORAGE_ASREADONLY;

/**
 * A {@code StorageContext} represents access to a contract’s persistent storage. It can be passed to other contracts
 * as an argument, allowing them to perform read and write operations on the storage of the originating contract.
 * <p>
 * Traditionally, a {@code StorageContext} was required by all {@link Storage} methods to determine which contract’s
 * storage should be accessed.
 *
 * @deprecated since 3.24.1; planned to be removed in 3.25.0.
 * <p>
 * Since the Faun hard fork, new storage-related system calls operate on the storage of the executing contract
 * without requiring an explicit {@code StorageContext}. For accessing a contract’s own storage, providing a context
 * is therefore no longer necessary.
 * <p>
 * Context-based storage system calls remain available for backwards compatibility and advanced use cases (such as
 * explicitly delegating storage access to another contract). However, their use is discouraged in new code, as it is
 * easier to misuse and can unintentionally broaden storage write access.
 */
@Deprecated
public class StorageContext implements InteropInterface {

    private StorageContext() {
    }

    /**
     * Gets this {@code StorageContext} in read-only mode, meaning that after calling this method, write access to
     * the contract's storage is denied.
     *
     * @return this {@code StorageContext}.
     * @deprecated since 3.24.1; planned to be removed in 3.25.0.
     */
    @Deprecated
    @Instruction(interopService = SYSTEM_STORAGE_ASREADONLY)
    public native StorageContext asReadOnly();

    /**
     * Compares this context to the given object. The comparison happens by reference only.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same storage context. False, otherwise.
     * @deprecated since 3.24.1; planned to be removed in 3.25.0.
     */
    @Deprecated
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

}
