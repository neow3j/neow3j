package io.neow3j.model.types;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class CallFlagsTypeTest {

    private CallFlagsType callFlagsType;

    @Before
    public void setUp() {
        this.callFlagsType = CallFlagsType.ALL;
    }

    @Test
    public void testJsonValue() {
        assertThat(this.callFlagsType.jsonValue(), is("All"));
    }

    @Test
    public void testByteValue() {
        byte value = 0B00001111;
        assertThat(this.callFlagsType.byteValue(), is(value));
    }

    @Test
    public void testValueOf() {
        byte value = 0B00001111;
        assertThat(CallFlagsType.valueOf(value), is(CallFlagsType.ALL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOf_NotFound() {
        byte invalid = 0B00001011;
        assertThat(CallFlagsType.valueOf(invalid), is(CallFlagsType.ALL));
    }

    @Test
    public void testFromJsonValue() {
        assertThat(CallFlagsType.fromJsonValue("All"), is(CallFlagsType.ALL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonValue_NotFound() {
        assertThat(CallFlagsType.fromJsonValue("Anything"), is(CallFlagsType.ALL));
    }

}
