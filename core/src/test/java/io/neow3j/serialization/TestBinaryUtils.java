package io.neow3j.serialization;

import io.neow3j.utils.ArrayUtils;
import io.neow3j.utils.Numeric;

import java.util.Arrays;

public class TestBinaryUtils {

    protected byte[] buildArray(int size) {
        byte[] data = new byte[size];
        Arrays.fill(data, (byte) 0xAA);
        return data;
    }

    protected String buildString(int size) {
        char[] data = new char[size];
        Arrays.fill(data, 'a');
        return new String(data);
    }

    public static class ByteArrayBuilder {

        private byte[] prefix;
        private byte[] data;
        private byte[] suffix;

        public ByteArrayBuilder setPrefix(byte[] prefix) {
            this.prefix = prefix;
            return this;
        }

        public ByteArrayBuilder setPrefix(String hex) {
            this.prefix = Numeric.hexStringToByteArray(hex);
            return this;
        }

        public ByteArrayBuilder setSuffix(byte[] suffix) {
            this.suffix = suffix;
            return this;
        }

        public ByteArrayBuilder setSuffix(String hex) {
            this.suffix = Numeric.hexStringToByteArray(hex);
            return this;
        }

        public ByteArrayBuilder setAnyStringWithSize(int size) {
            this.data = new byte[size];
            Arrays.fill(this.data, (byte) 'A');
            return this;
        }

        public ByteArrayBuilder setAnyDataWithSize(int size) {
            this.data = new byte[size];
            Arrays.fill(this.data, (byte) 0xAA);
            return this;
        }

        public ByteArrayBuilder setData(byte[] data) {
            this.data = data;
            return this;
        }

        public ByteArrayBuilder setData(String hex) {
            this.data = Numeric.hexStringToByteArray(hex);
            return this;
        }

        public byte[] getData() {
            return data;
        }

        public byte[] getPrefix() {
            return prefix;
        }

        public byte[] getSuffix() {
            return suffix;
        }

        public String getDataAsHexStringNoPrefix() {
            return Numeric.toHexStringNoPrefix(this.data);
        }

        public byte[] build() {
            return ArrayUtils.concatenate(prefix, data, suffix);
        }

        public String buildAsHexStringNoPrefix() {
            return Numeric.toHexStringNoPrefix(prefix)
                    + Numeric.toHexStringNoPrefix(data)
                    + Numeric.toHexStringNoPrefix(suffix);
        }

    }

}
