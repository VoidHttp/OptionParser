package net.voidhttp.optionparser;

import net.voidhttp.optionparser.util.convertible.Convertible;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a command line arguments parser.
 */
public class Options {
    /**
     * The parsed key-value data of the options.
     */
    private final Map<String, String> data;

    /**
     * Initialize the options.
     * @param data options data
     */
    public Options(Map<String, String> data) {
        this.data = data;
    }

    /**
     * Determine if the key is present in the data.
     * @param key option key
     * @return option exists
     */
    public boolean has(String key) {
        return data.containsKey(key);
    }

    /**
     * Get the string value of the key in the data.
     * @param key option key
     * @return option value
     */
    public Convertible<String, String> getString(String key) {
        return has(key)
            ? Convertible.completed(data.get(key))
            : Convertible.empty();
    }

    /**
     * Get the int value of the key in the data.
     * @param key option key
     * @return option value
     */
    public Convertible<String, Integer> getInt(String key) {
        return has(key)
            ? Convertible.of(data.get(key), Integer::parseInt)
            : Convertible.empty();
    }

    /**
     * Get the float value of the key in the data.
     * @param key option key
     * @return option value
     */
    public Convertible<String, Float> getFloat(String key) {
        return has(key)
            ? Convertible.of(data.get(key), Float::parseFloat)
            : Convertible.empty();
    }

    /**
     * Get the double value of the key in the data.
     * @param key option key
     * @return option value
     */
    public Convertible<String, Double> getDouble(String key) {
        return has(key)
            ? Convertible.of(data.get(key), Double::parseDouble)
            : Convertible.empty();
    }

    /**
     * Get the double value of the key in the data.
     * @param key option key
     * @return option value
     */
    public Convertible<String, Boolean> getBoolean(String key) {
        return has(key)
            ? Convertible.of(data.get(key), value -> {
                switch (value.toLowerCase()) {
                    case "enable":
                    case "allow":
                    case "true":
                    case "yes":
                    case "on":
                    case "y":
                        return true;
                    default:
                        return false;
                }
            })
            : Convertible.empty();
    }

    /**
     * Get the parsed key-value data of the options.
     * @return parsed options map
     */
    public Map<String, String> getData() {
        return data;
    }

    /**
     * Get the string representation of the options.
     * @return options debug data
     */
    @Override
    public String toString() {
        return "Options{"
            + "data=" + data
            + '}';
    }

    /**
     * Parse command line options to a map.
     * @param args raw command line arguments
     * @return parsed options
     */
    public static Options parse(String[] args) {
        Map<String, String> data = new HashMap<>();

        // declare variables for argument parsing
        String key = "";

        boolean lastWasKey = false;

        // parse the program arguments
        // skip the first argument, because that is the file path
        for (int i = 0; i < args.length; i++) {
            // get the current argument
            String arg = args[i];

            // test if the current argument is a key
            boolean isKey = arg.charAt(0) == '-';

            // handle key declaration
            if (isKey) {
                // if the key follows a key, the previous key has no value
                if (lastWasKey) {
                    // set the data and reset the key
                    data.put(key, "");
                    key = "";
                }

                // remove the prefix of the key
                key = arg;//.substring(1);

                // handle the last argument
                if (i == args.length - 1) {
                    data.put(key, "");
                    key = "";
                }
            }

            // handle value declaration
            else {
                // set the key and value and reset the key
                data.put(key, arg);
                key = "";
            }
            // update last key state
            lastWasKey = isKey;
        }

        return new Options(data);
    }
}
