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
import android.view.View;
import android.view.WindowInsets;

public class ChinHelper {
    private static ChinHelper INSTANCE = new ChinHelper();
    private int mChinHeight;
    private boolean mInitialized;

    private ChinHelper() {}

    public static ChinHelper get() {
        return INSTANCE;
    }

    public void init(Activity activity, @Nullable final Runnable onAfterInit) {
        if (mInitialized) return;
        View contentView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                mChinHeight = insets.getSystemWindowInsetBottom();
                v.onApplyWindowInsets(insets);

                if (onAfterInit != null) onAfterInit.run();

                return insets;
            }
        });
        mInitialized = true;
    }

    public int getChinHeight() {
        return mChinHeight;
    }
}
