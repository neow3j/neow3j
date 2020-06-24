package io.neow3j.devpack.framework.annotations;

import io.neow3j.devpack.framework.annotations.ManifestExtra.ManifestExtras;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

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
