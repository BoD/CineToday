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
package org.jraf.android.cinetoday.network.api.codec.showtime

import org.jraf.android.cinetoday.model.showtime.Showtime
import org.jraf.android.cinetoday.network.api.ParseException
import org.jraf.android.cinetoday.network.api.graphql.MovieShowtimesQuery
import org.jraf.android.cinetoday.network.api.graphql.type.Projection

class ShowtimeCodec {
    private val projection3d =
        setOf(
            Projection.F_3D,
            Projection.F_3D70MM,
            Projection.F_3DHFR,
            Projection.F_4K3D,
            Projection.IMAX_3D,
            Projection.IMAX_3D_HFR,
            Projection.REALD_3D,
        )

    @Throws(ParseException::class)
    fun convert(graphqlShowtime: MovieShowtimesQuery.Showtime, movieId: String, theaterId: String): Showtime {
        return Showtime(
            id = graphqlShowtime.id.hashCode().toLong(),
            theaterId = theaterId,
            movieId = movieId,
            time = graphqlShowtime.startsAt!!,
            is3d = graphqlShowtime.projection.orEmpty().any { it in projection3d }
        )
    }
}
