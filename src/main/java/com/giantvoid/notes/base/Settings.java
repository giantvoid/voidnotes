package com.giantvoid.notes.base;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Stream;

public class Settings {
    private static final String SETTINGS_FILE_NAME = "settings.txt";

    private static final String PROP_DIRECTORY = "notes_directory";
    private static final String DEFAULT_DIRECTORY_NAME = "VoidNotes";
    private static final String DEFAULT_DIRECTORY = System.getProperty("user.home") + File.separator + DEFAULT_DIRECTORY_NAME;
    private String directory = DEFAULT_DIRECTORY;

    private static final String PROP_TAB_SIZE = "tab_size";
    private static final int DEFAULT_TAB_SIZE = 4;
    private int tabSize = DEFAULT_TAB_SIZE;
    public static final int MIN_TAB_SIZE = 1;

    private static final String PROP_DARK_MODE = "dark_mode";
    private static final boolean DEFAULT_DARK_MODE = false;
    private boolean darkMode = DEFAULT_DARK_MODE;

    private static final String PROP_ALWAYS_ON_TOP = "always_on_top";
    private static final boolean DEFAULT_ALWAYS_ON_TOP = false;
    private boolean alwaysOnTop = DEFAULT_ALWAYS_ON_TOP;

    private static final String PROP_SAVE_MODE = "save_mode";
    public static final String SAVE_MODE_ON_CLOSE = "on_close";
    public static final String SAVE_MODE_IMMEDIATE = "immediate";
    private static final String DEFAULT_SAVE_MODE = SAVE_MODE_ON_CLOSE;
    private String saveMode = DEFAULT_SAVE_MODE;

    private static final String PROP_ENABLE_SYSTEM_TRAY = "enable_system_tray";
    private static final boolean DEFAULT_ENABLE_SYSTEM_TRAY = true;
    private boolean enableSystemTray = DEFAULT_ENABLE_SYSTEM_TRAY;

    private static final String PROP_SEARCH_FRAME_WIDTH = "search_frame_width";
    private static final int DEFAULT_SEARCH_FRAME_WIDTH = 400;
    private int searchFrameWidth = DEFAULT_SEARCH_FRAME_WIDTH;
    public static final int MIN_SEARCH_FRAME_WIDTH = 100;

    private static final String PROP_SEARCH_FRAME_HEIGHT = "search_frame_height";
    private static final int DEFAULT_SEARCH_FRAME_HEIGHT = 500;
    private int searchFrameHeight = DEFAULT_SEARCH_FRAME_HEIGHT;
    public static final int MIN_SEARCH_FRAME_HEIGHT = 100;

    private static final String PROP_SEARCH_FRAME_X = "search_frame_x";
    private static final int DEFAULT_SEARCH_FRAME_X = (Toolkit.getDefaultToolkit().getScreenSize().width - DEFAULT_SEARCH_FRAME_WIDTH) / 2;
    private int searchFrameX = DEFAULT_SEARCH_FRAME_X;

    private static final String PROP_SEARCH_FRAME_Y = "search_frame_y";
    private static final int DEFAULT_SEARCH_FRAME_Y = (Toolkit.getDefaultToolkit().getScreenSize().height - DEFAULT_SEARCH_FRAME_HEIGHT) / 2;
    private int searchFrameY = DEFAULT_SEARCH_FRAME_Y;

    private static final String PROP_EDITOR_FRAME_WIDTH = "editor_frame_width";
    private static final int DEFAULT_EDITOR_FRAME_WIDTH = 800;
    private int editorFrameWidth = DEFAULT_EDITOR_FRAME_WIDTH;
    public static final int MIN_EDITOR_FRAME_WIDTH = 100;

    private static final String PROP_EDITOR_FRAME_HEIGHT = "editor_frame_height";
    private static final int DEFAULT_EDITOR_FRAME_HEIGHT = 600;
    private int editorFrameHeight = DEFAULT_EDITOR_FRAME_HEIGHT;
    public static final int MIN_EDITOR_FRAME_HEIGHT = 100;

