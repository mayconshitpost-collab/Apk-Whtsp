package com.viewonce.saver;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaScanner {

    private static final String[] IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".webp"};
    private static final String[] VIDEO_EXTS = {".mp4", ".3gp", ".mkv"};

    /** Busca todas as mídias do WhatsApp (view-once e normais). */
    public static List<MediaItem> scan(Context ctx) {
        List<MediaItem> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // 1. Varredura direta nas pastas do WhatsApp
        String base = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] dirs = {
            base + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images",
            base + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video",
            base + "/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/Sent",
            base + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Images",
            base + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/WhatsApp Video",
            base + "/WhatsApp/Media/WhatsApp Images",
            base + "/WhatsApp/Media/WhatsApp Video",
        };

        for (String dirPath : dirs) {
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) continue;
            File[] files = dir.listFiles();
            if (files == null) continue;
            for (File f : files) {
                if (!f.isFile() || f.length() < 1024) continue;
                if (!hasMediaExt(f.getName())) continue;
                if (seen.contains(f.getAbsolutePath())) continue;
                seen.add(f.getAbsolutePath());
                results.add(new MediaItem(
                    f.getAbsolutePath(), f.getName(),
                    f.lastModified() / 1000L, f.length()));
            }
        }

        // 2. MediaStore — pega imagens recentes do WhatsApp
        addFromMediaStore(ctx, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, results, seen);
        addFromMediaStore(ctx, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,  results, seen);

        // Ordena por data decrescente
        Collections.sort(results, (a, b) -> Long.compare(b.date, a.date));
        return results;
    }

    /** Busca fotos JÁ salvas pelo app na pasta ViewOnceSaved. */
    public static List<MediaItem> scanSaved(Context ctx) {
        List<MediaItem> results = new ArrayList<>();
        File dir = SaveHelper.getSaveDir(ctx);
        File[] files = dir.listFiles();
        if (files == null) return results;
        for (File f : files) {
            if (!f.isFile() || !hasMediaExt(f.getName())) continue;
            results.add(new MediaItem(
                f.getAbsolutePath(), f.getName(),
                f.lastModified() / 1000L, f.length()));
        }
        Collections.sort(results, (a, b) -> Long.compare(b.date, a.date));
        return results;
    }

    private static void addFromMediaStore(Context ctx, Uri uri,
                                          List<MediaItem> results, Set<String> seen) {
        String[] proj = {
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
        };
        String sel  = MediaStore.MediaColumns.DATA + " LIKE ?";
        String[] sv = {"%whatsapp%"};
        String ord  = MediaStore.MediaColumns.DATE_ADDED + " DESC";

        try (Cursor c = ctx.getContentResolver().query(uri, proj, sel, sv, ord)) {
            if (c == null) return;
            while (c.moveToNext()) {
                String path = c.getString(0);
                String name = c.getString(1);
                long   date = c.getLong(2);
                long   size = c.getLong(3);
                if (path == null || size < 1024) continue;
                if (!new File(path).exists()) continue;
                if (seen.contains(path)) continue;
                seen.add(path);
                results.add(new MediaItem(path, name != null ? name : new File(path).getName(), date, size));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean hasMediaExt(String name) {
        String lower = name.toLowerCase();
        for (String e : IMAGE_EXTS) if (lower.endsWith(e)) return true;
        for (String e : VIDEO_EXTS) if (lower.endsWith(e)) return true;
        return false;
    }
}
