package io.neow3j.devpack.constants;

/**
 * Interoperability services that a Neo node provides to the NeoVM execution environment.
 * These services can be used in smart contract code via the {@link OpCode#SYSCALL} instruction.
 */
// This enum can also be found in the neow3j core module (io.neow3j.script.InteropService). Make sure to update it here
// and in the core, when things change.
public enum InteropService {

    /**
     * Checks the signature for the current script container.
     */
    SYSTEM_CRYPTO_CHECKSIG,

    /**
     * Checks the signatures for the current script container.
     */
    SYSTEM_CRYPTO_CHECKMULTISIG,

    /**
     * Call another contract dynamically.
     */
    SYSTEM_CONTRACT_CALL,

    // Not meant to be used by non-native smart contracts. Thus, not exposed in the devpack, but here for completeness.
    // SYSTEM_CONTRACT_CALLNATIVE,

    /**
     * Gets the call flags of the current context.
     */
    SYSTEM_CONTRACT_GETCALLFLAGS,

    /**
     * Calculates the account scripthash for a given public key.
     */
    SYSTEM_CONTRACT_CREATESTANDARDACCOUNT,

    /**
     * Calculates the multisig account scripthash for given public keys.
     */
    SYSTEM_CONTRACT_CREATEMULTISIGACCOUNT,

    // Not meant to be used by non-native smart contracts. Thus, not exposed in the devpack, but here for completeness.
    // SYSTEM_CONTRACT_NATIVEONPERSIST,

    // Not meant to be used by non-native smart contracts. Thus, not exposed in the devpack, but here for completeness.
    // SYSTEM_CONTRACT_NATIVEPOSTPERSIST,

    /**
     * Advances the iterator to the next element of the collection.
     */
    SYSTEM_ITERATOR_NEXT,

    /**
     * Gets the element in the collection at the current position of the iterator.
     */
    SYSTEM_ITERATOR_VALUE,

    /**
     * Gets the name of the current platform.
     */
    SYSTEM_RUNTIME_PLATFORM,

    /**
     * Gets the trigger of the execution.
     */
    SYSTEM_RUNTIME_GETTRIGGER,

    /**
     * Gets the timestamp of the current block.
     */
    SYSTEM_RUNTIME_GETTIME,

    /**
     * Gets the current script container.
     */
    SYSTEM_RUNTIME_GETSCRIPTCONTAINER,

    /**
     * Gets the script hash of the current context.
     */
    SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH,

    /**
     * Gets the script hash of the calling contract.
     */
    SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH,

    /**
     * Gets the script hash of the entry context.
     */
    SYSTEM_RUNTIME_GETENTRYSCRIPTHASH,

    /**
     * Loads a script at rumtime.
     */
    SYSTEM_RUNTIME_LOADSCRIPT,

    /**
     * Determines whether the specified account has witnessed the current transaction.
     */
    SYSTEM_RUNTIME_CHECKWITNESS,

    /**
     * Gets the number of times the current contract has been called during the execution
     */
    SYSTEM_RUNTIME_GETINVOCATIONCOUNTER,

    /**
     * Writes a log.
     */
    SYSTEM_RUNTIME_LOG,

    /**
     * Sends a notification. This syscall is used to fire events. It consumes the top stack item as event name, and
     * another array stack item that contains the event's state entries. When using this interoperability service
     * manually in the devpack (i.e., in {@code Instruction} annotations), note, that the event name that is read from
     * the stack MUST exist in the contract manifest's ABI. Otherwise, the NeoVM will exit with an error as soon as it
     * reaches this syscall. The compiler will not add it automatically.
     */
    SYSTEM_RUNTIME_NOTIFY,

    /**
     * Gets the notifications sent by the specified contract during the execution.
     */
    SYSTEM_RUNTIME_GETNOTIFICATIONS,

    /**
     * Gets the remaining GAS that can be spent in order to complete the execution.
     */
    SYSTEM_RUNTIME_GASLEFT,

    /**
     * Burn GAS basically to the benefit the NEO ecosystem.
     */
    SYSTEM_RUNTIME_BURNGAS,

    /**
     * Get the signers of the current transaction.
     */
    SYSTEM_RUNTIME_CURRENTSIGNERS,

    /**
     * Gets the magic number of the current network.
     */
    SYSTEM_RUNTIME_GETNETWORK,

    /**
     * Gets the next random number.
     */
    SYSTEM_RUNTIME_GETRANDOM,

    /**
     * Gets the address version of the current network.
     */
    SYSTEM_RUNTIME_GETADDRESSVERSION,

    /**
     * Gets the storage context for the current contract.
     */
    SYSTEM_STORAGE_GETCONTEXT,

    /**
     * Gets the readonly storage context for the current contract.
     */
    SYSTEM_STORAGE_GETREADONLYCONTEXT,

    /**
     * Converts the specified storage context to a new readonly storage context.
     */
    SYSTEM_STORAGE_ASREADONLY,

    /**
     * Gets the entry with the specified key from the storage.
     */
    SYSTEM_STORAGE_GET,

    /**
     * Finds the entries from the storage with a given prefix.
     */
    SYSTEM_STORAGE_FIND,

    /**
     * Puts a new entry into the storage.
     */
    SYSTEM_STORAGE_PUT,

    /**
     * Deletes an entry from the storage.
     */
    SYSTEM_STORAGE_DELETE,

    /**
     * A dummy value used as a placeholder.
     */
    DUMMY;
}
