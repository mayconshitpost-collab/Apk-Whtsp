package com.viewonce.saver;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class ViewOnceAccessibilityService extends AccessibilityService {

    // Palavras-chave que aparecem na UI do WhatsApp quando view-once abre
    private static final String[] VIEWONCE_HINTS = {
        "view once", "visualizar uma vez", "visualização única",
        "view_once", "disappearing", "expiring"
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastSaveTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null) return;
        String pkgStr = pkg.toString();
        if (!pkgStr.equals("com.whatsapp") && !pkgStr.equals("com.whatsapp.w4b")) return;

        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
         && type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return;

        // Detecta abertura de view-once pelo conteúdo da tela
        if (isViewOnceScreen(event)) {
            long now = System.currentTimeMillis();
            if (now - lastSaveTime > 3000) { // debounce 3s
                lastSaveTime = now;
                handler.postDelayed(this::autoSaveLatest, 800);
            }
        }
    }

    private boolean isViewOnceScreen(AccessibilityEvent event) {
        // Checa o texto do evento
        List<CharSequence> texts = event.getText();
        if (texts != null) {
            for (CharSequence t : texts) {
                if (t == null) continue;
                String lower = t.toString().toLowerCase();
                for (String hint : VIEWONCE_HINTS) {
                    if (lower.contains(hint)) return true;
                }
            }
        }
        // Checa a janela raiz
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                boolean found = searchNode(root);
                root.recycle();
                return found;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean searchNode(AccessibilityNodeInfo node) {
        if (node == null) return false;
        CharSequence desc = node.getContentDescription();
        CharSequence text = node.getText();
        for (String t : new CharSequence[]{desc, text}) {
            if (t == null) continue;
            String lower = t.toString().toLowerCase();
            for (String hint : VIEWONCE_HINTS) {
                if (lower.contains(hint)) return true;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (searchNode(child)) { child.recycle(); return true; }
                child.recycle();
            }
        }
        return false;
    }

    private void autoSaveLatest() {
        try {
            List<MediaItem> items = MediaScanner.scan(getApplicationContext());
            if (items.isEmpty()) return;

            // Pega a mídia mais recente (a que acabou de ser aberta)
            MediaItem latest = items.get(0);
            long now = System.currentTimeMillis() / 1000L;

            // Só salva se for recente (últimos 30 segundos)
            if (now - latest.date > 30) return;

            File saved = SaveHelper.save(getApplicationContext(), latest);

            handler.post(() ->
                Toast.makeText(getApplicationContext(),
                    "✅ View Once salva: " + saved.getName(),
                    Toast.LENGTH_LONG).show()
            );

            // Notifica MainActivity para atualizar lista
            Intent intent = new Intent("com.viewonce.MEDIA_SAVED");
            sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {}
}
