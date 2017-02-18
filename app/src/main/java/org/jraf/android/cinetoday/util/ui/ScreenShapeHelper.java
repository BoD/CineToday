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

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;

public class ScreenShapeHelper {
    public interface Callbacks {
        void onScreenShapeAvailable(boolean isRound, int chinHeight, float safeMargin);
    }

    private static ScreenShapeHelper INSTANCE = new ScreenShapeHelper();
    private Integer mChinHeight;
    private Boolean mIsRound;
    private Float mSafeMargin;

    private ScreenShapeHelper() {}

    public static ScreenShapeHelper get() {
        return INSTANCE;
    }

    public void init(Activity activity, @Nullable final Callbacks callbacks) {
        if (mChinHeight != null && mIsRound != null && mSafeMargin != null) {
            if (callbacks != null) callbacks.onScreenShapeAvailable(mIsRound, mChinHeight, mSafeMargin);
            return;
        }
        mIsRound = activity.getResources().getConfiguration().isScreenRound();
        if (mIsRound) {
            // Assume width=height for round screens (I guess oval is not supported)
            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            // Pythagorean Theorem
            double edge = metrics.widthPixels / Math.sqrt(2);
            mSafeMargin = (float) ((metrics.widthPixels - edge) / 2.0);
        } else {
            mSafeMargin = 0F;
        }
        final View contentView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                mChinHeight = insets.getSystemWindowInsetBottom();
                v.onApplyWindowInsets(insets);
                if (callbacks != null) callbacks.onScreenShapeAvailable(mIsRound, mChinHeight, mSafeMargin);
                contentView.setOnApplyWindowInsetsListener(null);
                return insets;
            }
        });
    }

    public int getChinHeight() {
        if (mChinHeight == null) throw new IllegalStateException("init must be called prior to calling getChinHeight");
        return mChinHeight;
    }

    public boolean getIsRound() {
        if (mIsRound == null) throw new IllegalStateException("init must be called prior to calling getIsRound");
        return mIsRound;
    }

    public float getSafeMargin() {
        if (mSafeMargin == null) throw new IllegalStateException("init must be called prior to calling getSafeMargin");
        return mSafeMargin;
    }
}
