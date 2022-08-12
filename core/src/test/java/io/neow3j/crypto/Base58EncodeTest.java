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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class Base58EncodeTest {

    private static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(new Object[]{"Hello World".getBytes(), "JxF12TrwUP45BMd"}),
                Arguments.of(new Object[]{BigInteger.valueOf(3471844090L).toByteArray(), "16Ho7Hs"}),
                Arguments.of(new Object[]{new byte[1], "1"}),
                Arguments.of(new Object[]{new byte[7], "1111111"}),
                Arguments.of(new Object[]{new byte[0], ""})
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testEncode(byte[] input, String expected) {
        assertEquals(expected, Base58.encode(input));
    }

}
