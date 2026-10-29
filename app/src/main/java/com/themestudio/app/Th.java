package com.themestudio.app;

import android.graphics.Typeface;
import org.json.JSONObject;

class Th {
    String name = "مخصص";
    int shape = 1, scale = 100, bgMode = 1;
    int c1 = 0xFF3F51B5, c2 = 0xFF9C27B0;
    int tint = 0, tc = 0xFFFFFFFF;
    int bw = 0, bc = 0xFFFFFFFF;
    int cols = 4, labelColor = 0xFFFFFFFF, labelSize = 12, font = 0;
    boolean labels = true, force = false;
    int dock = 0x66000000, drawer = 0xEE101018, accent = 0xFF00BCD4, btnR = 50;
    String pack = "";

    String toJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("name", name); o.put("shape", shape); o.put("scale", scale); o.put("bgMode", bgMode);
            o.put("c1", c1); o.put("c2", c2); o.put("tint", tint); o.put("tc", tc);
            o.put("bw", bw); o.put("bc", bc); o.put("cols", cols); o.put("labelColor", labelColor);
            o.put("labelSize", labelSize); o.put("font", font); o.put("labels", labels); o.put("force", force);
            o.put("dock", dock); o.put("drawer", drawer); o.put("accent", accent); o.put("btnR", btnR);
            o.put("pack", pack);
            return o.toString();
        } catch (Exception e) { return "{}"; }
    }

    static Th fromJson(String s) {
        Th t = new Th();
        try {
            JSONObject o = new JSONObject(s);
            t.name = o.optString("name", t.name); t.shape = o.optInt("shape", t.shape);
            t.scale = o.optInt("scale", t.scale); t.bgMode = o.optInt("bgMode", t.bgMode);
            t.c1 = o.optInt("c1", t.c1); t.c2 = o.optInt("c2", t.c2);
            t.tint = o.optInt("tint", t.tint); t.tc = o.optInt("tc", t.tc);
            t.bw = o.optInt("bw", t.bw); t.bc = o.optInt("bc", t.bc);
            t.cols = o.optInt("cols", t.cols); t.labelColor = o.optInt("labelColor", t.labelColor);
            t.labelSize = o.optInt("labelSize", t.labelSize); t.font = o.optInt("font", t.font);
            t.labels = o.optBoolean("labels", t.labels); t.force = o.optBoolean("force", t.force);
            t.dock = o.optInt("dock", t.dock); t.drawer = o.optInt("drawer", t.drawer);
            t.accent = o.optInt("accent", t.accent); t.btnR = o.optInt("btnR", t.btnR);
            t.pack = o.optString("pack", "");
        } catch (Exception ignored) { }
        return t;
    }

    Th copy() { return fromJson(toJson()); }

    static Typeface tf(int f) {
        switch (f) {
            case 1: return Typeface.SERIF;
            case 2: return Typeface.MONOSPACE;
            case 3: return Typeface.create("sans-serif-light", Typeface.NORMAL);
            case 4: return Typeface.DEFAULT_BOLD;
            case 5: return Typeface.create("cursive", Typeface.NORMAL);
            default: return Typeface.DEFAULT;
        }
    }

    static final String[] FONTS = {"افتراضي", "سيريف", "أحادي", "خفيف", "عريض", "يدوي"};

    static Th P(String n, int shape, int mode, int c1, int c2, int tint, int tc, int bw, int bc,
                int drawer, int dock, int accent, int btnR) {
        Th t = new Th();
        t.name = n; t.shape = shape; t.bgMode = mode; t.c1 = c1; t.c2 = c2; t.tint = tint; t.tc = tc;
        t.bw = bw; t.bc = bc; t.drawer = drawer; t.dock = dock; t.accent = accent; t.btnR = btnR;
        return t;
    }

    static Th[] presets() {
        return new Th[]{
            P("كلاسيك", 0, 0, 0, 0, 0, 0xFFFFFFFF, 0, 0, 0xEE101018, 0x66000000, 0xFF00BCD4, 50),
            P("زجاجي", 1, 1, 0x40FFFFFF, 0, 2, 0xFFFFFFFF, 2, 0x88FFFFFF, 0xDD1B2735, 0x44FFFFFF, 0xFF82B1FF, 40),
            P("نيون", 2, 1, 0xFF0B0B1A, 0, 3, 0xFF00E5FF, 3, 0xFF00E5FF, 0xF0050510, 0x88000020, 0xFF00E5FF, 20),
            P("غروب", 4, 2, 0xFFFF512F, 0xFFDD2476, 2, 0xFFFFFFFF, 0, 0, 0xEE2B0A1E, 0x55FF512F, 0xFFFF6E7F, 50),
            P("أبيض بسيط", 2, 1, 0xFFFFFFFF, 0, 0, 0xFFFFFFFF, 0, 0, 0xF0F5F5F7, 0xAAFFFFFF, 0xFF2979FF, 30),
            P("داكن مينيمال", 0, 1, 0xFF1E1E1E, 0, 2, 0xFFFFFFFF, 0, 0, 0xF2000000, 0x99000000, 0xFFBDBDBD, 50),
            P("وردة", 8, 1, 0xFFFFC1E3, 0, 0, 0xFFFFFFFF, 0, 0, 0xEE3A1230, 0x55FF80AB, 0xFFFF80AB, 50),
            P("سداسي أخضر", 5, 2, 0xFF11998E, 0xFF38EF7D, 2, 0xFFFFFFFF, 0, 0, 0xEE0B2B24, 0x5538EF7D, 0xFF38EF7D, 25),
            P("شارة ذهبية", 9, 2, 0xFFF7971E, 0xFFFFD200, 3, 0xFF3E2723, 0, 0, 0xEE2B1D00, 0x66FFD200, 0xFFFFD200, 50),
            P("قطرة المحيط", 4, 2, 0xFF2193B0, 0xFF6DD5ED, 2, 0xFFFFFFFF, 0, 0, 0xEE062B3A, 0x556DD5ED, 0xFF6DD5ED, 50),
            P("ورقة الطبيعة", 7, 2, 0xFF56AB2F, 0xFFA8E063, 2, 0xFFFFFFFF, 0, 0, 0xEE12260A, 0x55A8E063, 0xFFA8E063, 35),
            P("ثماني ملكي", 6, 2, 0xFF4B1248, 0xFFF0C27B, 2, 0xFFFFFFFF, 0, 0, 0xEE1E0A1C, 0x66F0C27B, 0xFFF0C27B, 20)
        };
    }
}
