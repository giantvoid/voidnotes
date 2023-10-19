package com.giantvoid.notes.base;

import java.nio.file.Path;

public class Objects {
    public static final String APP_NAME = "Void Notes";

    public static final String CMD_DIR = "/dir";
    public static final String CMD_FILES = "/files";

    public static final String CMD_PREFIX_DARK_MODE = "/dark ";
    public static final String CMD_PREFIX_ALWAYS_ON_TOP = "/top ";
    public static final String CMD_PREFIX_TAB_SIZE = "/tab ";

    public static final String ARG_ON = "on";
    public static final String ARG_OFF = "off";

    public enum SearchItemType {
        INVALID,
        CREATE_NOTE,
        OPEN_NOTE,
        CHOOSE_DIR,
        START_FILE_EXPLORER,
        DARK_MODE,
        ALWAYS_ON_TOP,
        TAB_SIZE,
    }

    public record SearchItem(SearchItemType type, String label, String commandArgument, Path path) {
    }
}
