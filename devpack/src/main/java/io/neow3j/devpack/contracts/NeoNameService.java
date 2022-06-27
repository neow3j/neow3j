package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;

/**
 * Represents a NameService interface. Initialize this class with a {@link Hash160} to create an "interface" to a
 * NameService contract contract on the Neo blockchain.
 * <p>
 * When this class is extended, the constructor of the extending class must take exactly one parameter of type
 * {@link Hash160} and pass it to the {@code super()} call without any additional logic.
 * <p>
 * This interface is based on an implemented
 * <a href="https://github.com/neo-project/non-native-contracts/blob/c4035e9d458c903fd76a08fb53e6f338c2f69dc4/src/NameService/NameService.cs">NNS</a>
 * contract.
 */
public class NeoNameService extends NonDivisibleNonFungibleToken {

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

}
