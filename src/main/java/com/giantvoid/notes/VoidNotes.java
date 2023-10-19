package com.giantvoid.notes;

import com.giantvoid.notes.base.AppController;

import static com.giantvoid.notes.base.Objects.APP_NAME;

public class VoidNotes {
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", APP_NAME);
        new AppController().start();
    }
}