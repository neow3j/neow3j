package io.neow3j.contract;

import io.neow3j.constants.NeoConstants;
import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.contract.exceptions.UnresolvableDomainNameException;
import io.neow3j.contract.types.NNSName;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.response.RecordState;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.exceptions.InvocationFaultStateException;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.StackItemType;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.neow3j.constants.NeoConstants.MAX_ITERATOR_ITEMS_DEFAULT;
import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Represents the official NeoNameService contract and provides methods to invoke its functions.
 * <p>
 * To set a specific script hash for this contract, see {@link io.neow3j.protocol.Neow3jConfig#setNNSResolver(Hash160)}.
 * <p>
 * By default the official NNS contract deployed on mainnet with the script hash
 * {@code 0x50ac1c37690cc2cfc594472833cf57505d5f46de} is used.
 */
@SuppressWarnings("unchecked")
public class NeoNameService extends NonFungibleToken {

    private static final String ADD_ROOT = "addRoot";
    private static final String ROOTS = "roots";
    private static final String SET_PRICE = "setPrice";
    private static final String GET_PRICE = "getPrice";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String REGISTER = "register";
    private static final String RENEW = "renew";
    private static final String SET_ADMIN = "setAdmin";
    private static final String SET_RECORD = "setRecord";
    private static final String GET_RECORD = "getRecord";
    private static final String GET_ALL_RECORDS = "getAllRecords";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String RESOLVE = "resolve";

    private static final ByteStringStackItem NAME_PROPERTY = new ByteStringStackItem("name".getBytes());
    private static final ByteStringStackItem EXPIRATION_PROPERTY = new ByteStringStackItem("expiration".getBytes());
    private static final ByteStringStackItem ADMIN_PROPERTY = new ByteStringStackItem("admin".getBytes());

    private static final String PROPERTIES = "properties";

    /**
     * Initializes an interface to the NeoNameService smart contract.
     * <p>
     * Uses the NNS script hash specified in the {@link io.neow3j.protocol.Neow3jConfig}. By default the official NNS
     * smart contract deployed on mainnet with the script hash {@code 0x50ac1c37690cc2cfc594472833cf57505d5f46de} is
     * used.
     * <p>
     * Uses the given {@link Neow3j} instance for invocations.
     *
     * @param neow3j the {@link Neow3j} instance to use for invocations.
     */
    public NeoNameService(Neow3j neow3j) {
        super(neow3j.getNNSResolver(), neow3j);
    }

    /**
     * Returns the name of the NeoNameService contract.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return "NameService";
    }

    // region NEP-11 Methods

    /**
     * Returns the symbol of the NeoNameService contract.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the symbol of the NeoNameService contract.
     */
    @Override
    public String getSymbol() {
        return "NNS";
    }

    /**
     * Returns the decimals of the NeoNameService contract.
     * <p>
     * Doesn't require a call to the Neo node.
     *
     * @return the decimals of the NeoNameService contract.
     */
    @Override
    public int getDecimals() {
        return 0;
    }

    /**
     * Gets the owner of the domain name.
     *
     * @param name the domain name.
     * @return the owner of the domain name.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Hash160 ownerOf(NNSName name) throws IOException {
        return ownerOf(name.getBytes());
    }

    /**
     * Gets the properties of the domain name.
     *
     * @param name the domain name.
     * @return the properties of the domain name.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Map<String, String> properties(NNSName name) throws IOException {
        return properties(name.getBytes());
    }

    /**
     * Creates a transaction script to transfer a domain name and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * The returned {@link TransactionBuilder} is ready to be signed and sent. The {@code from} account is set as a
     * signer on the transaction.
     *
     * @param from the owner of the domain name.
     * @param to   the receiver of the domain name.
     * @param name the domain name.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, NNSName name) throws IOException {
        return transfer(from, to, name, null);
    }

    /**
     * Creates a transaction script to transfer a domain name and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * The returned {@link TransactionBuilder} is ready to be signed and sent. The {@code from} account is set as a
     * signer on the transaction.
     * <p>
     * Use the {@code data} parameter if the receiver of the domain name is a smart contract and you want to pass data
     * to its {@code onNEP11Payment} method.
     *
     * @param from the owner of the domain name.
     * @param to   the receiver of the domain name.
     * @param name the domain name.
     * @param data the data that is passed to the {@code onNEP11Payment} method of the receiving smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, NNSName name, ContractParameter data)
            throws IOException {
        checkDomainNameAvailability(name, false);
        return transfer(from, to, name.getBytes(), data);
    }

    // endregion NEP-11 Methods
    // region Custom Name Service Methods

    /**
     * Creates a transaction script to add a root domain (like .neo) and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Only the committee is allowed to add a new root domain.
     * <p>
     * Requires to be signed by the committee.
     *
     * @param nnsRoot the new root domain.
     * @return a transaction builder.
     */
    public TransactionBuilder addRoot(NNSName.NNSRoot nnsRoot) {
        return invokeFunction(ADD_ROOT, string(nnsRoot.getRoot()));
    }

    /**
     * Gets all existing roots.
     * <p>
     * This method requires sessions to be enabled on the Neo node. If sessions are disabled on the Neo node, use
     * {@link NeoNameService#getRootsUnwrapped()}.
     *
     * @return the roots.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<String> getRoots() throws IOException {
        return callFunctionReturningIterator(StackItem::getString, ROOTS);
    }

    /**
     * Gets all existing roots.
     * <p>
     * Use this method if sessions are disabled on the Neo node.
     * <p>
     * This method returns at most {@link NeoConstants#MAX_ITERATOR_ITEMS_DEFAULT} values. If there are more values,
     * connect to a Neo node that supports sessions and use {@link NeoNameService#getRoots()}.
     *
     * @return the roots.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<String> getRootsUnwrapped() throws IOException {
        return callFunctionAndUnwrapIterator(ROOTS, asList(), MAX_ITERATOR_ITEMS_DEFAULT)
                .stream().map(StackItem::getString).collect(Collectors.toList());
    }

    /**
     * Creates a transaction script to set the prices for registering a domain and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Only the committee is allowed to set the price.
     * <p>
     * Requires to be signed by the committee.
     *
     * @param priceList the prices for registering a domain. The index refers to the length of the domain. The value
     *                  at index 0 is used for domain names longer than the price list's highest index. Use -1 for
     *                  domain name lengths that are
     * @return a transaction builder.
     */
    public TransactionBuilder setPrice(List<BigInteger> priceList) {
        ContractParameter priceListParameter = array(priceList);
        return invokeFunction(SET_PRICE, priceListParameter);
    }

    /**
     * Gets the price to register a domain name of a certain length.
     *
     * @param domainNameLength the length of the domain name.
     * @return the price to register a domain.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getPrice(int domainNameLength) throws IOException {
        return callFunctionReturningInt(GET_PRICE, integer(domainNameLength));
    }

    /**
     * Checks if the specified domain name is available.
     *
     * @param name the domain name.
     * @return true if the domain name is available, false otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isAvailable(NNSName name) throws IOException {
        return callFunctionReturningBool(IS_AVAILABLE, string(name.getName()));
    }

    /**
     * Creates a transaction script to register a new domain name and initializes a {@link TransactionBuilder} based
     * on this script.
     * <p>
     * Requires to be signed by the domain owner.
     *
     * @param name  the domain name.
     * @param owner the address of the domain owner.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder register(NNSName name, Hash160 owner) throws IOException {
        checkDomainNameAvailability(name, true);
        return invokeFunction(REGISTER, string(name.getName()), hash160(owner));
    }

    /**
     * Creates a transaction script to update the TTL of the domain name and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Each call will extend the validity period of the domain name by one year.
     *
     * @param name the domain name.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder renew(NNSName name) throws IOException {
        checkDomainNameAvailability(name, false);
        return invokeFunction(RENEW, string(name.getName()));
    }

    /**
     * Creates a transaction script to update the TTL of the domain name and initializes a {@link TransactionBuilder}
     * based on this script.
     *
     * @param name  the domain name.
     * @param years the number of years to renew this domain name. Has to be in the range of 1 to 10.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder renew(NNSName name, int years) throws IOException {
        if (years < 1 || years > 10) {
            throw new IllegalArgumentException("Domain names can only be renewed by at least 1, and at most 10 years.");
        }
        checkDomainNameAvailability(name, false);
        return invokeFunction(RENEW, string(name.getName()), integer(years));
    }

    /**
     * Creates a transaction script to set the admin for the specified domain name and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Requires to be signed by the current owner and the new admin of the domain.
     *
     * @param name  the domain name.
     * @param admin the script hash of the admin address.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder setAdmin(NNSName name, Hash160 admin) throws IOException {
        checkDomainNameAvailability(name, false);
        return invokeFunction(SET_ADMIN, string(name.getName()), hash160(admin));
    }

    /**
     * Creates a transaction script to set the type of the specified domain name and the corresponding type data and
     * initializes a {@link TransactionBuilder} based on this script.
     * <p>
     * Requires to be signed by the domain owner or the domain admin.
     *
     * @param name the domain name.
     * @param type the record type.
     * @param data the corresponding data.
     * @return a transaction builder.
     */
    public TransactionBuilder setRecord(NNSName name, RecordType type, String data) {
        return invokeFunction(SET_RECORD, string(name.getName()), integer(type.byteValue()), string(data));
    }

    /**
     * Gets the record type data of the domain name.
     *
     * @param name the domain name.
     * @param type the record type.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public String getRecord(NNSName name, RecordType type) throws IOException {
        try {
            return callFunctionReturningString(GET_RECORD, string(name.getName()), integer(type.byteValue()));
        } catch (UnexpectedReturnTypeException e) {
            throw new IllegalArgumentException(
                    format("Could not get a record of type '%s' for the domain name '%s'.", type, name.getName()));
        } catch (InvocationFaultStateException e) {
            throw new IllegalArgumentException(
                    format("The domain name '%s' might not be registered or is in an invalid format.", name.getName()));
        }
    }

    /**
     * Gets all records of the domain name.
     *
     * @param name the domain name.
     * @return all records of the domain name.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Iterator<RecordState> getAllRecords(NNSName name) throws IOException {
        return callFunctionReturningIterator(RecordState::fromStackItem, GET_ALL_RECORDS, string(name.getName()));
    }

    /**
     * Gets all records of the domain name.
     * <p>
     * Use this method if sessions are disabled on the Neo node.
     * <p>
     * This method returns at most {@link NeoConstants#MAX_ITERATOR_ITEMS_DEFAULT} values. If there are more values,
     * connect to a Neo node that supports sessions and use {@link NeoNameService#getAllRecords(NNSName)} )}.
     *
     * @param name the domain name.
     * @return all records of the domain name.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public List<RecordState> getAllRecordsUnwrapped(NNSName name) throws IOException {
        return callFunctionAndUnwrapIterator(GET_ALL_RECORDS, asList(string(name.getName())),
                MAX_ITERATOR_ITEMS_DEFAULT).stream().map(RecordState::fromStackItem).collect(Collectors.toList());
    }

    /**
     * Creates a transaction script to delete record data of a domain name initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Requires to be signed by the domain owner or the domain admin.
     *
     * @param name the domain name.
     * @param type the record type.
     * @return a transaction builder.
     */
    public TransactionBuilder deleteRecord(NNSName name, RecordType type) {
        return invokeFunction(DELETE_RECORD, string(name.getName()), integer(type.byteValue()));
    }

    /**
     * Resolves a domain name.
     *
     * @param name the domain name.
     * @param type the record type.
     * @return the resolution result.
     * @throws IOException                     if there was a problem fetching information from the Neo node.
     * @throws UnresolvableDomainNameException if the domain name could not be resolved by the NeoNameService contract.
     */
    public String resolve(NNSName name, RecordType type) throws IOException, UnresolvableDomainNameException {
        try {
            return callFunctionReturningString(RESOLVE, string(name.getName()), integer(type.byteValue()));
        } catch (UnexpectedReturnTypeException | InvocationFaultStateException e) {
            throw new UnresolvableDomainNameException(name.getName());
        }
    }

    void checkDomainNameAvailability(NNSName name, boolean shouldBeAvailable) throws IOException {
        boolean isAvailable = isAvailable(name);
        if (shouldBeAvailable && !isAvailable) {
            throw new IllegalArgumentException(format("The domain name '%s' is already taken.", name.getName()));
        }
        if (!shouldBeAvailable && isAvailable) {
            throw new IllegalArgumentException(format("The domain name '%s' is not registered.", name.getName()));
        }
    }

    /**
     * Gets the state of the domain name.
     * <p>
     * Relates to the NEP-11 properties deserialized into a {@link NameState} object.
     *
     * @param name the domain name.
     * @return the state of the domain name as {@link NameState}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NameState getNameState(NNSName name) throws IOException {
        return getNameState(name.getBytes());
    }

    /**
     * Gets the state of the domain name.
     * <p>
     * Relates to the NEP-11 properties deserialized into a {@link NameState} object.
     *
     * @param name the domain name.
     * @return the state of the domain name as {@link NameState}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NameState getNameState(byte[] name) throws IOException {
        InvocationResult invocationResult =
                callInvokeFunction(PROPERTIES, asList(byteArray(name))).getInvocationResult();
        return deserializeNameState(invocationResult);
    }

    private NameState deserializeNameState(InvocationResult invocationResult) {
        throwIfFaultState(invocationResult);

        StackItem stackItem = invocationResult.getStack().get(0);
        if (!stackItem.getType().equals(StackItemType.MAP)) {
            throw new UnexpectedReturnTypeException(stackItem.getType(), StackItemType.MAP);
        }

        Map<StackItem, StackItem> map = stackItem.getMap();
        String name = map.get(NAME_PROPERTY).getString();
        BigInteger expiration = map.get(EXPIRATION_PROPERTY).getInteger();
        StackItem adminStackItem = map.get(ADMIN_PROPERTY);
        if (adminStackItem == null || adminStackItem.getValue() == null) {
            return new NameState(name, expiration.longValue(), null);
        }
        Hash160 admin = Hash160.fromAddress(adminStackItem.getAddress());
        return new NameState(name, expiration.longValue(), admin);
    }

    // endregion Custom Name Service Methods

}
