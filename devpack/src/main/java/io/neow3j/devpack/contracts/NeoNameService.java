package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.List;

/**
 * Represents a NameService interface.
 * <p>
 * This interface is based on an implemented
 * <a href="https://github.com/neo-project/non-native-contracts/blob/c4035e9d458c903fd76a08fb53e6f338c2f69dc4/src/NameService/NameService.cs">NNS</a>
 * contract.
 */
public abstract class NeoNameService extends NonDivisibleNonFungibleToken {

    /**
     * Adds a root domain.
     * <p>
     * This method is restricted to the Neo committee.
     *
     * @param root the root domain to add.
     */
    public static native void addRoot(String root);

    /**
     * Sets the fees required to register or renew a domain name based on its length.
     * <p>
     * This method is restricted to the Neo committee.
     *
     * @param priceList the price list.
     */
    public static native void setPrice(List<Integer> priceList);

    /**
     * Gets the fee required to register or renew a domain name based on its length.
     *
     * @param length the length of the domain name.
     * @return the fee to register or renew a domain name.
     */
    public static native int getPrice(int length);

    /**
     * Checks if a domain name is available.
     *
     * @param name the domain name.
     * @return true if the domain name is available. False, otherwise.
     */
    public static native boolean isAvailable(String name);

    /**
     * Registers a second-level domain name.
     *
     * @param name  the domain name to register.
     * @param owner the owner of the domain name.
     * @return true if the registration was successful. False, otherwise.
     */
    public static native boolean register(String name, Hash160 owner);

    /**
     * Extends the validity of the domain name by one year.
     *
     * @param name the domain name.
     * @return the expiration time in milliseconds.
     */
    public static native int renew(String name);

    /**
     * Sets the administrator of a domain name.
     *
     * @param name  the domain name.
     * @param admin the administrator of the domain name.
     */
    public static native void setAdmin(String name, Hash160 admin);

    /**
     * Sets a data type for the domain name.
     *
     * @param name the domain name.
     * @param type the data type.
     * @param data the data.
     */
    public static native void setRecord(String name, int type, String data);

    /**
     * Gets the data type for the domain name.
     *
     * @param name the domain name.
     * @param type the data type.
     * @return the data.
     */
    public static native String getRecord(String name, int type);

    /**
     * Deletes a data type for the domain name.
     *
     * @param name the domain name.
     * @param type the data type.
     */
    public static native void deleteRecord(String name, int type);

    /**
     * Resolves the domain name data.
     *
     * @param name the domain name.
     * @param type the data type.
     * @return the resolved domain name.
     */
    public static native String resolve(String name, int type);

}
