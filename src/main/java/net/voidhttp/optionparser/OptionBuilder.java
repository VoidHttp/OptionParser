package net.voidhttp.optionparser;

import net.voidhttp.optionparser.util.Validator;

public class OptionBuilder {
    private String name;

    private OptionType type;

    private boolean required;

    private Object defaultValue;

    private String help;

    private String[] aliases;

    public OptionBuilder(String name, OptionType type, String... aliases) {
        this.name = name;
        this.type = type;
        this.aliases = aliases;
    }

    public OptionBuilder() {
    }

    public OptionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public OptionBuilder setType(OptionType type) {
        this.type = type;
        return this;
    }

    public OptionBuilder setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public OptionBuilder setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public OptionBuilder setHelp(String help) {
        this.help = help;
        return this;
    }

    public OptionBuilder setAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    public Option build() {
        Validator.notNull(name, "option name");
        Validator.notNull(type, "option type");
        Validator.notNull(aliases, "option aliases");
        return new Option(name, type, required, defaultValue, help, aliases);
    }
}
