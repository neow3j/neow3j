package io.neow3j.transaction;

import io.neow3j.script.VerificationScript;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

/**
 * A signer of a transaction. It defines a in which scope the witness/signature of an account is valid, i.e., which
 * contracts can use the witness in an invocation.
 * <p>
 * If an {@code AccountSigner} is created with an private key-holding {@code Account}, it can be used to
 * automatically sign a transaction via {@link TransactionBuilder#sign()}.
 */
public class AccountSigner extends Signer {

    private Account account;

    private AccountSigner(Hash160 signerHash, WitnessScope scope) {
        super(signerHash, scope);
        account = Account.fromAddress(signerHash.toAddress());
    }

    private AccountSigner(Account account, WitnessScope scope) {
        super(account.getScriptHash(), scope);
        this.account = account;
    }

    /**
     * @return the account of this signer.
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Checks whether this signer's account holds a verification script.
     * <p>
     * A hash-only account, created from an address or script hash, does not. It can still be used as a signer, but
     * its witness size is unknown. Network fee calculation then assumes a standard single-sig verification script,
     * which is only accurate for an ordinary address. An account created from a verification script, a public key,
     * or a multi-sig script does hold one, so the fee can be sized from that script and the signer can be exported
     * in a contract parameters context.
     *
     * @return true if the account has a verification script.
     */
    public boolean hasVerificationScript() {
        return account.getVerificationScript() != null;
    }

    /**
     * Checks whether this signer can be witnessed locally by {@link TransactionBuilder#sign()}.
     * <p>
     * That requires a decrypted single-sig key whose verification script is a standard signature check. A hash-only
     * account, an account with only a verification script, an account whose private key is still encrypted, and a
     * multi-sig account are not locally signable.
     *
     * @return true if {@link TransactionBuilder#sign()} can produce this signer's witness.
     */
    public boolean canSign() {
        if (account.isMultiSig() || account.getECKeyPair() == null) {
            return false;
        }
        VerificationScript verificationScript = account.getVerificationScript();
        return verificationScript != null && verificationScript.isSingleSigScript();
    }

    /**
     * Creates a signer for the given account with no witness scope ({@link WitnessScope#NONE}).
     * <p>
     * The signature of this signer is only used for transactions and is disabled in contracts.
     *
     * @param account the signer account.
     * @return the signer.
     */
    public static AccountSigner none(Account account) {
        return new AccountSigner(account, WitnessScope.NONE);
    }

    /**
     * Creates a signer for the given account with no witness scope ({@link WitnessScope#NONE}).
     * <p>
     * The signature of this signer is only used for transactions and is disabled in contracts.
     *
     * @param accountHash the script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner none(Hash160 accountHash) {
        return new AccountSigner(accountHash, WitnessScope.NONE);
    }

    /**
     * Creates a signer for the given account with a scope ({@link WitnessScope#CALLED_BY_ENTRY}) that only allows
     * the entry point contract to use this signer's witness.
     *
     * @param account the signer account.
     * @return the signer.
     */
    public static AccountSigner calledByEntry(Account account) {
        return new AccountSigner(account, WitnessScope.CALLED_BY_ENTRY);
    }

    /**
     * Creates a signer for the given account with a scope ({@link WitnessScope#CALLED_BY_ENTRY}) that only allows
     * the entry point contract to use this signer's witness.
     *
     * @param accountHash the script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner calledByEntry(Hash160 accountHash) {
        return new AccountSigner(accountHash, WitnessScope.CALLED_BY_ENTRY);
    }

    /**
     * Creates a signer for the given account with global witness scope ({@link WitnessScope#GLOBAL}).
     *
     * @param account the account.
     * @return the signer.
     */
    public static AccountSigner global(Account account) {
        return new AccountSigner(account, WitnessScope.GLOBAL);
    }

    /**
     * Creates a signer for the given account with global witness scope ({@link WitnessScope#GLOBAL}).
     *
     * @param accountHash the script hash of the signer account.
     * @return the signer.
     */
    public static AccountSigner global(Hash160 accountHash) {
        return new AccountSigner(accountHash, WitnessScope.GLOBAL);
    }

}
