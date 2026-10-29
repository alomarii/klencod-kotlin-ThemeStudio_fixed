package com.themestudio.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

final class AppInfo {
    String label, pkg, cls, key;
    Bitmap icon;
}

final class Apps {
    static final HashMap<String, Pack> cache = new HashMap<String, Pack>();

    static Pack pack(Context c, String pkg) {
        if (pkg == null || pkg.isEmpty()) return null;
        Pack p = cache.get(pkg);
        if (p == null) {
            p = Pack.load(c, pkg);
            if (p != null) cache.put(pkg, p);
        }
        return p;
    }

    static File cf(Context c, String prefix, String key) {
        return new File(c.getFilesDir(), prefix + key.replaceAll("[^A-Za-z0-9._]", "_") + ".png");
    }

    static Bitmap icon(Context c, PackageManager pm, Th t, Pack pk, ResolveInfo ri) {
        String key = ri.activityInfo.packageName + "/" + ri.activityInfo.name;
        try {
            File img = cf(c, "ci_", key), emo = cf(c, "ce_", key);
            if (img.exists()) {
                Bitmap b = BitmapFactory.decodeFile(img.getPath());
                if (b != null) return IconRenderer.make(c, t, new BitmapDrawable(c.getResources(), b), 2);
            }
            if (emo.exists()) {
                Bitmap b = BitmapFactory.decodeFile(emo.getPath());
                if (b != null) return IconRenderer.make(c, t, new BitmapDrawable(c.getResources(), b), 0);
            }
            if (pk != null) {
                Drawable pd = pk.get(key);
                if (pd != null) return IconRenderer.make(c, t, pd, 1);
            }
            return IconRenderer.make(c, t, ri.loadIcon(pm), 0);
        } catch (Throwable e) {
            return IconRenderer.blank();
        }
    }

    /** limit <= 0 means all apps. */
    static List<AppInfo> load(Context c, int limit) {
        PackageManager pm = c.getPackageManager();
        Th t = Store.cur(c);
        Intent mi = new Intent(Intent.ACTION_MAIN, null);
        mi.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> l = pm.queryIntentActivities(mi, 0);
        Pack pk = pack(c, t.pack);
        ArrayList<AppInfo> out = new ArrayList<AppInfo>();
        for (ResolveInfo ri : l) {
            try {
                AppInfo a = new AppInfo();
                a.pkg = ri.activityInfo.packageName;
                a.cls = ri.activityInfo.name;
                a.key = a.pkg + "/" + a.cls;
                a.label = Store.sp(c).getString("lbl_" + a.key, String.valueOf(ri.loadLabel(pm)));
                a.icon = icon(c, pm, t, pk, ri);
                out.add(a);
                if (limit > 0 && out.size() >= limit) break;
            } catch (Throwable ignored) { }
        }
        Collections.sort(out, new Comparator<AppInfo>() {
            @Override public int compare(AppInfo a, AppInfo b) { return a.label.compareToIgnoreCase(b.label); }
        });
        return out;
    }

    static LinkedHashMap<String, String> packs(Context c) {
        LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        PackageManager pm = c.getPackageManager();
        String[] acts = {"org.adw.launcher.THEMES", "com.novalauncher.THEME", "com.anddoes.launcher.THEME", "com.gau.go.launcherex.theme"};
        for (String a : acts) {
            try {
                for (ResolveInfo ri : pm.queryIntentActivities(new Intent(a), 0)) {
                    m.put(ri.activityInfo.packageName, String.valueOf(ri.loadLabel(pm)));
                }
            } catch (Throwable ignored) { }
        }
        return m;
    }

    /** Center-cropped square bitmap from a picked image. */
    static Bitmap square(Context c, Uri u, int size) throws Exception {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        InputStream in = c.getContentResolver().openInputStream(u);
        BitmapFactory.decodeStream(in, null, o);
        in.close();
        int s = 1;
        while (Math.min(o.outWidth, o.outHeight) / s > size * 2) s *= 2;
        BitmapFactory.Options p = new BitmapFactory.Options();
        p.inSampleSize = s;
        in = c.getContentResolver().openInputStream(u);
        Bitmap b = BitmapFactory.decodeStream(in, null, p);
        in.close();
        int m = Math.min(b.getWidth(), b.getHeight());
        Bitmap sq = Bitmap.createBitmap(b, (b.getWidth() - m) / 2, (b.getHeight() - m) / 2, m, m);
        return Bitmap.createScaledBitmap(sq, size, size, true);
    }
}
