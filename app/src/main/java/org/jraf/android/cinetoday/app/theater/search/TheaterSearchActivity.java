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
package org.jraf.android.cinetoday.app.theater.search;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.TheaterSearchBinding;
import org.jraf.android.cinetoday.model.theater.Theater;
import org.jraf.android.util.log.Log;

public class TheaterSearchActivity extends FragmentActivity implements TheaterCallbacks {
    private static final String PREFIX = TheaterSearchActivity.class.getName() + ".";
    public static final String EXTRA_RESULT = PREFIX + "EXTRA_RESULT";

    private TheaterSearchBinding mBinding;
    private Handler mHandler;
    private TheaterSearchFragment mTheaterSearchFragment;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.theater_search);
        mHandler = new Handler();
    }

    @Override
    public void onTheaterClicked(Theater theater) {
        Log.d("theater=%s", theater);
        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT, theater);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onSearch(String query) {
        mQuery = query;
        mHandler.removeCallbacks(mQueryRunnable);
        mHandler.postDelayed(mQueryRunnable, 500);
    }

    public TheaterSearchFragment getTheaterSearchFragment() {
        if (mTheaterSearchFragment == null) {
            mTheaterSearchFragment = (TheaterSearchFragment) getSupportFragmentManager().findFragmentById(R.id.fraTheaterSearch);
        }
        return mTheaterSearchFragment;
    }

    private final Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            String query = mQuery.trim();
            getTheaterSearchFragment().search(query);
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mQueryRunnable);
        super.onDestroy();
    }
}