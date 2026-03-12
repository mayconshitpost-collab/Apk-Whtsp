package com.viewonce.saver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

public class PreviewActivity extends Activity {

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_SIZE = "size";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        String path = getIntent().getStringExtra(EXTRA_PATH);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        long   date = getIntent().getLongExtra(EXTRA_DATE, 0);
        long   size = getIntent().getLongExtra(EXTRA_SIZE, 0);

        MediaItem item = new MediaItem(path, name, date, size);

        ImageView iv = findViewById(R.id.ivPreview);
        Glide.with(this).load(new File(path)).into(iv);

        TextView tvInfo = findViewById(R.id.tvPreviewInfo);
        tvInfo.setText(name + "\n" + (size / 1024) + " KB");

        Button btnSave = findViewById(R.id.btnPreviewSave);
        btnSave.setOnClickListener(v -> {
            try {
                File saved = SaveHelper.save(this, item);
                Toast.makeText(this, "✅ Salvo: " + saved.getName(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "❌ Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Button btnClose = findViewById(R.id.btnPreviewClose);
        btnClose.setOnClickListener(v -> finish());
    }
}
