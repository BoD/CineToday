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

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.model.theater.Theater;
import org.jraf.android.util.app.base.BaseFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TheaterSearchFragment extends BaseFragment<TheaterCallbacks> implements LoaderManager.LoaderCallbacks<List<Theater>> {
    @Bind(R.id.rclList)
    protected RecyclerView mRclList;

    @Bind(R.id.pgbLoading)
    protected ProgressBar mPgbLoading;

    @Bind(R.id.txtEmpty)
    protected TextView mTxtEmpty;

    private TheaterAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.theater_search_list, container, false);
        ButterKnife.bind(this, res);
        mRclList.setHasFixedSize(true);
        mRclList.setLayoutManager(new LinearLayoutManager(getActivity()));

        mTxtEmpty.setVisibility(View.GONE);
        mPgbLoading.setVisibility(View.GONE);
        return res;
    }

    public void search(String query) {
        if (mAdapter != null) mAdapter.clear();

        mPgbLoading.setVisibility(View.VISIBLE);
        mTxtEmpty.setVisibility(View.GONE);

        Bundle args = new Bundle();
        args.putString("query", query);
        getLoaderManager().restartLoader(0, args, this);
    }


    /*
     * LoaderCallbacks implementation.
     */
    // region

    @Override
    public Loader<List<Theater>> onCreateLoader(int id, Bundle args) {
        String query = args.getString("query");
        return new TheaterLoader(getActivity(), query);
    }

    @Override
    public void onLoadFinished(Loader<List<Theater>> loader, List<Theater> data) {
        mPgbLoading.setVisibility(View.GONE);

        if (mAdapter == null) {
            mAdapter = new TheaterAdapter(getActivity(), getCallbacks());
            mRclList.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
        }

        boolean empty = data == null || data.isEmpty();
        if (empty) {
            mTxtEmpty.setVisibility(View.VISIBLE);
        } else {
            mTxtEmpty.setVisibility(View.GONE);
            mAdapter.addAll(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<Theater>> loader) {
        mAdapter.clear();
    }

    // endregion
}
