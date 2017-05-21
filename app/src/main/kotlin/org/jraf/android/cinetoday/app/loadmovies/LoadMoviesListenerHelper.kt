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
package org.jraf.android.cinetoday.app.loadmovies

import org.jraf.android.util.listeners.Listeners

class LoadMoviesListenerHelper private constructor() : LoadMoviesListener {

    private var mStarted: Boolean = false
    private val mListeners = Listeners<LoadMoviesListener>()
    private var mError: Throwable? = null
    private var mCurrentMovieIndex: Int? = null
    private var mCurrentMovieName: String? = null
    private var mTotalMovie: Int? = null

    init {
        mListeners.setNewListenerDispatcher { listener ->
            if (mStarted) {
                listener.onLoadMoviesStarted()
            } else {
                listener.onLoadMoviesSuccess()
            }
            mError?.let { listener.onLoadMoviesError(it) }
            if (mCurrentMovieIndex != null && mTotalMovie != null && mCurrentMovieName != null) {
                listener.onLoadMoviesProgress(mCurrentMovieIndex!!, mTotalMovie!!, mCurrentMovieName!!)
            }
        }
    }

    fun addListener(listener: LoadMoviesListener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: LoadMoviesListener) {
        mListeners.remove(listener)
    }

    override fun onLoadMoviesStarted() {
        mStarted = true
        mListeners.dispatch { it.onLoadMoviesStarted() }
    }

    override fun onLoadMoviesProgress(currentMovie: Int, totalMovies: Int, movieName: String) {
        mCurrentMovieIndex = currentMovie
        mTotalMovie = totalMovies
        mCurrentMovieName = movieName
        mListeners.dispatch { listener -> listener.onLoadMoviesProgress(currentMovie, totalMovies, movieName) }
    }

    override fun onLoadMoviesInterrupted() {
        mStarted = false
        mTotalMovie = null
        mCurrentMovieIndex = mTotalMovie
        mCurrentMovieName = null
        mListeners.dispatch { it.onLoadMoviesInterrupted() }
    }

    override fun onLoadMoviesSuccess() {
        mStarted = false
        mTotalMovie = null
        mCurrentMovieIndex = mTotalMovie
        mCurrentMovieName = null
        mListeners.dispatch { it.onLoadMoviesSuccess() }
    }

    override fun onLoadMoviesError(error: Throwable) {
        mError = error
        mStarted = false
        mTotalMovie = null
        mCurrentMovieIndex = mTotalMovie
        mCurrentMovieName = null
        mListeners.dispatch { listener -> listener.onLoadMoviesError(error) }
    }

    fun resetError() {
        mError = null
    }

    companion object {
        private val INSTANCE = LoadMoviesListenerHelper()

        fun get(): LoadMoviesListenerHelper {
            return INSTANCE
        }
    }
}
