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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.MovieListBinding;
import org.jraf.android.cinetoday.provider.movie.MovieCursor;
import org.jraf.android.cinetoday.provider.movie.MovieSelection;
import org.jraf.android.util.app.base.BaseFragment;
import org.jraf.android.util.log.Log;

public class MovieListFragment extends BaseFragment<MovieListCallbacks> implements LoaderManager.LoaderCallbacks<Cursor>, PaletteListener {
    private MovieListBinding mBinding;
    private MovieListAdapter mAdapter;
    private SparseIntArray mPalette = new SparseIntArray();
    private int mCurrentPosition;
    private ValueAnimator mColorAnimation;

    public static MovieListFragment newInstance() {
        return new MovieListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.movie_list, container, false);
        mBinding.setCallbacks(getCallbacks());
        mBinding.rclList.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper() {
            @Nullable
            @Override
            public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                View res = super.findSnapView(layoutManager);
                int position = mBinding.rclList.getChildAdapterPosition(res);
                Log.d("position=" + position);
                if (mCurrentPosition != position) {
                    mCurrentPosition = position;
                    updateBackgroundColor();
                }
                return res;
            }
        };
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
                mAdapter = new MovieListAdapter(getContext(), getCallbacks(), this);
                mBinding.rclList.setAdapter(mAdapter);
            }
            mAdapter.swapCursor((MovieCursor) data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null) mAdapter.swapCursor(null);
    }


    @Override
    public void onPaletteAvailable(int position, @ColorInt int color) {
        mPalette.put(position, color);
        if (mCurrentPosition == position) {
            updateBackgroundColor();
        }
    }

    private void updateBackgroundColor() {
        if (mPalette.indexOfKey(mCurrentPosition) > -1) {
            if (mColorAnimation != null) mColorAnimation.cancel();

            int colorFrom = ((ColorDrawable) mBinding.conRoot.getBackground()).getColor();
            int colorTo = mPalette.get(mCurrentPosition);
            mColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            mColorAnimation.setDuration(200);
            mColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    mBinding.conRoot.setBackgroundColor((int) animator.getAnimatedValue());
                }
            });
            mColorAnimation.start();
        }
    }
}
