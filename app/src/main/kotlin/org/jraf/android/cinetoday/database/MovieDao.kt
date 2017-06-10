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
import org.jraf.android.cinetoday.model.movie.Movie

@Dao
interface MovieDao {
    @Insert
    fun insert(movies: List<Movie>)

    @Query("SELECT * FROM movie ORDER BY releaseDate DESC")
    fun allMoviesLive(): LiveData<Array<Movie>>

    @Query("SELECT * FROM movie ORDER BY releaseDate DESC")
    fun allMovies(): Array<Movie>

    // TODO "p0" is named "p0" because of this issue: https://youtrack.jetbrains.com/issue/KT-17959
    @Query("SELECT * FROM movie where id = :p0")
    fun movieById(id: String): Movie?

    // TODO "p0" is named "p0" because of this issue: https://youtrack.jetbrains.com/issue/KT-17959
    @Query("SELECT * FROM movie where id = :p0")
    fun movieByIdLive(id: String): LiveData<Movie?>

    @Query("DELETE FROM movie")
    fun deleteAll()

    @Query("DELETE FROM movie WHERE"
            + " ( SELECT COUNT(*) FROM showtime WHERE showtime.movieId = movie.id ) "
            + " = 0")
    fun deleteWithNoShowtimes()

    // TODO "p0" is named "p0" because of this issue: https://youtrack.jetbrains.com/issue/KT-17959
    @Query("UPDATE movie SET color = :p1 WHERE id = :p0")
    fun updateColor(id: String, color: Int)
}