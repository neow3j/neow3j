package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.SupportedStandard.SupportedStandards;
import io.neow3j.devpack.constants.NeoStandard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Used to define features of a smart contract. The attributes will be written to the contract's manifest file.
 * <p>
 * The {@code neoStandard} and {@code customStandard} fields must not be used conjointly.
 */
@Target(ElementType.TYPE)
@Repeatable(SupportedStandards.class)
public @interface SupportedStandard {

    NeoStandard neoStandard() default NeoStandard.None;

    String customStandard() default "";

    @Target(ElementType.TYPE)
    @interface SupportedStandards {

        SupportedStandard[] value();

    }

}
