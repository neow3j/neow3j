package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.model.types.StackItemType;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.methods.response.NFTokenProperties;
import io.neow3j.protocol.core.methods.response.StackItem;
import io.neow3j.transaction.Signer;
import io.neow3j.utils.Numeric;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.InsufficientFundsException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a NEP-11 Non-Fungible Token contract and provides methods to invoke it.
 */
public class NFToken extends Token {

    private static final String TRANSFER = "transfer";
    private static final String OWNER_OF = "ownerOf";
    private static final String BALANCE_OF = "balanceOf";
    private static final String TOKENS_OF = "tokensOf";
    private static final String PROPERTIES = "properties";

    /**
     * Constructs a new {@code NFT} representing the contract with the given script
     * hash. Uses the given {@link Neow3j} instance for all invocations.
     *
     * @param scriptHash the token contract's script hash.
     * @param neow the {@link Neow3j} instance to use for invocations.
     */
    public NFToken(ScriptHash scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Transfers the token with {@code tokenID} to the account {@code to}.
     *
     * @param wallet the wallet that holds the account of the token owner.
     * @param to the receiver of the token.
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Wallet wallet, ScriptHash to, byte[] tokenID) throws IOException {
        int decimals = getDecimals();
        if (decimals != 0) {
            throw new IllegalStateException("This method is only implemented on NF tokens that are " +
                    "indivisible. This token has " + decimals + " decimals.");
        }

        List<ScriptHash> owners = ownerOf(tokenID);
        if (owners.size() != 1) {
            throw new IllegalStateException("The token with ID " + Numeric.toHexString(tokenID) + " has " +
                    owners.size() + " owners. To transfer fractions use the method transferFractions.");
        }

        ScriptHash tokenOwner = owners.get(0);
        if (!wallet.holdsAccount(tokenOwner)) {
            throw new IllegalArgumentException("The provided wallet does not contain the account that " +
                    "owns the token with ID " + Numeric.toHexString(tokenID) + ". The address of the " +
                    "owner of this token is " + tokenOwner.toAddress() + ".");
        }

        return invokeFunction(TRANSFER,
                ContractParameter.hash160(to),
                ContractParameter.byteArray(tokenID))
                .wallet(wallet)
                .signers(Signer.calledByEntry(tokenOwner));
    }

    /**
     * Transfers the {@code amount} of the token with {@code tokenID} to the account {@code to}.
     * The {@code wallet} has to contain the {@code from} account and that account has to be in possession of
     * {@code amount} fractions of the token with the given {@code tokenID}.
     *
     * @param wallet the wallet that holds the {@code from} account.
     * @param from the account to send the token fractions from.
     * @param to the receiver of the token fraction.
     * @param amount the amount of the token to transfer as a decimal number (not token fractions).
     * @param tokenID the token ID.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transferFraction(Wallet wallet, ScriptHash from, ScriptHash to,
            BigDecimal amount, byte[] tokenID) throws IOException {
        List<ScriptHash> owners = ownerOf(tokenID);
        if (amount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("The amount to transfer must be in the range of 0 to 1.");
        }

        if (amount.scale() > getDecimals()) {
            throw new IllegalArgumentException("The scale of the provided amount is higher than the " +
                    "decimals of this token contract.");
        }

        if (owners.stream().noneMatch(from::equals)) {
            throw new IllegalStateException("The account " + from.toAddress() + " is not an owner " +
                    "of the token with ID " + Numeric.toHexString(tokenID) + ".");
        }

        BigInteger balanceInFractions = balanceOf(from, tokenID);
        BigDecimal balance = getAmountAsBigDecimal(balanceInFractions);
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("The provided account can not cover the given amount. " +
                    "The amount to transfer was " + amount + " but only " + balance + " is held by the " +
                    "given account.");
        }

        if (!wallet.holdsAccount(from)) {
            throw new IllegalArgumentException("The provided wallet does not contain the provided from " +
                    "account.");
        }

        BigInteger fractions = getAmountAsBigInteger(amount);

        return invokeFunction(TRANSFER,
                ContractParameter.hash160(from),
                ContractParameter.hash160(to),
                ContractParameter.integer(fractions),
                ContractParameter.byteArray(tokenID))
                .wallet(wallet)
                .signers(Signer.calledByEntry(from));
    }

    /**
     * Gets the owner(s) of the token with {@code tokenID}.
     *
     * @param tokenID the token ID.
     * @return a list of owners of the token.
     */
    // According to the not yet final NEP-11 proposal, this method returns an enumerator that contains all
    //  the co-owners that own the specified token. Hence, this method expects an array stack item
    //  containing the co-owners.
    // TODO: 15.10.20 Michael: Adapt this method as soon as an implementation of the NEP-11 proposal exists.
    public List<ScriptHash> ownerOf(byte[] tokenID) throws IOException {
        return callFunctionReturningListOfScriptHashes(OWNER_OF,
                Arrays.asList(ContractParameter.byteArrayAsBase64(tokenID)));
    }

