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

import androidx.room.Database
import androidx.room.RoomDatabase
import org.jraf.android.cinetoday.model.knownmovie.KnownMovie
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.model.showtime.Showtime
import org.jraf.android.cinetoday.model.theater.Theater

@Database(
    entities = [
        Theater::class,
        Movie::class,
        KnownMovie::class,
        Showtime::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "cinetoday.db"
    }

    abstract val theaterDao: TheaterDao
    abstract val movieDao: MovieDao
    abstract val knownMovieDao: KnownMovieDao
    abstract val showtimeDao: ShowtimeDao
}

