package com.viewonce.saver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GalleryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerView rv = findViewById(R.id.recyclerGallery);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<MediaItem> saved = MediaScanner.scanSaved(this);

        if (saved.isEmpty()) {
            Toast.makeText(this, "Nenhuma foto salva ainda.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rv.setAdapter(new MediaAdapter(this, saved, new MediaAdapter.Listener() {
            @Override public void onPreview(MediaItem item) { openPreview(item); }
            @Override public void onSave(MediaItem item) {
                Toast.makeText(GalleryActivity.this,
                    "Este arquivo já está salvo em:\n" + item.path,
                    Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void openPreview(MediaItem item) {
        Intent i = new Intent(this, PreviewActivity.class);
        i.putExtra(PreviewActivity.EXTRA_PATH, item.path);
        i.putExtra(PreviewActivity.EXTRA_NAME, item.name);
        i.putExtra(PreviewActivity.EXTRA_DATE, item.date);
        i.putExtra(PreviewActivity.EXTRA_SIZE, item.size);
        startActivity(i);
    }
}
