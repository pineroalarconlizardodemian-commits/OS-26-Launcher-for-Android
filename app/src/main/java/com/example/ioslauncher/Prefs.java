package com.example.ioslauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;

public final class Prefs {
    private static final String FILE = "lock_style";
    private static final String K_SIZE = "size";
    private static final String K_COLOR = "color";
    private static final String K_FONT = "font";
    private static final String K_WALLPAPER = "wallpaper_uri";

    private Prefs() {}

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static int getClockSize(Context ctx) { return sp(ctx).getInt(K_SIZE, 90); }
    public static void setClockSize(Context ctx, int size) { sp(ctx).edit().putInt(K_SIZE, size).apply(); }

    public static int getClockColor(Context ctx) { return sp(ctx).getInt(K_COLOR, Color.WHITE); }
    public static void setClockColor(Context ctx, int color) { sp(ctx).edit().putInt(K_COLOR, color).apply(); }

    public static int getClockFont(Context ctx) { return sp(ctx).getInt(K_FONT, 1); }
    public static void setClockFont(Context ctx, int idx) { sp(ctx).edit().putInt(K_FONT, idx).apply(); }

    public static String getWallpaperUri(Context ctx) { return sp(ctx).getString(K_WALLPAPER, null); }
    public static void setWallpaperUri(Context ctx, String uri) { sp(ctx).edit().putString(K_WALLPAPER, uri).apply(); }

    public static final int[] COLORS = new int[]{
            Color.WHITE, 0xFFFF453A, 0xFFFF9F0A, 0xFFFFD60A,
            0xFF32D74B, 0xFF64D2FF, 0xFF0A84FF, 0xFFBF5AF2,
            0xFFFF375F, 0xFF8E8E93
    };

    public static Typeface[] fontTypefaces() {
        return new Typeface[]{
                Typeface.create("sans-serif-thin", Typeface.NORMAL),
                Typeface.create("sans-serif-light", Typeface.NORMAL),
                Typeface.create("sans-serif-medium", Typeface.NORMAL),
                Typeface.create("sans-serif-black", Typeface.NORMAL),
                Typeface.create("serif", Typeface.BOLD),
                Typeface.create("monospace", Typeface.BOLD),
                Typeface.create("cursive", Typeface.NORMAL)
        };
    }

    public static String[] fontNames() {
        return new String[]{"Fina","Ligera","Normal","Negrita","Serif","Mono","Cursiva"};
    }
}
