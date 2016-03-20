/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.mobile.app.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.jraf.android.cinetoday.mobile.provider.theater.TheaterCursor;

public class TheaterFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private TheaterCursor mTheaterCursor;

    public TheaterFragmentStatePagerAdapter(FragmentManager fm, TheaterCursor theaterCursor) {
        super(fm);
        mTheaterCursor = theaterCursor;
    }

    @Override
    public Fragment getItem(int position) {
        if (position < mTheaterCursor.getCount()) {
            // Theater
            mTheaterCursor.moveToPosition(position);
            return TheaterPageFragment.newInstance(mTheaterCursor);
        }
        // Add
        return AddPageFragment.newInstance(position == 0);
    }

    @Override
    public int getCount() {
        return mTheaterCursor.getCount() + 1;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void swapTheaterCursor(TheaterCursor theaterCursor) {
        mTheaterCursor = theaterCursor;
        notifyDataSetChanged();
    }
}
