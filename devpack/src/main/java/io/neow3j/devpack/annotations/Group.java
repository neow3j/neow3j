package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Group.Groups;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to represent a set of mutually trusted
 * contracts. A contract will trust and allow any contract in the same group to invoke it, and
 * the user interface will not give any warnings.
 * <p>
 * <ul>
 * <li>A group is identified by a public key and must be accompanied by
 * a signature for the contract hash to prove that the contract is indeed
 * included in the group.</li>
 * <li>The `pubKey` represents the public key of the group, and `signature` is the
 * signature of the contract hash encoded in Base64.</li>
 * </ul>
 * <p>
 * Usage of this annotation is not mandatory.
 */
@Target(ElementType.TYPE)
@Repeatable(Groups.class)
public @interface Group {

    String pubKey();

    String signature();

    @Target(ElementType.TYPE)
    @interface Groups {

        Group[] value();
        
    }

}
