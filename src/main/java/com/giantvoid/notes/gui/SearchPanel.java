package com.giantvoid.notes.gui;

import com.giantvoid.notes.base.AppController;
import com.giantvoid.notes.base.Objects;
import com.giantvoid.notes.base.Objects.SearchItem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.giantvoid.notes.base.Objects.SearchItemType.INVALID;

public class SearchPanel extends JPanel {
    private final AppController controller;

    private JTextField searchField;
    private JList<SearchItem> noteList;
    private DefaultListModel<SearchItem> noteListModel;

    private boolean updateEnabled = true;

    public SearchPanel(AppController controller) {
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
        searchField = GuiFactory.createTextField(controller);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (updateEnabled) {
                    controller.updateSearchInput(searchField.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (updateEnabled) {
                    controller.updateSearchInput(searchField.getText());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (updateEnabled) {
                    controller.updateSearchInput(searchField.getText());
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && noteList.getSelectedIndex() < noteListModel.size() - 1) {
                    noteList.setSelectedIndex(noteList.getSelectedIndex() + 1);
                    updateSearch();
                } else if (e.getKeyCode() == KeyEvent.VK_UP && noteList.getSelectedIndex() > 0) {
                    noteList.setSelectedIndex(noteList.getSelectedIndex() - 1);
                    updateSearch();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && noteList.getSelectedValue() != null && noteList.getSelectedValue().type() != INVALID) {
                    controller.executeSearchInput(noteList.getSelectedValue());
                }
            }
        });
        add(searchField, BorderLayout.NORTH);

        noteList = GuiFactory.createList(controller);
        noteListModel = new DefaultListModel<>();
        noteList.setModel(noteListModel);
        noteList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            SearchItem item = noteList.getSelectedValue();
            if (item == null) {
                return;
            }
            controller.searchSelectionChanged(item);
        });
        noteList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchField.requestFocus();
                if (e.getClickCount() == 2 && noteList.getSelectedValue() != null && noteList.getSelectedValue().type() != INVALID) {
                    controller.executeSearchInput(noteList.getSelectedValue());
                }
            }
        });
        noteList.setCellRenderer(new SearchItemRenderer(controller));
        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane noteListScrollPane = GuiFactory.createScrollPane(controller, noteList, false);
        add(noteListScrollPane, BorderLayout.CENTER);
    }

    private void updateSearch() {
        noteList.ensureIndexIsVisible(noteList.getSelectedIndex());
        if (noteList.getSelectedValue() != null && noteList.getSelectedValue().type() == Objects.SearchItemType.OPEN_NOTE) {
            updateEnabled = false;
            searchField.setText(noteList.getSelectedValue().label());
            updateEnabled = true;
        }
        noteList.repaint();
        SwingUtilities.invokeLater(() -> {
            searchField.setCaretPosition(searchField.getText().length());
        });
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public void setSearchText(String text) {
        searchField.setText(text);
    }

    public DefaultListModel<SearchItem> getNoteListModel() {
        return noteListModel;
    }

    public JList<SearchItem> getNoteList() {
        return noteList;
    }

    public void focusSearchInput() {
        searchField.requestFocus();
    }
}
