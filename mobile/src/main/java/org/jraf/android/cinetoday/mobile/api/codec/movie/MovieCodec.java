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
package org.jraf.android.cinetoday.mobile.api.codec.movie;

import java.util.ArrayList;

import android.text.Html;

import org.jraf.android.cinetoday.common.model.ParseException;
import org.jraf.android.cinetoday.common.model.movie.Movie;
import org.jraf.android.cinetoday.mobile.api.Api;
import org.jraf.android.cinetoday.mobile.api.codec.Codec;
import org.jraf.android.util.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieCodec implements Codec<Movie> {
    private static final MovieCodec INSTANCE = new MovieCodec();

    private MovieCodec() {}

    public static MovieCodec get() {
        return INSTANCE;
    }

    public void fill(Movie movie, JSONObject jsonMovie) throws ParseException {
        try {
            movie.id = jsonMovie.getString("code");
            movie.localTitle = jsonMovie.getString("title");
            movie.originalTitle = jsonMovie.optString("originalTitle", null);
            movie.synopsis = jsonMovie.optString("synopsis", null);
            if (movie.synopsis != null) {
                // Strip html
                movie.synopsis = Html.fromHtml(movie.synopsis).toString().trim();
            }

            JSONObject jsonCastingShort = jsonMovie.optJSONObject("castingShort");
            if (jsonCastingShort != null) {
                movie.directors = jsonCastingShort.optString("directors", null);
                movie.actors = jsonCastingShort.optString("actors", null);
            }

            JSONObject jsonRelease = jsonMovie.optJSONObject("release");
            if (jsonRelease != null) {
                String releaseDateStr = jsonRelease.getString("releaseDate");
                try {
                    movie.releaseDate = Api.SIMPLE_DATE_FORMAT.parse(releaseDateStr);
                } catch (java.text.ParseException e) {
                    Log.d(e, "Invalid releaseDate %s in movie %s", movie, releaseDateStr);
                }
            }

            movie.durationSeconds = jsonMovie.getInt("runtime");

            JSONArray jsonGenreArray = jsonMovie.getJSONArray("genre");
            int len = jsonGenreArray.length();
            ArrayList<String> genres = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                JSONObject jsonGenre = jsonGenreArray.getJSONObject(i);
                genres.add(jsonGenre.getString("$"));
            }
            movie.genres = genres.toArray(new String[len]);

            JSONObject jsonPoster = jsonMovie.getJSONObject("poster");
            movie.posterUri = jsonPoster.getString("href");

            JSONObject jsonTrailer = jsonMovie.optJSONObject("trailer");
            if (jsonTrailer != null) movie.trailerUri = jsonTrailer.getString("href");

            JSONArray jsonLinkArray = jsonMovie.getJSONArray("link");
            JSONObject jsonLink = jsonLinkArray.getJSONObject(0);
            movie.webUri = jsonLink.getString("href");
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }
}
