package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Permission.Permissions;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to describe which contracts may be
 * invoked and which methods are called.
 * <p>
 * <ul>
 * <li>The `contract` field indicates the contract to be invoked. It can be a hash of a contract, a
 * public key of a group, or a wildcard "*".</li>
 * <li>If it specifies a hash of a contract, then the contract will be invoked; If it specifies a
 * public key of a group, then any contract in this group will be invoked; If it specifies a
 * wildcard "*", then any contract will be invoked.</li>
 * <li>The `methods` field are a set of methods to be called. It can also be assigned with a
 * wildcard "*", which means that any method can be called.</li>
 * <li>If a contract invokes a contract or method that is not declared in the manifest at runtime,
 * the invocation will fail.</li>
 * </ul>
 * <p>
 * Usage of this annotation is not mandatory.
 */
@Target(ElementType.TYPE)
@Repeatable(Permissions.class)
public @interface Permission {

    String contract();

    String[] methods() default "*";

    @Target(ElementType.TYPE)
    @interface Permissions {

        Permission[] value();

    }

}
