package io.neow3j.devpack.template;

import io.neow3j.devpack.framework.EntryPoint;
import io.neow3j.devpack.framework.Manifest;
import io.neow3j.devpack.framework.SmartContract;
import io.neow3j.devpack.framework.Storage;
import io.neow3j.devpack.framework.Storage.StorageContext;

/*
 * Goals of the prototype iteration:
 *   Supported types: Arrays, Strings, Integer, int, Boolean, boolean, Byte, byte, Object
 *   Supported functionality: Compile to VM script, entry point, Storage, CheckWitness, Events,
 *                            one class, one method, arithmetic.
 *
 * Later iterations include:
 *   Supported types: Long long, List<>, Map<>, BigInteger
 *   Supported functionality: Generate manifest, generate NEF, trigger types, all system calls,
 *                            if-else, for loops, multiple methods, multiple classes,
 *                            inheritance, exception throwing.
 *
 *   Design decisions:
 *      - All field variables and methods are static. Contracts are not meant to have instances.
 */
@Manifest(name = "example", description = "An example contract.", author = "AxLabs")
public class ExampleContract extends SmartContract {

    @EntryPoint
    public static boolean entryPoint(String method, Object[] params) {
        StorageContext context = Storage.getStorageContext();
        byte[] b = new byte[]{0x01};
        Storage.put(context, b, "value");
        return true;
    }

}
