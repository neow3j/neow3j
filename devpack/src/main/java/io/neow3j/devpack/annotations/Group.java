package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Group.Groups;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to represent a set of mutually trusted contracts.
 * Your contract will trust any contract in the same group, meaning that the user interface will
 * not give any warnings if a contract of the group invokes your contract.
 *
 * A group is identified by a public key ({@code pubKey}) and must be accompanied by a signature
 * over the hash of your contract to prove that the contract is indeed included in the group, i.e.,
 * has access to the corresponding private key material.
 * The {@code pubKey} has to be provided as a hexadecimal string and the {@code signature} as a
 * Base64 encoded string.
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
