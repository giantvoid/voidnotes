package com.giantvoid.notes.base;

import com.giantvoid.notes.gui.EditorFrame;
import com.giantvoid.notes.gui.GuiFactory;
import com.giantvoid.notes.gui.SearchFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.giantvoid.notes.base.Objects.ARG_ON;
import static com.giantvoid.notes.base.Objects.SearchItem;
import static com.giantvoid.notes.base.Objects.SearchItemType.*;

public class AppController {
    private SearchFrame searchFrame;
    private EditorFrame editorFrame;

    private JFileChooser notesDirectoryChooser;
    private File notesDirectory;

    private boolean escapeEnabled = true;
    private boolean systemTrayEnabled;

    private final Settings settings;
    private final SearchService searchService;

    public AppController() {
        settings = new Settings();
        searchService = new SearchService(this);
        createFrames();

        initializeTaskbar();
        initializeInitialValues();
        initializeSystemTray();
        initializeNotesDirectoryChooser();
        initializeEscapeKeyListener();
    }

    private void initializeTaskbar() {
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar == null) {
                return;
            }
            if (OSUtils.isMac()) {
                taskbar.setMenu(GuiFactory.createPopupMenu(this));
                // TODO: set dock icon as soon as icon is available
                //taskbar.setIconImage(GuiFactory.APP_IMAGE_MAC);
                return;
            }
            //taskbar.setIconImage(GuiFactory.APP_IMAGE);
        }
    }

    private void createFrames() {
        if (searchFrame != null) {
            searchFrame.dispose();
        }
        searchFrame = new SearchFrame(this);
        if (editorFrame != null) {
            editorFrame.dispose();
        }
        editorFrame = new EditorFrame(this);
    }

    private void initializeSystemTray() {
        systemTrayEnabled = SystemTray.isSupported() && settings.isSystemTrayEnabled();
        if (!systemTrayEnabled) {
            return;
        }

        try {
            SystemTray.getSystemTray().add(GuiFactory.createTrayIcon(this));
        } catch (AWTException e) {
            systemTrayEnabled = false;
            e.printStackTrace();
        }
    }

    private void initializeInitialValues() {
        notesDirectory = null;

        File dirFile = new File(settings.getDirectory());
        if (dirFile.exists() && dirFile.isDirectory() && dirFile.canWrite()) {
            notesDirectory = dirFile;
        }

        if (notesDirectory == null) {
            settings.setDirectory(null);
            notesDirectory = new File(settings.getDirectory());
            if (!notesDirectory.exists()) {
                notesDirectory.mkdirs();
            }
            settings.save();
        }
    }

    private void initializeEscapeKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (!escapeEnabled || e.getID() != KeyEvent.KEY_PRESSED || e.getKeyCode() != KeyEvent.VK_ESCAPE) {
                return false;
            }
            escapePressed();
            return true;
        });
    }

    public String getSearchInput() {
        return searchFrame.getSearchInput();
    }

    private void escapePressed() {
        if (editorFrame.isVisible()) {
            closeEditorFrame();
            if (editorFrame.getSearchItem().type() == CREATE_NOTE) {
                updateSearchInput(getSearchInput());
            }
            return;
        }
        if (searchFrame.isVisible()) {
            String searchInput = getSearchInput();
            if (searchInput != null && !searchInput.isBlank()) {
                searchFrame.setSearchInput("");
            } else {
                closeSearchFrame();
            }
        }
    }

    public SearchFrame getSearchFrame() {
        return searchFrame;
    }

    public void start() {
        showSearchFrame();
    }

    public void showSearchFrame() {
        searchFrame.setVisible(true);
        searchFrame.toFront();
        searchFrame.requestFocus();
        searchFrame.focusSearchInput();
    }

    public void closeSearchFrame() {
        searchFrame.dispose();
        if (!systemTrayEnabled) {
            exit();
        }
    }

    public void exit() {
        System.exit(0);
    }

    public void showEditorFrame(SearchItem searchItem) {
        File file = searchItem.path().toFile();
        if (!file.exists()) {
            try {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                Files.createFile(searchItem.path());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editorFrame.openFile(notesDirectory, searchItem);
        editorFrame.setVisible(true);
    }

    public void closeEditorFrame() {
        editorFrame.dispose();
    }

    public void executeSearchInput(SearchItem searchItem) {
        if (searchItem == null || searchItem.type() == INVALID) {
            return;
        }

        if (searchItem.type() == CREATE_NOTE || searchItem.type() == OPEN_NOTE) {
            showEditorFrame(searchItem);
            return;
        }

        searchFrame.setSearchInput("");
        if (searchItem.type() == CHOOSE_DIR || searchItem.type() == START_FILE_EXPLORER) {
            switch (searchItem.type()) {
                case CHOOSE_DIR -> chooseNotesDirectory();
                case START_FILE_EXPLORER -> OSUtils.openFileManager(notesDirectory);
            }
            return;
        }

        switch (searchItem.type()) {
            case DARK_MODE -> settings.setDarkMode(ARG_ON.equalsIgnoreCase(searchItem.commandArgument()));
            case ALWAYS_ON_TOP -> settings.setAlwaysOnTop(ARG_ON.equalsIgnoreCase(searchItem.commandArgument()));
            case TAB_SIZE -> settings.setTabSize(Integer.parseInt(searchItem.commandArgument()));
        }
        settings.save();
        createFrames();
        showSearchFrame();
    }

    public void updateSearchInput(String text) {
        JList<SearchItem> noteList = searchFrame.getNoteList();
        DefaultListModel<SearchItem> noteListModel = searchFrame.getNoteListModel();
        noteList.clearSelection();
        noteListModel.clear();

        List<SearchItem> searchItems = searchService.getSearchItems(text);

        noteListModel.addAll(searchItems);
        if (!noteListModel.isEmpty()) {
            noteList.setSelectedIndex(0);
        }
    }

    public File getNotesDirectory() {
        return notesDirectory;
    }

    public void searchSelectionChanged(SearchItem item) {
        // TODO
    }

    private void initializeNotesDirectoryChooser() {
        notesDirectoryChooser = new JFileChooser();
        notesDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        notesDirectoryChooser.setDialogTitle("Choose notes directory");
        notesDirectoryChooser.setApproveButtonText("Choose");
        notesDirectoryChooser.setApproveButtonToolTipText("Choose notes directory");
        notesDirectoryChooser.setMultiSelectionEnabled(false);
        notesDirectoryChooser.setFileHidingEnabled(false);
        notesDirectoryChooser.setAcceptAllFileFilterUsed(false);
        notesDirectoryChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directories";
            }
        });
        notesDirectoryChooser.setCurrentDirectory(notesDirectory);
        notesDirectoryChooser.addActionListener(e -> {
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                notesDirectory = notesDirectoryChooser.getSelectedFile();
                settings.setDirectory(notesDirectory.getAbsolutePath());
                settings.save();
            }
        });
    }

    private void chooseNotesDirectory() {
        escapeEnabled = false;
        notesDirectoryChooser.showOpenDialog(searchFrame);
        escapeEnabled = true;
    }

    public void contentChanged(Path path, String text) {
        try {
            Files.writeString(path, text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Settings getSettings() {
        return settings;
    }
}