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
package org.jraf.android.cinetoday.network.api.codec.movie

import android.text.Html
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.network.api.Api
import org.jraf.android.cinetoday.network.api.ParseException
import org.jraf.android.util.log.Log
import org.json.JSONException
import org.json.JSONObject

class MovieCodec {
    @Throws(ParseException::class)
    fun fill(movie: Movie, jsonMovie: JSONObject) {
        try {
            movie.id = jsonMovie.getString("code")
            movie.localTitle = jsonMovie.getString("title")
            // Original title defaults to local title
            movie.originalTitle = jsonMovie.optString("originalTitle", null) ?: movie.localTitle
            movie.synopsis = jsonMovie.optString("synopsis", null)?.let {
                // Strip html
                Html.fromHtml(it).toString().trim()
            }

            jsonMovie.optJSONObject("castingShort")?.let {
                movie.directors = it.optString("directors", null)
                movie.actors = it.optString("actors", null)
            }

            movie.releaseDate = jsonMovie.optJSONObject("release")?.let {
                val releaseDateStr = it.getString("releaseDate")
                try {
                    Api.SIMPLE_DATE_FORMAT.parse(releaseDateStr)
                } catch (e: java.text.ParseException) {
                    Log.d(e, "Invalid releaseDate %s in movie %s", releaseDateStr, movie.id)
                    null
                }
            }

            movie.durationSeconds = jsonMovie.optInt("runtime", -1).let { if (it == -1) null else it }

            val jsonGenreArray = jsonMovie.getJSONArray("genre")
            val len = jsonGenreArray.length()
            val genres = mutableListOf<String>()
            for (i in 0 until len) {
                val jsonGenre = jsonGenreArray.getJSONObject(i)
                genres.add(jsonGenre.getString("$"))
            }
            movie.genres = genres.toTypedArray()

            movie.posterUri = jsonMovie.optJSONObject("poster")?.getString("href")
            movie.trailerUri = jsonMovie.optJSONObject("trailer")?.getString("href")

            val jsonLinkArray = jsonMovie.getJSONArray("link")
            val jsonLink = jsonLinkArray.getJSONObject(0)
            movie.webUri = jsonLink.getString("href")

        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }
}
