package net.voidhttp.optionparser;

import net.voidhttp.optionparser.exception.KeyRequiredError;
import net.voidhttp.optionparser.exception.UnknownKeyError;

import java.io.File;
import java.util.*;

public class OptionParser extends OptionHolder {
    private static final int MAX_COMMANDS_LENGTH = 30;
    private static final int MAX_HELP_LENGTH = 50;

    public Options parse(String[] args) {
        Options options = Options.parse(args);
        if ((options.has("-h") || options.has("--help")) || options.getData().isEmpty()) {
            getOptions().add(0, new OptionBuilder()
                .setName("help")
                .setType(OptionType.TEXT)
                .setAliases("-h", "--help")
                .setHelp("show this help message and exit")
                .build());
            System.out.println(generateHelp());
            System.exit(0);
        }
        try {
            testKeys(options);
        } catch (UnknownKeyError e) {
            System.out.println(generateUsage());
            List<String> invalidArguments = getInvalidArguments(options.getData().keySet());
            if (invalidArguments.isEmpty()) {
                getOptions().add(0, new OptionBuilder()
                    .setName("help")
                    .setType(OptionType.TEXT)
                    .setAliases("-h", "--help")
                    .setHelp("show this help message and exit")
                    .build());
                System.out.println(generateHelp());
            } else
                System.out.println("error: unrecognized arguments: " + String.join(" ", invalidArguments) + " - " + invalidArguments.size() + " = " + invalidArguments);
            System.exit(0);
        }
        try {
            validate(options);
        } catch (KeyRequiredError e) {
            System.out.println(generateUsage());
            System.out.print("error: the following arguments are required: ");
            Set<Option> coreRequiredOptions = getCoreRequiredOptions();
            coreRequiredOptions.add(e.getOption());
            Iterator<Option> iterator = coreRequiredOptions.iterator();
            while (iterator.hasNext()) {
                System.out.print(String.join("/", iterator.next().getAliases()));
                if (iterator.hasNext()) {
                    System.out.print(", ");
                }
            }
            System.exit(0);
        }
        return options;
    }

    private Set<Option> getCoreRequiredOptions() {
        Set<Option> options = new HashSet<>();
        for (Option option : getOptions()) {
            if (option.isRequired())
                options.add(option);
        }
        for (OptionGroup group : getGroups()) {
            Option root = group.getRoot();
            if (root.isRequired())
                options.add(root);
        }
        return options;
    }

    private List<String> getInvalidArguments(Set<String> arguments) {
        List<String> invalid = new ArrayList<>();
        for (String argument : arguments) {
            if (!testKeyUsed(argument) && !argument.isEmpty())
                invalid.add(argument);
        }
        return invalid;
    }

    public String generateUsage() {
        StringBuilder builder = new StringBuilder();

        String jarName = new File(OptionParser.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath())
            .getName();

        List<Option> options = new ArrayList<>();
        getAllOptions(options);

        builder.append("usage: ").append(jarName).append(" ");
        if (options.stream().noneMatch(option -> option.getName().equals("help")))
            builder.append("[-h] ");

        for (Option option : getOptions()) {
            if (option.isRequired())
                builder.append(option.getAliases()[0]).append(' ');
        }
        for (OptionGroup group : getGroups()) {
            Option root = group.getRoot();
            if (root.isRequired())
                builder.append(root.getAliases()[0]).append(' ');
        }

        for (Option option : getOptions()) {
            if (!option.isRequired())
                builder.append('[').append(option.getAliases()[0]).append("] ");
        }
        for (OptionGroup group : getGroups()) {
            Option root = group.getRoot();
            if (!root.isRequired())
                builder.append('[').append(root.getAliases()[0]).append("] ");
        }

        return builder.toString();
    }

