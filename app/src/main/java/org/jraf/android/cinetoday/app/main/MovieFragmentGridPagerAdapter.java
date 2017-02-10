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
package org.jraf.android.cinetoday.app.main;

import java.util.HashMap;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

import org.jraf.android.cinetoday.model.movie.Movie;

public class MovieFragmentGridPagerAdapter extends FragmentGridPagerAdapter {
    public static final int INDEX_POSTER = 0;
    public static final int INDEX_MAIN = 1;
    public static final int INDEX_SYNOPSIS = 2;
    public static final int INDEX_SHOWTIMES = 3;

    private final List<Movie> mMovies;
    private HashMap<Movie, Bitmap> mPosterMap;

    public MovieFragmentGridPagerAdapter(Context context, FragmentManager fragmentManager, List<Movie> movies, HashMap<Movie, Bitmap> posterMap) {
        super(fragmentManager);
        mMovies = movies;
        mPosterMap = posterMap;
    }

    @Override
    public Fragment getFragment(int row, int column) {
        Movie movie = mMovies.get(row);
        Fragment res = null;
        if (column == INDEX_MAIN || column == INDEX_SYNOPSIS || column >= INDEX_SHOWTIMES) {
            MovieCardFragment.CardType cardType = MovieCardFragment.CardType.fromIndex(column);
            CardFragment cardFragment = MovieCardFragment.create(cardType, movie, column);

            // Advanced settings (card gravity, card expansion/scrolling)
            cardFragment.setExpansionEnabled(true);
            cardFragment.setExpansionDirection(CardFragment.EXPAND_DOWN);
            cardFragment.setContentPadding(0, 0, 0, 0);
            res = cardFragment;
        } else if (column == INDEX_POSTER) {
            res = PosterFragment.create(mPosterMap.get(movie));
        }
        return res;
    }

    @Override
    public int getRowCount() {
        return mMovies.size();
    }

    @Override
    public int getColumnCount(int row) {
        return INDEX_SHOWTIMES + mMovies.get(row).todayShowtimes.size();
    }

    @Override
    public Drawable getBackgroundForRow(int row) {
        Movie movie = mMovies.get(row);
        Bitmap posterBitmap = mPosterMap.get(movie);
        return new BitmapDrawable(posterBitmap);
    }
}
