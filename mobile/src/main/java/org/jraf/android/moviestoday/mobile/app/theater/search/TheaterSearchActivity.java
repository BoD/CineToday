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
package org.jraf.android.moviestoday.mobile.app.theater.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.model.theater.Theater;
import org.jraf.android.util.log.wrapper.Log;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

public class TheaterSearchActivity extends AppCompatActivity implements TheaterCallbacks {
    private static final String PREFIX = TheaterSearchActivity.class.getName() + ".";
    public static final String EXTRA_RESULT = PREFIX + "EXTRA_RESULT";

    @Bind(R.id.edtSearch)
    protected EditText mEdtSearch;

    @Bind(R.id.btnClear)
    protected ImageButton mBtnClear;

    private TheaterSearchFragment mTheaterSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theater_search);
        ButterKnife.bind(this);
        mBtnClear.setVisibility(View.GONE);
    }

    @OnEditorAction(R.id.edtSearch)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            return true;
        }
        return false;
    }

    @OnTextChanged(R.id.edtSearch)
    public void onSearchTextChanged() {
        String query = mEdtSearch.getText().toString().trim();
        getTheaterSearchFragment().search(query);
        if (query.length() == 0) {
            mBtnClear.setVisibility(View.GONE);
        } else {
            mBtnClear.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTheaterClicked(Theater theater) {
        Log.d("theater=" + theater);
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

    @OnClick(R.id.btnClear)
    protected void onClearClicked() {
        mEdtSearch.getText().clear();
    }
}
