package com.example.ioslauncher;

import android.content.Context;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

public final class BrightnessController {

    private BrightnessController() {}

    /** Lee el brillo actual 0..255 (o 128 si falla). */
    public static int get(Context ctx) {
        try {
            return Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            return 128;
        }
    }

    /** Aplica el brillo al sistema (requiere WRITE_SETTINGS) o solo a la ventana. */
    public static void set(Context ctx, Window window, int value) {
        int v = clamp(value, 5, 255);
        // 1) Intentar escribir en el sistema
        try {
            if (Settings.System.canWrite(ctx)) {
                Settings.System.putInt(ctx.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, v);
            }
        } catch (Exception ignored) {}

        // 2) Aplicar a la ventana (funciona sin permiso, solo afecta a la app)
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.screenBrightness = v / 255f;
            window.setAttributes(lp);
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
