package io.neow3j.contract;

import io.neow3j.contract.exceptions.UnexpectedReturnTypeException;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.core.RecordType;
import io.neow3j.protocol.core.response.InvocationResult;
import io.neow3j.protocol.core.response.NameState;
import io.neow3j.protocol.core.stackitem.ByteStringStackItem;
import io.neow3j.protocol.core.stackitem.StackItem;
import io.neow3j.protocol.exceptions.InvocationFaultStateException;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.neow3j.types.ContractParameter.array;
import static io.neow3j.types.ContractParameter.byteArray;
import static io.neow3j.types.ContractParameter.hash160;
import static io.neow3j.types.ContractParameter.integer;
import static io.neow3j.types.ContractParameter.string;
import static io.neow3j.types.StackItemType.MAP;
import static io.neow3j.utils.Numeric.hexToString;
import static io.neow3j.utils.Numeric.toHexString;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

/**
 * Represents the NameService contract and provides methods to invoke its functions.
 */
public class NeoNameService extends NonFungibleToken {

    public static final String NAME = "NameService";

    private static final String ADD_ROOT = "addRoot";
    private static final String SET_PRICE = "setPrice";
    private static final String GET_PRICE = "getPrice";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String REGISTER = "register";
    private static final String RENEW = "renew";
    private static final String SET_ADMIN = "setAdmin";
    private static final String SET_RECORD = "setRecord";
    private static final String GET_RECORD = "getRecord";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String RESOLVE = "resolve";

    private static final ByteStringStackItem NAME_PROPERTY = new ByteStringStackItem("name".getBytes(UTF_8));
    private static final ByteStringStackItem EXPIRATION_PROPERTY =
            new ByteStringStackItem("expiration".getBytes(UTF_8));
    private static final ByteStringStackItem ADMIN_PROPERTY = new ByteStringStackItem("admin".getBytes(UTF_8));

    private static final String PROPERTIES = "properties";

    private static final BigInteger MAXIMAL_PRICE = BigInteger.valueOf(10000_00000000L);
    private static final Pattern ROOT_REGEX_PATTERN = Pattern.compile("^[a-z][a-z0-9]{0,15}$");
    private static final Pattern NAME_REGEX_PATTERN = Pattern.compile(
            "^(?=.{3,255}$)([a-z0-9]{1,62}\\.)+[a-z][a-z0-9]{0,15}$");
    private static final Pattern IPV4_REGEX_PATTERN = Pattern.compile(
            "^(?=\\d+\\.\\d+\\.\\d+\\.\\d+$)(?:(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])\\.?){4}$");
    private static final Pattern IPV6_REGEX_PATTERN = Pattern.compile(
            "(?:^)(([0-9a-f]{1,4}:){7,7}[0-9a-f]{1,4}|([0-9a-f]{1,4}:){1,7}:|([0-9a-f]{1,4}:){1,6}:[0-9a-f]{1,4}|" +
                    "([0-9a-f]{1,4}:){1,5}(:[0-9a-f]{1,4}){1,2}|([0-9a-f]{1,4}:){1,4}(:[0-9a-f]{1,4}){1,3}|" +
                    "([0-9a-f]{1,4}:){1,3}(:[0-9a-f]{1,4}){1,4}|([0-9a-f]{1,4}:){1,2}(:[0-9a-f]{1,4}){1,5}|" +
                    "[0-9a-f]{1,4}:((:[0-9a-f]{1,4}){1,6})|:((:[0-9a-f]{1,4}){1,7}|:))(?=$)");

    /**
     * Constructs a new {@code NeoNameService} contract that uses the given {@link Neow3j} instance for invocations.
     *
     * @param scriptHash The script hash of the name service contract.
     * @param neow       The {@link Neow3j} instance to use for invocations.
     */
    public NeoNameService(Hash160 scriptHash, Neow3j neow) {
        super(scriptHash, neow);
    }

    /**
     * Returns the name of the NeoNameService contract. Doesn't require a call to the Neo node.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Creates a transaction script to add a root domain and initializes a {@link TransactionBuilder} based on this
     * script.
     * <p>
     * Only committee members are allowed to add a new root domain.
     *
     * @param root The new root domain.
     * @return a transaction builder.
     */
    public TransactionBuilder addRoot(String root) {
        checkRegexMatch(ROOT_REGEX_PATTERN, root);
        return invokeFunction(ADD_ROOT, string(root));
    }

