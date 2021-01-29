package io.neow3j.devpack.annotations;

import io.neow3j.devpack.annotations.Group.Groups;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

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
