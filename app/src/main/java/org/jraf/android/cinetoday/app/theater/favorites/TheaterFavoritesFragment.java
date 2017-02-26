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
package org.jraf.android.cinetoday.app.theater.favorites;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.TheaterFavoritesBinding;
import org.jraf.android.cinetoday.provider.theater.TheaterCursor;
import org.jraf.android.cinetoday.provider.theater.TheaterModel;
import org.jraf.android.cinetoday.provider.theater.TheaterSelection;
import org.jraf.android.util.app.base.BaseFrameworkFragment;

public class TheaterFavoritesFragment extends BaseFrameworkFragment<TheaterFavoritesCallbacks> implements LoaderManager.LoaderCallbacks<Cursor> {
    private TheaterFavoritesBinding mBinding;
    private TheaterFavoritesAdapter mAdapter;

    public static TheaterFavoritesFragment newInstance() {
        return new TheaterFavoritesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.theater_favorites, container, false);
        mBinding.setCallbacks(getCallbacks());

        mBinding.rclList.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mBinding.rclList);

        mBinding.rclList.addOnScrollListener(mOnScrollListener);

        return mBinding.getRoot();
    }


    //--------------------------------------------------------------------------
    // region Loader.
    //--------------------------------------------------------------------------

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new TheaterSelection().getCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBinding.pgbLoading.setVisibility(View.GONE);
        if (mAdapter == null) {
            mAdapter = new TheaterFavoritesAdapter(getContext(), getCallbacks());
            mBinding.rclList.setAdapter(mAdapter);
        }
        mAdapter.swapCursor((TheaterCursor) data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) mAdapter.swapCursor(null);
    }

    // endregion


    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) getCallbacks().onTheaterListScrolled();
        }
    };

    public TheaterModel getCurrentVisibleTheater() {
        int firstItemPosition = ((LinearLayoutManager) mBinding.rclList.getLayoutManager()).findFirstVisibleItemPosition();
        if (firstItemPosition == RecyclerView.NO_POSITION) return null;
        return mAdapter.getTheater(firstItemPosition);
    }
}
