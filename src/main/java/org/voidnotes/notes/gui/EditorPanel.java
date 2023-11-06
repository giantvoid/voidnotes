package org.voidnotes.notes.gui;

import org.voidnotes.notes.base.AppController;
import org.voidnotes.notes.base.OSUtils;
import org.voidnotes.notes.base.Settings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EditorPanel extends JPanel {
    private final AppController controller;

    private Path path;

    private JTextArea editor;
    private UndoManager undoManager;

    public EditorPanel(AppController controller) {
        super();
        this.controller = controller;
        initializePanel();
        initializeComponents();
    }

    private void initializePanel() {
        GuiFactory.setBackgroundColor(controller, this);
        setLayout(new BorderLayout());
    }

    private void initializeComponents() {
        editor = GuiFactory.createTextArea(controller);
        editor.setTabSize(controller.getSettings().getTabSize());
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                controller.contentChanged(path, editor.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                controller.contentChanged(path, editor.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                controller.contentChanged(path, editor.getText());
            }
        });
        JScrollPane editorScrollPane = GuiFactory.createScrollPane(controller, editor, true);
        add(editorScrollPane, BorderLayout.CENTER);

        undoManager = new UndoManager();
        editor.getDocument().addUndoableEditListener(undoManager);

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() || (OSUtils.isMac() && e.isMetaDown())) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        if (undoManager.canUndo()) {
                            undoManager.undo();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_Y) {
                        if (undoManager.canRedo()) {
                            undoManager.redo();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_M) {
                        controller.getSettings().setEditorFontMonospaced(!controller.getSettings().isEditorFontMonospaced());
                        saveFontSettings();
                    } else if (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_ADD || (OSUtils.isMac() && e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET)) {
                        controller.getSettings().setEditorFontSize(controller.getSettings().getEditorFontSize() + 1);
                        saveFontSettings();
                    } else if (e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == KeyEvent.VK_SUBTRACT || (OSUtils.isMac() && e.getKeyCode() == KeyEvent.VK_SLASH)) {
                        if (controller.getSettings().getEditorFontSize() > Settings.MIN_EDITOR_FONT_SIZE) {
                            controller.getSettings().setEditorFontSize(controller.getSettings().getEditorFontSize() - 1);
                            saveFontSettings();
                        }
                    }
                }
            }
        });
    }

    private void saveFontSettings() {
        controller.getSettings().save();
        editor.setFont(GuiFactory.getEditorFont(controller));
    }

    public void openFile(Path path) {
        this.path = path;
        try {
            editor.setText(Files.readString(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        editor.setCaretPosition(0);
        undoManager.discardAllEdits();
    }

    public String getText() {
        return editor.getText();
    }
}
