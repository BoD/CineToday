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
package org.jraf.android.cinetoday.network.api.codec.theater

import org.jraf.android.cinetoday.model.ParseException
import org.jraf.android.cinetoday.model.theater.Theater
import org.json.JSONException
import org.json.JSONObject

class TheaterCodec {
    @Throws(ParseException::class)
    fun fill(theater: Theater, jsonTheater: JSONObject) {
        try {
            theater.id = jsonTheater.getString("code")
            theater.name = jsonTheater.getString("name")
            theater.address = jsonTheater.getString("address").trim() +
                    "\n" +
                    jsonTheater.getString("postalCode").trim() + " " + jsonTheater.getString("city").trim()
            theater.pictureUri = jsonTheater.optJSONObject("poster")?.getString("href")
        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }
}
