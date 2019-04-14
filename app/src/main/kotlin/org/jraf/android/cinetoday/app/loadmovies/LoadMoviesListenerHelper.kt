/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class LoadMoviesListenerHelper {

    private val _progressInfo = BehaviorSubject.create<ProgressInfo>().toSerialized()
    private val _error = PublishSubject.create<Exception>().toSerialized()

    val progressInfo: Observable<ProgressInfo> get() = _progressInfo.hide()
    val error: Observable<Exception> get() = _error.hide()

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
        _progressInfo.onNext(ProgressInfo.PreLoading())
    }

    fun setIdle() {
        _progressInfo.onNext(ProgressInfo.Idle())
    }

    fun setLoading(totalMovies: Int, currentMovieIndex: Int, currentMovieTitle: String) {
        _progressInfo.onNext(ProgressInfo.Loading(totalMovies, currentMovieIndex, currentMovieTitle))
    }

    fun pushError(error: Exception) {
        _error.onNext(error)
        setIdle()
    }

}
