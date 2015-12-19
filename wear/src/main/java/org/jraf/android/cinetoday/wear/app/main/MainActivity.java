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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.common.model.movie.Movie;
import org.jraf.android.cinetoday.common.wear.WearHelper;
import org.jraf.android.cinetoday.wear.app.configure.ConfigureIntentService;
import org.jraf.android.util.log.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {
    @Bind(R.id.gridViewPager)
    protected GridViewPager mGridViewPager;

    @Bind(R.id.pgbLoading)
    protected ProgressBar mPgbLoading;

    @Bind(R.id.conEmpty)
    protected LinearLayout mConEmpty;

    protected boolean mHasConnected;

    private class loadMoviesAsyncTask extends AsyncTask<Void, Void, List<Movie>> {
        HashMap<Movie, Bitmap> mPosterMap = new HashMap<>();

        @Override
        protected List<Movie> doInBackground(Void... params) {
            WearHelper wearHelper = WearHelper.get();
            if (!mHasConnected) {
                wearHelper.connect(MainActivity.this);
                mHasConnected = true;
            }

            wearHelper.addListener(mDataListener);

            List<Movie> movieList = wearHelper.getMovies();
            if (movieList == null) return null;
            for (Movie movie : movieList) {
                mPosterMap.put(movie, wearHelper.getMoviePoster(movie));
            }
            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            mPgbLoading.setVisibility(View.GONE);
            if (movies == null) {
                Log.d("Movie list was empty");
                mConEmpty.setVisibility(View.VISIBLE);
                mGridViewPager.setVisibility(View.GONE);
            } else {
                mConEmpty.setVisibility(View.GONE);
                mGridViewPager.setVisibility(View.VISIBLE);
                MovieFragmentGridPagerAdapter adapter = new MovieFragmentGridPagerAdapter(MainActivity.this, getFragmentManager(), movies, mPosterMap);
                mGridViewPager.setAdapter(adapter);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        new loadMoviesAsyncTask().execute();
    }

    @OnClick(R.id.btnConfigure)
    protected void onConfigureClicked() {
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
                        new loadMoviesAsyncTask().execute();
                        break;

                    case WearHelper.PATH_MOVIE_LOADING:
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                        DataMap dataMap = dataMapItem.getDataMap();
                        boolean loading = dataMap.getBoolean(WearHelper.KEY_VALUE);
                        handleLoadingChanged(loading);
                        break;
                }
            }
        }
    };

    private void handleLoadingChanged(boolean loading) {
        Log.d("loading=%s", loading);
        if (loading) {
            mPgbLoading.setVisibility(View.VISIBLE);
            mConEmpty.setVisibility(View.GONE);
        } else {
            mPgbLoading.setVisibility(View.GONE);
        }
    }

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


