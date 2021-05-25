package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;

/**
 * Base class for fungible token contracts that are compliant with the NEP-17 standard. Extend this
 * class in combination with the {@link ContractHash} annotation to create an "interface" to a
 * NEP-17 token contract on the Neo blockchain. Examples are the {@link NeoToken} and
 * {@link GasToken} contracts.
 */
public abstract class FungibleToken extends ContractInterface {

    /**
     * Gets the symbol of this token.
     *
     * @return the symbol.
     */
    public static native String symbol();

    /**
     * Gets the number of decimals of this token.
     *
     * @return the number of decimals.
     */
    public static native int decimals();

    /**
     * Gets the total supply of this token.
     *
     * @return the total supply.
     */
    public static native int totalSupply();

    /**
     * Gets the token balance of the given account.
     *
     * @param scriptHash The script hash of the account to get the balance for.
     * @return the account's balance.
     */
    public static native int balanceOf(Hash160 scriptHash);

    /**
     * Transfers the token {@code amount} from the {@code from} account to the {@code to} account.
     * The {@code data} is passed along with the call. For example, if the {@code to} account is a
     * contract it is provided as an argument to the contract's {@code onPayment} method.
     *
     * @param from The hash of the payment originator.
     * @param to The hash of the receiver.
     * @param amount The token amount to transfer.
     * @param data The data to pass along with the transfer.
     * @return True, if the transfer was successful. False, otherwise.
     */
    public static native boolean transfer(Hash160 from, Hash160 to, int amount, Object data);
}
