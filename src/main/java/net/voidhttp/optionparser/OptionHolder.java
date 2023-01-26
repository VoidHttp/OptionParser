package net.voidhttp.optionparser;

import net.voidhttp.optionparser.exception.KeyRequiredError;
import net.voidhttp.optionparser.exception.UnknownKeyError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class OptionHolder {
    private final List<Option> options = new ArrayList<>();

    private final List<OptionGroup> groups = new ArrayList<>();

    public void addOption(Option option) {
        options.add(option);
    }

    public void addOption(OptionBuilder builder) {
        addOption(builder.build());
    }

    public void addOptionGroup(OptionGroup group) {
        groups.add(group);
    }

    boolean testKeyUsed(String key) {
        for (Option option : options) {
            if (option.test(key))
                return true;
        }
        for (OptionGroup group : groups) {
            if (group.testKeyUsed(key))
                return true;
        }
        return false;
    }

    void testKeys(Options opts) {
        for (String key : opts.getData().keySet()) {
            if (!testKeyUsed(key))
                throw new UnknownKeyError(key);
        }
    }

    public void validate(Options opts) {
        Map<String, String> data = opts.getData();
        loop: for (Option option : options) {
            for (String alias : option.getAliases()) {
                if (opts.has(alias)) {
                    option.setValue(data.get(alias));
                    continue loop;
                }
            }
            if (option.isRequired())
                throw new KeyRequiredError(option);
        }

        loop: for (OptionGroup group : groups) {
            Option root = group.getRoot();
            for (String alias : root.getAliases()) {
                if (opts.has(alias)) {
                    root.setValue(data.get(alias));
                    group.validate(opts);
                    continue loop;
                }
            }
            if (root.isRequired())
                throw new KeyRequiredError(root);
        }
    }

    public List<Option> getOptions() {
        return options;
    }

    public List<OptionGroup> getGroups() {
        return groups;
    }

    void getAllOptions(List<Option> options) {
        options.addAll(getOptions());
        if (this instanceof OptionGroup)
            options.add(((OptionGroup) this).getRoot());
        for (OptionGroup group : groups) {
            group.getAllOptions(options);
        }
    }
}
