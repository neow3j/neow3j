package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ContractInterface;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;
import io.neow3j.devpack.neo.Contract;

@ContractHash("0xa501d7d7d10983673b61b7a2d3a813b36f9f0e43")
public class ContractManagement extends ContractInterface {

    /**
     * Gets the contract with the given script hash.
     *
     * @param hash The contract's script hash.
     * @return the contract.
     */
    public static native Contract getContract(Hash160 hash);

    /**
     * Deploys a new contract with the given NEF file and manifest.
     *
     * @param nefFile  The NEF file of the contract to deploy.
     * @param manifest The manifest of the contract to deploy.
     * @return The deployed <tt>Contract</tt>.
     */
    public static native Contract deploy(byte[] nefFile, String manifest);

    /**
     * Deploys a new contract with the given NEF file and manifest.
     *
     * @param nefFile  The NEF file of the contract to deploy.
     * @param manifest The manifest of the contract to deploy.
     * @param data     Data that is passed on to the {@code _deploy} method of the deployed contract
     *                 if it exists.
     * @return The deployed <tt>Contract</tt>.
     */
    public static native Contract deploy(byte[] nefFile, String manifest, Object data);

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
     * Updates the calling smart contract with the given NEF file and manifest.
     * <p>
     * A deployed smart contract cannot be updated from the outside. Thus, if the contract should be
     * updatable, the logic has to be written into the contract during development.
     *
     * @param nefFile  The updated NEF file of the contract.
     * @param manifest The updated manifest of the contract.
     * @param data     Data passed {@code update} method of the contract being deployed.
     */
    public static native void update(byte[] nefFile, String manifest, Object data);

    /**
     * Destroys the calling smart contract.
     * <p>
     * A deployed smart contract cannot be destroyed from the outside. Thus, if the contract should
     * be destroyable, the logic has to be written into the contract during development.
     */
    public static native void destroy();

    /**
     * Gets the minimum fee needed for deploying a contract.
     *
     * @return the minumum deployment fee.
     */
    public static native int getMinimumDeploymentFee();

}
