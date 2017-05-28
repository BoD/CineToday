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
package org.jraf.android.cinetoday.model.movie

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import org.jraf.android.cinetoday.database.Converters
import org.jraf.android.cinetoday.util.uri.HasId
import java.util.Date
import java.util.TreeMap

@Entity
data class Movie(
        @PrimaryKey
        override var id: String,

        var originalTitle: String,
        var localTitle: String,

        var directors: String?,
        var actors: String?,

        @field:TypeConverters(Converters.DateConverter::class)
        var releaseDate: Date?,
        var durationSeconds: Int?,

        @field:TypeConverters(Converters.ListConverter::class)
        var genres: Array<String>,

        var posterUri: String?,
        var trailerUri: String?,

        var webUri: String,
        var synopsis: String?,

        var isNew: Boolean,
        var color: Int?
) : HasId, Comparable<Movie> {

    @Ignore
    constructor() : this("", "", "", null, null, null, null, emptyArray<String>(), null, null, "", null, false, null)

    /**
     * Keys: id of the theater.
     *
     * Values: showtimes for today at a given theater.
     */
    @Ignore
    var todayShowtimes = TreeMap<String, List<Showtime>>()


    override fun equals(other: Any?) = (other as? Movie)?.id == id

    override fun hashCode() = id.hashCode()

    /**
     * Compare in *reverse* release date order.
     */
    override fun compareTo(other: Movie): Int {
        var res: Int
        val otherReleaseDate = other.releaseDate
        if (otherReleaseDate != null && releaseDate != null) {
            res = otherReleaseDate.compareTo(releaseDate)
            if (res == 0) res = id.compareTo(other.id)
        } else {
            res = id.compareTo(other.id)
        }
        return res
    }
}
