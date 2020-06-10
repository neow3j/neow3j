package io.neow3j.devpack;

import io.neow3j.devpack.framework.EntryPoint;
import io.neow3j.devpack.framework.Manifest;
import io.neow3j.devpack.framework.Runtime;
import io.neow3j.devpack.framework.SmartContract;
import io.neow3j.devpack.framework.Storage;

/*
 * Goals of the prototype iteration:
 *
 *   Supported types: Arrays, Strings, Integer, int, Boolean, boolean, Byte, byte, Object
 *   Supported functionality: Compile to VM script, Entrypoint, Storage, CheckWitness, Events
 *
 *   Design decisions:
 *      - All field variables and methods are static. Contracts are not meant to have instances.
 *
 *
 * Later iterations include:
 *
 *   Supported types: Long long, List<>, Map<>, BigInteger?, Float?
 *   Supported functionality: Generate manifest, generate NEF, trigger types, all system calls.
 */
@Manifest(name = "hello_world", description = "The hello world contract.", author = "AxLabs")
public class HelloWorldContract extends SmartContract {

    private static byte[] ownerScriptHash = new byte[20];

    @EntryPoint
    public static void entryPoint(String method, Object[] params) {
        Runtime.checkWitness(ownerScriptHash);
        Storage.put("key", params[0]);
        Object value = Storage.get("key");
        Runtime.notify("Hello, world!", value);
    }

}
