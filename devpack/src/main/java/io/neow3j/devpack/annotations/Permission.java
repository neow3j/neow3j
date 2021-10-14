package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Permission.Permissions;
import io.neow3j.devpack.constants.NativeContract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to describe which contracts and methods your
 * contract is allowed to call. These permissions are enforced when your contract makes calls to
 * other contracts. By default, your contract has no permissions. Permissions can give more trust
 * into a smart contract because they prevent certain misuse of a user's signature. I.e., using
 * permissions signals some reliability of your contract.
 * <p>
 * The {@code contract} field indicates which contract or contract group is permitted. It can
 * be a hash of a contract, a public key of a contract group, or the wildcard "*", which includes
 * all contracts. When specifying a public key of a group, then any contract in that group can be
 * invoked.
 * <p>
 * The {@code nativeContract} field allows to indicate a permission for a native contract using the
 * {@link NativeContract} enum. The {@code contract} and {@code nativeContract} fields must not
 * be used conjointly.
 * <p>
 * The {@code methods} field indicates which methods are permitted. This corresponds to the value
 * set in the {@code contract} or {@code nativeContract} field. A wildcard "*" means that any
 * method can be called.
 * <p>
 * You can use this annotation multiple times if you want to set permissions for multiple contracts
 * or groups.
 */
@Target(ElementType.TYPE)
@Repeatable(Permissions.class)
public @interface Permission {

    String contract() default "";

    NativeContract nativeContract() default NativeContract.None;

    String[] methods() default "*";

    @Target(ElementType.TYPE)
    @interface Permissions {

        Permission[] value();

    }

}
