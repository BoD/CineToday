/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.cinetoday.util.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class ScreenShapeHelper {
    private static ScreenShapeHelper INSTANCE = new ScreenShapeHelper();
    private boolean isInitialized;

    public int width;
    public int height;
    public int chinHeight;
    public boolean isRound;
    public float safeMargin;

    private ScreenShapeHelper() {}

    public static ScreenShapeHelper get(Context context) {
        if (!INSTANCE.isInitialized) INSTANCE.init(context);
        return INSTANCE;
    }

    public void init(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        isRound = resources.getConfiguration().isScreenRound();
        if (isRound) {
            // Assume width=height for round screens (I guess that means oval is not supported!)
            chinHeight = width - height;

            // Pythagorean Theorem
            double edge = width / Math.sqrt(2);
            safeMargin = (float) ((width - edge) / 2.0);
        }
        isInitialized = true;
    }
}
