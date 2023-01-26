package net.voidhttp.optionparser.exception;

import net.voidhttp.optionparser.Option;

public class KeyRequiredError extends RuntimeException {
    private final Option option;

    public KeyRequiredError(Option option) {
        this.option = option;
    }

    public Option getOption() {
        return option;
    }
}
