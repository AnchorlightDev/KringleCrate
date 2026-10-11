package dev.anchorlight.kringlecrate.commands;

import java.util.ArrayList;
import java.util.List;

/** Small shared helper for prefix-filtering tab-completion suggestions. */
final class Tab {

    private Tab() {
    }

    static List<String> filter(List<String> options, String input) {
        List<String> results = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                results.add(option);
            }
        }
        return results;
    }
}
