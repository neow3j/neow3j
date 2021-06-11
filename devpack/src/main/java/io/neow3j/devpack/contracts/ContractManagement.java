package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xfffdc93764dbaddd97c48f252a53ea4643faa3fd")
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
     * @return The deployed {@code Contract}.
     */
    public static native Contract deploy(ByteString nefFile, String manifest);

    /**
     * Deploys a new contract with the given NEF file and manifest.
     *
     * @param nefFile  The NEF file of the contract to deploy.
     * @param manifest The manifest of the contract to deploy.
     * @param data     Data that is passed on to the {@code _deploy} method of the deployed contract
     *                 if it exists.
     * @return The deployed {@code Contract}.
     */
    public static native Contract deploy(ByteString nefFile, String manifest, Object data);

    /**
     * Updates the calling smart contract with the given NEF file and manifest.
     * <p>
     * A deployed smart contract cannot be updated from the outside. Thus, if the contract should be
     * updatable, the logic has to be written into the contract during development.
     *
     * @param nefFile  The updated NEF file of the contract.
     * @param manifest The updated manifest of the contract.
     */
    public static native void update(ByteString nefFile, String manifest);

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
    public static native void update(ByteString nefFile, String manifest, Object data);

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
