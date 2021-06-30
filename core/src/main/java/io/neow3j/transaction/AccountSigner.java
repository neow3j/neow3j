package io.neow3j.transaction;

import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

/**
 * A signer of a transaction. It defines a in which scope the witness/signature of an account is
 * valid, i.e., which contracts can use the witness in an invocation.
 */
public class AccountSigner extends Signer {

    private AccountSigner(Hash160 signerHash, WitnessScope scope) {
        super(signerHash, scope);
    }

    /**
     * Creates a signer for the given account with no witness scope ({@link WitnessScope#NONE}),
     * also called the fee only witness scope.
     *
     * @param account The signer account.
     * @return the signer.
     */
    public static AccountSigner none(Account account) {
        return none(account.getScriptHash());
    }

    /**
     * Creates a signer for the given account with no witness scope ({@link WitnessScope#NONE}),
     * also called the fee only witness scope.
     *
     * @param account The script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner none(Hash160 account) {
        return new AccountSigner(account, WitnessScope.NONE);
    }

    /**
     * Creates a signer for the given account with a scope ({@link WitnessScope#CALLED_BY_ENTRY})
     * that only allows the entry point contract to use this signer's witness.
     *
     * @param account The signer account.
     * @return the signer.
     */
    public static AccountSigner calledByEntry(Account account) {
        return calledByEntry(account.getScriptHash());
    }

    /**
     * Creates a signer for the given account with a scope ({@link WitnessScope#CALLED_BY_ENTRY})
     * that only allows the entry point contract to use this signer's witness.
     *
     * @param account The script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner calledByEntry(Hash160 account) {
        return new AccountSigner(account, WitnessScope.CALLED_BY_ENTRY);
    }

    /**
     * Creates a signer for the given account with global witness scope
     * ({@link WitnessScope#GLOBAL}).
     *
     * @param account The account.
     * @return the signer.
     */
    public static AccountSigner global(Account account) {
        return global(account.getScriptHash());
    }

    /**
     * Creates a signer for the given account with global witness scope
     * ({@link WitnessScope#GLOBAL}).
     *
     * @param account The account's script hash.
     * @return the signer.
     */
    public static AccountSigner global(Hash160 account) {
        return new AccountSigner(account, WitnessScope.GLOBAL);
    }

}
