package com.tuapp.ioslauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.List;

public class IOSLauncherCore {

    private PackageManager packageManager;

    public IOSLauncherCore(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    // Método para obtener la lista de aplicaciones instaladas en el dispositivo
    public List<String> getInstalledAppsList() {
        List<String> appNames = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : availableActivities) {
            String appName = ri.loadLabel(packageManager).toString();
            appNames.add(appName);
        }

        return appNames;
    }
}
