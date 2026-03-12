package com.viewonce.saver;

public class MediaItem {
    public String path;
    public String name;
    public long   date;
    public long   size;
    public boolean isVideo;

    public MediaItem(String path, String name, long date, long size) {
        this.path    = path;
        this.name    = name;
        this.date    = date;
        this.size    = size;
        String lower = name.toLowerCase();
        this.isVideo = lower.endsWith(".mp4") || lower.endsWith(".3gp")
                    || lower.endsWith(".mkv") || lower.endsWith(".avi");
    }
}
