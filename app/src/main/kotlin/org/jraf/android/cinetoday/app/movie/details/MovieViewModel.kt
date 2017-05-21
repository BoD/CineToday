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
import android.database.Cursor

import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.provider.movie.MovieCursor

class MovieViewModel(private val mContext: Context, cursor: Cursor) : MovieCursor(cursor) {

    val durationFormatted: String?
        get() {
            if (duration == null) return null
            return formatDuration(duration!!)
        }

    val genresFormatted: String?
        get() {
            if (genres == null) return null
            return genres!!.replace("\\|".toRegex(), " · ")
        }

    private fun formatDuration(durationSeconds: Int): String {
        if (durationSeconds < 60 * 60) return mContext.getString(R.string.durationMinutes, durationSeconds / 60)
        val hours = durationSeconds / (60 * 60)
        val minutes = durationSeconds % (60 * 60) / 60
        if (minutes == 0) return mContext.resources.getQuantityString(R.plurals.durationHours, hours, hours)
        return mContext.getString(R.string.durationHoursMinutes, hours, minutes)
    }

    override fun getTitleLocal(): String {
        return super.getTitleLocal()
    }

    override fun getDirectors(): String? {
        return super.getDirectors()
    }

    override fun getGenres(): String? {
        return super.getGenres()
    }

    override fun getActors(): String? {
        return super.getActors()
    }

    override fun getSynopsis(): String {
        return super.getSynopsis()
    }

    override fun getTitleOriginal(): String {
        return super.getTitleOriginal()
    }
}
