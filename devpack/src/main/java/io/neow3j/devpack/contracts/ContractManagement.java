package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.NativeContract;

import static io.neow3j.devpack.constants.NativeContract.ContractManagementScriptHash;

/**
 * Represents an interface to the native ContractManagement contract that is used to manage all deployed smart
 * contracts.
 */
@NativeContract(ContractManagementScriptHash)
public class ContractManagement extends ContractInterface {

    public ContractManagement() {
        super(null);
    }

    /**
     * Gets the contract with the given script hash.
     *
     * @param hash the contract's script hash.
     * @return the contract.
     */
    public native Contract getContract(Hash160 hash);

    /**
     * Deploys a new contract with the given NEF file and manifest.
     *
     * @param nefFile  the NEF file of the contract to deploy.
     * @param manifest the manifest of the contract to deploy.
     * @return the deployed {@code Contract}.
     */
    public native Contract deploy(ByteString nefFile, String manifest);

    /**
     * Deploys a new contract with the given NEF file and manifest.
     *
     * @param nefFile  the NEF file of the contract to deploy.
     * @param manifest the manifest of the contract to deploy.
     * @param data     data that is passed on to the {@code _deploy} method of the deployed contract if it exists.
     * @return the deployed {@code Contract}.
     */
    public native Contract deploy(ByteString nefFile, String manifest, Object data);

    /**
     * Updates the calling smart contract with the given NEF file and manifest.
     * <p>
     * A deployed smart contract cannot be updated from the outside. Thus, if the contract should be updatable, the
     * logic has to be written into the contract during development.
     *
     * @param nefFile  the updated NEF file of the contract.
     * @param manifest the updated manifest of the contract.
     */
    public native void update(ByteString nefFile, String manifest);

    /**
     * Updates the calling smart contract with the given NEF file and manifest.
     * <p>
     * A deployed smart contract cannot be updated from the outside. Thus, if the contract should be updatable, the
     * logic has to be written into the contract during development.
     *
     * @param nefFile  the updated NEF file of the contract.
     * @param manifest the updated manifest of the contract.
     * @param data     data passed {@code update} method of the contract being deployed.
     */
    public native void update(ByteString nefFile, String manifest, Object data);

    /**
     * Destroys the calling smart contract.
     * <p>
     * A deployed smart contract cannot be destroyed from the outside. Thus, if the contract should be destroyable,
     * the logic has to be written into the contract during development.
     */
    public native void destroy();

    /**
     * @return the minumum deployment fee.
     */
    public native int getMinimumDeploymentFee();

}
