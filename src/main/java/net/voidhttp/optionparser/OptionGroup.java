package net.voidhttp.optionparser;

public class OptionGroup extends OptionHolder {
    private final Option root;

    public OptionGroup(Option root) {
        this.root = root;
    }

    public Option getRoot() {
        return root;
    }

    @Override
    boolean testKeyUsed(String key) {
        if (root.test(key))
            return true;
        return super.testKeyUsed(key);
    }
}
