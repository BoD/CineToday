/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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

class Showtime : Comparable<Showtime> {
    var time: Date? = null
    var is3d: Boolean = false

    override fun toString(): String {
        return "Showtime{" +
                "time='" + time + '\'' +
                ", is3d=" + is3d +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val showtime = o as Showtime?

        if (is3d != showtime!!.is3d) return false
        return if (time != null) time == showtime.time else showtime.time == null
    }

    override fun hashCode(): Int {
        var result = if (time != null) time!!.hashCode() else 0
        result = 31 * result + if (is3d) 1 else 0
        return result
    }

    override fun compareTo(another: Showtime): Int {
        val res = time!!.compareTo(another.time)
        if (res != 0) return res
        if (is3d) {
            if (another.is3d) return 0
            return 1
        } else {
            if (!another.is3d) return 0
            return -1
        }
    }
}
