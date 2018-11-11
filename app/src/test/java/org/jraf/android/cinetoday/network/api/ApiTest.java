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
package org.jraf.android.cinetoday.network.api;

import okhttp3.OkHttpClient;
import org.fest.assertions.api.Assertions;
import org.jraf.android.cinetoday.mobile.TestUtil;
import org.jraf.android.cinetoday.model.movie.Movie;
import org.jraf.android.cinetoday.model.movie.MovieAssert;
import org.jraf.android.cinetoday.network.api.codec.movie.MovieCodec;
import org.jraf.android.cinetoday.network.api.codec.showtime.ShowtimeCodec;
import org.jraf.android.cinetoday.network.api.codec.theater.TheaterCodec;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class ApiTest {

    @Test
    public void testParseMovieList1() throws IOException, JSONException, ParseException, java.text.ParseException {
        testParseMovieListFile("movie_list1.json", 6);
    }

    private void testParseMovieListFile(String filename, int movieCount) throws IOException, JSONException, ParseException, java.text.ParseException {
        String json = TestUtil.readTestResource(filename);
        Set<Movie> movies = new HashSet<>();
        Api api = new Api(new OkHttpClient.Builder().build(), new MovieCodec(), new ShowtimeCodec(), new TheaterCodec());
        api.parseMovieList(movies, json, "Test", Api.Companion.getSIMPLE_DATE_FORMAT().parse("2016-04-10"));
        Assertions.assertThat(movies).hasSize(movieCount);
        for (Movie movie : movies) {
            MovieAssert.assertThat(movie).hasRequiredMovieListFields();
        }
    }
}
