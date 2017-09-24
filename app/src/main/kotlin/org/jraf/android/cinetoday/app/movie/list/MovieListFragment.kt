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
package org.jraf.android.cinetoday.app.movie.list

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesListenerHelper
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.databinding.MovieListBinding
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.util.async.doAsync
import org.jraf.android.cinetoday.util.base.BaseFragment
import org.jraf.android.cinetoday.widget.RotaryPagerSnapHelper
import javax.inject.Inject


class MovieListFragment : BaseFragment<MovieListCallbacks>(), PaletteListener {
    companion object {
        fun newInstance(): MovieListFragment {
            return MovieListFragment()
        }
    }

    @Inject lateinit var mDatabase: AppDatabase
    @Inject lateinit var mLoadMoviesListenerHelper: LoadMoviesListenerHelper

    private lateinit var mBinding: MovieListBinding
    private var mAdapter: MovieListAdapter? = null
    private val mPalette = SparseIntArray()
    private var mColorAnimation: ValueAnimator? = null
    private var mScrolling: Boolean = false
    private var mLoadMoviesStarted: Boolean = false

    private lateinit var mProgressSubscription: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Components.application.inject(this)
        mDatabase.movieDao.allMoviesLive().observe(this, Observer { if (it != null) onMoviesResult(it) })
    }

    override fun onDestroy() {
        mProgressSubscription.dispose()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.movie_list, container, false)
        mBinding.callbacks = callbacks

        mBinding.rclList.setHasFixedSize(true)
        mBinding.rclList.layoutManager = LinearLayoutManager(context)
        RotaryPagerSnapHelper().attachToRecyclerView(mBinding.rclList)
        mBinding.rclList.addOnScrollListener(mOnScrollListener)

        mProgressSubscription = mLoadMoviesListenerHelper.progressInfo.observeOn(AndroidSchedulers.mainThread()).subscribe {
            when (it) {
                is LoadMoviesListenerHelper.ProgressInfo.Idle -> {
                    mLoadMoviesStarted = false
                    mBinding.conMoviesLoading.visibility = View.GONE
                }

                is LoadMoviesListenerHelper.ProgressInfo.PreLoading -> {
                    if (mAdapter == null || mAdapter?.itemCount ?: 0 == 0) {
                        mLoadMoviesStarted = true
                        mBinding.conMoviesLoading.visibility = View.VISIBLE
                        mBinding.txtEmpty.visibility = View.GONE
                        mBinding.txtMoviesLoadingInfo.text = null
                    }
                }

                is LoadMoviesListenerHelper.ProgressInfo.Loading -> {
                    if (mAdapter == null || mAdapter?.itemCount ?: 0 == 0) {
                        mLoadMoviesStarted = true
                        mBinding.conMoviesLoading.visibility = View.VISIBLE
                        mBinding.txtEmpty.visibility = View.GONE
                        mBinding.txtMoviesLoadingInfo.text = getString(R.string.movie_list_loadingMovies_progress, it.currentMovieIndex, it.totalMovies)
                    }
                }
            }
        }

        // TODO Also observe errors

        return mBinding.root
    }

    private fun onMoviesResult(movies: Array<Movie>) {
        mBinding.pgbLoading.visibility = View.GONE
        if (movies.isEmpty()) {
            // No movies
            if (mLoadMoviesStarted) {
                // Loading
                mBinding.conMoviesLoading.visibility = View.VISIBLE
                mBinding.txtEmpty.visibility = View.GONE
            } else {
                mBinding.conMoviesLoading.visibility = View.GONE
                mBinding.txtEmpty.visibility = View.VISIBLE
            }
            mBinding.rclList.visibility = View.GONE
        } else {
            mBinding.conMoviesLoading.visibility = View.GONE
            mBinding.txtEmpty.visibility = View.GONE
            mBinding.rclList.visibility = View.VISIBLE
            // Needed for the rotary input to work
            mBinding.rclList.requestFocus()

            var adapter: MovieListAdapter? = mAdapter
            if (adapter == null) {
                adapter = MovieListAdapter(context, callbacks, this)
                mAdapter = adapter
                mBinding.rclList.adapter = adapter
            }
            adapter.data = movies
        }
    }

    override fun onPaletteAvailable(position: Int, @ColorInt color: Int, cached: Boolean, id: String) {
        if (mPalette.indexOfKey(position) >= 0 && position > 0) return
        mPalette.put(position, color)
        var firstItemPosition = (mBinding.rclList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (firstItemPosition == RecyclerView.NO_POSITION) firstItemPosition = 0
        if (firstItemPosition == position && !mScrolling) {
            updateBackgroundColor(position)
        }
        if (!cached) {
            // Save the value
            doAsync {
                mDatabase.movieDao.updateColor(id, color)
            }
        }
    }

    private fun updateBackgroundColor(position: Int) {
        if (mPalette.indexOfKey(position) >= 0) {
            mColorAnimation?.cancel()

            val colorFrom = (mBinding.conRoot.background as ColorDrawable).color
            val colorTo = mPalette.get(position)
            mColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                duration = 200
                addUpdateListener { animator -> mBinding.conRoot.setBackgroundColor(animator.animatedValue as Int) }
                start()
            }
        }
    }

    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        internal var mArgbEvaluator = ArgbEvaluator()

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            mScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val firstItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val firstItem = recyclerView.layoutManager.findViewByPosition(firstItemPosition)
            val firstItemTop = firstItem.y
            val firstItemRatio = Math.abs(firstItemTop / recyclerView.height)

            val nextItemPosition = firstItemPosition + 1
            if (nextItemPosition >= mAdapter?.itemCount ?: 0) return

            val firstItemColor = mPalette.get(firstItemPosition, -1)
            if (firstItemColor == -1) return

            val nextItemColor = mPalette.get(nextItemPosition, -1)
            if (nextItemColor == -1) return

            val resultColor = mArgbEvaluator.evaluate(firstItemRatio, firstItemColor, nextItemColor) as Int
            mBinding.conRoot.setBackgroundColor(resultColor)
        }
    }
}
