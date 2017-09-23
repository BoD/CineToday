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
package org.jraf.android.cinetoday.app.movie.details

import android.content.Context
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.model.movie.Movie

class MovieViewModel(movie: Movie, private val mContext: Context) {

    val originalTitle = movie.originalTitle
    val localTitle = movie.localTitle
    val directors = movie.directors
    val actors = movie.actors
    val synopsis = movie.synopsis
    val durationFormatted = movie.durationSeconds?.let { formatDuration(it) }
    val genres = movie.genres
    val genresFormatted = movie.genres.joinToString(" · ")

    private fun formatDuration(durationSeconds: Int): String {
        if (durationSeconds < 60 * 60) return mContext.getString(R.string.durationMinutes, durationSeconds / 60)
        val hours = durationSeconds / (60 * 60)
        val minutes = durationSeconds % (60 * 60) / 60
        if (minutes == 0) return mContext.resources.getQuantityString(R.plurals.durationHours, hours, hours)
        return mContext.getString(R.string.durationHoursMinutes, hours, minutes)
    }
}
