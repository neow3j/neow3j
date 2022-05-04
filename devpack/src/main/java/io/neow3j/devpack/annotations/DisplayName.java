package io.neow3j.devpack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation is used on contract class level or on event variables to set the name of corresponding element.
 * <ul>
 * <li>Use it on a contract class to set the contract's name as it will appear in the name attribute of the contract
 *     manifest.</li>
 * <li>Use it on an {@link io.neow3j.devpack.events.Event} variable to set the event name as it will appear in the
 *     contract manifest.</li>
 * </ul>
 * <p>
 * Usage of this annotation is not mandatory. If not used the class name or variable name is used in the manifest,
 * respectively.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface DisplayName {

    // No default value because it is mandatory to set it if used.
    String value();

}
