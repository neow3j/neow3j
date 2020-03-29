package io.neow3j.protocol;

import io.neow3j.constants.InteropServiceCode;
import io.neow3j.contract.ScriptBuilder;
import io.neow3j.contract.ScriptHash;

public class TestHelper {

    public static final ScriptHash NEO_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_NEO).toArray());

    public static final ScriptHash GAS_HASH = ScriptHash.fromScript(
            new ScriptBuilder().sysCall(InteropServiceCode.NEO_NATIVE_TOKENS_GAS).toArray());

}
