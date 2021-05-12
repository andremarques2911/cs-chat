package utils;

public enum Commands {
    CREATE_ROOM("::CREATE_ROOM"),
    CREATE_CLIENT("::CREATE_CLIENT"),
    LST_ROOMS("::LST_ROOMS"),
    LST_CLIENTS("::LST_CLIENTS"),
    ENTER_ROOM("::ENTER_ROOM"),
    PV("::PV"),
    HELP("::HELP"),
    END("::END"),
    CURRENT_ROOM("::CURRENT_ROOM"),
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
