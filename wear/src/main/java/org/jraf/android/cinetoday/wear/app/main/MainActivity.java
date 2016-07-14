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
package org.jraf.android.cinetoday.wear.app.main;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.common.model.movie.Movie;
import org.jraf.android.cinetoday.common.wear.WearHelper;
import org.jraf.android.cinetoday.databinding.MainBinding;
import org.jraf.android.cinetoday.wear.app.Application;
import org.jraf.android.cinetoday.wear.app.configure.ConfigureIntentService;
import org.jraf.android.util.log.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

public class MainActivity extends Activity {
    private MainBinding mBinding;
    private boolean mHasConnected;

    private class LoadMoviesAsyncTask extends AsyncTask<Void, Void, Void> {
        private List<Movie> mMovieList;
        private HashMap<Movie, Bitmap> mPosterMap = new HashMap<>();
        private boolean mLoading;

        @Override
        protected void onPreExecute() {
            mBinding.pgbLoading.setVisibility(View.VISIBLE);
            mBinding.conEmpty.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            WearHelper wearHelper = WearHelper.get();
            if (!mHasConnected) {
                wearHelper.connect(MainActivity.this);
                mHasConnected = true;
            }
            wearHelper.addListener(mDataListener);

            mMovieList = wearHelper.getMovies();
            if (mMovieList == null) {
                Log.d("Movie list was empty");
                mLoading = wearHelper.getMoviesLoading();
                Log.d("mLoading=%s", mLoading);
            } else {
                mPosterMap.clear();
                // Call gc here because we may have a few bitmap in memory that we want to get rid of
                System.gc();
                for (Movie movie : mMovieList) {
                    Bitmap moviePoster = wearHelper.getMoviePoster(movie);
                    mPosterMap.put(movie, moviePoster);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mMovieList == null) {
                // No movies
                mBinding.gridViewPager.setVisibility(View.GONE);
                if (mLoading) {
                    // Because currently loading
                    mBinding.conEmpty.setVisibility(View.GONE);
                    mBinding.pgbLoading.setVisibility(View.VISIBLE);
                } else {
                    // Because no theater was selected
                    mBinding.conEmpty.setVisibility(View.VISIBLE);
                    mBinding.pgbLoading.setVisibility(View.GONE);
                }
            } else {
                // We have movies
                mBinding.conEmpty.setVisibility(View.GONE);
                mBinding.pgbLoading.setVisibility(View.GONE);
                mBinding.gridViewPager.setVisibility(View.VISIBLE);
                MovieFragmentGridPagerAdapter adapter = new MovieFragmentGridPagerAdapter(MainActivity.this, getFragmentManager(), mMovieList, mPosterMap);
                mBinding.gridViewPager.setAdapter(adapter);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main);

        // Determine if round or not
        // This is FUCKED UP srsly
        View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                Application.sIsRound = insets.isRound();
                return insets;
            }
        });

        new LoadMoviesAsyncTask().execute();
    }

    public void onConfigureClick(View v) {
        Intent intent = new Intent(this, ConfigureIntentService.class);
        intent.setAction(ConfigureIntentService.ACTION_CONFIGURE);
        startService(intent);
    }

    private DataApi.DataListener mDataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            int count = dataEventBuffer.getCount();
            Log.d("count=%s", count);
            for (int i = 0; i < count; i++) {
                DataEvent dataEvent = dataEventBuffer.get(i);

                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) continue;
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                Log.d("uri=" + uri);
                String path = uri.getPath();
                Log.d("path=" + path);

                switch (path) {
                    case WearHelper.PATH_MOVIE_ALL:
                        new LoadMoviesAsyncTask().execute();
                        break;

                    case WearHelper.PATH_MOVIE_LOADING:
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                        DataMap dataMap = dataMapItem.getDataMap();
                        boolean loading = dataMap.getBoolean(WearHelper.KEY_VALUE);
                        Log.d("loading=%s", loading);
                        if (loading) {
                            mBinding.pgbLoading.setVisibility(View.VISIBLE);
                            mBinding.conEmpty.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (mHasConnected) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    WearHelper wearHelper = WearHelper.get();
                    wearHelper.removeListener(mDataListener);
                    wearHelper.disconnect();
                    return null;
                }
            }.execute();
        }
        super.onDestroy();
    }
}


