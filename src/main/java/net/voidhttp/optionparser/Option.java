package net.voidhttp.optionparser;

import net.voidhttp.optionparser.exception.TypeError;
import net.voidhttp.optionparser.util.convertible.ConversionException;
import net.voidhttp.optionparser.util.convertible.Convertible;

public class Option {
    private final String name;

    private final OptionType type;

    private final boolean required;

    private final Object defaultValue;

    private final String help;

    private final String[] aliases;

    private Object value;
    private boolean present;

    public Option(String name, OptionType type, boolean required, Object defaultValue, String help, String[] aliases) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.help = help;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public OptionType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getHelp() {
        return help;
    }

    public String[] getAliases() {
        return aliases;
    }

    void setValue(String value) {
        try {
            switch (type) {
                case TEXT:
                    this.value = value != null ? value : defaultValue;
                    break;
                case INTEGER:
                    setIntegerValue(value);
                    break;
                case DECIMAL:
                    setDecimalValue(value);
                    break;
                case BOOLEAN:
                    setBooleanValue(value);
                    break;
            }
            present = true;
        } catch (ConversionException e) {
            throw new TypeError("Expected type " + type + " for option '" + name + "', but got '" + value + "'");
        }
    }

    private void setIntegerValue(String value) throws ConversionException {
        Convertible<String, Integer> intConverter = Convertible.of(value, Integer::parseInt);
        if (defaultValue instanceof Number)
            intConverter = intConverter.fallback(((Number) defaultValue).intValue());
        this.value = intConverter.get();
    }

    private void setDecimalValue(String value) throws ConversionException {
        Convertible<String, Double> doubleConverter = Convertible.of(value, Double::parseDouble);
        if (defaultValue instanceof Number)
            doubleConverter = doubleConverter.fallback(((Number) defaultValue).doubleValue());
        this.value = doubleConverter.get();
    }

    private void setBooleanValue(String value) throws ConversionException {
        Convertible<String, Boolean> booleanConverter = Convertible.of(value, val -> {
            switch (val.toLowerCase()) {
                case "enable":
                case "allow":
                case "true":
                case "yes":
                case "on":
                case "y":
                    return true;
                default:
                    throw new TypeError("Expected boolean, but got '" + val + "'");
            }
        });
        if (defaultValue instanceof Boolean)
            booleanConverter = booleanConverter.fallback((Boolean) defaultValue);
        this.value = booleanConverter.get();
    }

    public String stringValue() {
        return String.valueOf(value);
    }

    public int intValue() {
        return (int) value;
    }

    public double doubleValue() {
        return (double) value;
    }

    public boolean booleanValue() {
        return (boolean) value;
    }

    boolean test(String option) {
        option = option.toLowerCase();
        for (String alias : aliases) {
            if (option.equals(alias.toLowerCase()))
                return true;
        }
        return false;
    }

    public boolean isPresent() {
        return present;
    }
}
