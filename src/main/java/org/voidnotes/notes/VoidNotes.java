package org.voidnotes.notes;

import org.voidnotes.notes.base.AppController;
import org.voidnotes.notes.base.Objects;

public class VoidNotes {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", Objects.APP_NAME);
        new AppController().start();
    }
}