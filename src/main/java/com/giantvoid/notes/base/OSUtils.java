package com.giantvoid.notes.base;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class OSUtils {
    public static void openFileManager(File file) {
        try {
            if (isWindows()) {
                Runtime.getRuntime().exec("explorer.exe " + file.getAbsolutePath());
            } else if (isMac()) {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", file.getAbsolutePath()});
            } else {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "/usr/bin/xdg-open '" + file.getAbsolutePath() + "'"});
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void openUrl(URL url) {
        try {
            Desktop.getDesktop().browse(url.toURI());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }
}
