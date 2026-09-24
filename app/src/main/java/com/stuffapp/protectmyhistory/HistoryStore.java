package com.stuffapp.protectmyhistory;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public final class HistoryStore {
    private static final Object LOCK = new Object();
    private HistoryStore() {}
    public static void add(Context c, String kind, String app, String detail) {
        if (detail == null || detail.trim().isEmpty()) return;
        String clean = detail.replace("\n", " ").replace("\r", " ").trim();
        String line = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date()) + "\t" + safe(kind) + "\t" + safe(app) + "\t" + safe(clean) + "\n";
        synchronized (LOCK) {
            try (FileOutputStream out = c.openFileOutput("history.tsv", Context.MODE_APPEND)) { out.write(line.getBytes(StandardCharsets.UTF_8)); }
            catch (IOException ignored) {}
        }
    }
    public static String read(Context c) {
        synchronized (LOCK) {
            try (FileInputStream in = c.openFileInput("history.tsv")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] b = new byte[8192]; int n;
                while ((n = in.read(b)) > 0) out.write(b, 0, n);
                return out.toString("UTF-8");
            } catch (Exception e) { return ""; }
        }
    }
    private static String safe(String s) { return s == null ? "" : s.replace("\t", " "); }
}
