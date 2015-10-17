/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.moviestoday.wear.app.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

public class MovieFragmentGridPagerAdapter extends FragmentGridPagerAdapter {
    private final Context mContext;

    public MovieFragmentGridPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = context;
    }

    @Override
    public Fragment getFragment(int row, int column) {
        String title = "Title " + row;
        CharSequence description = "Description for row " + row;
        CardFragment fragment = CardFragment.create(title, description);

        // Advanced settings (card gravity, card expansion/scrolling)
        fragment.setExpansionEnabled(true);
        fragment.setExpansionDirection(CardFragment.EXPAND_DOWN);
//        fragment.setExpansionFactor(page.expansionFactor);
        return fragment;
    }

    @Override
    public int getRowCount() {
        return 4;
    }

    @Override
    public int getColumnCount(int row) {
        return 1;
    }
}
