package com.themestudio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

final class ColorPicker {
    static final int[] PAL = {0xFF000000, 0xFFFFFFFF, 0xFFF44336, 0xFFFF9800, 0xFFFFEB3B, 0xFF4CAF50,
            0xFF00BCD4, 0xFF2196F3, 0xFF3F51B5, 0xFF9C27B0, 0xFFE91E63, 0xFF795548};

    static void pick(Context c, int init, final Cb cb) {
        final int[] v = {Color.alpha(init), Color.red(init), Color.green(init), Color.blue(init)};
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        int pd = UI.dp(c, 16);
        l.setPadding(pd, pd, pd, pd);
        final View prev = new View(c);
        prev.setBackgroundColor(init);
        l.addView(prev, new LinearLayout.LayoutParams(-1, UI.dp(c, 48)));

        final SeekBar[] sb = new SeekBar[4];
        String[] nm = {"الشفافية", "أحمر", "أخضر", "أزرق"};
        for (int i = 0; i < 4; i++) {
            final int k = i;
            TextView tv = new TextView(c);
            tv.setText(nm[i]);
            l.addView(tv);
            sb[i] = new SeekBar(c);
            sb[i].setMax(255);
            sb[i].setProgress(v[i]);
            sb[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                    v[k] = p;
                    prev.setBackgroundColor(Color.argb(v[0], v[1], v[2], v[3]));
                }
                @Override public void onStartTrackingTouch(SeekBar s) { }
                @Override public void onStopTrackingTouch(SeekBar s) { }
            });
            l.addView(sb[i]);
        }
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (final int col : PAL) {
            View sw = new View(c);
            sw.setBackgroundColor(col);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, UI.dp(c, 28), 1);
            lp.setMargins(2, 8, 2, 2);
            sw.setLayoutParams(lp);
            sw.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View x) {
                    sb[0].setProgress(255);
                    sb[1].setProgress(Color.red(col));
                    sb[2].setProgress(Color.green(col));
                    sb[3].setProgress(Color.blue(col));
                }
            });
            row.addView(sw);
        }
        l.addView(row);
        ScrollView sv = new ScrollView(c);
        sv.addView(l);
        new AlertDialog.Builder(c).setTitle("اختر اللون").setView(sv)
                .setPositiveButton("تم", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { cb.on(Color.argb(v[0], v[1], v[2], v[3])); }
                }).setNegativeButton("إلغاء", null).show();
    }
}
