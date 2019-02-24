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
import org.jraf.android.cinetoday.model.showtime.Showtime
import java.util.Date

@Dao
interface ShowtimeDao {
    @Insert
    fun insert(movies: List<Showtime>)

    @Query("DELETE FROM Showtime")
    fun deleteAll()

    @Query("SELECT * FROM Showtime WHERE movieId = :movieId")
    fun showtimesByMovieIdLive(movieId: String): LiveData<Array<Showtime>>

    @Query(
        "SELECT "
                + "Showtime.theaterId, "
                + "Showtime.movieId, "
                + "Showtime.time, "
                + "Showtime.is3d, "
                + "Theater.name as theaterName "
                + "FROM "
                + "Showtime JOIN Theater ON Showtime.theaterId = theater.id "
                + "WHERE "
                + "movieId = :movieId "
                + "ORDER BY "
                + "Showtime.theaterId, "
                + "Showtime.time"
    )
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