    /**
     * Creates a transaction script to set the price for registering a domain and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Only committee members are allowed to set the price.
     *
     * @param priceList The price for registering a domain. The index refers to the length of the domain. The value
     *                  at index 0 is used for domain names longer than the price list's highest index.
     * @return a transaction builder.
     */
    public TransactionBuilder setPrice(List<BigInteger> priceList) {
        Optional<BigInteger> bigIntegerStream = priceList.stream().filter(p -> !isValidPrice(p)).findFirst();
        if (bigIntegerStream.isPresent()) {
            throw new IllegalArgumentException("The price need to be greater than 0 and smaller than 10000_00000000.");
        }
        ContractParameter priceListParameter = array(priceList);
        return invokeFunction(SET_PRICE, priceListParameter);
    }

    // true if the price is in the allowed range, false otherwise.
    private boolean isValidPrice(BigInteger price) {
        return price.compareTo(BigInteger.ZERO) > 0 && price.compareTo(MAXIMAL_PRICE) <= 0;
    }

    /**
     * Gets the price to register a domain of a certain length.
     *
     * @return The price to register a domain.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public BigInteger getPrice(int domainNameLength) throws IOException {
        return callFuncReturningInt(GET_PRICE, integer(domainNameLength));
    }

    /**
     * Checks if the specified second-level domain name is available.
     *
     * @param name The domain name.
     * @return true if the domain name is available, false otherwise.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public boolean isAvailable(String name) throws IOException {
        checkDomainNameValidity(name);
        return callFuncReturningBool(IS_AVAILABLE, string(name));
    }

    /**
     * Creates a transaction script to register a new domain and initializes a {@link TransactionBuilder} based on
     * this script.
     *
     * @param name  The domain name.
     * @param owner The address of the domain owner.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder register(String name, Hash160 owner) throws IOException {
        checkDomainNameAvailability(name, true);
        return invokeFunction(REGISTER, string(name), hash160(owner));
    }

    // checks if a domain name is available
    void checkDomainNameAvailability(String name, boolean shouldBeAvailable) throws IOException {
        boolean isAvailable = isAvailable(name);
        if (shouldBeAvailable && !isAvailable) {
            throw new IllegalArgumentException("The domain name '" + name + "' is already taken.");
        }
        if (!shouldBeAvailable && isAvailable) {
            throw new IllegalArgumentException("The domain name '" + name + "' is not registered.");
        }
    }

    // checks that the domain name matches the required regex and contains two levels.
    private void checkDomainNameValidity(String name) {
        checkRegexMatch(NAME_REGEX_PATTERN, name);
        if (name.split("\\.").length != 2) {
            throw new IllegalArgumentException("Only second-level domain names are allowed to be registered.");
        }
    }

    // checks if an input matches the provided regex pattern.
    private void checkRegexMatch(Pattern pattern, String input) {
        if (!pattern.matcher(input).matches()) {
            throw new IllegalArgumentException("The provided input does not match the required regex.");
        }
    }

    /**
     * Creates a transaction script to update the TTL of the domain name and initializes a {@link TransactionBuilder}
     * based on this script.
     * <p>
     * Each call will extend the validity period of the domain name by one year.
     * <p>
     * Only supports renewing the second-level domain name.
     *
     * @param name The domain name.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder renew(String name) throws IOException {
        checkDomainNameAvailability(name, false);
        return invokeFunction(RENEW, string(name));
    }

    /**
     * Creates a transaction script to set the admin for the specified domain name and initializes a
     * {@link TransactionBuilder} based on this script.
     * <p>
     * Requires to be signed by the current owner and the new admin of the domain.
     *
     * @param name  The domain name.
     * @param admin The script hash of the admin address.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder setAdmin(String name, Hash160 admin) throws IOException {
        checkDomainNameAvailability(name, false);
        return invokeFunction(SET_ADMIN, string(name), hash160(admin));
    }

    /**
     * Creates a transaction script to set the type of the specified domain name and the corresponding type data and
     * initializes a {@link TransactionBuilder} based on this script.
     *
     * @param name The domain name.
     * @param type The record type.
     * @param data The corresponding data.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder setRecord(String name, RecordType type, String data) throws IOException {
        checkDomainNameAvailability(name, false);
        checkDataMatchingRecordType(type, data);
        return invokeFunction(SET_RECORD, string(name), integer(type.byteValue()), string(data));
    }

    private void checkDataMatchingRecordType(RecordType type, String data) {
        if (type.equals(RecordType.A)) {
            checkRegexMatch(IPV4_REGEX_PATTERN, data);
        } else if (type.equals(RecordType.CNAME)) {
            checkRegexMatch(NAME_REGEX_PATTERN, data);
        } else if (type.equals(RecordType.TXT)) {
            byte[] bytes = data.getBytes(UTF_8);
            if (bytes.length > 255) {
                throw new IllegalArgumentException("The provided data is not valid for the record type TXT.");
            }
        } else {
            checkRegexMatch(IPV6_REGEX_PATTERN, data);
        }
    }

    /**
     * Gets the type data of the domain.
     *
     * @param name The domain name.
     * @param type The record type.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public String getRecord(String name, RecordType type) throws IOException {
        checkDomainNameAvailability(name, false);
        try {
            return callFuncReturningString(GET_RECORD, string(name), integer(type.byteValue()));
        } catch (InvocationFaultStateException e) {
            throw new InvocationFaultStateException(format("Could not get any record of type '%s' for the domain name" +
                    " '%s'.", type.jsonValue(), name), e.getMessage());
        }
    }

    /**
     * Creates a transaction script to delete the record data initializes a {@link TransactionBuilder} based on this
     * script.
     *
     * @param name The domain name.
     * @param type The record type.
     * @return a transaction builder.
     */
    public TransactionBuilder deleteRecord(String name, RecordType type) {
        return invokeFunction(DELETE_RECORD, string(name), integer(type.byteValue()));
    }

