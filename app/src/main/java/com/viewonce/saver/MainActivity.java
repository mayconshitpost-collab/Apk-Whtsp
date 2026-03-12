package com.viewonce.saver;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQ_PERMS = 1;

    private TextView  tvStatus, tvAccessStatus;
    private Button    btnScan, btnAccessibility, btnGallery;
    private RecyclerView recycler;

    private final BroadcastReceiver mediaSavedReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context ctx, Intent intent) {
            // Atualiza lista quando serviço salvar automaticamente
            doScan();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus       = findViewById(R.id.tvStatus);
        tvAccessStatus = findViewById(R.id.tvAccessStatus);
        btnScan        = findViewById(R.id.btnScan);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        btnGallery     = findViewById(R.id.btnGallery);
        recycler       = findViewById(R.id.recyclerView);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        btnScan.setOnClickListener(v -> doScan());
        btnGallery.setOnClickListener(v ->
            startActivity(new Intent(this, GalleryActivity.class)));
        btnAccessibility.setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        requestPermissions();
        registerReceiver(mediaSavedReceiver,
            new IntentFilter("com.viewonce.MEDIA_SAVED"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccessibilityStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(mediaSavedReceiver); } catch (Exception ignored) {}
    }

    private void updateAccessibilityStatus() {
        boolean active = isAccessibilityEnabled();
        tvAccessStatus.setText(active
            ? "✅ Acessibilidade: ATIVA — Salva automaticamente!"
            : "⚠️  Acessibilidade INATIVA — Toque em Ativar");
        tvAccessStatus.setTextColor(active
            ? 0xFF22C55E : 0xFFF59E0B);
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager am =
            (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        List<AccessibilityServiceInfo> services =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : services) {
            if (info.getId().contains(getPackageName())) return true;
        }
        return false;
    }

    private void doScan() {
        tvStatus.setText("🔍 Buscando...");
        btnScan.setEnabled(false);

        new Thread(() -> {
            List<MediaItem> items = MediaScanner.scan(this);
            runOnUiThread(() -> {
                btnScan.setEnabled(true);
                if (items.isEmpty()) {
                    tvStatus.setText("📭 Nenhuma mídia encontrada.\nAbra uma foto view-once no WhatsApp e tente novamente.");
                } else {
                    tvStatus.setText("✅ " + items.size() + " mídia(s) encontrada(s)");
                    recycler.setAdapter(new MediaAdapter(this, items, new MediaAdapter.Listener() {
                        @Override public void onPreview(MediaItem item) { openPreview(item); }
                        @Override public void onSave(MediaItem item)    { saveItem(item); }
                    }));
                }
            });
        }).start();
    }

    private void openPreview(MediaItem item) {
        Intent i = new Intent(this, PreviewActivity.class);
        i.putExtra(PreviewActivity.EXTRA_PATH, item.path);
        i.putExtra(PreviewActivity.EXTRA_NAME, item.name);
        i.putExtra(PreviewActivity.EXTRA_DATE, item.date);
        i.putExtra(PreviewActivity.EXTRA_SIZE, item.size);
        startActivity(i);
    }

    private void saveItem(MediaItem item) {
        new Thread(() -> {
            try {
                File saved = SaveHelper.save(this, item);
                runOnUiThread(() ->
                    Toast.makeText(this,
                        "✅ Salvo em ViewOnceSaved:\n" + saved.getName(),
                        Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "❌ Erro: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            }, REQ_PERMS);
        } else {
            requestPermissions(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }, REQ_PERMS);
        }
    }
}
