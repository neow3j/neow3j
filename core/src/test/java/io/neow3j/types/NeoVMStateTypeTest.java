package io.neow3j.types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NeoVMStateTypeTest {

    private NeoVMStateType neoVMStateType;

    @BeforeAll
    public void setUp() {
        this.neoVMStateType = NeoVMStateType.HALT;
    }

    @Test
    public void testJsonValue() {
        assertThat(this.neoVMStateType.jsonValue(), is("HALT"));
    }

    @Test
    public void testIntValue() {
        assertThat(this.neoVMStateType.intValue(), is(1));
    }

    @Test
    public void testFromJsonValue() {
        assertThat(NeoVMStateType.fromJsonValue("HALT"), is(NeoVMStateType.HALT));
    }

    @Test
    public void testFromJsonValue_null() {
        assertThat(NeoVMStateType.fromJsonValue(null), is(NeoVMStateType.NONE));
    }

    @Test
    public void testFromIntValue() {
        assertThat(NeoVMStateType.fromIntValue(4), is(NeoVMStateType.BREAK));
    }

    @Test
    public void testFromIntValue_null() {
        assertThat(NeoVMStateType.fromIntValue(null), is(NeoVMStateType.NONE));
    }

    @Test
    public void testFromJsonValue_notFound() {
        assertThrows(IllegalArgumentException.class, () -> NeoVMStateType.fromJsonValue("Anything"));
    }

    @Test
    public void testFromIntValue_notFound() {
        assertThrows(IllegalArgumentException.class, () -> NeoVMStateType.fromIntValue(12));
    }

    @Test
    public void testFromJsonValue_Empty() {
        assertThat(NeoVMStateType.fromJsonValue(""), is(NeoVMStateType.NONE));
    }

    @Test
    public void testToString() {
        assertThat(this.neoVMStateType.jsonValue(), is(NeoVMStateType.HALT.toString()));
    }

}
