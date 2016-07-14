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
package org.jraf.android.cinetoday.mobile.app.theater.search;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.common.model.theater.Theater;
import org.jraf.android.cinetoday.databinding.TheaterSearchBinding;
import org.jraf.android.util.log.Log;

public class TheaterSearchActivity extends AppCompatActivity implements TheaterCallbacks {
    private static final String PREFIX = TheaterSearchActivity.class.getName() + ".";
    public static final String EXTRA_FIRST_USE = PREFIX + "EXTRA_FIRST_USE";
    public static final String EXTRA_RESULT = PREFIX + "EXTRA_RESULT";

    private TheaterSearchBinding mBinding;
    private Handler mHandler;
    private TheaterSearchFragment mTheaterSearchFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.theater_search);
        mBinding.btnClear.setVisibility(View.GONE);
        if (!getIntent().getBooleanExtra(EXTRA_FIRST_USE, false)) {
            // Do not show the up arrow if in 'first use' mode
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mHandler = new Handler();
        mBinding.edtSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mHandler.removeCallbacks(mQueryRunnable);
                        mHandler.postDelayed(mQueryRunnable, 500);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
        mBinding.edtSearch.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        return actionId == EditorInfo.IME_ACTION_SEARCH;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTheaterClicked(Theater theater) {
        Log.d("theater=%s", theater);
        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT, theater);
        setResult(RESULT_OK, result);
        finish();
    }

    public TheaterSearchFragment getTheaterSearchFragment() {
        if (mTheaterSearchFragment == null) {
            mTheaterSearchFragment = (TheaterSearchFragment) getSupportFragmentManager().findFragmentById(R.id.fraTheaterSearch);
        }
        return mTheaterSearchFragment;
    }

    public void onClearClicked(View v) {
        mBinding.edtSearch.getText().clear();
    }

    private final Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            String query = mBinding.edtSearch.getText().toString().trim();
            getTheaterSearchFragment().search(query);
            if (query.length() == 0) {
                mBinding.btnClear.setVisibility(View.GONE);
            } else {
                mBinding.btnClear.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mQueryRunnable);
        super.onDestroy();
    }
}
