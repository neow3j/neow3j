package io.neow3j.compiler;

import io.neow3j.devpack.annotations.DisplayName;
import io.neow3j.devpack.events.Event2Args;
import io.neow3j.devpack.events.Event5Args;

public class EventsContract {

    private static Event2Args<String, Integer> event1;

    @DisplayName("displayName")
    private static Event5Args<String, Integer, Boolean, String, Object> event2;

    public static boolean main() {
        return true;
    }
}
