package com.example.ioslauncher;

import android.content.Context;
import android.media.AudioManager;

public final class VolumeController {

    private VolumeController() {}

    public static AudioManager am(Context ctx) {
        return (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
    }

    public static int getMax(Context ctx) {
        return am(ctx).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public static int getCurrent(Context ctx) {
        return am(ctx).getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /** value entre 0 y max. */
    public static void set(Context ctx, int value) {
        AudioManager a = am(ctx);
        int max = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int v = Math.max(0, Math.min(max, value));
        a.setStreamVolume(AudioManager.STREAM_MUSIC, v, 0);
    }

    public static float getPercent(Context ctx) {
        return getCurrent(ctx) / (float) getMax(ctx);
    }
}
