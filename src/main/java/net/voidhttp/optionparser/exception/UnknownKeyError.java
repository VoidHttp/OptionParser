package net.voidhttp.optionparser.exception;

public class UnknownKeyError extends RuntimeException {
    private final String key;

    public UnknownKeyError(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
