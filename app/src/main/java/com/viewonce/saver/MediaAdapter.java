package com.viewonce.saver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.VH> {

    public interface Listener {
        void onPreview(MediaItem item);
        void onSave(MediaItem item);
    }

    private final List<MediaItem> items;
    private final Listener        listener;
    private final Context         ctx;

    public MediaAdapter(Context ctx, List<MediaItem> items, Listener listener) {
        this.ctx      = ctx;
        this.items    = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_media, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        MediaItem item = items.get(pos);
        h.tvName.setText(item.name);

        String ts   = new SimpleDateFormat("dd/MM  HH:mm", Locale.getDefault())
                          .format(new Date(item.date * 1000L));
        String size = (item.size / 1024) + " KB";
        h.tvMeta.setText(ts + "  •  " + size);

        // Thumbnail via Glide
        Glide.with(ctx)
            .load(new File(item.path))
            .transform(new CenterCrop(), new RoundedCorners(8))
            .placeholder(item.isVideo ? android.R.drawable.ic_media_play
                                      : android.R.drawable.ic_menu_gallery)
            .into(h.ivThumb);

        h.btnPreview.setOnClickListener(v -> listener.onPreview(item));
        h.btnSave.setOnClickListener(v -> listener.onSave(item));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView  tvName, tvMeta;
        Button    btnPreview, btnSave;

        VH(View v) {
            super(v);
            ivThumb    = v.findViewById(R.id.ivThumb);
            tvName     = v.findViewById(R.id.tvName);
            tvMeta     = v.findViewById(R.id.tvMeta);
            btnPreview = v.findViewById(R.id.btnPreview);
            btnSave    = v.findViewById(R.id.btnSave);
        }
    }
}
