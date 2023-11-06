package org.voidnotes.notes.gui;

import org.voidnotes.notes.base.AppController;
import org.voidnotes.notes.base.Settings;
import org.voidnotes.notes.base.Objects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class EditorFrame extends JDialog {
    private final AppController controller;

    private EditorPanel editorPanel;

    private Objects.SearchItem searchItem;

    public EditorFrame(AppController controller) {
        super(controller.getSearchFrame());
        this.controller = controller;
        initializeFrame();
        initializeComponents();
    }

    private void initializeFrame() {
        setTitle("Editor");
        setModal(true);
        setSize(controller.getSettings().getEditorFrameWidth(), controller.getSettings().getEditorFrameHeight());
        setLocation(controller.getSettings().getEditorFrameX(), controller.getSettings().getEditorFrameY());
        setMinimumSize(new Dimension(Settings.MIN_EDITOR_FRAME_WIDTH, Settings.MIN_EDITOR_FRAME_HEIGHT));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        GuiFactory.setBackgroundColor(controller, this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.closeEditor();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                controller.getSettings().setEditorFrameWidth(getWidth());
                controller.getSettings().setEditorFrameHeight(getHeight());
                controller.getSettings().save();
            }

            public void componentMoved(ComponentEvent evt) {
                controller.getSettings().setEditorFrameX(getX());
                controller.getSettings().setEditorFrameY(getY());
                controller.getSettings().save();
            }
        });
        setContentPane(GuiFactory.createBorderLayoutPanel(controller));
    }

    private void initializeComponents() {
        editorPanel = new EditorPanel(controller);
        getContentPane().add(editorPanel, BorderLayout.CENTER);
    }

    public void openFile(File notesDirectory, Objects.SearchItem searchItem) {
        this.searchItem = searchItem;
        String title = searchItem.path().toFile().getAbsolutePath().substring(notesDirectory.getAbsolutePath().length() + 1);
        title = title.substring(0, title.lastIndexOf('.'));
        setTitle(title);
        editorPanel.openFile(searchItem.path());
    }

    public Objects.SearchItem getSearchItem() {
        return searchItem;
    }

    public String getText() {
        return editorPanel.getText();
    }
}
