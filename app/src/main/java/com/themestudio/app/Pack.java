package com.themestudio.app;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;
import java.util.HashMap;

/** Reads standard icon packs (ADW / Nova / Apex format: appfilter.xml). */
final class Pack {
    String pkg;
    Resources res;
    final HashMap<String, String> map = new HashMap<String, String>();

    static Pack load(Context c, String pkg) {
        try {
            Pack p = new Pack();
            p.pkg = pkg;
            p.res = c.getPackageManager().getResourcesForApplication(pkg);
            XmlPullParser xp;
            int id = p.res.getIdentifier("appfilter", "xml", pkg);
            if (id != 0) {
                xp = p.res.getXml(id);
            } else {
                InputStream in = p.res.getAssets().open("appfilter.xml");
                xp = Xml.newPullParser();
                xp.setInput(in, "UTF-8");
            }
            int ev = xp.getEventType();
            while (ev != XmlPullParser.END_DOCUMENT) {
                if (ev == XmlPullParser.START_TAG && "item".equals(xp.getName())) {
                    String comp = xp.getAttributeValue(null, "component");
                    String dr = xp.getAttributeValue(null, "drawable");
                    if (comp != null && dr != null && comp.startsWith("ComponentInfo{") && comp.endsWith("}")) {
                        p.map.put(comp.substring(14, comp.length() - 1), dr);
                    }
                }
                ev = xp.next();
            }
            return p;
        } catch (Throwable e) {
            return null;
        }
    }

    Drawable get(String key) {
        try {
            String n = map.get(key);
            if (n == null) return null;
            int id = res.getIdentifier(n, "drawable", pkg);
            if (id == 0) return null;
            return res.getDrawable(id, null);
        } catch (Throwable e) {
            return null;
        }
    }
}
