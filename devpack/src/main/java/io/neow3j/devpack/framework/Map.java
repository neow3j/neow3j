package io.neow3j.devpack.framework;

import io.neow3j.constants.OpCode;
import io.neow3j.devpack.framework.annotations.Instruction;

public class Map<K, V> {

        @Instruction(opcode = OpCode.NEWMAP)
        public native Map<K,V> Map();

        @Instruction(opcode = OpCode.PICKITEM)
        public native V get(K key);

        @Instruction(opcode = OpCode.SETITEM)
        public native void set(K key);

        @Instruction(opcode = OpCode.KEYS)
        public native K[] getKeys();

        @Instruction(opcode = OpCode.VALUES)
        public native V[] getValues();

        @Instruction(opcode = OpCode.HASKEY)
        public native boolean hasKey(K key);

        @Instruction(opcode = OpCode.REMOVE)
        public native void Remove(K key);

}
