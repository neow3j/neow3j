package io.neow3j.contract;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.neow3j.script.ScriptReader;
import org.junit.jupiter.api.Test;

public class ScriptReaderTest {

    @Test
    public void convertToOpCodeString() {
        String script = "0c0548656c6c6f0c05576f726c642150419bf667ce41e63f18841140";
        String out = ScriptReader.convertToOpCodeString(script);
        String expected = ""
                + "PUSHDATA1 5 48656c6c6f\n"
                + "PUSHDATA1 5 576f726c64\n"
                + "NOP\n"
                + "SWAP\n"
                + "SYSCALL 9bf667ce\n"
                + "SYSCALL e63f1884\n"
                + "PUSH1\n"
                + "RET\n";

        assertThat(out, is(expected));
    }

}
