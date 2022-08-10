/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.neow3j.crypto;

import io.neow3j.crypto.exceptions.AddressFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Base58DecodeTest {

    private static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(new Object[]{"JxF12TrwUP45BMd", "Hello World".getBytes()}),
                Arguments.of(new Object[]{"1", new byte[1]}),
                Arguments.of(new Object[]{"1111", new byte[4]})
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testDecode(String input, byte[] expected) {
        byte[] actualBytes = Base58.decode(input);
        assertArrayEquals(actualBytes, expected);
    }

    @Test
    public void testDecode_emptyString() {
        assertEquals(0, Base58.decode("").length);
    }

    @Test
    public void testDecode_invalidBase58() {
        AddressFormatException thrown =
                assertThrows(AddressFormatException.class, () -> Base58.decode("This isn't valid base58"));
        assertThat(thrown.getMessage(), is("Invalid character ' ' at position 4"));
    }

}
