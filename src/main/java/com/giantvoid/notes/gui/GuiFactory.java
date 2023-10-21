package com.giantvoid.notes.gui;

import com.giantvoid.notes.base.AppController;
import com.giantvoid.notes.base.OSUtils;
import com.giantvoid.notes.base.Objects;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.giantvoid.notes.base.Objects.APP_NAME;

public class GuiFactory {
    private static final Image APP_IMAGE = Toolkit.getDefaultToolkit().getImage(Objects.class.getResource("/logo.png"));
    private static final Image APP_IMAGE_MAC = Toolkit.getDefaultToolkit().getImage(Objects.class.getResource("/logo_mac.png"));

    private static final Color DARK_MODE_BORDER_COLOR = Color.DARK_GRAY;
    private static final Color LIGHT_MODE_BORDER_COLOR = Color.LIGHT_GRAY;

    private static final Color DARK_MODE_BACKGROUND_COLOR = new Color(0x1E1E1E);
    private static final Color LIGHT_MODE_BACKGROUND_COLOR = Color.WHITE;

    private static final Color DARK_MODE_TEXT_COLOR = new Color(0xCCCCCC);
    private static final Color LIGHT_MODE_TEXT_COLOR = new Color(0x333333);

    private static final Color DARK_MODE_TEXT_CARET_COLOR = new Color(0xEEEEEE);
    private static final Color LIGHT_MODE_TEXT_CARET_COLOR = Color.BLACK;

    private static final Color DARK_MODE_SELECTION_COLOR = new Color(0x999999);
    private static final Color LIGHT_MODE_SELECTION_COLOR = new Color(0xCCCCCC);

    private static final Color DARK_MODE_SELECTED_TEXT_COLOR = Color.BLACK;
    private static final Color LIGHT_MODE_SELECTED_TEXT_COLOR = Color.BLACK;

    private static final Font EDITOR_FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final Font EDITOR_FONT_SANS_SERIF = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    public static Image getAppImage() {
        return OSUtils.isMac() ? APP_IMAGE_MAC : APP_IMAGE;
    }
    public static JPanel createBorderLayoutPanel(AppController controller) {
        JPanel panel = new JPanel(new BorderLayout());
        setBackgroundColor(controller, panel);
        return panel;
    }

    public static JTextField createTextField(AppController controller) {
        JTextField textField = new JTextField();
        setBackgroundColor(controller, textField);
        setTextColor(controller, textField);
        setBorder(controller, textField, true);
        return textField;
    }

    public static <T> JList<T> createList(AppController controller) {
        JList<T> list = new JList<>();
        setBackgroundColor(controller, list);
        setBorder(controller, list, false);
        list.setSelectionBackground(controller.getSettings().isDarkMode() ? DARK_MODE_SELECTION_COLOR : LIGHT_MODE_SELECTION_COLOR);
        return list;
    }

    public static JTextArea createTextArea(AppController controller) {
        JTextArea textArea = new JTextArea();
        setBackgroundColor(controller, textArea);
        setTextColor(controller, textArea);
        setBorder(controller, textArea, false);
        textArea.setSelectionColor(controller.getSettings().isDarkMode() ? DARK_MODE_SELECTION_COLOR : LIGHT_MODE_SELECTION_COLOR);
        textArea.setFont(getEditorFont(controller));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    public static Font getEditorFont(AppController controller) {
        Font font = controller.getSettings().isEditorFontMonospaced() ? EDITOR_FONT_MONO : EDITOR_FONT_SANS_SERIF;
        if (font.getSize() != controller.getSettings().getEditorFontSize()) {
            font = font.deriveFont((float) controller.getSettings().getEditorFontSize());
        }
        return font;
    }

    public static JScrollPane createScrollPane(AppController controller, Component component, boolean verticalScrollBarAlways) {
        JScrollPane scrollPane = new JScrollPane(component);
        setBackgroundColor(controller, scrollPane);
        scrollPane.setBorder(new LineBorder(controller.getSettings().isDarkMode() ? DARK_MODE_BORDER_COLOR : LIGHT_MODE_BORDER_COLOR, 1, true));

        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarAlways ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS : JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setBackgroundColor(controller, scrollPane.getVerticalScrollBar());
        scrollPane.getVerticalScrollBar().setBorder(null);

        return scrollPane;
    }

    public static TrayIcon createTrayIcon(AppController controller) {
        PopupMenu popup = createPopupMenu(controller);

        TrayIcon trayIcon = new TrayIcon(getAppImage(), APP_NAME, popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    controller.showSearchFrame();
                }
            }
        });
        trayIcon.addActionListener(e -> controller.showSearchFrame());

        return trayIcon;
    }

    public static PopupMenu createPopupMenu(AppController controller) {
        PopupMenu popup = new PopupMenu();
        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(e -> controller.showSearchFrame());
        popup.add(openItem);

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> controller.exit());
        popup.add(exitItem);
        return popup;
    }

    public static void setBackgroundColor(AppController controller, Component component) {
        if (controller.getSettings().isDarkMode()) {
            component.setBackground(DARK_MODE_BACKGROUND_COLOR);
        } else {
            component.setBackground(LIGHT_MODE_BACKGROUND_COLOR);
        }
    }

    private static void setTextColor(AppController controller, Component component) {
        if (controller.getSettings().isDarkMode()) {
            component.setForeground(DARK_MODE_TEXT_COLOR);
        } else {
            component.setForeground(LIGHT_MODE_TEXT_COLOR);
        }

        if (component instanceof JTextComponent textComponent) {
            if (controller.getSettings().isDarkMode()) {
                textComponent.setCaretColor(DARK_MODE_TEXT_CARET_COLOR);
                textComponent.setSelectionColor(DARK_MODE_SELECTION_COLOR);
                textComponent.setSelectedTextColor(DARK_MODE_SELECTED_TEXT_COLOR);
            } else {
                textComponent.setCaretColor(LIGHT_MODE_TEXT_CARET_COLOR);
                textComponent.setSelectionColor(LIGHT_MODE_SELECTION_COLOR);
                textComponent.setSelectedTextColor(LIGHT_MODE_SELECTED_TEXT_COLOR);
            }
        }
    }

    private static void setBorder(AppController controller, JComponent component, boolean lineBorder) {
        if (lineBorder) {
            component.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(controller.getSettings().isDarkMode() ? DARK_MODE_BORDER_COLOR : LIGHT_MODE_BORDER_COLOR, 1, true),
                    new EmptyBorder(5, 5, 5, 5)
            ));
        } else {
            component.setBorder(new EmptyBorder(5, 5, 5, 5));
        }
    }
}
