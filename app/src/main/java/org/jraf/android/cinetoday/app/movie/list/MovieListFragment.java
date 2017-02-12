/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.app.movie.list;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.MovieListBinding;
import org.jraf.android.cinetoday.provider.movie.MovieCursor;
import org.jraf.android.cinetoday.provider.movie.MovieSelection;
import org.jraf.android.util.app.base.BaseFragment;

public class MovieListFragment extends BaseFragment<MovieListCallbacks> implements LoaderManager.LoaderCallbacks<Cursor> {
    private MovieListBinding mBinding;
    private MovieListAdapter mAdapter;

    public static MovieListFragment newInstance() {
        return new MovieListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.movie_list, container, false);
        mBinding.setCallbacks(getCallbacks());
        mBinding.rclList.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mBinding.rclList);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MovieSelection().getCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBinding.pgbLoading.setVisibility(View.GONE);
        if (data.getCount() == 0) {
            // No favorite theaters yet
            mBinding.btnEmptyPickTheater.setVisibility(View.VISIBLE);
        } else {
            mBinding.btnEmptyPickTheater.setVisibility(View.GONE);
            if (mAdapter == null) {
                mAdapter = new MovieListAdapter(getContext(), getCallbacks());
                mBinding.rclList.setAdapter(mAdapter);
            }
            mAdapter.swapCursor((MovieCursor) data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) mAdapter.swapCursor(null);
    }
}
