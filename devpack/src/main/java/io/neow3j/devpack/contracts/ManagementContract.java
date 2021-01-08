package io.neow3j.devpack.contracts;

import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.neo.Contract;

@ContractHash("0xcd97b70d82d69adfcd9165374109419fade8d6ab")
public class ManagementContract {

    public static native byte[] getHash();

    /**
     * Gets the contract with the given script hash.
     *
     * @param hash The contract's script hash.
     * @return the contract.
     */
    public static native Contract getContract(byte[] hash);

    /**
     * Deploys a new contract with the given NEF (Neo Executable Format) file and manifest.
     *
     * @param nefFile  The NEF file of the contract to deploy.
     * @param manifest The manifest of the contract to deploy.
     * @return The deployed <tt>Contract</tt>.
     */
    public static native Contract deploy(byte[] nefFile, String manifest);

    /**
     * Updates the calling smart contract with the given NEF file and manifest.
     * <p>
     * A deployed smart contract cannot be updated from the outside. Thus, if the contract should be
     * updatable, the logic has to be written into the contract during development.
     *
     * @param nefFile  The updated NEF file of the contract.
     * @param manifest The updated manifest of the contract.
     */
    public static native void update(byte[] nefFile, String manifest);

    /**
     * Destroys the calling smart contract.
     * <p>
     * A deployed smart contract cannot be destroyed from the outside. Thus, if the contract should
     * be destroyable, the logic has to be written into the contract during development.
     */
    public static native void destroy();

}
