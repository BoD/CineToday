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
package org.jraf.android.cinetoday.app.loadmovies;

import org.jraf.android.util.listeners.Listeners;

public class LoadMoviesListenerHelper implements LoadMoviesListener {
    private static final LoadMoviesListenerHelper INSTANCE = new LoadMoviesListenerHelper();

    private boolean mStarted;
    private Listeners<LoadMoviesListener> mListeners = new Listeners<>();
    private Throwable mError;
    private Integer mCurrentMovieIndex;
    private String mCurrentMovieName;
    private Integer mTotalMovie;

    private LoadMoviesListenerHelper() {
        mListeners.setNewListenerDispatcher(listener -> {
            if (mStarted) {
                listener.onLoadMoviesStarted();
            } else {
                listener.onLoadMoviesSuccess();
            }
            if (mError != null) listener.onLoadMoviesError(mError);
            if (mCurrentMovieIndex != null && mTotalMovie != null && mCurrentMovieName != null) {
                listener.onLoadMoviesProgress(mCurrentMovieIndex, mTotalMovie, mCurrentMovieName);
            }
        });
    }

    public static LoadMoviesListenerHelper get() {
        return INSTANCE;
    }

    public void addListener(LoadMoviesListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(LoadMoviesListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onLoadMoviesStarted() {
        mStarted = true;
        mListeners.dispatch(LoadMoviesListener::onLoadMoviesStarted);
    }

    @Override
    public void onLoadMoviesProgress(int currentMovie, int totalMovies, String movieName) {
        mCurrentMovieIndex = currentMovie;
        mTotalMovie = totalMovies;
        mCurrentMovieName = movieName;
        mListeners.dispatch(listener -> listener.onLoadMoviesProgress(currentMovie, totalMovies, movieName));
    }

    @Override
    public void onLoadMoviesInterrupted() {
        mStarted = false;
        mCurrentMovieIndex = mTotalMovie = null;
        mCurrentMovieName = null;
        mListeners.dispatch(LoadMoviesListener::onLoadMoviesInterrupted);
    }

    @Override
    public void onLoadMoviesSuccess() {
        mStarted = false;
        mCurrentMovieIndex = mTotalMovie = null;
        mCurrentMovieName = null;
        mListeners.dispatch(LoadMoviesListener::onLoadMoviesSuccess);
    }

    @Override
    public void onLoadMoviesError(Throwable error) {
        mError = error;
        mStarted = false;
        mCurrentMovieIndex = mTotalMovie = null;
        mCurrentMovieName = null;
        mListeners.dispatch(listener -> listener.onLoadMoviesError(error));
    }

    public void resetError() {
        mError = null;
    }
}
