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

import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jraf.android.moviestoday.BuildConfig;
import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.model.theater.Theater;
import org.jraf.android.moviestoday.mobile.app.api.LoadMoviesIntentService;
import org.jraf.android.moviestoday.mobile.app.api.LoadMoviesListener;
import org.jraf.android.moviestoday.mobile.app.api.LoadMoviesListenerHelper;
import org.jraf.android.moviestoday.mobile.app.api.LoadMoviesTaskService;
import org.jraf.android.moviestoday.mobile.app.prefs.PreferencesActivity;
import org.jraf.android.moviestoday.mobile.app.theater.search.TheaterSearchActivity;
import org.jraf.android.moviestoday.mobile.prefs.MainPrefs;
import org.jraf.android.util.about.AboutActivityIntentBuilder;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_THEATER = 0;

    @Bind(R.id.conTheater)
    protected View mConTheater;

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

        // First use: pick a theater
        if (!MainPrefs.get(this).containsTheaterId()) {
            onPickTheaterClicked();
        }
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
            String dateStr = DateUtils.formatDateTime(this, lastUpdateDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
            mTxtLastUpdateDate.setText(getString(R.string.main_lastUpdateDate, dateStr));
        }
    }

    @OnClick(R.id.conTheater)
    protected void onPickTheaterClicked() {
        Intent intent = new Intent(this, TheaterSearchActivity.class);
        intent.putExtra(TheaterSearchActivity.EXTRA_FIRST_USE, !MainPrefs.get(this).containsTheaterId());
        startActivityForResult(intent, REQUEST_PICK_THEATER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_THEATER:
                if (resultCode == RESULT_CANCELED) {
                    if (!MainPrefs.get(this).containsTheaterId()) {
                        // First use case, no theater was picked: finish now
                        finish();
                    }
                    break;
                }
                Theater theater = data.getParcelableExtra(TheaterSearchActivity.EXTRA_RESULT);
                // Save picked theater to prefs
                MainPrefs.get(this).edit()
                        .putTheaterId(theater.id)
                        .putTheaterName(theater.name)
                        .putTheaterAddress(theater.address)
                        .apply();
                // Update labels
                updateTheaterLabels();

                // Update now
                updateNowAndScheduleTask();
        }
    }

    private void updateNowAndScheduleTask() {
        // Update now
        LoadMoviesIntentService.startActionLoadMovies(this);

        // Schedule the daily task
        scheduleTask();
    }

    private void scheduleTask() {
//        long periodSecs = TimeUnit.DAYS.toSeconds(1);
        long periodSecs = TimeUnit.HOURS.toSeconds(12);
        long flexSecs = TimeUnit.HOURS.toSeconds(1);
        String tag = "dailyLoadMovies";
        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setTag(tag)
                .setService(LoadMoviesTaskService.class)
                .setPeriod(periodSecs)
                .setFlex(flexSecs)
                .setPersisted(true)
                .build();
        GcmNetworkManager.getInstance(this).schedule(periodicTask);
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
            mConTheater.setEnabled(false);
        }

        @Override
        public void onLoadMoviesProgress(int currentMovie, int totalMovies) {
            mPgbLoadingProgress.setMax(totalMovies);
            mPgbLoadingProgress.setProgress(currentMovie);
        }

        @Override
        public void onLoadMoviesFinished() {
            updateLastUpdateDateLabel();
            mPgbLoadingProgress.setVisibility(View.GONE);
            mSwiRefresh.setRefreshing(false);
            mConTheater.setEnabled(true);
        }

        @Override
        public void onLoadMoviesError(Throwable t) {
//TODO
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
        builder.addLink(getString(R.string.about_sources_uri), getString(R.string.about_sources_text));
        startActivity(builder.build(this));
    }
}
