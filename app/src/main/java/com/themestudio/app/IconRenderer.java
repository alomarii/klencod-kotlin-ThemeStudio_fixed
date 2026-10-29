package com.themestudio.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;

final class IconRenderer {
    static final int PX = 192;

    /** kind: 0 = normal app icon, 1 = icon-pack icon, 2 = full-bleed picture. */
    static Bitmap make(Context c, Th t, Drawable d, int kind) {
        Bitmap out = Bitmap.createBitmap(PX, PX, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(out);
        float s = Math.min(PX, PX * t.scale / 100f);
        float off = (PX - s) / 2f;

        if (kind == 1 && !t.force) {
            d.setBounds((int) off, (int) off, (int) (off + s), (int) (off + s));
            d.draw(cv);
            return out;
        }

        Path shape = Shapes.path(t.shape, off, off, s);
        Bitmap content = Bitmap.createBitmap(PX, PX, Bitmap.Config.ARGB_8888);
        Canvas cc = new Canvas(content);

        Drawable fg = null, bgd = null;
        if (kind == 0 && Build.VERSION.SDK_INT >= 26) {
            Drawable[] parts = A26.split(d);
            if (parts != null) { fg = parts[0]; bgd = parts[1]; }
        }
        float L = s * 1.5f;
        int l = (int) ((PX - L) / 2f), r = (int) (l + L);

        if (kind == 2) {
            d.setBounds((int) off, (int) off, (int) (off + s), (int) (off + s));
            d.draw(cc);
        } else {
            if (t.bgMode == 1) {
                cc.drawColor(t.c1);
            } else if (t.bgMode == 2) {
                Paint gp = new Paint();
                gp.setShader(new LinearGradient(off, off, off + s, off + s, t.c1, t.c2, Shader.TileMode.CLAMP));
                cc.drawRect(0, 0, PX, PX, gp);
            } else if (bgd != null) {
                bgd.setBounds(l, l, r, r);
                bgd.draw(cc);
            } else {
                cc.drawColor(0xFFF2F2F2);
            }
            Drawable f = fg != null ? fg : d;
            if (fg != null) {
                f.setBounds(l, l, r, r);
            } else {
                float is = s * 0.66f;
                int io = (int) ((PX - is) / 2f);
                f.setBounds(io, io, (int) (io + is), (int) (io + is));
            }
            applyTint(f, t);
            f.draw(cc);
        }

        Paint mp = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (Shapes.poly(t.shape)) mp.setPathEffect(new CornerPathEffect(s * 0.07f));
        cv.drawPath(shape, mp);
        Paint xp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        xp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cv.drawBitmap(content, 0, 0, xp);

        if (t.bw > 0) {
            Paint bp = new Paint(Paint.ANTI_ALIAS_FLAG);
            bp.setStyle(Paint.Style.STROKE);
            bp.setStrokeWidth(t.bw * 2f);
            bp.setColor(t.bc);
            if (Shapes.poly(t.shape)) bp.setPathEffect(new CornerPathEffect(s * 0.07f));
            cv.drawPath(shape, bp);
        }
        return out;
    }

    static void applyTint(Drawable f, Th t) {
        if (t.tint == 1) {
            ColorMatrix m = new ColorMatrix();
            m.setSaturation(0f);
            f.setColorFilter(new ColorMatrixColorFilter(m));
        } else if (t.tint == 2 || t.tint == 3) {
            f.setColorFilter(new PorterDuffColorFilter(t.tint == 2 ? Color.WHITE : t.tc, PorterDuff.Mode.SRC_IN));
        }
    }

    static Bitmap blank() {
        Bitmap b = Bitmap.createBitmap(PX, PX, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFF9E9E9E);
        c.drawCircle(PX / 2f, PX / 2f, PX * 0.4f, p);
        return b;
    }
}
