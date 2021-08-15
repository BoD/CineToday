/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.jraf.android.cinetoday.model.movie.Movie

@Dao
interface MovieDao {
    @Insert
    fun insert(movies: List<Movie>)

    @Query("SELECT * FROM Movie ORDER BY releaseDate DESC")
    fun allMoviesLive(): LiveData<Array<Movie>>

    @Query("SELECT * FROM Movie ORDER BY releaseDate DESC")
    fun allMovies(): Array<Movie>

    @Query("SELECT * FROM Movie where id = :id")
    fun movieById(id: String): Movie?

    @Query("SELECT * FROM Movie where id = :id")
    fun movieByIdLive(id: String): LiveData<Movie?>

    @Query("DELETE FROM Movie")
    fun deleteAll()

    @Query(
        "DELETE FROM Movie WHERE"
                + " ( SELECT COUNT(*) FROM showtime WHERE showtime.movieId = movie.id ) "
                + " = 0"
    )
    fun deleteWithNoShowtimes()

    @Query("UPDATE Movie SET color = :colorDark, colorLight = :colorLight WHERE id = :id")
    fun updateColor(id: String, colorDark: Int, colorLight: Int)
}