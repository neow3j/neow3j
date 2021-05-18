package io.neow3j.types;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class NeoVMStateTypeTest {

    private NeoVMStateType neoVMStateType;

    @Before
    public void setUp() {
        this.neoVMStateType = NeoVMStateType.HALT;
    }

    @Test
    public void testJsonValue() {
        assertThat(this.neoVMStateType.jsonValue(), is("HALT"));
    }

    @Test
    public void testFromJsonValue() {
        assertThat(NeoVMStateType.fromJsonValue("HALT"), is(NeoVMStateType.HALT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonValue_NotFound() {
        NeoVMStateType.fromJsonValue("Anything");
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
