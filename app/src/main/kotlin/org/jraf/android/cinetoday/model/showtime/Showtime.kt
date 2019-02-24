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
package org.jraf.android.cinetoday.model.showtime

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.jraf.android.cinetoday.database.Converters
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.model.theater.Theater
import java.util.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Theater::class,
            parentColumns = ["id"],
            childColumns = ["theaterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Movie::class,
            parentColumns = ["id"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("theaterId"),
        Index("movieId")
    ]
)
data class Showtime(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val theaterId: String,
    val movieId: String,

    @field:TypeConverters(Converters.DateConverter::class)
    val time: Date,
    val is3d: Boolean
) : Comparable<Showtime> {

    override fun compareTo(other: Showtime): Int {
        val res = time.compareTo(other.time)
        if (res != 0) return res
        if (is3d) {
            if (other.is3d) return 0
            return 1
        } else {
            if (!other.is3d) return 0
            return -1
        }
    }
}
