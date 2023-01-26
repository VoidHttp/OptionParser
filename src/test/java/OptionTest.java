import net.voidhttp.optionparser.*;

public class OptionTest {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();

        Option car = new OptionBuilder()
            .setName("car")
            .setType(OptionType.TEXT)
            .setAliases("-car", "--car")
            .setHelp("enter cars option group")
            .build();

        Option ferrari = new OptionBuilder()
            .setName("ferrari")
            .setType(OptionType.TEXT)
            .setAliases("-f", "--ferrari", "--ferr", "--errari", "--usperlong")
            .setAliases("-f", "--ferrari", "--ferr", "--errari", "--usperlong")
            .setRequired(true)
            .setHelp("use ferrari cars")
            .build();

        Option lamborghini = new OptionBuilder()
            .setName("lamborghini")
            .setType(OptionType.TEXT)
            .setAliases("-l", "--LAMBORGHINI")
            .setRequired(true)
            .setHelp("use lamborghini cars")
            .build();

        OptionGroup carGroup = new OptionGroup(car);
        carGroup.addOption(ferrari);
        carGroup.addOption(lamborghini);

        parser.addOptionGroup(carGroup);

        Option language = new OptionBuilder()
            .setName("language")
            .setType(OptionType.TEXT)
            .setAliases("-lang", "--language")
            .build();

        Option java = new OptionBuilder()
            .setName("java")
            .setType(OptionType.TEXT)
            .setAliases("-j", "--java")
            .setRequired(true)
            .setHelp("do some spaghetti code also this help message is extremely long for some reason idk why tho")
            .build();

        Option php = new OptionBuilder()
            .setName("php")
            .setType(OptionType.TEXT)
            .setAliases("-p", "--php")
            .setRequired(true)
            .build();

        OptionGroup languageGroup = new OptionGroup(language);
        languageGroup.addOption(java);
        languageGroup.addOption(php);

        parser.addOptionGroup(languageGroup);

        parser.addOption(new OptionBuilder()
            .setName("wtf")
            .setType(OptionType.TEXT)
            .setRequired(true)
            .setAliases("-w", "--wtf"));

        parser.parse(new String[] { "-lang", "lol", "-j", "true", "-p", "wtf", "-w", "-h"});

        // System.out.println(java.stringValue());
        // System.out.println(php.stringValue());

        //System.out.println(parser.generateHelp());
    }
}
