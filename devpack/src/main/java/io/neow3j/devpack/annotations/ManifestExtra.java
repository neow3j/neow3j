package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.ManifestExtra.ManifestExtras;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Used to define manifest information on a smart contract that will be written to the contract's manifest file.
 * Extra information can, for example, contain the name of the contract's author or the name of the contract itself.
 * In the latter the key attribute would be "name" and the value attribute the desired name of the contract.
 */
@Target(ElementType.TYPE)
@Repeatable(ManifestExtras.class)
public @interface ManifestExtra {

    String key();

    String value();

    @Target(ElementType.TYPE)
    @interface ManifestExtras {

        ManifestExtra[] value();

    }

}