    private List<ScriptHash> callFunctionReturningListOfScriptHashes(String function,
            List<ContractParameter> params) throws IOException {

        StackItem arrayItem = callInvokeFunction(function, params).getInvocationResult().getStack().get(0);
        if (!arrayItem.getType().equals(StackItemType.ARRAY)) {
            throw new UnexpectedReturnTypeException(arrayItem.getType(), StackItemType.ARRAY);
        }
        List<ScriptHash> scriptHashes = new ArrayList<>();
        for (StackItem item : arrayItem.asArray().getValue()) {
            scriptHashes.add(extractScriptHash(item));
        }
        return scriptHashes;
    }

    private ScriptHash extractScriptHash(StackItem item) {
        if (!item.getType().equals(StackItemType.BYTE_STRING)) {
            throw new UnexpectedReturnTypeException(item.getType(),
                    StackItemType.BYTE_STRING);
        }
        try {
            return ScriptHash.fromAddress(item.asByteString().getAsAddress());
        } catch (IllegalArgumentException e) {
            throw new UnexpectedReturnTypeException("Byte array return type did not contain "
                    + "script hash in expected format.", e);
        }
    }

    /**
     * Gets the balance of the token with {@code tokenID} for the given account.
     * <p>
     * The balance is returned in token fractions. E.g., a balance of 0.5 of a token with 2 decimals
     * is returned as 50 (= 0.5 * 10^2) token fractions.
     * <p>
     * The balance is not cached locally. Every time this method is called requests are send to the
     * neo-node.
     *
     * @param owner the script hash of the account to fetch the balance for.
     * @param tokenID the token ID.
     * @return the token balance of the given account.
     * @throws IOException                   if there was a problem fetching information from the
     *                                       Neo node.
     * @throws UnexpectedReturnTypeException if the contract invocation did not return something
     *                                       interpretable as a number.
     */
    public BigInteger balanceOf(ScriptHash owner, byte[] tokenID) throws IOException {
        return callFuncReturningInt(BALANCE_OF,
                ContractParameter.hash160(owner),
                ContractParameter.byteArrayAsBase64(tokenID));
    }

    /**
     * Gets all the token IDs owned by the given account.
     *
     * @param owner the account.
     * @return a list of all token IDs that are owned by the given account.
     */
    public List<ScriptHash> tokensOf(ScriptHash owner) throws IOException {
        return callFunctionReturningListOfScriptHashes(TOKENS_OF,
                Arrays.asList(ContractParameter.hash160(owner)));
    }

    /**
     * Gets the properties of the token with {@code tokenID}.
     *
     * @param tokenID the token ID.
     * @return the properties of the token.
     */
    public NFTokenProperties properties(byte[] tokenID) throws IOException {
        StackItem item = callInvokeFunction(PROPERTIES,
                Arrays.asList(ContractParameter.byteArrayAsBase64(tokenID)))
                .getInvocationResult().getStack().get(0);
        if (item.getType().equals(StackItemType.BYTE_STRING)) {
            return item.asByteString().getAsJson(NFTokenProperties.class);
        }
        throw new UnexpectedReturnTypeException(item.getType(), StackItemType.BYTE_STRING);
    }
}
