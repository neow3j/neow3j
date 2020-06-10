package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ScriptReaderTest {

    @Test
    public void convertToOpCodeString() {
        String script = "0c0548656c6c6f0c05576f726c64";
        String out = ScriptReader.convertToOpCodeString(script);
        assertThat(out, is("PUSHDATA1 5 48656c6c6f\nPUSHDATA1 5 576f726c64\n"));
    }
}