package com.themestudio.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

final class Shapes {
    static final String[] NAMES = {"دائرة", "سكويركل", "مربع مدور", "مربع", "قطرة", "سداسي", "ثماني", "ورقة", "وردة", "شارة"};

    static boolean poly(int sh) { return sh == 5 || sh == 6 || sh == 9; }

    static void polygon(Path p, float cx, float cy, float r, int n, double startDeg) {
        for (int i = 0; i < n; i++) {
            double a = Math.toRadians(startDeg + 360.0 * i / n);
            float x = (float) (cx + r * Math.cos(a)), y = (float) (cy + r * Math.sin(a));
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.close();
    }

    static Path path(int sh, float x, float y, float s) {
        Path p = new Path();
        float cx = x + s / 2f, cy = y + s / 2f, r = s / 2f;
        RectF rc = new RectF(x, y, x + s, y + s);
        switch (sh) {
            case 0:
                p.addCircle(cx, cy, r, Path.Direction.CW);
                break;
            case 1: {
                int n = 120;
                for (int i = 0; i < n; i++) {
                    double a = 2 * Math.PI * i / n;
                    double c = Math.cos(a), sn = Math.sin(a);
                    float px = (float) (cx + r * Math.signum(c) * Math.pow(Math.abs(c), 0.5));
                    float py = (float) (cy + r * Math.signum(sn) * Math.pow(Math.abs(sn), 0.5));
                    if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
                }
                p.close();
                break;
            }
            case 2:
                p.addRoundRect(rc, s * 0.26f, s * 0.26f, Path.Direction.CW);
                break;
            case 3:
                p.addRoundRect(rc, s * 0.07f, s * 0.07f, Path.Direction.CW);
                break;
            case 4: {
                float b = r, m = s * 0.08f;
                p.addRoundRect(rc, new float[]{b, b, b, b, m, m, b, b}, Path.Direction.CW);
                break;
            }
            case 5:
                polygon(p, cx, cy, r, 6, -90);
                break;
            case 6:
                polygon(p, cx, cy, (float) (r / Math.cos(Math.PI / 8)), 8, 22.5);
                break;
            case 7: {
                float b = r, m = s * 0.1f;
                p.addRoundRect(rc, new float[]{b, b, m, m, b, b, m, m}, Path.Direction.CW);
                break;
            }
            case 8: {
                float pr = s * 0.22f, q = r - pr;
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    p.addCircle(cx + (float) (q * Math.cos(a)), cy + (float) (q * Math.sin(a)), pr, Path.Direction.CW);
                }
                p.addCircle(cx, cy, q, Path.Direction.CW);
                break;
            }
            default: {
                int n = 24;
                for (int i = 0; i < n; i++) {
                    double a = -Math.PI / 2 + 2 * Math.PI * i / n;
                    float rr = (i % 2 == 0) ? r : r * 0.88f;
                    float px = (float) (cx + rr * Math.cos(a)), py = (float) (cy + rr * Math.sin(a));
                    if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
                }
                p.close();
            }
        }
        return p;
    }

    static Bitmap preview(int sh, int color, int px) {
        Bitmap b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint pt = new Paint(Paint.ANTI_ALIAS_FLAG);
        pt.setColor(color);
        if (poly(sh)) pt.setPathEffect(new CornerPathEffect(px * 0.06f));
        c.drawPath(path(sh, px * 0.08f, px * 0.08f, px * 0.84f), pt);
        return b;
    }
}
