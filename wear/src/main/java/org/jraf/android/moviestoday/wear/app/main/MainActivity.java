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

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.widget.ProgressBar;

import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.model.movie.Movie;
import org.jraf.android.moviestoday.common.wear.WearHelper;
import org.jraf.android.util.log.Log;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {
    @Bind(R.id.gridViewPager)
    protected GridViewPager mGridViewPager;

    @Bind(R.id.pgbLoading)
    protected ProgressBar mPgbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        new AsyncTask<Void, Void, List<Movie>>() {
            HashMap<Movie, Bitmap> mPosterMap = new HashMap<>();

            @Override
            protected List<Movie> doInBackground(Void... params) {
                WearHelper.get().connect(MainActivity.this);
                List<Movie> movieList = WearHelper.get().getMovies();
                if (movieList == null) return null;
                for (Movie movie : movieList) {
                    mPosterMap.put(movie, WearHelper.get().getMoviePoster(movie));
                }
                return movieList;
            }

            @Override
            protected void onPostExecute(List<Movie> movies) {
                if (movies == null) {
                    Log.d("Movie list was empty");
                } else {
                    mPgbLoading.setVisibility(View.GONE);
                    MovieFragmentGridPagerAdapter adapter = new MovieFragmentGridPagerAdapter(MainActivity.this, getFragmentManager(), movies, mPosterMap);
                    mGridViewPager.setAdapter(adapter);
                }
            }
        }.execute();
    }
}
