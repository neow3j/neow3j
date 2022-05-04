package io.neow3j.devpack.constants;

import io.neow3j.types.Hash160;

import static java.lang.String.format;

/**
 * The native smart contracts with their corresponding contract hash.
 */
public enum NativeContract {

    ContractManagement(new Hash160("fffdc93764dbaddd97c48f252a53ea4643faa3fd")),
    StdLib(new Hash160("acce6fd80d44e1796aa0c2c625e9e4e0ce39efc0")),
    CryptoLib(new Hash160("726cb6e0cd8628a1350a611384688911ab75f51b")),
    LedgerContract(new Hash160("da65b600f7124ce6c79950c1772a36403104f2be")),
    NeoToken(new Hash160("ef4073a0f2b305a38ec4050e4d3d28bc40ea63f5")),
    GasToken(new Hash160("d2a4cff31913016155e38e474a2c06d08be276cf")),
    PolicyContract(new Hash160("cc5e4edd9f5f8dba8bb65734541df7a1c081c67b")),
    RoleManagement(new Hash160("49cf4e5378ffcd4dec034fd98a174c5491e395e2")),
    OracleContract(new Hash160("fe924b7cfe89ddd271abaf7210a80a7e11178758")),
    // This value is required as default dummy value in the Permission annotation interface.
    None(Hash160.ZERO);

    private final Hash160 contractHash;

    NativeContract(Hash160 contractHash) {
        this.contractHash = contractHash;
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
