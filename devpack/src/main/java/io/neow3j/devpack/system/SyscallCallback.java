package io.neow3j.devpack.system;

public class SyscallCallback {

    public static final int SYSTEM_BINARY_SERIALIZE = 0x24011c3f;
    public static final int SYSTEM_BINARY_DESERIALIZE = 0xdfd07c52;
    public static final int SYSTEM_BINARY_BASE64ENCODE = 0x7653bfac;
    public static final int SYSTEM_BINARY_BASE64DECODE = 0xc384a3db;
    public static final int SYSTEM_BLOCKCHAIN_GETHEIGHT = 0x1f72f57e;
    public static final int SYSTEM_BLOCKCHAIN_GETBLOCK = 0x2d924783;
    public static final int SYSTEM_BLOCKCHAIN_GETTRANSACTION = 0x488d55e6;
    public static final int SYSTEM_BLOCKCHAIN_GETTRANSACTIONHEIGHT = 0xb188324a;
    public static final int SYSTEM_BLOCKCHAIN_GETTRANSACTIONFROMBLOCK = 0x69fd567e;
    public static final int SYSTEM_BLOCKCHAIN_GETCONTRACT = 0x414bc5a9;
    public static final int SYSTEM_CONTRACT_ISSTANDARD = 0x859d6bd7;
    public static final int SYSTEM_CONTRACT_CREATESTANDARDACCOUNT = 0x28799cf;
    public static final int NEO_CRYPTO_RIPEMD160 = 0xd2d6d126;
    public static final int NEO_CRYPTO_SHA256 = 0x1174acd7;
    public static final int NEO_CRYPTO_VERIFYWITHECDSASECP256R1 = 0x780d4495;
    public static final int NEO_CRYPTO_VERIFYWITHECDSASECP256K1 = 0xb7533c7e;
    public static final int NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256R1 = 0xafef8d13;
    public static final int NEO_CRYPTO_CHECKMULTISIGWITHECDSASECP256K1 = 0xb2efc657;
    public static final int SYSTEM_JSON_SERIALIZE = 0x4b268d24;
    public static final int SYSTEM_JSON_DESERIALIZE = 0xe479ca7;
    public static final int SYSTEM_RUNTIME_PLATFORM = 0xf6fc79b2;
    public static final int SYSTEM_RUNTIME_GETTRIGGER = 0xa0387de9;
    public static final int SYSTEM_RUNTIME_GETTIME = 0x388c3b7;
    public static final int SYSTEM_RUNTIME_GETSCRIPTCONTAINER = 0x3008512d;
    public static final int SYSTEM_RUNTIME_GETEXECUTINGSCRIPTHASH = 0x74a8fedb;
    public static final int SYSTEM_RUNTIME_GETCALLINGSCRIPTHASH = 0x3c6e5339;
    public static final int SYSTEM_RUNTIME_GETENTRYSCRIPTHASH = 0x38e2b4f9;
    public static final int SYSTEM_RUNTIME_CHECKWITNESS = 0x8cec27f8;
    public static final int SYSTEM_RUNTIME_GETINVOCATIONCOUNTER = 0x43112784;
    public static final int SYSTEM_RUNTIME_GETNOTIFICATIONS = 0xf1354327;
    public static final int SYSTEM_RUNTIME_GASLEFT = 0xced8881;

}
