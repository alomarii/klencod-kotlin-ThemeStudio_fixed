package com.themestudio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StudioActivity extends Activity {
    LinearLayout box, prev;
    ScrollView sv;
    Th th;
    List<ResolveInfo> sample;

    int dp(int v) { return UI.dp(this, v); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        th = Store.cur(this);
        PackageManager pm = getPackageManager();
        Intent mi = new Intent(Intent.ACTION_MAIN, null);
        mi.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> all = pm.queryIntentActivities(mi, 0);
        sample = new ArrayList<ResolveInfo>(all.subList(0, Math.min(6, all.size())));
        sv = new ScrollView(this);
        sv.setFitsSystemWindows(true);
        box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(10), dp(14), dp(30));
        sv.addView(box);
        setContentView(sv);
        build();
    }

    // ---------- small UI helpers ----------
    TextView label(String s) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(14);
        v.setTextColor(0xFFDDDDDD);
        v.setPadding(0, dp(8), 0, dp(2));
        box.addView(v);
        return v;
    }

    void head(String s) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(18);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setTextColor(0xFF80DEEA);
        v.setPadding(0, dp(22), 0, dp(4));
        box.addView(v);
    }

    Button btn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        box.addView(b);
        return b;
    }

    GradientDrawable pill(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(20));
        g.setColor(color);
        return g;
    }

    void changed() {
        Store.set(this, th);
        refreshPreview();
    }

    void seek(final String title, final int min, int max, int val, final Cb cb) {
        final TextView tv = label(title + ": " + val);
        SeekBar sb = new SeekBar(this);
        sb.setMax(max - min);
        sb.setProgress(val - min);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                if (u) { tv.setText(title + ": " + (min + p)); cb.on(min + p); }
            }
            @Override public void onStartTrackingTouch(SeekBar s) { }
            @Override public void onStopTrackingTouch(SeekBar s) { }
        });
        box.addView(sb);
    }

    void paint(List<TextView> l, int sel) {
        for (int i = 0; i < l.size(); i++) l.get(i).setBackground(pill(i == sel ? 0xFF00838F : 0xFF33343F));
    }

    void chips(final String[] names, int sel, final Cb cb) {
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        final List<TextView> tvs = new ArrayList<TextView>();
        for (int i = 0; i < names.length; i++) {
            final int k = i;
            TextView tv = new TextView(this);
            tv.setText(names[i]);
            tv.setTextSize(14);
            tv.setTextColor(Color.WHITE);
            tv.setPadding(dp(14), dp(8), dp(14), dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            tv.setLayoutParams(lp);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { paint(tvs, k); cb.on(k); }
            });
            tvs.add(tv);
            row.addView(tv);
        }
        paint(tvs, sel);
        hs.addView(row);
        box.addView(hs);
    }

    void colorBtn(String title, int color, final Cb cb) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(15);
        tv.setTextColor(Color.WHITE);
        row.addView(tv, new LinearLayout.LayoutParams(0, -2, 1));
        final View sw = new View(this);
        final int[] curC = {color};
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(8));
        g.setColor(color);
        g.setStroke(dp(2), 0xFFFFFFFF);
        sw.setBackground(g);
        row.addView(sw, new LinearLayout.LayoutParams(dp(56), dp(34)));
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ColorPicker.pick(StudioActivity.this, curC[0], new Cb() {
                    @Override public void on(int c) {
                        curC[0] = c;
                        GradientDrawable g2 = new GradientDrawable();
                        g2.setCornerRadius(dp(8));
                        g2.setColor(c);
                        g2.setStroke(dp(2), 0xFFFFFFFF);
                        sw.setBackground(g2);
                        cb.on(c);
                    }
                });
            }
        });
        box.addView(row);
    }

    void refreshPreview() {
        if (prev == null) return;
        prev.removeAllViews();
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(18));
        g.setColor(0xFF14141E);
        prev.setBackground(g);
        PackageManager pm = getPackageManager();
        Pack pk = Apps.pack(this, th.pack);
        for (ResolveInfo ri : sample) {
            Bitmap bm = Apps.icon(this, pm, th, pk, ri);
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(bm);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(48));
            lp.setMargins(dp(4), 0, dp(4), 0);
            prev.addView(iv, lp);
        }
    }

    // ---------- main screen ----------
    void build() {
        final int sy = sv.getScrollY();
        box.removeAllViews();

        TextView title = new TextView(this);
        title.setText("🎨 استوديو الثيمات");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        box.addView(title);
        label("الأيقونات والأشكال تتغير في الشاشة الرئيسية لهذا البرنامج (اللانشر). اجعله شاشتك الرئيسية من الزر في الأسفل.");

        prev = new LinearLayout(this);
        prev.setOrientation(LinearLayout.HORIZONTAL);
        prev.setGravity(Gravity.CENTER);
        prev.setPadding(dp(8), dp(14), dp(8), dp(14));
        LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(-1, -2);
        pl.topMargin = dp(10);
        box.addView(prev, pl);
        refreshPreview();

        head("ثيمات جاهزة");
        final Th[] ps = Th.presets();
        String[] pn = new String[ps.length];
        int psel = -1;
        for (int i = 0; i < ps.length; i++) { pn[i] = ps[i].name; if (ps[i].name.equals(th.name)) psel = i; }
        chips(pn, psel, new Cb() {
            @Override public void on(int i) { th = ps[i].copy(); changed(); build(); }
        });

        head("شكل الأيقونة");
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        final List<ImageView> ivs = new ArrayList<ImageView>();
        for (int i = 0; i < Shapes.NAMES.length; i++) {
            final int k = i;
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(Shapes.preview(i, 0xFFB0BEC5, 96));
            iv.setPadding(dp(6), dp(6), dp(6), dp(6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(60), dp(60));
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            iv.setLayoutParams(lp);
            iv.setBackground(pill(i == th.shape ? 0xFF00838F : 0xFF33343F));
            iv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    th.shape = k;
                    for (int j = 0; j < ivs.size(); j++) ivs.get(j).setBackground(pill(j == k ? 0xFF00838F : 0xFF33343F));
                    changed();
                }
            });
            ivs.add(iv);
            row.addView(iv);
        }
        hs.addView(row);
        box.addView(hs);
        seek("حجم الأيقونة %", 60, 110, th.scale, new Cb() { @Override public void on(int v) { th.scale = v; changed(); } });

        head("خلفية الأيقونة");
        chips(new String[]{"الأصلية", "لون واحد", "تدرّج"}, th.bgMode, new Cb() { @Override public void on(int v) { th.bgMode = v; changed(); } });
        colorBtn("اللون الأول", th.c1, new Cb() { @Override public void on(int v) { th.c1 = v; changed(); } });
        colorBtn("اللون الثاني (للتدرّج)", th.c2, new Cb() { @Override public void on(int v) { th.c2 = v; changed(); } });

        head("تلوين الرمز");
        chips(new String[]{"أصلي", "رمادي", "أبيض", "لون مخصص"}, th.tint, new Cb() { @Override public void on(int v) { th.tint = v; changed(); } });
        colorBtn("اللون المخصص", th.tc, new Cb() { @Override public void on(int v) { th.tc = v; changed(); } });

        head("الإطار");
        seek("سمك الإطار", 0, 6, th.bw, new Cb() { @Override public void on(int v) { th.bw = v; changed(); } });
        colorBtn("لون الإطار", th.bc, new Cb() { @Override public void on(int v) { th.bc = v; changed(); } });

        head("حزم الأيقونات");
        final LinkedHashMap<String, String> packs = Apps.packs(this);
        final List<String> pk = new ArrayList<String>(packs.keySet());
        String[] names = new String[pk.size() + 1];
        names[0] = "بدون";
        for (int i = 0; i < pk.size(); i++) names[i + 1] = packs.get(pk.get(i));
        chips(names, pk.indexOf(th.pack) + 1, new Cb() {
            @Override public void on(int i) { th.pack = i == 0 ? "" : pk.get(i - 1); changed(); }
        });
        label("أيقونات الحزمة:");
        chips(new String[]{"كما هي", "طبّق الشكل عليها"}, th.force ? 1 : 0, new Cb() { @Override public void on(int v) { th.force = v == 1; changed(); } });
        btn("🔎 تحميل حزم أيقونات من Google Play", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=icon%20pack&c=apps"))); }
                catch (Exception e) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=icon%20pack&c=apps"))); } catch (Exception ignored) { }
                }
            }
        });

        head("الشاشة الرئيسية والأزرار");
        seek("عدد الأعمدة", 3, 6, th.cols, new Cb() { @Override public void on(int v) { th.cols = v; changed(); } });
        seek("استدارة الأزرار (الدوك وشريط البحث)", 0, 50, th.btnR, new Cb() { @Override public void on(int v) { th.btnR = v; changed(); } });
        label("أسماء التطبيقات:");
        chips(new String[]{"إظهار", "إخفاء"}, th.labels ? 0 : 1, new Cb() { @Override public void on(int v) { th.labels = v == 0; changed(); } });
        seek("حجم الخط", 9, 16, th.labelSize, new Cb() { @Override public void on(int v) { th.labelSize = v; changed(); } });
        label("نوع الخط:");
        chips(Th.FONTS, th.font, new Cb() { @Override public void on(int v) { th.font = v; changed(); } });
        colorBtn("لون النصوص", th.labelColor, new Cb() { @Override public void on(int v) { th.labelColor = v; changed(); } });
        colorBtn("لون الدوك", th.dock, new Cb() { @Override public void on(int v) { th.dock = v; changed(); } });
        colorBtn("لون قائمة التطبيقات", th.drawer, new Cb() { @Override public void on(int v) { th.drawer = v; changed(); } });
        colorBtn("لون التمييز", th.accent, new Cb() { @Override public void on(int v) { th.accent = v; changed(); } });

        head("ثيماتي");
        btn("💾 حفظ الإعدادات الحالية كثيم جديد", new View.OnClickListener() {
            @Override public void onClick(View v) {
                UI.ask(StudioActivity.this, "اسم الثيم", "ثيمي", false, new SCb() {
                    @Override public void on(String s) {
                        if (s.trim().isEmpty()) return;
                        Th n = th.copy();
                        n.name = s.trim();
                        List<Th> l = Store.mine(StudioActivity.this);
                        l.add(n);
                        Store.saveMine(StudioActivity.this, l);
                        th = n;
                        changed();
                        build();
                    }
                });
            }
        });
        final List<Th> mine = Store.mine(this);
        for (int i = 0; i < mine.size(); i++) {
            final int k = i;
            final Th m = mine.get(i);
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);
            r.setGravity(Gravity.CENTER_VERTICAL);
            TextView tv = new TextView(this);
            tv.setText("⭐ " + m.name);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(15);
            r.addView(tv, new LinearLayout.LayoutParams(0, -2, 1));
            r.addView(small("تطبيق", new View.OnClickListener() {
                @Override public void onClick(View v) { th = m.copy(); changed(); build(); }
            }));
            r.addView(small("مشاركة", new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Intent s = new Intent(Intent.ACTION_SEND);
                    s.setType("text/plain");
                    s.putExtra(Intent.EXTRA_TEXT, m.toJson());
                    startActivity(Intent.createChooser(s, "مشاركة الثيم"));
                }
            }));
            r.addView(small("حذف", new View.OnClickListener() {
                @Override public void onClick(View v) {
                    List<Th> l = Store.mine(StudioActivity.this);
                    if (k < l.size()) l.remove(k);
                    Store.saveMine(StudioActivity.this, l);
                    build();
                }
            }));
            box.addView(r);
        }
        btn("📥 استيراد ثيم (الصق الكود)", new View.OnClickListener() {
            @Override public void onClick(View v) {
                UI.ask(StudioActivity.this, "الصق كود الثيم", "", true, new SCb() {
                    @Override public void on(String s) {
                        try {
                            org.json.JSONObject jo = new org.json.JSONObject(s.trim());
                            if (!jo.has("shape")) throw new Exception();
                        } catch (Exception ex) { Toast.makeText(StudioActivity.this, "كود غير صالح", Toast.LENGTH_SHORT).show(); return; }
                        th = Th.fromJson(s.trim());
                        changed();
                        build();
                    }
                });
            }
        });

        head("النظام");
        btn("🏠 اجعل هذا البرنامج الشاشة الرئيسية", new View.OnClickListener() {
            @Override public void onClick(View v) {
                try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
                catch (Exception e) { Toast.makeText(StudioActivity.this, "افتح إعدادات الهاتف ← التطبيقات الافتراضية ← الشاشة الرئيسية", Toast.LENGTH_LONG).show(); }
            }
        });
        btn("↩️ إعادة الضبط", new View.OnClickListener() {
            @Override public void onClick(View v) { th = Th.presets()[0].copy(); changed(); build(); }
        });
        btn("✅ رجوع للشاشة الرئيسية", new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });

        sv.post(new Runnable() { @Override public void run() { sv.scrollTo(0, sy); } });
    }

    Button small(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(12);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setOnClickListener(l);
        return b;
    }
}