    /**
     * Resolves a domain name.
     *
     * @param name The domain name.
     * @param type The record type.
     * @return the resolution result.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public String resolve(String name, RecordType type) throws IOException {
        checkDomainNameAvailability(name, false);
        try {
            return callFuncReturningString(RESOLVE, string(name), integer(type.byteValue()));
        } catch (UnexpectedReturnTypeException e) {
            throw new IllegalArgumentException("No record of type " + type.jsonValue() + " found for the domain name " +
                    "'" + name + "'.");
        }
    }

    /**
     * Gets the owner of the domain name.
     *
     * @param name The domain name.
     * @return the owner of the domain name.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public Hash160 ownerOf(String name) throws IOException {
        checkDomainNameAvailability(name, false);
        return ownerOf(name.getBytes(UTF_8));
    }

    /**
     * Gets the properties of the domain name.
     *
     * @param name The domain name.
     * @return the properties of the domain name as {@link NameState}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NameState getNameState(String name) throws IOException {
        return getNameState(name.getBytes(UTF_8));
    }

    /**
     * Gets the state of the domain name.
     *
     * @param name The domain name.
     * @return the state of the domain name as {@link NameState}.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public NameState getNameState(byte[] name) throws IOException {
        String domainAsString = hexToString(toHexString(name));
        checkDomainNameAvailability(domainAsString, false);
        InvocationResult invocationResult = callInvokeFunction(PROPERTIES, asList(byteArray(name)))
                .getInvocationResult();
        return deserializeNameState(invocationResult);
    }

    private NameState deserializeNameState(InvocationResult invocationResult) {
        throwIfFaultState(invocationResult);

        StackItem stackItem = invocationResult.getStack().get(0);
        if (!stackItem.getType().equals(MAP)) {
            throw new UnexpectedReturnTypeException(stackItem.getType(), MAP);
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

    /**
     * Creates a transaction script to transfer a domain name and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * The returned {@link TransactionBuilder} is ready to be signed and sent. The {@code from} account is set as a
     * signer on the transaction.
     *
     * @param from The owner of the domain name.
     * @param to   The receiver of the domain name.
     * @param name The domain name.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, String name) throws IOException {
        return transfer(from, to, name, null);
    }

    /**
     * Creates a transaction script to transfer a domain name and initializes a {@link TransactionBuilder} based on
     * this script.
     * <p>
     * The returned {@link TransactionBuilder} is ready to be signed and sent. The {@code from} account is set as a
     * signer on the transaction.
     *
     * @param from The owner of the domain name.
     * @param to   The receiver of the domain name.
     * @param name The domain name.
     * @param data The data that is passed to the {@code onNEP11Payment} method of the receiving smart contract.
     * @return a transaction builder.
     * @throws IOException if there was a problem fetching information from the Neo node.
     */
    public TransactionBuilder transfer(Account from, Hash160 to, String name, ContractParameter data)
            throws IOException {

        checkDomainNameAvailability(name, false);
        return transfer(from, to, name.getBytes(UTF_8), data);
    }

}
