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
package org.jraf.android.cinetoday.mobile.app.main;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jraf.android.cinetoday.BuildConfig;
import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.common.model.theater.Theater;
import org.jraf.android.cinetoday.mobile.app.loadmovies.LoadMoviesHelper;
import org.jraf.android.cinetoday.mobile.app.loadmovies.LoadMoviesIntentService;
import org.jraf.android.cinetoday.mobile.app.loadmovies.LoadMoviesListener;
import org.jraf.android.cinetoday.mobile.app.loadmovies.LoadMoviesListenerHelper;
import org.jraf.android.cinetoday.mobile.app.loadmovies.LoadMoviesTaskService;
import org.jraf.android.cinetoday.mobile.app.prefs.PreferencesActivity;
import org.jraf.android.cinetoday.mobile.app.theater.search.TheaterSearchActivity;
import org.jraf.android.cinetoday.mobile.prefs.MainPrefs;
import org.jraf.android.cinetoday.mobile.provider.theater.TheaterColumns;
import org.jraf.android.cinetoday.mobile.provider.theater.TheaterContentValues;
import org.jraf.android.cinetoday.mobile.provider.theater.TheaterCursor;
import org.jraf.android.cinetoday.mobile.provider.theater.TheaterSelection;
import org.jraf.android.cinetoday.mobile.ui.CirclePageIndicator;
import org.jraf.android.cinetoday.mobile.ui.ZoomOutPageTransformer;
import org.jraf.android.util.about.AboutActivityIntentBuilder;
import org.jraf.android.util.log.Log;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainCallbacks, LoaderCallbacks<Cursor> {
    private static final int REQUEST_ADD_THEATER = 0;

    @Bind(R.id.vpgTheaters)
    protected ViewPager mVpgTheaters;

    @Bind(R.id.txtStatus)
    protected TextView mTxtStatus;

    @Bind(R.id.txtCurrentMovie)
    protected TextView mTxtCurrentMovie;

    @Bind((R.id.swrRefresh))
    protected SwipeRefreshLayout mSwrRefresh;

    @Bind((R.id.pgbLoadingProgress))
    protected ProgressBar mPgbLoadingProgress;

    @Bind((R.id.cpiTheaters))
    protected CirclePageIndicator mCpiTheaters;

    /**
     * If {@code true}, this activity is running.
     */
    private static boolean sRunning;
    private TheaterFragmentStatePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sRunning = true;

        setContentView(R.layout.main);
        ButterKnife.bind(this);

        mSwrRefresh.setOnRefreshListener(mOnRefreshListener);
        mSwrRefresh.setColorSchemeColors(ActivityCompat.getColor(this, R.color.accent), ActivityCompat.getColor(this, R.color.primary));

        // Prevent the swipe refresh view to be triggered when swiping the view pager
        mVpgTheaters.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {
                mSwrRefresh.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
        mVpgTheaters.setPageTransformer(true, new ZoomOutPageTransformer());
        mVpgTheaters.setPageMargin(0);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoadMoviesListenerHelper.get().addListener(mLoadMoviesListener);
    }

    @Override
    protected void onStop() {
        LoadMoviesListenerHelper.get().removeListener(mLoadMoviesListener);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                onAboutClicked();
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateLastUpdateDateLabel() {
        MainPrefs prefs = MainPrefs.get(this);
        Long lastUpdateDate = prefs.getLastUpdateDate();
        if (lastUpdateDate == null) {
            mTxtStatus.setText(R.string.main_lastUpdateDate_none);
        } else {
            String dateStr = DateUtils.formatDateTime(this, lastUpdateDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
            mTxtStatus.setText(getString(R.string.main_lastUpdateDate, dateStr));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ADD_THEATER:
                if (resultCode == RESULT_CANCELED) {
                    break;
                }
                final Theater theater = data.getParcelableExtra(TheaterSearchActivity.EXTRA_RESULT);

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        // Insert the picked theater into list
                        TheaterContentValues values = new TheaterContentValues();
                        values.putPublicId(theater.id)
                                .putName(theater.name)
                                .putAddress(theater.address)
                                .putPictureUri(theater.pictureUri);
                        values.insert(MainActivity.this);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        // Update now
                        updateNowAndScheduleTask();
                    }
                }.execute();
                break;
        }
    }

    private void updateNowAndScheduleTask() {
        // Interrupt any ongoing loading
        LoadMoviesHelper.get().setWantStop(true);

        // Update now
        LoadMoviesIntentService.startActionLoadMovies(this);

        // Schedule the daily task
        LoadMoviesTaskService.scheduleTask(this);
    }

    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            updateNowAndScheduleTask();
        }
    };

    private LoadMoviesListener mLoadMoviesListener = new LoadMoviesListener() {
        @Override
        public void onLoadMoviesStarted() {
            mTxtStatus.setText(R.string.main_lastUpdateDate_ongoing);
            mPgbLoadingProgress.setVisibility(View.VISIBLE);
            // XXX Do this in a post  because it won't work if called before the SwipeRefreshView's onMeasure
            // (see https://code.google.com/p/android/issues/detail?id=77712)
            mSwrRefresh.post(new Runnable() {
                @Override
                public void run() {
                    mSwrRefresh.setRefreshing(true);
                }
            });
        }

        @Override
        public void onLoadMoviesProgress(int currentMovie, int totalMovies, String movieName) {
            mPgbLoadingProgress.setMax(totalMovies);
            mPgbLoadingProgress.setProgress(currentMovie);
            mTxtCurrentMovie.setText(movieName);
        }

        @Override
        public void onLoadMoviesSuccess() {
            updateLastUpdateDateLabel();
            mPgbLoadingProgress.setVisibility(View.INVISIBLE);
            mSwrRefresh.setRefreshing(false);
            mTxtCurrentMovie.setText(null);
        }

        @Override
        public void onLoadMoviesError(Throwable t) {
            // TODO
            updateLastUpdateDateLabel();
            mPgbLoadingProgress.setVisibility(View.INVISIBLE);
            mSwrRefresh.setRefreshing(false);
            mTxtCurrentMovie.setText(null);
        }

        @Override
        public void onLoadMoviesInterrupted() {
            updateLastUpdateDateLabel();
            mPgbLoadingProgress.setVisibility(View.INVISIBLE);
            mSwrRefresh.setRefreshing(false);
            mTxtCurrentMovie.setText(null);
        }
    };

    private void onAboutClicked() {
        AboutActivityIntentBuilder builder = new AboutActivityIntentBuilder();
        builder.setAppName(getString(R.string.app_name));
        builder.setBuildDate(BuildConfig.BUILD_DATE);
        builder.setGitSha1(BuildConfig.GIT_SHA1);
        builder.setAuthorCopyright(getString(R.string.about_authorCopyright));
        builder.setLicense(getString(R.string.about_License));
        builder.setShareTextSubject(getString(R.string.about_shareText_subject));
        builder.setShareTextBody(getString(R.string.about_shareText_body));
        builder.setBackgroundResId(R.drawable.about_bg);
        builder.addLink(getString(R.string.about_email_uri), getString(R.string.about_email_text));
        builder.addLink(getString(R.string.about_web_uri), getString(R.string.about_web_text));
        builder.addLink(getString(R.string.about_artwork_uri), getString(R.string.about_artwork_text));
        builder.addLink(getString(R.string.about_sources_uri), getString(R.string.about_sources_text));
        builder.setIsLightIcons(true);
        startActivity(builder.build(this));
    }

    @Override
    protected void onDestroy() {
        sRunning = false;
        super.onDestroy();
    }

    public static boolean isRunning() {
        return sRunning;
    }


    //--------------------------------------------------------------------------
    // region Loader.
    //--------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, TheaterColumns.CONTENT_URI, null, null, null, null) {
            @Override
            public Cursor loadInBackground() {
                return new TheaterCursor(super.loadInBackground());
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean isEmpty = data.getCount() == 0;
        mCpiTheaters.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
        if (mAdapter == null) {
            mAdapter = new TheaterFragmentStatePagerAdapter(getSupportFragmentManager(), (TheaterCursor) data);
            mVpgTheaters.setAdapter(mAdapter);
            mCpiTheaters.setViewPager(mVpgTheaters);
        } else {
            mAdapter.swapTheaterCursor((TheaterCursor) data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    // endregion


    //--------------------------------------------------------------------------
    // region MainCallbacks.
    //--------------------------------------------------------------------------

    @Override
    public void onAddTheater() {
        Intent intent = new Intent(this, TheaterSearchActivity.class);
        startActivityForResult(intent, REQUEST_ADD_THEATER);
    }

    @Override
    public void onDeleteTheater(final long id) {
        Log.d("id=%s", id);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                TheaterSelection theaterSelection = new TheaterSelection();
                theaterSelection.id(id);
                theaterSelection.delete(MainActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Update now
                updateNowAndScheduleTask();
            }
        }.execute();
    }

    // endregion
}
