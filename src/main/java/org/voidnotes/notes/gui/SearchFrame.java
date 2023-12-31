package org.voidnotes.notes.gui;

import org.voidnotes.notes.base.AppController;
import org.voidnotes.notes.base.Objects;
import org.voidnotes.notes.base.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static org.voidnotes.notes.base.Objects.APP_NAME;

public class SearchFrame extends JFrame {
    private final AppController controller;

    private SearchPanel searchPanel;

    public SearchFrame(AppController controller) {
        super();
        this.controller = controller;
        initializeFrame();
        initializeComponents();
    }

    private void initializeFrame() {
        setTitle(APP_NAME);
        setSize(controller.getSettings().getSearchFrameWidth(), controller.getSettings().getSearchFrameHeight());
        setLocation(controller.getSettings().getSearchFrameX(), controller.getSettings().getSearchFrameY());
        setMinimumSize(new Dimension(Settings.MIN_SEARCH_FRAME_WIDTH, Settings.MIN_SEARCH_FRAME_HEIGHT));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(GuiFactory.getAppImage());
        GuiFactory.setBackgroundColor(controller, this);
        setAlwaysOnTop(controller.getSettings().isAlwaysOnTop());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.closeSearchFrame();
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                controller.reopenEditorIfNecessary();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                saveFramePosition();
            }

            public void componentMoved(ComponentEvent evt) {
                saveFramePosition();
            }
        });
        setContentPane(GuiFactory.createBorderLayoutPanel(controller));
    }

    private void saveFramePosition() {
        controller.getSettings().setSearchFrameX(getX());
        controller.getSettings().setSearchFrameY(getY());
        controller.getSettings().setSearchFrameWidth(getWidth());
        controller.getSettings().setSearchFrameHeight(getHeight());
        controller.getSettings().save();
    }

    private void initializeComponents() {
        searchPanel = new SearchPanel(controller);
        getContentPane().add(searchPanel, BorderLayout.CENTER);
    }

    public String getSearchInput() {
        return searchPanel.getSearchText();
    }

    public void setSearchInput(String text) {
        searchPanel.setSearchText(text);
    }

    public DefaultListModel<Objects.SearchItem> getNoteListModel() {
        return searchPanel.getNoteListModel();
    }

    public JList<Objects.SearchItem> getNoteList() {
        return searchPanel.getNoteList();
    }

    public void focusSearchInput() {
        searchPanel.focusSearchInput();
    }
}