    private static final String PROP_EDITOR_FRAME_X = "editor_frame_x";
    private static final int DEFAULT_EDITOR_FRAME_X = (Toolkit.getDefaultToolkit().getScreenSize().width - DEFAULT_EDITOR_FRAME_WIDTH) / 2;
    private int editorFrameX = DEFAULT_EDITOR_FRAME_X;

    private static final String PROP_EDITOR_FRAME_Y = "editor_frame_y";
    private static final int DEFAULT_EDITOR_FRAME_Y = (Toolkit.getDefaultToolkit().getScreenSize().height - DEFAULT_EDITOR_FRAME_HEIGHT) / 2;
    private int editorFrameY = DEFAULT_EDITOR_FRAME_Y;

    private static final String PROP_EDITOR_FONT_MONOSPACED = "editor_font_monospaced";
    private static final boolean DEFAULT_EDITOR_FONT_MONOSPACED = true;
    private boolean editorFontMonospaced = DEFAULT_EDITOR_FONT_MONOSPACED;

    private static final String PROP_EDITOR_FONT_SIZE = "editor_font_size";
    private static final int DEFAULT_EDITOR_FONT_SIZE = 12;
    private int editorFontSize = DEFAULT_EDITOR_FONT_SIZE;
    public static final int MIN_EDITOR_FONT_SIZE = 6;

    private String settingsTemplate;
    private final File settingsFile;

    public Settings() {
        settingsFile = getSettingsFile();
        readSettingsTemplate();
        ensureSettingsFileExist();
        load();
    }

    private File getSettingsFile() {
        if (OSUtils.isWindows()) {
            return new File(System.getProperty("user.home"), "AppData/Roaming/VoidNotes/" + SETTINGS_FILE_NAME);
        } else if (OSUtils.isMac()) {
            return new File(System.getProperty("user.home"), "Library/Application Support/VoidNotes/" + SETTINGS_FILE_NAME);
        }
        return new File(System.getProperty("user.home"), ".config/VoidNotes/" + SETTINGS_FILE_NAME);
    }

