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

import java.util.*

class Movie {
    var id: String? = null
    var originalTitle: String? = null
    var localTitle: String? = null
    var directors: String? = null
    var actors: String? = null
    var releaseDate: Date? = null
    var durationSeconds: Int? = null
    var genres: Array<String>? = null
    var posterUri: String? = null
    var trailerUri: String? = null
    var webUri: String? = null
    var synopsis: String? = null

    var isNew: Boolean = false
    var color: Int? = null

    /**
     * Keys: id of the theater<br></br>
     * Values: showtimes for today at a given theater.
     */
    var todayShowtimes: SortedMap<String, List<Showtime>>? = null

    override fun toString(): String {
        return "Movie{" +
                "id='" + id + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", localTitle='" + localTitle + '\'' +
                ", directors='" + directors + '\'' +
                ", actors='" + actors + '\'' +
                ", releaseDate=" + releaseDate +
                ", durationSeconds=" + durationSeconds +
                ", genres=" + Arrays.toString(genres) +
                ", posterUri='" + posterUri + '\'' +
                ", trailerUri='" + trailerUri + '\'' +
                ", webUri='" + webUri + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", todayShowtimes=" + todayShowtimes +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val movie = o as Movie?
        return id == movie!!.id

    }

    override fun hashCode(): Int {
        return id!!.hashCode()
    }

    companion object {

        /**
         * Compares in reverse release date order.
         */
        val COMPARATOR: Comparator<Movie> = object : Comparator<Movie> {
            override fun compare(lhs: Movie, rhs: Movie): Int {
                var res: Int
                if (rhs.releaseDate != null && lhs.releaseDate != null) {
                    res = rhs.releaseDate!!.compareTo(lhs.releaseDate)
                    if (res == 0) res = lhs.id!!.compareTo(rhs.id!!)
                } else {
                    res = lhs.id!!.compareTo(rhs.id!!)
                }
                return res
            }
        }
    }
}
