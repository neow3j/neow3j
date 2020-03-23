package io.neow3j.model.types;

// TODO: Transform into subclass of Nep5Token.
public class NEOAsset {

    public static final String NAME = "NEO";

    // TODO: This hash is not up to date. Use the following to retrieve the correct hash:
    //  ScriptHash.fromScript(new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_NEO).toArray())
    public static final String HASH_ID = "43cf98eddbe047e198a3e5d57006311442a0ca15";
}
