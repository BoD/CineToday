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

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class LoadMoviesListenerHelper {

    private val mProgressInfo = BehaviorSubject.create<ProgressInfo>()
    private val mError = PublishSubject.create<Exception>()

    val progressInfo get() = mProgressInfo.hide()
    val error get() = mError.hide()

    sealed class ProgressInfo {
        class Idle : ProgressInfo()
        class PreLoading : ProgressInfo()
        data class Loading(
                val totalMovies: Int,
                val currentMovieIndex: Int,
                val currentMovieTitle: String
        ) : ProgressInfo()
    }

    fun setPreloading() {
        mProgressInfo.onNext(ProgressInfo.PreLoading())
    }

    fun setIdle() {
        mProgressInfo.onNext(ProgressInfo.Idle())
    }

    fun setLoading(totalMovies: Int, currentMovieIndex: Int, currentMovieTitle: String) {
        mProgressInfo.onNext(ProgressInfo.Loading(totalMovies, currentMovieIndex, currentMovieTitle))
    }

    fun pushError(error: Exception) {
        mError.onNext(error)
        setIdle()
    }

}
