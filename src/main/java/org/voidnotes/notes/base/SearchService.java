package org.voidnotes.notes.base;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchService {
    private static final Objects.SearchItem SI_CHOOSE_DIR = new Objects.SearchItem(Objects.SearchItemType.CHOOSE_DIR, Objects.CMD_DIR + " - choose notes base directory", null, null);
    private static final Objects.SearchItem SI_FILE_EXPLORER = new Objects.SearchItem(Objects.SearchItemType.START_FILE_EXPLORER, Objects.CMD_FILES + " - open system file manager", null, null);

    private static final Objects.SearchItem SI_DARK_MODE_INV = new Objects.SearchItem(Objects.SearchItemType.INVALID, Objects.CMD_PREFIX_DARK_MODE + "[on/off] - enable/disable dark mode", null, null);
    private static final Objects.SearchItem SI_DARK_MODE_ON = new Objects.SearchItem(Objects.SearchItemType.DARK_MODE, Objects.CMD_PREFIX_DARK_MODE + "on - enable dark mode", Objects.ARG_ON, null);
    private static final Objects.SearchItem SI_DARK_MODE_OFF = new Objects.SearchItem(Objects.SearchItemType.DARK_MODE, Objects.CMD_PREFIX_DARK_MODE + "off - disable dark mode", Objects.ARG_OFF, null);

    private static final Objects.SearchItem SI_ALWAYS_ON_TOP_INV = new Objects.SearchItem(Objects.SearchItemType.INVALID, Objects.CMD_PREFIX_ALWAYS_ON_TOP + "[on/off] - enable/disable always on top mode", null, null);
    private static final Objects.SearchItem SI_ALWAYS_ON_TOP_ON = new Objects.SearchItem(Objects.SearchItemType.ALWAYS_ON_TOP, Objects.CMD_PREFIX_ALWAYS_ON_TOP + "on - enable always on top mode", Objects.ARG_ON, null);
    private static final Objects.SearchItem SI_ALWAYS_ON_TOP_OFF = new Objects.SearchItem(Objects.SearchItemType.ALWAYS_ON_TOP, Objects.CMD_PREFIX_ALWAYS_ON_TOP + "off - disable always on top mode", Objects.ARG_OFF, null);

    private static final Objects.SearchItem SI_TAB_SIZE_INV = new Objects.SearchItem(Objects.SearchItemType.INVALID, Objects.CMD_PREFIX_TAB_SIZE + "[number] - set tab size in spaces, minimum " + Settings.MIN_TAB_SIZE, null, null);

    private final AppController controller;

    public SearchService(AppController controller) {
        this.controller = controller;
    }

    public List<Objects.SearchItem> getSearchItems(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return Collections.emptyList();
        }

        String lcSearchText = searchText.toLowerCase();

        if (lcSearchText.startsWith("/")) {
            return getCommandSearchItems(lcSearchText);
        }

        List<Objects.SearchItem> searchItems = new ArrayList<>();
        AtomicBoolean exactMatch = new AtomicBoolean(false);
        int dirPrefixOffset = controller.getNotesDirectory().getAbsolutePath().length() + 1;
        try {
            Files.find(controller.getNotesDirectory().toPath(), 5, (path, attr) -> {
                if (path.toFile().isDirectory()) {
                    return false;
                }
                String pathString = path.toString().toLowerCase();
                if (!pathString.endsWith(".txt")) {
                    return false;
                }
                String fileName = pathString.substring(dirPrefixOffset);
                boolean isNoteFile = fileName.endsWith(".txt");
                if (isNoteFile && fileName.equals(lcSearchText + ".txt")) {
                    exactMatch.set(true);
                }
                return isNoteFile && fileName.substring(0, fileName.lastIndexOf('.')).contains(lcSearchText);
            }).forEach(path -> {
                String label = path.toFile().getAbsolutePath().substring(dirPrefixOffset);
                label = label.substring(0, label.lastIndexOf('.'));
                searchItems.add(new Objects.SearchItem(Objects.SearchItemType.OPEN_NOTE, label, null, path));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!exactMatch.get()) {
            searchItems.add(0, new Objects.SearchItem(Objects.SearchItemType.CREATE_NOTE, "Create note \"" + lcSearchText + "\"", null, controller.getNotesDirectory().toPath().resolve(lcSearchText + ".txt")));
        }

        searchItems.sort((o1, o2) -> {
            if (o1.type() == Objects.SearchItemType.INVALID) {
                return 1;
            } else if (o2.type() == Objects.SearchItemType.INVALID) {
                return -1;
            } else if (o1.type() == Objects.SearchItemType.CREATE_NOTE) {
                return -1;
            } else if (o2.type() == Objects.SearchItemType.CREATE_NOTE) {
                return 1;
            } else if (o1.type() == Objects.SearchItemType.OPEN_NOTE && o2.type() == Objects.SearchItemType.OPEN_NOTE) {
                return o1.label().compareTo(o2.label());
            } else {
                return 0;
            }
        });

        return searchItems;
    }

    private List<Objects.SearchItem> getCommandSearchItems(String lcSearchText) {
        List<Objects.SearchItem> searchItems = new ArrayList<>();
        Integer argValue;

        addCommand(searchItems, lcSearchText, Objects.CMD_FILES, SI_FILE_EXPLORER);
        addOnOffCommand(searchItems, lcSearchText, Objects.CMD_PREFIX_DARK_MODE, SI_DARK_MODE_ON, SI_DARK_MODE_OFF, SI_DARK_MODE_INV);
        addCommand(searchItems, lcSearchText, Objects.CMD_DIR, SI_CHOOSE_DIR);
        if ((argValue = addNumberCommand(searchItems, lcSearchText, Objects.CMD_PREFIX_TAB_SIZE, Settings.MIN_TAB_SIZE, SI_TAB_SIZE_INV)) != null) {
            searchItems.add(new Objects.SearchItem(Objects.SearchItemType.TAB_SIZE, Objects.CMD_PREFIX_TAB_SIZE + argValue + " - set editor tab size to " + argValue + " spaces", String.valueOf(argValue), null));
        }
        addOnOffCommand(searchItems, lcSearchText, Objects.CMD_PREFIX_ALWAYS_ON_TOP, SI_ALWAYS_ON_TOP_ON, SI_ALWAYS_ON_TOP_OFF, SI_ALWAYS_ON_TOP_INV);

        return searchItems;
    }

    private void addCommand(List<Objects.SearchItem> searchItems, String lcSearchText, String cmd, Objects.SearchItem item) {
        if (cmd.startsWith(lcSearchText)) {
            searchItems.add(item);
        }
    }

    private void addOnOffCommand(List<Objects.SearchItem> searchItems, String lcSearchText, String cmdPrefix, Objects.SearchItem onItem, Objects.SearchItem offItem, Objects.SearchItem invalidItem) {
        if (lcSearchText.startsWith(cmdPrefix)) {
            if (lcSearchText.length() > cmdPrefix.length()) {
                String arg = lcSearchText.substring(cmdPrefix.length()).toLowerCase().trim();
                if (arg.equals(Objects.ARG_ON)) {
                    searchItems.add(onItem);
                } else if (arg.equals(Objects.ARG_OFF)) {
                    searchItems.add(offItem);
                } else {
                    searchItems.add(invalidItem);
                }
            } else {
                searchItems.add(invalidItem);
            }
        } else if (cmdPrefix.startsWith(lcSearchText)) {
            searchItems.add(invalidItem);
        }
    }

    private Integer addNumberCommand(List<Objects.SearchItem> searchItems, String lcSearchText, String cmdPrefix, int minValue, Objects.SearchItem invalidItem) {
        if (lcSearchText.startsWith(cmdPrefix)) {
            if (lcSearchText.length() > cmdPrefix.length()) {
                String arg = lcSearchText.substring(cmdPrefix.length()).toLowerCase().trim();
                if (isInteger(arg)) {
                    int argInt = Integer.parseInt(arg);
                    if (argInt >= minValue) {
                        return argInt;
                    } else {
                        searchItems.add(invalidItem);
                    }
                } else {
                    searchItems.add(invalidItem);
                }
            } else {
                searchItems.add(invalidItem);
            }
        } else if (cmdPrefix.startsWith(lcSearchText)) {
            searchItems.add(invalidItem);
        }
        return null;
    }

    // https://stackoverflow.com/a/237204
    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
