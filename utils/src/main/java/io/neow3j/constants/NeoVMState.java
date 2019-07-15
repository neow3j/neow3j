package io.neow3j.constants;

public enum NeoVMState {

    NONE((byte)0),
    HALT((byte)1),
    FAULT((byte)2),
    BREAK((byte)4);

    NeoVMState(byte number) {
        this.number = number;
    }

    private byte number;
}
