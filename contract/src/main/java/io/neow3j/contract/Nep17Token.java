package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.transaction.Signer;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a NEP-17 token contract and provides methods to invoke it.
 */
public class Nep17Token extends Token {

    private static final String BALANCE_OF = "balanceOf";
    private static final String TRANSFER = "transfer";

    /**
     * Constructs a new {@code Nep17Token} representing the token contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash
     * @param neow       the {@link Neow3j} instance to use for invocations.
     */
    public Nep17Token(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Gets the token balance for the given account.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
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
     * Gets the token balance for the given account address.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param address the address of the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(String address) throws IOException,
            UnexpectedReturnTypeException {
        return getBalanceOf(ScriptHash.fromAddress(address));
    }

    /**
     * Gets the token balance for the given account script hash.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param scriptHash the script hash of the account to fetch the balance for.
     * @return the token balance.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger getBalanceOf(ScriptHash scriptHash) throws IOException,
            UnexpectedReturnTypeException {

        ContractParameter ofParam = ContractParameter.hash160(scriptHash);
        return callFuncReturningInt(BALANCE_OF, ofParam);
    }

    /**
     * Gets the token balance for the given wallet, i.e., all accounts in the wallet.
     * <p>
     * The token amount is returned in token fractions. E.g., an amount of 1 GAS is returned as
     * 1*10^8 GAS fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
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
     * Creates a transfer transaction that uses all accounts in the wallet to cover the amount.
     * <p>
     * The default account is used first to cover the amount. If it cannot cover the full amount,
     * the other accounts in the wallet are iterated one by one to cover the remaining amount. If
     * the amount can be covered, all necessary transfers are packed in one transaction.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the address of the receiver.
     * @param amount the amount to transfer as a decimal number (not token fractions).
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, String to, BigDecimal amount)
            throws IOException {
        return transfer(wallet, ScriptHash.fromAddress(to), amount);
    }

    /**
     * Creates a transfer transaction that uses all accounts in the wallet to cover the amount.
     * <p>
     * The default account is used first to cover the amount. If it cannot cover the full amount,
     * the other accounts in the wallet are iterated one by one to cover the remaining amount. If
     * the amount can be covered, all necessary transfers packed in one transaction.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer as a decimal number (not token fractions).
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, ScriptHash to, BigDecimal amount)
            throws IOException {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "The parameter amount must be greater than or equal to 0");
        }
        if (!amountDecimalsIsValid(amount)) {
            throw new IllegalArgumentException("The amount contains more decimal places than this token " +
                    "can handle. This token has " + getDecimals() + " decimals. The amount provided " +
                    "had " + amount.stripTrailingZeros().scale() + " decimal places.");
        }

        List<Account> accountsOrdered = new ArrayList<>(wallet.getAccounts());
        accountsOrdered.remove(wallet.getDefaultAccount());
        accountsOrdered.add(0, wallet.getDefaultAccount()); // Start with default account.
        return buildMultiTransferInvocation(wallet, to, amount, accountsOrdered);
    }

    /**
     * Creates a transfer transaction that uses the provided accounts.
     * <p>
     * The accounts are used in the order provided to cover the transaction amount. If the first
     * account cannot cover the full amount, the second account is used to cover the remaining
     * amount and so on. If the amount can be covered by the specified accounts, all necessary
     * transfers are packed in one transaction.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the address of the receiver.
     * @param amount the amount to transfer as a decimal number (not token fractions).
     * @param from   the script hashes of the accounts in the wallet that should be used to cover
     *               the amount.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromSpecificAccounts(Wallet wallet, String to,
            BigDecimal amount, ScriptHash... from) throws IOException {
        return transferFromSpecificAccounts(wallet, ScriptHash.fromAddress(to), amount, from);
    }

    /**
     * Creates a transfer transaction that uses the provided accounts.
     * <p>
     * The accounts are used in the order provided to cover the transaction amount. If the first
     * account cannot cover the full amount, the second account is used to cover the remaining
     * amount and so on. If the amount can be covered by the specified accounts, all necessary
     * transfers are packed in one transaction.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer as a decimal number (not token fractions).
     * @param from   the script hashes of the accounts in the wallet that should be used to cover
     *               the amount.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromSpecificAccounts(Wallet wallet, ScriptHash to,
            BigDecimal amount, ScriptHash... from) throws IOException {

        if (from.length == 0) {
            throw new IllegalArgumentException(
                    "An account address must be provided to build an invocation.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "The parameter amount must be greater than or equal to 0");
        }
        if (!amountDecimalsIsValid(amount)) {
            throw new IllegalArgumentException("The amount contains more decimal places than this token " +
                    "can handle. This token has " + getDecimals() + " decimals. The amount provided " +
                    "had " + amount.stripTrailingZeros().scale() + " decimal places.");
        }

        List<Account> accounts = new ArrayList<>();
        for (ScriptHash fromScriptHash : from) {
            Account a = wallet.getAccount(fromScriptHash);
            // TODO: 15.10.20 Michael: Remove this multi-sig check. The signers for a multi-sig can still
            //  be added to the TransactionBuilder.
            // Verify that potential multi-sig accounts can be used.
            if (a.isMultiSig() && a.getVerificationScript() != null &&
                    !wallet.privateKeysArePresentForMultiSig(a.getVerificationScript())) {
                throw new IllegalArgumentException("The multi-sig account with script hash "
                        + fromScriptHash.toString() + " does not have the corresponding private "
                        + "keys in the wallet that are required for signing the transfer "
                        + "transaction.");
            }
            accounts.add(a);
        }
        return buildMultiTransferInvocation(wallet, to, amount, accounts);
    }

    TransactionBuilder buildMultiTransferInvocation(Wallet wallet, ScriptHash to, BigDecimal amount,
            List<Account> accounts) throws IOException {

        List<byte[]> scripts = new ArrayList<>(); // List of the individual invocation scripts.
        List<Signer> signers = new ArrayList<>(); // Accounts taking part in the transfer.
        Iterator<Account> it = accounts.iterator();
        BigInteger remainingAmount = getAmountAsBigInteger(amount);
        while (remainingAmount.signum() > 0 && it.hasNext()) {
            Account a = it.next();
            if (a.isMultiSig() && a.getVerificationScript() != null &&
                    !wallet.privateKeysArePresentForMultiSig(a.getVerificationScript())) {
                continue;
            }
            BigInteger balance = getBalanceOf(a.getScriptHash());
            if (balance.signum() <= 0) {
                continue;
            }
            signers.add(Signer.calledByEntry(a.getScriptHash()));
            if (balance.compareTo(remainingAmount) >= 0) {
                // Full remaining amount can be covered by current account.
                scripts.add(buildSingleTransferScript(a, to, remainingAmount));
            } else {
                // Full balance of account is needed but doesn't yet cover the full amount.
                scripts.add(buildSingleTransferScript(a, to, balance));
            }
            remainingAmount = remainingAmount.subtract(balance);
        }

        if (remainingAmount.signum() > 0) {
            BigInteger amountToCover = getAmountAsBigInteger(amount);
            BigInteger coveredAmount = amountToCover.subtract(remainingAmount);
            throw new InsufficientFundsException("The wallet does not hold enough tokens, resp. "
                    + "token-holding accounts with available private keys. The transfer amount is "
                    + amountToCover.toString() + " " + getSymbol() + " but the wallet only holds "
                    + coveredAmount.toString() + " " + getSymbol() + " (in token fractions).");
        }
        return assembleMultiTransferTransaction(wallet, scripts, signers);
    }

    private byte[] buildSingleTransferScript(Account acc, ScriptHash to, BigInteger amount) {
        List<ContractParameter> params = Arrays.asList(
                ContractParameter.hash160(acc.getScriptHash()),
                ContractParameter.hash160(to),
                ContractParameter.integer(amount),
                ContractParameter.any(null));

        return new ScriptBuilder().contractCall(scriptHash, TRANSFER, params).toArray();
    }

    private TransactionBuilder assembleMultiTransferTransaction(Wallet wallet, List<byte[]> scripts,
            List<Signer> signers) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (byte[] script : scripts) {
            byteArrayOutputStream.write(script);
        }
        byte[] concatenatedScript = byteArrayOutputStream.toByteArray();

        return new TransactionBuilder(neow)
                .wallet(wallet)
                .script(concatenatedScript)
                .signers(signers.toArray(new Signer[]{}));
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * Uses only the wallet's default account to cover the token amount.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the address of the receiver.
     * @param amount the amount to transfer as a decimal number (not token fractions).
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromDefaultAccount(Wallet wallet, String to,
            BigDecimal amount) throws IOException {
        return transferFromDefaultAccount(wallet, ScriptHash.fromAddress(to), amount);
    }

    /**
     * Creates a transfer transaction.
     * <p>
     * Uses only the wallet's default account to cover the token amount.
     *
     * @param wallet the wallet from which to send the tokens from.
     * @param to     the script hash of the receiver.
     * @param amount the amount to transfer as a decimal number (not token fractions).
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFromDefaultAccount(Wallet wallet, ScriptHash to,
            BigDecimal amount) throws IOException {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("The amount must be greater than or equal to 0.");
        }
        if (!amountDecimalsIsValid(amount)) {
            throw new IllegalArgumentException("The amount contains more decimal places than this token " +
                    "can handle. This token has " + getDecimals() + " decimals. The amount provided " +
                    "had " + amount.stripTrailingZeros().scale() + " decimal places.");
        }

        Account acc = wallet.getDefaultAccount();
        BigInteger fractions = getAmountAsBigInteger(amount);
        BigInteger accBalance = getBalanceOf(acc.getScriptHash());
        if (accBalance.compareTo(fractions) < 0) {
            throw new InsufficientFundsException("The wallet's default account does not hold enough"
                    + " tokens. Transfer amount is " + fractions.toString() + " but account"
                    + " only holds " + accBalance.toString() + " (in token fractions).");
        }

        return invokeFunction(TRANSFER,
                ContractParameter.hash160(acc.getScriptHash()),
                ContractParameter.hash160(to),
                ContractParameter.integer(fractions),
                ContractParameter.any(null))
                .wallet(wallet)
                .signers(Signer.calledByEntry(acc.getScriptHash()));
    }

    private boolean amountDecimalsIsValid(BigDecimal amount) throws IOException {
        return amount.stripTrailingZeros().scale() <= getDecimals();
    }
}
