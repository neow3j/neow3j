package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Iterator;
import io.neow3j.devpack.List;
import io.neow3j.devpack.Map;

/**
 * Represents an interface to the official NeoNameService smart contract.
 */
public class NeoNameService extends NonDivisibleNonFungibleToken {

    /**
     * Initializes an interface to the official NeoNameService smart contract that is deployed on mainnet.
     * <p>
     * Use {@link NeoNameService#NeoNameService(Hash160)} or {@link NeoNameService#NeoNameService(String)} to
     * initialise an interface to a NeoNameService contract with a different contract hash, e.g., when testing with
     * testnet or a privatenet.
     */
    public NeoNameService() {
        super("0x50ac1c37690cc2cfc594472833cf57505d5f46de");
    }

    /**
     * Initializes an interface to a NeoNameService smart contract.
     * <p>
     * For the official NeoNameService smart contract deployed on mainnet use {@link NeoNameService#NeoNameService()}.
     * <p>
     * Use this constructor only with a string literal.
     *
     * @param contractHash the big-endian contract script hash.
     */
    public NeoNameService(String contractHash) {
        super(contractHash);
    }

    /**
     * Initializes an interface to a NeoNameService smart contract.
     * <p>
     * For the official NeoNameService smart contract deployed on mainnet use {@link NeoNameService#NeoNameService()}.
     *
     * @param contractHash the contract script hash.
     */
    public NeoNameService(Hash160 contractHash) {
        super(contractHash);
    }

    /**
     * Adds a root domain.
     * <p>
     * This method is restricted to the Neo committee.
     *
     * @param root the root domain to add.
     */
    public native void addRoot(String root);

    /**
     * @return an iterator to iterate over all roots.
     */
    public native Iterator<String> roots();

    /**
     * Sets the fees required to register or renew a domain name based on its length.
     * <p>
     * This method is restricted to the Neo committee.
     *
     * @param priceList the price list.
     */
    public native void setPrice(List<Integer> priceList);

    /**
     * Gets the fee required to register or renew a domain name based on its length.
     *
     * @param length the length of the domain name.
     * @return the fee to register or renew a domain name.
     */
    public native int getPrice(int length);

    /**
     * Checks if a domain name is available.
     *
     * @param name the domain name.
     * @return true if the domain name is available. False, otherwise.
     */
    public native boolean isAvailable(String name);

    /**
     * Registers a second-level domain name.
     *
     * @param name  the domain name to register.
     * @param owner the owner of the domain name.
     * @return true if the registration was successful. False, otherwise.
     */
    public native boolean register(String name, Hash160 owner);

    /**
     * Extends the validity of the domain name by one year.
     *
     * @param name the domain name.
     * @return the expiration time in milliseconds.
     */
    public native int renew(String name);

    /**
     * Extends the validity of the domain name.
     *
     * @param name  the domain name.
     * @param years the number of years to renew this domain name. Has to be in the range of 1 to 10.
     * @return the expiration time in milliseconds.
     */
    public native int renew(String name, int years);

    /**
     * Sets the administrator of a domain name.
     *
     * @param name  the domain name.
     * @param admin the administrator of the domain name.
     */
    public native void setAdmin(String name, Hash160 admin);

    /**
     * Sets a data type for the domain name.
     *
     * @param name the domain name.
     * @param type the data type.
     * @param data the data.
     */
    public native void setRecord(String name, int type, String data);

    /**
     * Gets the data type for the domain name.
     *
     * @param name the domain name.
     * @param type the data type.
     * @return the data.
     */
    public native String getRecord(String name, int type);

    /**
     * Gets an iterator to iterate over all records of the given domain name.
     *
     * @param name the domain name.
     * @return an iterator to get all records of the domain name.
     */
    public native Iterator<String> getAllRecords(String name);

    /**
     * Deletes a data type for the domain name.
     *
     * @param name the domain name.
     * @param type the data type.
     */
    public native void deleteRecord(String name, int type);

    /**
     * Resolves the domain name data.
     *
     * @param name the domain name.
     * @param type the data type.
     * @return the resolved domain name.
     */
    public native String resolve(String name, int type);

    /**
     * Gets the owner of the domain name.
     *
     * @param name the domain name.
     * @return the owner of the domain name.
     */
    public native Hash160 ownerOf(String name);

    /**
     * Gets the properties of the domain name.
     *
     * @param name the domain name.
     * @return the properties of the domain name.
     */
    public native Map<String, Object> properties(String name);

    /**
     * Transfers the domain name.
     *
     * @param to   the hash of the receiver.
     * @param name the domain name to transfer.
     * @param data optional data. This data is passed to the {@code onNEP11Payment} method, if the receiver is a
     *             deployed contract.
     * @return true if the transfer is successful. False otherwise, e.g., if the receiver is a contract and does not
     * accept the token in its {@code onNEP11Payment} method.
     */
    public native boolean transfer(Hash160 to, String name, Object data);

}
