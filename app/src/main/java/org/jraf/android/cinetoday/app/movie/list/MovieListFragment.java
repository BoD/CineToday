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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesListener;
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesListenerHelper;
import org.jraf.android.cinetoday.databinding.MovieListBinding;
import org.jraf.android.cinetoday.provider.movie.MovieContentValues;
import org.jraf.android.cinetoday.provider.movie.MovieCursor;
import org.jraf.android.cinetoday.provider.movie.MovieSelection;
import org.jraf.android.util.app.base.BaseFragment;

public class MovieListFragment extends BaseFragment<MovieListCallbacks> implements LoaderManager.LoaderCallbacks<Cursor>, PaletteListener {
    private MovieListBinding mBinding;
    private MovieListAdapter mAdapter;
    private SparseIntArray mPalette = new SparseIntArray();
    private ValueAnimator mColorAnimation;
    private boolean mScrolling;
    private boolean mLoadMoviesStarted;

    public static MovieListFragment newInstance() {
        return new MovieListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroy() {
        LoadMoviesListenerHelper.get().removeListener(mLoadMoviesListener);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.movie_list, container, false);
        mBinding.setCallbacks(getCallbacks());
        mBinding.rclList.setHasFixedSize(true);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mBinding.rclList);
        mBinding.rclList.addOnScrollListener(mOnScrollListener);

        LoadMoviesListenerHelper.get().addListener(mLoadMoviesListener);

        return mBinding.getRoot();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MovieSelection().getCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBinding.pgbLoading.setVisibility(View.GONE);
        if (data.getCount() == 0) {
            // No movies
            if (mLoadMoviesStarted) {
                mBinding.conMoviesLoading.setVisibility(View.VISIBLE);
                mBinding.txtEmpty.setVisibility(View.GONE);
            } else {
                mBinding.conMoviesLoading.setVisibility(View.GONE);
                mBinding.txtEmpty.setVisibility(View.VISIBLE);
            }
            mBinding.rclList.setVisibility(View.GONE);
        } else {
            mBinding.conMoviesLoading.setVisibility(View.GONE);
            mBinding.txtEmpty.setVisibility(View.GONE);
            mBinding.rclList.setVisibility(View.VISIBLE);
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
    public void onPaletteAvailable(int position, @ColorInt final int color, boolean cached, final long id) {
        if (mPalette.indexOfKey(position) >= 0 && position > 0) return;
        mPalette.put(position, color);
        int firstItemPosition = ((LinearLayoutManager) mBinding.rclList.getLayoutManager()).findFirstVisibleItemPosition();
        if (firstItemPosition == RecyclerView.NO_POSITION) firstItemPosition = 0;
        if (firstItemPosition == position && !mScrolling) {
            updateBackgroundColor(position);
        }
        if (!cached) {
            // Save the value
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    new MovieContentValues()
                            .putColor(color)
                            .notify(false)
                            .update(getContext(), new MovieSelection().id(id));
                    return null;
                }
            }.execute();
        }
    }

    private void updateBackgroundColor(int position) {
        if (mPalette.indexOfKey(position) >= 0) {
            if (mColorAnimation != null) mColorAnimation.cancel();

            int colorFrom = ((ColorDrawable) mBinding.conRoot.getBackground()).getColor();
            int colorTo = mPalette.get(position);
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

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            mScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int firstItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            View firstItem = recyclerView.getLayoutManager().findViewByPosition(firstItemPosition);
            float firstItemTop = firstItem.getY();
            float firstItemRatio = Math.abs(firstItemTop / recyclerView.getHeight());

            int nextItemPosition = firstItemPosition + 1;
            if (nextItemPosition >= mAdapter.getItemCount()) return;

            int firstItemColor = mPalette.get(firstItemPosition, -1);
            if (firstItemColor == -1) return;

            int nextItemColor = mPalette.get(nextItemPosition, -1);
            if (nextItemColor == -1) return;

            int resultColor = (int) mArgbEvaluator.evaluate(firstItemRatio, firstItemColor, nextItemColor);
            mBinding.conRoot.setBackgroundColor(resultColor);
        }
    };

    //--------------------------------------------------------------------------
    // region Movie loading.
    //--------------------------------------------------------------------------

    private LoadMoviesListener mLoadMoviesListener = new LoadMoviesListener() {
        @Override
        public void onLoadMoviesStarted() {
            if (mAdapter == null || mAdapter.getItemCount() == 0) {
                mLoadMoviesStarted = true;
                mBinding.conMoviesLoading.setVisibility(View.VISIBLE);
                mBinding.txtEmpty.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoadMoviesProgress(int currentMovie, int totalMovies, String movieName) {
            mBinding.txtMoviesLoadingInfo.setText(getString(R.string.movie_list_loadingMovies_progress, currentMovie, totalMovies));
        }

        @Override
        public void onLoadMoviesInterrupted() {}

        @Override
        public void onLoadMoviesSuccess() {
            mLoadMoviesStarted = false;
            mBinding.conMoviesLoading.setVisibility(View.GONE);
        }

        @Override
        public void onLoadMoviesError(Throwable t) {
            mLoadMoviesStarted = false;
            mBinding.conMoviesLoading.setVisibility(View.GONE);
        }
    };

    // endregion
}
