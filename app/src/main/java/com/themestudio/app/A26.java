package com.themestudio.app;

import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

/** Isolated so older Android versions never load AdaptiveIconDrawable. */
final class A26 {
    static Drawable[] split(Drawable d) {
        if (d instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable a = (AdaptiveIconDrawable) d;
            return new Drawable[]{a.getForeground(), a.getBackground()};
        }
        return null;
    }
}
