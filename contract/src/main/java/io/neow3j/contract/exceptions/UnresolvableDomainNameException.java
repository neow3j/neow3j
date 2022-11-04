package io.neow3j.contract.exceptions;

import static java.lang.String.format;

/**
 * Is thrown when the {@link io.neow3j.contract.NeoNameService} contract cannot resolve a domain name.
 */
public class UnresolvableDomainNameException extends Exception {

    public UnresolvableDomainNameException(String domainName) {
        super(format("The provided domain name '%s' could not be resolved.", domainName));
    }

}
