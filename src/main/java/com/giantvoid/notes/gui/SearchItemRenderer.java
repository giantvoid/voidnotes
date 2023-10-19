package com.giantvoid.notes.gui;

import com.giantvoid.notes.base.AppController;
import com.giantvoid.notes.base.Objects.SearchItem;

import javax.swing.*;
import java.awt.*;

import static com.giantvoid.notes.base.Objects.SearchItemType.OPEN_NOTE;

public class SearchItemRenderer implements ListCellRenderer<SearchItem> {
    private static final String LIGHT_MODE_TEXT_COLOR_HTML = "#777";
    private static final String DARK_MODE_TEXT_COLOR_HTML = "#999";

    private static final String LIGHT_MODE_BOLD_TEXT_COLOR_HTML = "#000";
    private static final String DARK_MODE_BOLD_TEXT_COLOR_HTML = "#bbb";

    private static final String LIGHT_MODE_SELECTED_TEXT_COLOR_HTML = "#333";
    private static final String DARK_MODE_SELECTED_TEXT_COLOR_HTML = "#333";

    private static final String LIGHT_MODE_SELECTED_BOLD_TEXT_COLOR_HTML = "#000";
    private static final String DARK_MODE_SELECTED_BOLD_TEXT_COLOR_HTML = "#000";

    private final AppController controller;

    public SearchItemRenderer(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Component getListCellRendererComponent(JList list, SearchItem value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String searchInput = controller.getSearchInput();
        if (searchInput != null && !searchInput.isBlank()) {
            String labelString = value.label();
            String labelText = "<html><span style=\"font-weight: normal; color: " + (isSelected ? getSelectedTextColorHtml() : getTextColorHtml()) + ";\">" + labelString + "</span></html>";

            int index1 = labelString.toLowerCase().indexOf(searchInput.toLowerCase());
            if (index1 >= 0 && value.type() == OPEN_NOTE) {
                String before = labelString.substring(0, index1);
                String match = labelString.substring(index1, index1 + searchInput.length());
                String after = labelString.substring(index1 + searchInput.length());
                if (isSelected) {
                    labelText = "<html><span style=\"font-weight: normal; color: " + getSelectedTextColorHtml() + ";\">" + before + "<b style=\"color: " + getSelectedBoldTextColorHtml() + ";\">" + match + "</b>" + after + "</span></html>";
                } else {
                    labelText = "<html><span style=\"font-weight: normal; color: " + getTextColorHtml() + ";\">" + before + "<b style=\"color: " + getBoldTextColorHtml() + ";\">" + match + "</b>" + after + "</span></html>";
                }
            }
            label.setText(labelText);
        } else {
            label.setText(value.label());
        }

        label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

        return label;
    }

    private String getTextColorHtml() {
        return controller.getSettings().isDarkMode() ? DARK_MODE_TEXT_COLOR_HTML : LIGHT_MODE_TEXT_COLOR_HTML;
    }

    private String getBoldTextColorHtml() {
        return controller.getSettings().isDarkMode() ? DARK_MODE_BOLD_TEXT_COLOR_HTML : LIGHT_MODE_BOLD_TEXT_COLOR_HTML;
    }

    private String getSelectedTextColorHtml() {
        return controller.getSettings().isDarkMode() ? DARK_MODE_SELECTED_TEXT_COLOR_HTML : LIGHT_MODE_SELECTED_TEXT_COLOR_HTML;
    }

    private String getSelectedBoldTextColorHtml() {
        return controller.getSettings().isDarkMode() ? DARK_MODE_SELECTED_BOLD_TEXT_COLOR_HTML : LIGHT_MODE_SELECTED_BOLD_TEXT_COLOR_HTML;
    }
}
