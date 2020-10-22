package io.neow3j.compiler;

import static io.neow3j.model.types.ContractParameterType.ANY;
import static io.neow3j.model.types.ContractParameterType.ARRAY;
import static io.neow3j.model.types.ContractParameterType.BOOLEAN;
import static io.neow3j.model.types.ContractParameterType.BYTE_ARRAY;
import static io.neow3j.model.types.ContractParameterType.INTEGER;
import static io.neow3j.model.types.ContractParameterType.INTEROP_INTERFACE;
import static io.neow3j.model.types.ContractParameterType.STRING;
import static io.neow3j.model.types.ContractParameterType.VOID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import io.neow3j.devpack.neo.Map;
import io.neow3j.devpack.neo.Transaction;
import io.neow3j.model.types.ContractParameterType;
import java.util.Arrays;
import org.hamcrest.text.StringContainsInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.objectweb.asm.Type;

public class CompilerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void mapTypeToParameterTypeShouldReturnTheCorrectTypes() {
        // Integers
        assertClassIsMappedToType(byte.class, INTEGER);
        assertClassIsMappedToType(Byte.class, INTEGER);
        assertClassIsMappedToType(char.class, INTEGER);
        assertClassIsMappedToType(Character.class, INTEGER);
        assertClassIsMappedToType(short.class, INTEGER);
        assertClassIsMappedToType(Short.class, INTEGER);
        assertClassIsMappedToType(int.class, INTEGER);
        assertClassIsMappedToType(Integer.class, INTEGER);
        assertClassIsMappedToType(long.class, INTEGER);
        assertClassIsMappedToType(Long.class, INTEGER);

        // Bools
        assertClassIsMappedToType(boolean.class, BOOLEAN);
        assertClassIsMappedToType(Boolean.class, BOOLEAN);

        // Strings
        assertClassIsMappedToType(String.class, STRING);

        // Void
        assertClassIsMappedToType(void.class, VOID);
        assertClassIsMappedToType(Void.class, VOID);

        // Byte arrays
        assertClassIsMappedToType(byte[].class, BYTE_ARRAY);
        assertClassIsMappedToType(Byte[].class, BYTE_ARRAY);

        // Arrays
        assertClassIsMappedToType(String[].class, ARRAY);
        assertClassIsMappedToType(int[].class, ARRAY);
        assertClassIsMappedToType(Integer[].class, ARRAY);
        assertClassIsMappedToType(boolean[].class, ARRAY);
        assertClassIsMappedToType(byte[][].class, ARRAY);

        // Others
        assertClassIsMappedToType(Transaction.class, INTEROP_INTERFACE);
        assertClassIsMappedToType(Object.class, ANY);
    }

    private void assertClassIsMappedToType(Class<?> clazz, ContractParameterType type) {
        Type t = Type.getType(clazz);
        assertThat(Compiler.mapTypeToParameterType(t), is(type));
    }

    @Test
    public void mapTypeToParameterShouldThrowAnExceptionOnUnsupportedTypes() {
        Type t = Type.getType(io.neow3j.devpack.neo.Map.class);
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(new StringContainsInOrder(
                Arrays.asList("No mapping from Java type", Map.class.getCanonicalName())));
        Compiler.mapTypeToParameterType(t);

        t = Type.getType(java.util.List.class);
        expectedException.expect(CompilerException.class);
        expectedException.expectMessage(new StringContainsInOrder(
                Arrays.asList("No mapping from Java type",
                        java.util.List.class.getCanonicalName())));
        Compiler.mapTypeToParameterType(t);
    }

}
