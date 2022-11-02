package io.neow3j.devpack.constants;

import io.neow3j.types.Hash160;

import static java.lang.String.format;

/**
 * The native smart contracts with their corresponding contract hash.
 */
public enum NativeContract {

    ContractManagement(NativeContract.ContractManagementScriptHash),
    StdLib(NativeContract.StdLibScriptHash),
    CryptoLib(NativeContract.CryptoLibScriptHash),
    LedgerContract(NativeContract.LedgerContractScriptHash),
    NeoToken(NativeContract.NeoTokenScriptHash),
    GasToken(NativeContract.GasTokenScriptHash),
    PolicyContract(NativeContract.PolicyContractScriptHash),
    RoleManagement(NativeContract.RoleManagementScriptHash),
    OracleContract(NativeContract.OracleContractScriptHash),
    // This value is required as default dummy value in the Permission annotation interface.
    None(Hash160.ZERO.toString());

    public static final String ContractManagementScriptHash = "0xfffdc93764dbaddd97c48f252a53ea4643faa3fd";
    public static final String StdLibScriptHash = "0xacce6fd80d44e1796aa0c2c625e9e4e0ce39efc0";
    public static final String CryptoLibScriptHash = "0x726cb6e0cd8628a1350a611384688911ab75f51b";
    public static final String LedgerContractScriptHash = "0xda65b600f7124ce6c79950c1772a36403104f2be";
    public static final String NeoTokenScriptHash = "0xef4073a0f2b305a38ec4050e4d3d28bc40ea63f5";
    public static final String GasTokenScriptHash = "0xd2a4cff31913016155e38e474a2c06d08be276cf";
    public static final String PolicyContractScriptHash = "0xcc5e4edd9f5f8dba8bb65734541df7a1c081c67b";
    public static final String RoleManagementScriptHash = "0x49cf4e5378ffcd4dec034fd98a174c5491e395e2";
    public static final String OracleContractScriptHash = "0xfe924b7cfe89ddd271abaf7210a80a7e11178758";

    private final Hash160 contractHash;

    NativeContract(String contractHash) {
        this.contractHash = new Hash160(contractHash);
    }

    public Hash160 getContractHash() {
        return contractHash;
    }

    public static NativeContract valueOf(Hash160 contractHash) {
        for (NativeContract nativeContract : NativeContract.values()) {
            if (nativeContract.getContractHash().equals(contractHash)) {
                return nativeContract;
            }
        }
        throw new IllegalArgumentException(
                format("There exists no native contract with the provided hash (%s)", contractHash));
    }

}
