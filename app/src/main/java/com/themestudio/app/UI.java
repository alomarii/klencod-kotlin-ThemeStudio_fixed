package com.themestudio.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

final class UI {
    static int dp(Context c, int v) { return (int) (v * c.getResources().getDisplayMetrics().density); }

    static void ask(Context c, String title, String init, boolean multi, final SCb cb) {
        final EditText e = new EditText(c);
        e.setText(init);
        if (multi) { e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); e.setMinLines(4); }
        else e.setSingleLine(true);
        new AlertDialog.Builder(c).setTitle(title).setView(e)
                .setPositiveButton("موافق", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { cb.on(e.getText().toString()); }
                }).setNegativeButton("إلغاء", null).show();
    }
}
