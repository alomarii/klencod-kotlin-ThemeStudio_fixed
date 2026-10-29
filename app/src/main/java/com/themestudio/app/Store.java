package com.themestudio.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

final class Store {
    static Th cached;
    static int ver = 0;

    static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("ts", Context.MODE_PRIVATE);
    }

    static Th cur(Context c) {
        if (cached == null) {
            String s = sp(c).getString("th", null);
            cached = s == null ? Th.presets()[0].copy() : Th.fromJson(s);
        }
        return cached;
    }

    static void set(Context c, Th t) {
        cached = t;
        sp(c).edit().putString("th", t.toJson()).apply();
        ver++;
    }

    static List<String> getList(Context c, String k) {
        String s = sp(c).getString(k, null);
        if (s == null) return null;
        ArrayList<String> out = new ArrayList<String>();
        try {
            JSONArray a = new JSONArray(s);
            for (int i = 0; i < a.length(); i++) out.add(a.getString(i));
        } catch (Exception ignored) { }
        return out;
    }

    static void putList(Context c, String k, List<String> l) {
        JSONArray a = new JSONArray();
        for (String s : l) a.put(s);
        sp(c).edit().putString(k, a.toString()).apply();
    }

    static List<Th> mine(Context c) {
        ArrayList<Th> out = new ArrayList<Th>();
        List<String> l = getList(c, "mine");
        if (l != null) for (String s : l) out.add(Th.fromJson(s));
        return out;
    }

    static void saveMine(Context c, List<Th> l) {
        ArrayList<String> s = new ArrayList<String>();
        for (Th t : l) s.add(t.toJson());
        putList(c, "mine", s);
    }
}
