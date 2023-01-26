package net.voidhttp.optionparser.util;

/**
 * Represents an argument input validator.
 */
public class Validator {
    /**
     * Check if the input parameter is null.
     * @param object parameter value
     * @param name parameter name
     */
    public static void notNull(Object object, String name) {
        if (object == null)
            throw new IllegalStateException("Value '" + name + "' is null.");
    }

    /**
     * Check if the input parameter is false.
     * @param state parameter value
     * @param name parameter name
     */
    public static void isTrue(boolean state, String name) {
        if (!state)
            throw new IllegalStateException("State '" + name + "' is false.");
    }

    /**
     * Check if the input parameter is true.
     * @param state parameter value
     * @param name parameter name
     */
    public static void isFalse(boolean state, String name) {
        if (state)
            throw new IllegalStateException("State '" + name + "' is true.");
    }

    /**
     * Check if the input parameter is out of the given range.
     * @param number parameter value
     * @param name parameter name
     * @param min range minimum
     * @param max range maximum
     */
    public static void checkRange(int number, String name, int min, int max) {
        if ((min >= 0 && number < min) || (max >= 0 && number > max))
            throw new IllegalStateException("Number '" + name + "' is out of range [" + min + ", " + max + "].");
    }
}
