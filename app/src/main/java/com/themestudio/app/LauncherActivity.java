package com.themestudio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.os.Build;
import android.widget.TextView.OnEditorActionListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LauncherActivity extends Activity {
    FrameLayout root;
    LinearLayout home, drawer, dockRow;
    GridView homeGrid, appsGrid;
    EditText search;
    TextClock clock, date;
    TextView hint, gear;
    AppAdapter homeAd, appsAd;
    List<AppInfo> all = new ArrayList<AppInfo>();
    int loadedVer = -1;
    int reloadSeq = 0;
    GestureDetector gd;
    String pendingKey;
    Th t;
    boolean drawerOpen;
    BroadcastReceiver pr;

    int dp(int v) { return UI.dp(this, v); }

    static class DotsView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        DotsView(Context c, int col) { super(c); p.setColor(col); }
        @Override protected void onDraw(Canvas cv) {
            float w = getWidth(), h = getHeight(), step = Math.min(w, h) / 5f;
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    cv.drawCircle(w / 2f + (i - 1) * step * 1.2f, h / 2f + (j - 1) * step * 1.2f, step * 0.35f, p);
        }
    }

    class AppAdapter extends BaseAdapter {
        List<AppInfo> d = new ArrayList<AppInfo>();
        void setData(List<AppInfo> l) { d = l; notifyDataSetChanged(); }
        @Override public int getCount() { return d.size(); }
        @Override public Object getItem(int i) { return d.get(i); }
        @Override public long getItemId(int i) { return i; }
        @Override public View getView(int i, View cv, ViewGroup p) {
            LinearLayout l;
            ImageView iv;
            TextView tv;
            if (cv == null) {
                l = new LinearLayout(LauncherActivity.this);
                l.setOrientation(LinearLayout.VERTICAL);
                l.setGravity(Gravity.CENTER_HORIZONTAL);
                l.setPadding(dp(2), dp(8), dp(2), dp(4));
                iv = new ImageView(LauncherActivity.this);
                l.addView(iv, new LinearLayout.LayoutParams(dp(58), dp(58)));
                tv = new TextView(LauncherActivity.this);
                tv.setGravity(Gravity.CENTER);
                tv.setSingleLine(true);
                tv.setEllipsize(TextUtils.TruncateAt.END);
                tv.setPadding(0, dp(4), 0, 0);
                l.addView(tv, new LinearLayout.LayoutParams(-1, -2));
                l.setTag(new View[]{iv, tv});
            } else {
                l = (LinearLayout) cv;
            }
            View[] vs = (View[]) l.getTag();
            iv = (ImageView) vs[0];
            tv = (TextView) vs[1];
            AppInfo a = d.get(i);
            iv.setImageBitmap(a.icon);
            tv.setText(a.label);
            tv.setVisibility(t.labels ? View.VISIBLE : View.GONE);
            tv.setTextColor(t.labelColor);
            tv.setTextSize(t.labelSize);
            tv.setTypeface(Th.tf(t.font));
            tv.setShadowLayer(3, 0, 1, 0xAA000000);
            return l;
        }
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        t = Store.cur(this);
        root = new FrameLayout(this);
        root.setFitsSystemWindows(true);

        home = new LinearLayout(this);
        home.setOrientation(LinearLayout.VERTICAL);
        home.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout ct = new LinearLayout(this);
        ct.setOrientation(LinearLayout.VERTICAL);
        clock = new TextClock(this);
        clock.setFormat12Hour("h:mm");
        clock.setFormat24Hour("HH:mm");
        clock.setTextSize(48);
        date = new TextClock(this);
        date.setFormat12Hour("EEEE d MMMM");
        date.setFormat24Hour("EEEE d MMMM");
        date.setTextSize(16);
        ct.addView(clock);
        ct.addView(date);
        head.addView(ct, new LinearLayout.LayoutParams(0, -2, 1));
        gear = new TextView(this);
        gear.setText("🎨");
        gear.setTextSize(26);
        gear.setPadding(dp(12), dp(8), dp(12), dp(8));
        gear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(LauncherActivity.this, StudioActivity.class)); }
        });
        head.addView(gear);
        home.addView(head);

        homeGrid = new GridView(this);
        homeGrid.setVerticalScrollBarEnabled(false);
        homeGrid.setSelector(new ColorDrawable(0));
        home.addView(homeGrid, new LinearLayout.LayoutParams(-1, 0, 1));

        dockRow = new LinearLayout(this);
        dockRow.setOrientation(LinearLayout.HORIZONTAL);
        dockRow.setGravity(Gravity.CENTER);
        dockRow.setPadding(dp(8), dp(6), dp(8), dp(6));
        home.addView(dockRow, new LinearLayout.LayoutParams(-1, dp(84)));
        root.addView(home, new FrameLayout.LayoutParams(-1, -1));

        hint = new TextView(this);
        hint.setText("اضغط مطولاً على أي تطبيق في القائمة\nلإضافته إلى الشاشة الرئيسية");
        hint.setGravity(Gravity.CENTER);
        hint.setTextColor(0xCCFFFFFF);
        root.addView(hint, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));

        drawer = new LinearLayout(this);
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setPadding(dp(12), dp(8), dp(12), dp(8));
        drawer.setVisibility(View.GONE);
        drawer.setClickable(true);
        search = new EditText(this);
        search.setHint("🔍 ابحث عن تطبيق");
        search.setSingleLine(true);
        search.setPadding(dp(18), 0, dp(18), 0);
        drawer.addView(search, new LinearLayout.LayoutParams(-1, dp(46)));
        appsGrid = new GridView(this);
        appsGrid.setVerticalScrollBarEnabled(false);
        appsGrid.setSelector(new ColorDrawable(0));
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, 0, 1);
        gp.topMargin = dp(8);
        drawer.addView(appsGrid, gp);
        root.addView(drawer, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        homeAd = new AppAdapter();
        appsAd = new AppAdapter();
        homeGrid.setAdapter(homeAd);
        appsGrid.setAdapter(appsAd);
        AdapterView.OnItemClickListener oc = new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) { launch((AppInfo) p.getAdapter().getItem(pos)); }
        };
        AdapterView.OnItemLongClickListener ol = new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> p, View v, int pos, long id) { menu((AppInfo) p.getAdapter().getItem(pos)); return true; }
        };
        homeGrid.setOnItemClickListener(oc);
        homeGrid.setOnItemLongClickListener(ol);
        appsGrid.setOnItemClickListener(oc);
        appsGrid.setOnItemLongClickListener(ol);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filterApps(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });

        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener(new OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent e) {
                if (appsAd.getCount() > 0) { launch((AppInfo) appsAd.getItem(0)); closeDrawer(); }
                return true;
            }
        });

        gd = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onFling(MotionEvent a, MotionEvent b2, float vx, float vy) {
                if (!drawerOpen && vy < -1200 && Math.abs(vy) > Math.abs(vx)) { openDrawer(); return true; }
                if (drawerOpen && vy > 1500 && Math.abs(vy) > Math.abs(vx) && appsGrid.getFirstVisiblePosition() == 0
                        && (appsGrid.getChildCount() == 0 || appsGrid.getChildAt(0).getTop() >= 0)) { closeDrawer(); return true; }
                return false;
            }
        });

        pr = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) { Apps.cache.clear(); Store.ver++; reload(); }
        };
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addAction(Intent.ACTION_PACKAGE_REPLACED);
        f.addDataScheme("package");
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(pr, f, Context.RECEIVER_EXPORTED);
        else registerReceiver(pr, f);
        applyTheme();
    }

    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
        gd.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override protected void onResume() {
        super.onResume();
        if (Store.ver != loadedVer) reload();
    }

    @Override protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        if (drawerOpen) closeDrawer();
    }

    @Override public void onBackPressed() {
        if (drawerOpen) closeDrawer();
    }

    @Override protected void onDestroy() {
        try { unregisterReceiver(pr); } catch (Exception ignored) { }
        super.onDestroy();
    }

    void reload() {
        final int v = Store.ver;
        final int seq = ++reloadSeq;
        t = Store.cur(this);
        final Context app = getApplicationContext();
        new Thread(new Runnable() {
            @Override public void run() {
                List<AppInfo> tmp;
                try { tmp = Apps.load(app, 0); } catch (Throwable e) { tmp = new ArrayList<AppInfo>(); }
                final List<AppInfo> l = tmp;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (seq != reloadSeq || isFinishing()) return;
                        all = l;
                        loadedVer = v;
                        applyTheme();
                        refreshLists();
                    }
                });
            }
        }).start();
    }

    GradientDrawable rounded(int color, int h) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(h * Math.min(50, t.btnR) / 100f);
        return g;
    }

    void applyTheme() {
        clock.setTextColor(t.labelColor);
        date.setTextColor(t.labelColor);
        clock.setTypeface(Th.tf(t.font));
        date.setTypeface(Th.tf(t.font));
        clock.setShadowLayer(4, 0, 1, 0x88000000);
        date.setShadowLayer(4, 0, 1, 0x88000000);
        dockRow.setBackground(rounded(t.dock, dp(84)));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dockRow.getLayoutParams();
        lp.setMargins(dp(8), dp(4), dp(8), dp(8));
        dockRow.setLayoutParams(lp);
        drawer.setBackgroundColor(t.drawer);
        search.setBackground(rounded(0x33FFFFFF, dp(46)));
        search.setTextColor(t.labelColor);
        search.setHintTextColor(0x99FFFFFF);
        homeGrid.setNumColumns(t.cols);
        appsGrid.setNumColumns(t.cols);
        homeAd.notifyDataSetChanged();
        appsAd.notifyDataSetChanged();
    }

    List<String> lst(String k) {
        List<String> l = Store.getList(this, k);
        return l == null ? new ArrayList<String>() : l;
    }

    AppInfo find(HashMap<String, AppInfo> m, String k) { return m.get(k); }

    List<String> defaultDock() {
        ArrayList<String> out = new ArrayList<String>();
        PackageManager pm = getPackageManager();
        Intent[] ins = {
            new Intent(Intent.ACTION_DIAL),
            new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
            new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")),
            new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        };
        for (Intent in : ins) {
            try {
                ResolveInfo r = pm.resolveActivity(in, PackageManager.MATCH_DEFAULT_ONLY);
                if (r == null || r.activityInfo == null) continue;
                for (AppInfo a : all) {
                    if (a.pkg.equals(r.activityInfo.packageName)) {
                        if (!out.contains(a.key)) out.add(a.key);
                        break;
                    }
                }
            } catch (Exception ignored) { }
        }
        return out;
    }

    void refreshLists() {
        HashMap<String, AppInfo> m = new HashMap<String, AppInfo>();
        for (AppInfo a : all) m.put(a.key, a);
        List<String> hk = lst("home");
        List<String> dk = Store.getList(this, "dock");
        if (dk == null) {
            dk = defaultDock();
            Store.putList(this, "dock", dk);
        }
        ArrayList<AppInfo> ha = new ArrayList<AppInfo>(), da = new ArrayList<AppInfo>();
        for (String k : hk) { AppInfo a = m.get(k); if (a != null) ha.add(a); }
        for (String k : dk) { AppInfo a = m.get(k); if (a != null) da.add(a); }
        homeAd.setData(ha);
        hint.setVisibility(ha.isEmpty() ? View.VISIBLE : View.GONE);
        rebuildDock(da);
        filterApps(search.getText().toString());
    }

    void rebuildDock(List<AppInfo> da) {
        dockRow.removeAllViews();
        for (final AppInfo a : da) {
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(a.icon);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(60), dp(60));
            lp.setMargins(dp(6), 0, dp(6), 0);
            iv.setLayoutParams(lp);
            iv.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { launch(a); } });
            iv.setOnLongClickListener(new View.OnLongClickListener() { @Override public boolean onLongClick(View v) { menu(a); return true; } });
            dockRow.addView(iv);
        }
        DotsView dv = new DotsView(this, 0xFFFFFFFF);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(t.accent);
        dv.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(52), dp(52));
        lp.setMargins(dp(6), 0, dp(6), 0);
        dv.setLayoutParams(lp);
        dv.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openDrawer(); } });
        dockRow.addView(dv);
    }

    void filterApps(String q) {
        q = q.trim().toLowerCase();
        ArrayList<AppInfo> l = new ArrayList<AppInfo>();
        for (AppInfo a : all) if (q.isEmpty() || a.label.toLowerCase().contains(q)) l.add(a);
        appsAd.setData(l);
    }

    void openDrawer() {
        if (drawerOpen) return;
        drawerOpen = true;
        drawer.setVisibility(View.VISIBLE);
        drawer.setTranslationY(root.getHeight());
        drawer.animate().translationY(0).setDuration(220).start();
    }

    void closeDrawer() {
        drawerOpen = false;
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null) im.hideSoftInputFromWindow(search.getWindowToken(), 0);
        search.setText("");
        drawer.animate().translationY(root.getHeight()).setDuration(200).withEndAction(new Runnable() {
            @Override public void run() { if (!drawerOpen) drawer.setVisibility(View.GONE); }
        }).start();
    }

    void launch(AppInfo a) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setClassName(a.pkg, a.cls);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح التطبيق", Toast.LENGTH_SHORT).show();
        }
    }

    void menu(final AppInfo a) {
        final List<String> hk = lst("home"), dk = lst("dock");
        final boolean inH = hk.contains(a.key), inD = dk.contains(a.key);
        String[] it = {"🎨 تعديل الأيقونة", "✏️ تغيير الاسم",
                inH ? "➖ إزالة من الرئيسية" : "➕ إضافة للرئيسية",
                inD ? "➖ إزالة من الدوك" : "➕ إضافة للدوك",
                "ℹ️ معلومات التطبيق", "🗑️ إلغاء التثبيت"};
        new AlertDialog.Builder(this).setTitle(a.label).setItems(it, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int w) {
                switch (w) {
                    case 0: editIcon(a); break;
                    case 1:
                        UI.ask(LauncherActivity.this, "اسم التطبيق", a.label, false, new SCb() {
                            @Override public void on(String s) {
                                SharedPreferences.Editor e = Store.sp(LauncherActivity.this).edit();
                                if (s.trim().isEmpty()) e.remove("lbl_" + a.key); else e.putString("lbl_" + a.key, s.trim());
                                e.apply();
                                Store.ver++;
                                reload();
                            }
                        });
                        break;
                    case 2:
                        if (inH) hk.remove(a.key); else hk.add(a.key);
                        Store.putList(LauncherActivity.this, "home", hk);
                        refreshLists();
                        break;
                    case 3:
                        if (!inD && dk.size() >= 4) { Toast.makeText(LauncherActivity.this, "الدوك يتسع لـ 4 تطبيقات", Toast.LENGTH_SHORT).show(); break; }
                        if (inD) dk.remove(a.key); else dk.add(a.key);
                        Store.putList(LauncherActivity.this, "dock", dk);
                        refreshLists();
                        break;
                    case 4:
                        try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + a.pkg))); } catch (Exception ignored) { }
                        break;
                    case 5:
                        try { startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + a.pkg))); } catch (Exception ignored) { }
                        break;
                }
            }
        }).show();
    }

    void editIcon(final AppInfo a) {
        String[] it = {"🖼️ صورة من المعرض", "😀 إيموجي أو حرف", "↩️ إعادة الأيقونة الأصلية"};
        new AlertDialog.Builder(this).setTitle("تعديل أيقونة " + a.label).setItems(it, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int w) {
                if (w == 0) {
                    pendingKey = a.key;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(i, 7);
                } else if (w == 1) {
                    UI.ask(LauncherActivity.this, "اكتب إيموجي أو حرفاً", "😀", false, new SCb() {
                        @Override public void on(String s) { saveEmoji(a.key, s); }
                    });
                } else {
                    Apps.cf(LauncherActivity.this, "ci_", a.key).delete();
                    Apps.cf(LauncherActivity.this, "ce_", a.key).delete();
                    Store.ver++;
                    reload();
                }
            }
        }).show();
    }

    void saveEmoji(String key, String s) {
        s = s.trim();
        if (s.isEmpty()) return;
        if (s.length() > 4) s = s.substring(0, 4);
        try {
            Bitmap b = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(Color.WHITE);
            p.setTextSize(s.length() > 2 ? 100 : 170);
            Paint.FontMetrics fm = p.getFontMetrics();
            c.drawText(s, 128, 128 - (fm.ascent + fm.descent) / 2f, p);
            FileOutputStream o = new FileOutputStream(Apps.cf(this, "ce_", key));
            b.compress(Bitmap.CompressFormat.PNG, 100, o);
            o.close();
            Apps.cf(this, "ci_", key).delete();
            Store.ver++;
            reload();
        } catch (Exception e) {
            Toast.makeText(this, "تعذر الحفظ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onActivityResult(int req, int res, Intent d) {
        if (req != 7 || res != RESULT_OK || d == null || d.getData() == null || pendingKey == null) return;
        final Uri u = d.getData();
        final String key = pendingKey;
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Bitmap b = Apps.square(LauncherActivity.this, u, 256);
                    FileOutputStream o = new FileOutputStream(Apps.cf(LauncherActivity.this, "ci_", key));
                    b.compress(Bitmap.CompressFormat.PNG, 100, o);
                    o.close();
                    Apps.cf(LauncherActivity.this, "ce_", key).delete();
                    Store.ver++;
                    runOnUiThread(new Runnable() { @Override public void run() { reload(); } });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(LauncherActivity.this, "تعذر فتح الصورة", Toast.LENGTH_SHORT).show(); } });
                }
            }
        }).start();
    }
}
