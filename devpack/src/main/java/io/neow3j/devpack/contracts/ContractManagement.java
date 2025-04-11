package io.neow3j.devpack.contracts;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Contract;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.constants.NativeContract;
import io.neow3j.devpack.annotations.CallFlags;

import static io.neow3j.devpack.constants.CallFlags.ReadOnly;
import static io.neow3j.devpack.constants.CallFlags.All;

/**
 * Represents an interface to the native ContractManagement contract that is used to manage all deployed smart
 * contracts.
 */
public class ContractManagement extends ContractInterface {

    /**
     * Initializes an interface to the native ContractManagement contract.
     */
    public ContractManagement() {
        super(NativeContract.ContractManagementScriptHash);
    }

    /**
     * Gets the contract with the given script hash.
     *
     * @param hash the contract's script hash.
     * @return the contract.
     */
    @CallFlags(ReadOnly)
    public native Contract getContract(Hash160 hash);

    /**
     * Gets the deployed contract with the specified id.
     *
     * @param id the contract id.
     * @return the contract.
     */
    @CallFlags(ReadOnly)
    public native Contract getContractById(int id);

    /**
     * Gets the ids and hashes of all non-native deployed contracts.
     * <p>
     * Each iterator entry is a struct with the id and the contract hash.
     * <p>
     * Note, that the id is stored as a {@link ByteString} in big-endian format. Thus, when using that id as an
     * integer within contract code, it needs to be reversed before converting it to an integer value. E.g., as follows:
     * <pre>
     * byte[] id = struct.key.toByteArray();
     * Helper.reverse(id);
     * int idAsInt = new ByteString(id).toInt();
     * </pre>
     *
     * @return an iterator of the ids and hashes of all non-native deployed contracts.
     */
    @CallFlags(ReadOnly)
    public native Iterator<Iterator.Struct<ByteString, Hash160>> getContractHashes();

    /**
     * Checks if a method exists in a contract.
     *
     * @param contractHash the contract hash.
     * @param method       the method.
     * @param paramCount   the number of parameters.
     * @return true if the method exists. False otherwise.
     */
    @CallFlags(ReadOnly)
    public native boolean hasMethod(Hash160 contractHash, String method, int paramCount);

    /**
     * Deploys a new contract with the given NEF file and manifest.
     *
     * @param nefFile  the NEF file of the contract to deploy.
     * @param manifest the manifest of the contract to deploy.
     * @return the deployed {@code Contract}.
     */
    @CallFlags(All)
    public native Contract deploy(ByteString nefFile, String manifest);

    /**
     * Deploys a new contract with the given NEF file and manifest.
     *
     * @param nefFile  the NEF file of the contract to deploy.
     * @param manifest the manifest of the contract to deploy.
     * @param data     data that is passed on to the {@code _deploy} method of the deployed contract if it exists.
     * @return the deployed {@code Contract}.
     */
    @CallFlags(All)
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
    @CallFlags(All)
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
    @CallFlags(All)
    public native void update(ByteString nefFile, String manifest, Object data);

    /**
     * Destroys the calling smart contract.
     * <p>
     * A deployed smart contract cannot be destroyed from the outside. Thus, if the contract should be destroyable,
     * the logic has to be written into the contract during development.
     */
    @CallFlags(All)
    public native void destroy();

    /**
     * @return the minimum deployment fee.
     */
    @CallFlags(ReadOnly)
    public native int getMinimumDeploymentFee();

}
