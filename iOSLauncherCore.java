package com.tuapp.ioslauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IOSLauncherCore {
    private static final String TAG = "IOSLauncherCore";
    private PackageManager packageManager;

    public IOSLauncherCore(PackageManager pm) {
        this.packageManager = pm;
    }

    // Método para obtener las aplicaciones instaladas y mostrarlas al estilo iOS
    public List<String> getInstalledAppsList() {
        List<String> appNames = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : resolveInfos) {
            appNames.add(info.loadLabel(packageManager).toString());
        }
        
        Log.d(TAG, "Aplicaciones cargadas: " + appNames.size());
        return appNames;
    }
}
