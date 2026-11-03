package com.example.ioslauncher;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.List;

public final class WallpaperRepository {
    private WallpaperRepository() {}

    public static List<Uri> getRecentImages(Context context, int limit) {
        List<Uri> result = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{ MediaStore.Images.Media._ID };
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

        try (Cursor c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder)) {
            if (c == null) return result;
            int idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            while (c.moveToNext() && result.size() < limit) {
                result.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getLong(idCol)));
            }
        }
        return result;
    }
}
