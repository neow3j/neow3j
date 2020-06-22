package io.neow3j.devpack.template;

import io.neow3j.devpack.framework.Manifest;
import io.neow3j.devpack.framework.SmartContract;
import io.neow3j.devpack.framework.Storage;
import io.neow3j.devpack.framework.annotations.EntryPoint;

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
@Manifest(name = "hello_world", description = "The hello world contract.", author = "AxLabs")
public class HelloWorldContract extends SmartContract {

    @EntryPoint
    public static boolean entryPoint(String method, Object[] params) {
        Storage.put("Hello", "World");
        return true;
    }

}
