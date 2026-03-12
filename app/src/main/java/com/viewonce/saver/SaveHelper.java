package com.viewonce.saver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaveHelper {

    public static File getSaveDir(Context ctx) {
        File dir = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "ViewOnceSaved"
        );
        dir.mkdirs();
        return dir;
    }

    public static File save(Context ctx, MediaItem item) throws IOException {
        String ext = item.name.contains(".")
            ? item.name.substring(item.name.lastIndexOf('.'))
            : (item.isVideo ? ".mp4" : ".jpg");

        String ts  = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File   dst = new File(getSaveDir(ctx), "viewonce_" + ts + ext);

        try (FileInputStream in  = new FileInputStream(item.path);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }

        // Notifica galeria
        Intent scan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scan.setData(Uri.fromFile(dst));
        ctx.sendBroadcast(scan);

        return dst;
    }
}