    public String generateHelp() {
        List<Option> options = new ArrayList<>();
        getAllOptions(options);

        StringBuilder builder = new StringBuilder(generateUsage());
        builder.append("\n\n");

        builder.append("where arguments are:\n");

        Map<String, String> helps = new LinkedHashMap<>();
        int longest = 0;

        for (Option option : options) {
            String commands = getOptionCommands(option);
            int length = commands.length();
            if (length <= MAX_COMMANDS_LENGTH && length > longest)
                longest = length;
            helps.put(commands, option.getHelp());
        }
        for (OptionGroup group : getGroups()) {
            Option root = group.getRoot();
            String commands = getOptionCommands(root);
            int length = commands.length();
            if (length <= MAX_COMMANDS_LENGTH && length > longest)
                longest = length;
            helps.put(commands, root.getHelp());
        }

        int index = 0;
        for (Map.Entry<String, String> entry : helps.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int length = key.length();
            if (length > MAX_COMMANDS_LENGTH) {
                builder.append(key).append('\n');
                builder.append(spaces(longest + 1));
            } else {
                builder.append(spaces(longest - length));
                builder.append(key).append(' ');
            }

            if (value != null) {
                builder.append("  ");

                List<String> help = splitHelp(value);

                builder.append(help.get(0));
                help.remove(0);

                if (!help.isEmpty())
                    builder.append('\n');

                Iterator<String> iterator = help.iterator();
                while (iterator.hasNext()) {
                    builder.append(spaces(longest + 3));
                    builder.append(iterator.next());
                    if (iterator.hasNext())
                        builder.append('\n');
                }
            }
            if (++index < helps.size())
                builder.append('\n');
        }

        return builder.toString();
    }

    private String getOptionCommands(Option option) {
        StringBuilder commands = new StringBuilder();
        Iterator<String> iterator = Arrays.stream(option.getAliases()).iterator();
        while (iterator.hasNext()) {
            commands.append(iterator.next());
            if (iterator.hasNext())
                commands.append(' ');
        }
        return commands.toString();
    }

    private String spaces(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    private List<String> splitHelp(String help) {
        int length = help.length();
        if (length <= MAX_HELP_LENGTH) {
            return new ArrayList<>(Collections.singletonList(help));
        }

        List<List<String>> data = new ArrayList<>();
        List<String> line = new ArrayList<>();
        String[] split = help.split(" ");

        for (String element : split) {
            int lineLength = getLineLength(line);
            int elemLength = element.length();
            // can add element
            if (lineLength + 1 + elemLength < MAX_HELP_LENGTH) {
                line.add(element);
            } else if (elemLength > MAX_HELP_LENGTH) {
                if (lineLength > 0) {
                    data.add(line);
                    line = new ArrayList<>();
                }
                data.add(Collections.singletonList(element));
            } else {
                data.add(line);
                line = new ArrayList<>();
                line.add(element);
            }
        }

        if (!line.isEmpty())
            data.add(line);

        List<String> result = new ArrayList<>();
        for (List<String> list : data)
            result.add(String.join(" ", list));
        return result;
    }

    private int getLineLength(List<String> line) {
        int length = line.size() - 1; // spaces between words
        for (String element : line) {
            length += element.length();
        }
        return length;
    }

    private List<String> splitHelp2(String help) {
        int length = help.length();
        if (length <= MAX_COMMANDS_LENGTH)
            return Collections.singletonList(help);

        List<String> result = new ArrayList<>();

        String[] data = help.split(" ");
        StringBuilder line = new StringBuilder();
        for (String element : data) {
            int lineLength = line.length();
            int elemLength = element.length();
            if (lineLength + 1 + elemLength < MAX_COMMANDS_LENGTH) {
                line.append(element);
            } else if (elemLength > MAX_COMMANDS_LENGTH) {
                if (lineLength > 0) {
                    result.add(line.toString());
                    line = new StringBuilder();
                }
                result.add(element);
            } else {
                result.add(line.toString());
                line = new StringBuilder(element);
            }
        }


        return result;
    }
}
