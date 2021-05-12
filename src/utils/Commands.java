package utils;

public enum Commands {
    CREATE_SERVER("::CREATE_SERVER"),
    CREATE_CLIENT("::CREATE_CLIENT"),
    LST_SERVERS("::LST_SERVERS"),
    LST_CLIENTS("::LST_CLIENTS"),
    ENTER_SERVER("::ENTER_SERVER"),
    PV("::PV"),
    HELP("::HELP"),
    END("::END"),
    BLOCK("::BLOCK"),
    DEFAULT(null);

    private String abbreviation;

    private Commands(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public static Commands valueOfAbbreviation(String abbreviation) {
        if (abbreviation != null && !abbreviation.isEmpty()) {
            for (Commands comand : Commands.values()) {
                if (abbreviation.equals(comand.abbreviation)) {
                    return comand;
                }
            }
        }
        return null;
    }

    public static String valueOfName(String name) {
        if (name != null && !name.isEmpty()) {
            for (Commands comand : Commands.values()) {
                if (name.equals(comand.name())) {
                    return comand.getAbbreviation();
                }
            }
        }
        return null;
    }
}
