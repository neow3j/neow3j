package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Use this to specify a URL that points to the contract's source code. For example,
 * {@code @ContractSourceCode("https://github.com/AxLabs/meme-governance-contracts/blob/main/src/main/java/com/axlabs
 * /GovernanceContract.java")}
 * <p>
 * Only use this annotation on the main contract class. Otherwise, the URL will not be added to the NEF file.
 */
@Target(ElementType.TYPE)
public @interface ContractSourceCode {

    String value();

}
