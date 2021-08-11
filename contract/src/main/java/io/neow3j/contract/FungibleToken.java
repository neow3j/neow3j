package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static io.neow3j.transaction.AccountSigner.calledByEntry;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static java.util.Arrays.asList;

/**
 * Represents a fungible token contract that is compliant with the NEP-17 standard and provides
 * methods to invoke it.
 */
public class FungibleToken extends Token {

    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";

    /**
     * Constructs a new {@code FungibleToken} representing the token contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash
     * @param neow       the {@link Neow3j} instance to use for invocations.
     */
    public FungibleToken(Hash160 scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Gets the token balance for the given account.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the
     * Neo node.
     *
     * @param account the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(Account account) throws IOException,
            UnexpectedReturnTypeException {
        return getBalanceOf(account.getScriptHash());
    }

    /**
     * Gets the token balance for the given account script hash.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the
     * Neo node.
     *
     * @param scriptHash the script hash of the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(Hash160 scriptHash) throws IOException,
            UnexpectedReturnTypeException {

        ContractParameter ofParam = hash160(scriptHash);
        return callFuncReturningInt(BALANCE_OF, ofParam);
    }

    /**
     * Gets the token balance for the given wallet, i.e., all accounts in the wallet.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are sent to the
     * Neo node.
     *
     * @param wallet the wallet to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(Wallet wallet) throws IOException,
            UnexpectedReturnTypeException {

        BigInteger sum = BigInteger.ZERO;
        for (Account a : wallet.getAccounts()) {
            sum = sum.add(getBalanceOf(a.getScriptHash()));
        }
        return sum;
    }

    /**
     * Creates a transfer transaction.
     *
     * @param to     the address of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @param from   the sender account.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, BigInteger amount)
            throws IOException {

        return transfer(from, to, amount, null);
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * Only use this method when the receiver is a deployed smart contract to avoid unnecessary
     * additional fees. Otherwise, use the method without a contract parameter for data.
     *
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer in token fractions.
     * @param data   the data that is passed to the {@code onPayment} method of the receiving
     *               smart contract.
     * @param from   the sender account.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, BigInteger amount,
            ContractParameter data) throws IOException {

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "The parameter amount must be greater than or equal to 0");
        }
        byte[] transferScript = buildSingleTransferScript(from, to, amount, data);
        return new TransactionBuilder(neow3j).script(transferScript).signers(calledByEntry(from));
    }

    private byte[] buildSingleTransferScript(Account acc, Hash160 to, BigInteger amount,
            ContractParameter data) {
        List<ContractParameter> params;
        params = asList(hash160(acc), hash160(to), integer(amount), data);

        return new ScriptBuilder().contractCall(scriptHash, TRANSFER, params).toArray();
    }

}