    private void readSettingsTemplate() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/settings.properties")))) {
            settingsTemplate = reader.lines().reduce("", (s1, s2) -> s1 + s2 + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        Properties settings = new Properties();
        try (Stream<String> linesStream = Files.lines(settingsFile.toPath())) {
            linesStream.
                    filter(line -> line != null && !line.isEmpty()).
                    map(String::trim).
                    filter(line -> !line.startsWith("#") && !line.startsWith("//") && !line.startsWith("!") && !line.startsWith(";")).
                    filter(line -> line.contains("=")).
                    forEach(line -> {
                        int equalsIndex = line.indexOf('=');
                        if (equalsIndex > 0 && equalsIndex < (line.length() - 1)) {
                            String name = line.substring(0, equalsIndex).trim();
                            String value = line.substring(equalsIndex + 1).trim();
                            settings.put(name, value);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        setValues(settings);
    }

    private void setValues(Properties settings) {
        setDirectory(settings.getProperty(PROP_DIRECTORY));
        setTabSize(getIntValue(settings, PROP_TAB_SIZE, DEFAULT_TAB_SIZE));
        setSearchFrameWidth(getIntValue(settings, PROP_SEARCH_FRAME_WIDTH, DEFAULT_SEARCH_FRAME_WIDTH));
        setSearchFrameHeight(getIntValue(settings, PROP_SEARCH_FRAME_HEIGHT, DEFAULT_SEARCH_FRAME_HEIGHT));
        setSearchFrameX(getIntValue(settings, PROP_SEARCH_FRAME_X, DEFAULT_SEARCH_FRAME_X));
        setSearchFrameY(getIntValue(settings, PROP_SEARCH_FRAME_Y, DEFAULT_SEARCH_FRAME_Y));
        setEditorFrameWidth(getIntValue(settings, PROP_EDITOR_FRAME_WIDTH, DEFAULT_EDITOR_FRAME_WIDTH));
        setEditorFrameHeight(getIntValue(settings, PROP_EDITOR_FRAME_HEIGHT, DEFAULT_EDITOR_FRAME_HEIGHT));
        setEditorFrameX(getIntValue(settings, PROP_EDITOR_FRAME_X, DEFAULT_EDITOR_FRAME_X));
        setEditorFrameY(getIntValue(settings, PROP_EDITOR_FRAME_Y, DEFAULT_EDITOR_FRAME_Y));
        setSystemTrayEnabled(getBooleanValue(settings, PROP_ENABLE_SYSTEM_TRAY, DEFAULT_ENABLE_SYSTEM_TRAY));
        setDarkMode(getBooleanValue(settings, PROP_DARK_MODE, DEFAULT_DARK_MODE));
        setAlwaysOnTop(getBooleanValue(settings, PROP_ALWAYS_ON_TOP, DEFAULT_ALWAYS_ON_TOP));
        setEditorFontMonospaced(getBooleanValue(settings, PROP_EDITOR_FONT_MONOSPACED, DEFAULT_EDITOR_FONT_MONOSPACED));
        setEditorFontSize(getIntValue(settings, PROP_EDITOR_FONT_SIZE, DEFAULT_EDITOR_FONT_SIZE));
        setSaveMode(getStringValue(settings, PROP_SAVE_MODE, DEFAULT_SAVE_MODE, SAVE_MODE_ON_CLOSE, SAVE_MODE_IMMEDIATE));
    }

    private boolean getBooleanValue(Properties settings, String propertyName, boolean defaultValue) {
        if (!settings.containsKey(propertyName)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(settings.getProperty(propertyName));
    }

    private int getIntValue(Properties settings, String propertyName, int defaultValue) {
        if (!settings.containsKey(propertyName)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(settings.getProperty(propertyName), 10);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String getStringValue(Properties settings, String propertyName, String defaultValue, String... allowedValues) {
        if (!settings.containsKey(propertyName)) {
            return defaultValue;
        }
        if (allowedValues != null) {
            String value = settings.getProperty(propertyName);
            for (String allowedValue : allowedValues) {
                if (allowedValue.equalsIgnoreCase(value)) {
                    return allowedValue;
                }
            }
        }
        return defaultValue;
    }

    public void save() {
        ensureSettingsFileExist();

        Properties settings = new Properties();
        settings.put(PROP_DIRECTORY, getDirectory());
        settings.put(PROP_TAB_SIZE, String.valueOf(getTabSize()));
        settings.put(PROP_SEARCH_FRAME_WIDTH, String.valueOf(getSearchFrameWidth()));
        settings.put(PROP_SEARCH_FRAME_HEIGHT, String.valueOf(getSearchFrameHeight()));
        settings.put(PROP_SEARCH_FRAME_X, String.valueOf(getSearchFrameX()));
        settings.put(PROP_SEARCH_FRAME_Y, String.valueOf(getSearchFrameY()));
        settings.put(PROP_EDITOR_FRAME_WIDTH, String.valueOf(getEditorFrameWidth()));
        settings.put(PROP_EDITOR_FRAME_HEIGHT, String.valueOf(getEditorFrameHeight()));
        settings.put(PROP_EDITOR_FRAME_X, String.valueOf(getEditorFrameX()));
        settings.put(PROP_EDITOR_FRAME_Y, String.valueOf(getEditorFrameY()));
        settings.put(PROP_ENABLE_SYSTEM_TRAY, String.valueOf(isSystemTrayEnabled()));
        settings.put(PROP_DARK_MODE, String.valueOf(isDarkMode()));
        settings.put(PROP_ALWAYS_ON_TOP, String.valueOf(isAlwaysOnTop()));
        settings.put(PROP_EDITOR_FONT_MONOSPACED, String.valueOf(isEditorFontMonospaced()));
        settings.put(PROP_EDITOR_FONT_SIZE, String.valueOf(getEditorFontSize()));
        settings.put(PROP_SAVE_MODE, getSaveMode());
        settings.put("min_" + PROP_TAB_SIZE, String.valueOf(MIN_TAB_SIZE));
        settings.put("min_" + PROP_EDITOR_FONT_SIZE, String.valueOf(MIN_EDITOR_FONT_SIZE));
        settings.put("default_" + PROP_DIRECTORY + "_name", DEFAULT_DIRECTORY_NAME);
        settings.put("default_" + PROP_TAB_SIZE, String.valueOf(DEFAULT_TAB_SIZE));
        settings.put("default_" + PROP_EDITOR_FONT_SIZE, String.valueOf(DEFAULT_EDITOR_FONT_SIZE));

        String settingsText = settingsTemplate;
        for (String propertyName : settings.stringPropertyNames()) {
            settingsText = settingsText.replace("${" + propertyName + "}", settings.getProperty(propertyName));
        }

        try (FileWriter writer = new FileWriter(settingsFile)) {
            writer.write(settingsText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureSettingsFileExist() {
        if (!settingsFile.exists()) {
            try {
                settingsFile.getParentFile().mkdirs();
                settingsFile.createNewFile();
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory == null ? DEFAULT_DIRECTORY : directory.trim();
    }

    public int getTabSize() {
        return tabSize;
    }

    public void setTabSize(int tabSize) {
        this.tabSize = tabSize < MIN_TAB_SIZE ? DEFAULT_TAB_SIZE : tabSize;
    }

    public int getSearchFrameWidth() {
        return searchFrameWidth;
    }

    public void setSearchFrameWidth(int searchFrameWidth) {
        this.searchFrameWidth = searchFrameWidth < MIN_SEARCH_FRAME_WIDTH ? DEFAULT_SEARCH_FRAME_WIDTH : searchFrameWidth;
    }

    public int getSearchFrameHeight() {
        return searchFrameHeight;
    }

    public void setSearchFrameHeight(int searchFrameHeight) {
        this.searchFrameHeight = searchFrameHeight < MIN_SEARCH_FRAME_HEIGHT ? DEFAULT_SEARCH_FRAME_HEIGHT : searchFrameHeight;
    }

    public int getSearchFrameX() {
        return searchFrameX;
    }

    public void setSearchFrameX(int searchFrameX) {
        this.searchFrameX = searchFrameX;
    }

    public int getSearchFrameY() {
        return searchFrameY;
    }

    public void setSearchFrameY(int searchFrameY) {
        this.searchFrameY = searchFrameY;
    }

    public int getEditorFrameWidth() {
        return editorFrameWidth;
    }

    public void setEditorFrameWidth(int editorFrameWidth) {
        this.editorFrameWidth = editorFrameWidth < MIN_EDITOR_FRAME_WIDTH ? DEFAULT_EDITOR_FRAME_WIDTH : editorFrameWidth;
    }

    public int getEditorFrameHeight() {
        return editorFrameHeight;
    }

    public void setEditorFrameHeight(int editorFrameHeight) {
        this.editorFrameHeight = editorFrameHeight < MIN_EDITOR_FRAME_HEIGHT ? DEFAULT_EDITOR_FRAME_HEIGHT : editorFrameHeight;
    }

    public int getEditorFrameX() {
        return editorFrameX;
    }

    public void setEditorFrameX(int editorFrameX) {
        this.editorFrameX = editorFrameX;
    }

    public int getEditorFrameY() {
        return editorFrameY;
    }

    public void setEditorFrameY(int editorFrameY) {
        this.editorFrameY = editorFrameY;
    }

    public boolean isSystemTrayEnabled() {
        return enableSystemTray;
    }

    public void setSystemTrayEnabled(boolean enableSystemTray) {
        this.enableSystemTray = enableSystemTray;
    }

    public boolean isEditorFontMonospaced() {
        return editorFontMonospaced;
    }

    public void setEditorFontMonospaced(boolean editorFontMonospaced) {
        this.editorFontMonospaced = editorFontMonospaced;
    }

    public int getEditorFontSize() {
        return editorFontSize;
    }

    public void setEditorFontSize(int editorFontSize) {
        this.editorFontSize = editorFontSize < MIN_EDITOR_FONT_SIZE ? DEFAULT_EDITOR_FONT_SIZE : editorFontSize;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    public String getSaveMode() {
        return saveMode;
    }

    public void setSaveMode(String saveMode) {
        if (saveMode != null && (saveMode.equals(SAVE_MODE_ON_CLOSE) || saveMode.equals(SAVE_MODE_IMMEDIATE))) {
            this.saveMode = saveMode;
        } else {
            this.saveMode = DEFAULT_SAVE_MODE;
        }
    }

    public boolean isSaveModeImmediate() {
        return SAVE_MODE_IMMEDIATE.equals(saveMode);
    }
}
