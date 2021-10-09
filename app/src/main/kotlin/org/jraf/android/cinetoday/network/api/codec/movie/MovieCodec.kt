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
package org.jraf.android.cinetoday.network.api.codec.movie

import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.network.api.ParseException
import org.jraf.android.cinetoday.network.api.graphql.MovieShowtimesQuery
import java.text.SimpleDateFormat
import java.util.Date

class MovieCodec {
    private val graphqlDateFormat = SimpleDateFormat("yyyy-MM-dd")

    @Throws(ParseException::class)
    fun fill(movie: Movie, graphqlMovie: MovieShowtimesQuery.Movie) {
        // Log.d(jsonMovie.toString(4))
        try {
            movie.apply {
                id = graphqlMovie.id
                originalTitle = graphqlMovie.originalTitle ?: graphqlMovie.title!!
                localTitle = graphqlMovie.title!!
                directors =
                    graphqlMovie.credits?.let { credits -> credits.edges!!.map { creditEdge -> creditEdge!!.node!!.person!!.stringValue }.joinToString() }
                actors =
                    graphqlMovie.cast?.let { casts -> casts.edges!!.map { castEdge -> castEdge!!.node!!.actor?.stringValue }.filterNotNull().joinToString() }
                releaseDate = graphqlMovie.releases?.getOrNull(0)?.releaseDate?.date?.let { graphqlDateStringToDate(it) }
                durationSeconds = graphqlMovie.runtime?.toInt()
                genres = graphqlMovie.genres.orEmpty().map { genre -> genre!!.name.lowercase().capitalize().replace('_', ' ') }.toTypedArray()
                posterUri = graphqlMovie.poster?.url
                trailerUri = graphqlMovie.videos?.getOrNull(0)?.files?.getOrNull(0)?.url
                synopsis = graphqlMovie.synopsis
            }

        } catch (e: Exception) {
            throw ParseException(e)
        }
    }

    private fun graphqlDateStringToDate(graphQLDateString: String): Date? {
        return graphqlDateFormat.parse(graphQLDateString)
    }

}
