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
package org.jraf.android.cinetoday.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.TypeConverters
import org.jraf.android.cinetoday.model.movie.Showtime
import java.util.Date

@Dao
interface ShowtimeDao {
    @Insert
    fun insert(movies: List<Showtime>)

    @Query("DELETE FROM showtime")
    fun deleteAll()

    // TODO "p0" is named "p0" because of this issue: https://youtrack.jetbrains.com/issue/KT-17959
    @Query("SELECT * FROM showtime WHERE movieId = :p0")
    fun showtimesByMovieIdLive(movieId: String): LiveData<Array<Showtime>>

    // TODO "p0" is named "p0" because of this issue: https://youtrack.jetbrains.com/issue/KT-17959
    @Query("SELECT "
            + "showtime.theaterId, "
            + "showtime.movieId, "
            + "showtime.time, "
            + "showtime.is3d, "
            + "theater.name as theaterName "
            + "FROM "
            + "showtime JOIN theater ON showtime.theaterId = theater.id "
            + "WHERE "
            + "movieId = :p0 "
            + "ORDER BY "
            + "showtime.theaterId, "
            + "showtime.time")
    fun showtimesWithTheaterByMovieIdLive(movieId: String): LiveData<Array<ShowtimeWithTheater>>
}

data class ShowtimeWithTheater(
        // Showtime
        val theaterId: String,
        val movieId: String,

        @field:TypeConverters(Converters.DateConverter::class)
        val time: Date,
        val is3d: Boolean,

        // Theater
        val theaterName: String
)