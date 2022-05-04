package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Permission.Permissions;
import io.neow3j.devpack.constants.NativeContract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level to describe which contracts and methods your contract is allowed
 * to call. These permissions are enforced when your contract makes calls to other contracts. By default, your
 * contract has no permissions. Permissions can give more trust into a smart contract because they prevent certain
 * misuse of a user's signature. I.e., using permissions signals some reliability of your contract.
 * <p>
 * You can use this annotation multiple times if you want to set permissions for multiple contracts or groups.
 * <p>
 * The {@code contract} and {@code nativeContract} fields must not be used conjointly.
 */
@Target(ElementType.TYPE)
@Repeatable(Permissions.class)
public @interface Permission {

    /**
     * Indicates which contract or contract group is permitted. It can be a hash of a contract, a public key of a
     * contract group, or the wildcard "*", which includes all contracts. When specifying a public key of a group,
     * then any contract in that group can be invoked.
     * <p>
     * If the field {@code nativeContract} is used, this field may not be used. Otherwise, it is mandatory.
     *
     * @return the contract hash or group key.
     */
    String contract() default "";

    /**
     * Indicates which native contract is permitted.
     * <p>
     * If the field {@code contract} is used, this field may not be used. Otherwise, it is mandatory.
     *
     * @return the native contract.
     */
    NativeContract nativeContract() default NativeContract.None;

    /**
     * Indicates which methods are permitted. This corresponds to the contract set in this permission. A wildcard "*"
     * means that any method can be called.
     *
     * @return the list of methods.
     */
    String[] methods() default "*";

    @Target(ElementType.TYPE)
    @interface Permissions {

        Permission[] value();

    }

}
