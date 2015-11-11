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
package org.jraf.android.moviestoday.mobile.app.main;

import java.util.Date;
import java.util.Set;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.model.movie.Movie;
import org.jraf.android.moviestoday.common.model.theater.Theater;
import org.jraf.android.moviestoday.common.wear.WearHelper;
import org.jraf.android.moviestoday.mobile.api.Api;
import org.jraf.android.moviestoday.mobile.api.ImageCache;
import org.jraf.android.moviestoday.mobile.app.api.LoadMoviesHelper;
import org.jraf.android.moviestoday.mobile.app.api.LoadMoviesIntentService;
import org.jraf.android.moviestoday.mobile.app.api.LoadMoviesListener;
import org.jraf.android.moviestoday.mobile.app.theater.search.TheaterSearchActivity;
import org.jraf.android.moviestoday.mobile.prefs.MainPrefs;
import org.jraf.android.util.log.wrapper.Log;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final int POSTER_THUMBNAIL_WIDTH = 240;
    private static final int POSTER_THUMBNAIL_HEIGHT = 240;
    private static final int REQUEST_PICK_THEATER = 0;

    @Bind(R.id.txtTheaterName)
    protected TextView mTxtTheaterName;

    @Bind(R.id.txtTheaterAddress)
    protected TextView mTxtTheaterAddress;

    @Bind(R.id.txtLastUpdateDate)
    protected TextView mTxtLastUpdateDate;

    @Bind((R.id.swiRefresh))
    protected SwipeRefreshLayout mSwiRefresh;

    @Bind((R.id.pgbLoadingProgress))
    protected ProgressBar mPgbLoadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        mSwiRefresh.setOnRefreshListener(mOnRefreshListener);
        mSwiRefresh.setColorSchemeColors(ActivityCompat.getColor(this, R.color.colorAccent), ActivityCompat.getColor(this, R.color.colorPrimary));

        updateTheaterLabels();

        Log.d();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoadMoviesHelper.get().addListener(mLoadMoviesListener);
    }

    @Override
    protected void onStop() {
        LoadMoviesHelper.get().removeListener(mLoadMoviesListener);
        super.onStop();
    }

    private void updateTheaterLabels() {
        MainPrefs prefs = MainPrefs.get(this);
        mTxtTheaterName.setText(prefs.getTheaterName());
        mTxtTheaterAddress.setText(prefs.getTheaterAddress());
    }

    private void updateLastUpdateDateLabel() {
        MainPrefs prefs = MainPrefs.get(this);
        Long lastUpdateDate = prefs.getLastUpdateDate();
        if (lastUpdateDate == null) {
            mTxtLastUpdateDate.setText(R.string.main_lastUpdateDate_none);
        } else {
//            Date date = new Date(lastUpdateDate);
            String dateStr = DateUtils.formatDateTime(this, lastUpdateDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
            mTxtLastUpdateDate.setText(getString(R.string.main_lastUpdateDate, dateStr));
        }
    }

    protected void onCallClicked() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Set<Movie> movies;
                try {
                    movies = Api.get(MainActivity.this).getMovieList("C2954", new Date());
                } catch (Exception e) {
                    Log.e("Could not make call", e);
                    return null;
                }
                for (Movie movie : movies) {
                    Log.d(movie.toString());
                }
                Log.d("=====================");

                WearHelper.get().connect(MainActivity.this);


                for (Movie movie : movies) {
                    // Get movie info
                    try {
                        Api.get(MainActivity.this).getMovieInfo(movie);
                        Log.d(movie.toString());
                    } catch (Exception e) {
                        Log.e("Could not make call", e);
                    }

                    // Get poster image
                    Bitmap posterBitmap = ImageCache.get(MainActivity.this).getBitmap(movie.posterUri, POSTER_THUMBNAIL_WIDTH, POSTER_THUMBNAIL_HEIGHT);
                    if (posterBitmap != null) {
                        // Save it for Wear (only if not already there)
                        Bitmap currentBitmap = WearHelper.get().getMoviePoster(movie);
                        if (currentBitmap == null) {
                            WearHelper.get().putMoviePoster(movie, posterBitmap);
                        }
                    }
                }


                WearHelper.get().putMovies(movies);
                return null;
            }
        }.execute();
    }

    @OnClick(R.id.btnPickTheater)
    protected void onPickTheaterClicked() {
        Intent intent = new Intent(this, TheaterSearchActivity.class);
        startActivityForResult(intent, REQUEST_PICK_THEATER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_THEATER:
                if (resultCode == RESULT_CANCELED) break;
                Theater theater = data.getParcelableExtra(TheaterSearchActivity.EXTRA_RESULT);
                // Save picked theater to prefs
                MainPrefs.get(this).edit()
                        .putTheaterId(theater.id)
                        .putTheaterName(theater.name)
                        .putTheaterAddress(theater.address)
                        .apply();
                // Update labels
                updateTheaterLabels();
        }
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            LoadMoviesIntentService.startActionLoadMovies(MainActivity.this);
        }
    };

    private LoadMoviesListener mLoadMoviesListener = new LoadMoviesListener() {
        @Override
        public void onLoadMoviesStarted() {
            mTxtLastUpdateDate.setText(R.string.main_lastUpdateDate_ongoing);
            mPgbLoadingProgress.setVisibility(View.VISIBLE);
            // XXX Do this in a post  because it won't work if called before the SwipeRefreshView's onMeasure
            // (see https://code.google.com/p/android/issues/detail?id=77712)
            mSwiRefresh.post(new Runnable() {
                @Override
                public void run() {
                    mSwiRefresh.setRefreshing(true);
                }
            });
        }

        @Override
        public void onLoadMoviesProgress(int currentMovie, int totalMovies) {

        }

        @Override
        public void onLoadMoviesFinished() {
            updateLastUpdateDateLabel();
            mPgbLoadingProgress.setVisibility(View.GONE);
            mSwiRefresh.setRefreshing(false);
        }
    };
}
