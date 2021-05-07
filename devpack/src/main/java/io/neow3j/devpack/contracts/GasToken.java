package io.neow3j.devpack.contracts;

import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.annotations.ContractHash;

@ContractHash("0xd2a4cff31913016155e38e474a2c06d08be276cf")
public class GasToken extends FungibleToken {

    /**
     * Takes GAS from the given account, if available, and adds it to the current invocation, i.e.,
     * refuels the current invocation's GAS amount.
     * <p>
     * A witness for the given account has to be available in the invocation.
     *
     * @param account The account to take the GAS from.
     * @param amount  The amount of GAS used for refueling.
     */
    public static native void refuel(Hash160 account, int amount);
}
