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
package org.jraf.android.cinetoday.network.api.codec.movie;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import org.jraf.android.cinetoday.BuildConfig;
import org.jraf.android.cinetoday.mobile.TestUtil;
import org.jraf.android.cinetoday.model.ParseException;
import org.jraf.android.cinetoday.model.movie.Movie;
import org.jraf.android.cinetoday.model.movie.MovieAssert;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MovieCodecTest {

    @Test
    public void testParseMovie1() throws IOException, JSONException, ParseException {
        testParseMovieFile("movie_detail1.json",
                "Jean Gabin, Danièle Delorme, Robert Arnoux, Liliane Bert, Gérard Blain",
                6780);
    }

    @Test
    public void testParseMovie2() throws IOException, JSONException, ParseException {
        testParseMovieFile("movie_detail2.json",
                null,
                11400);
    }

    @Test
    public void testParseMovie3() throws IOException, JSONException, ParseException {
        testParseMovieFile("movie_detail3.json",
                "Jean Gabin, Charles Vanel, Raymond Aimos, Viviane Romance, Jacques Baumer",
                5700);
    }

    @Test
    public void testParseMovie4() throws IOException, JSONException, ParseException {
        testParseMovieFile("movie_detail4.json",
                "James Mason, Robert Newton, Cyril Cusack, Kathleen Ryan, F.-J. McCormick",
                6960);
    }

    @Test
    public void testParseMovie5() throws IOException, JSONException, ParseException {
        testParseMovieFile("movie_detail5.json",
                "Donald Pleasence, Françoise Dorléac, Lionel Stander, Jack MacGowran, Iain Quarrier",
                6780);
    }

    private void testParseMovieFile(String filename, String actors, Integer duration) throws IOException, JSONException, ParseException {
        String json = TestUtil.readTestResource(filename);
        JSONObject jsonRoot = new JSONObject(json);
        JSONObject jsonMovie = jsonRoot.getJSONObject("movie");

        Movie movie = new Movie("",
                "",
                "",
                "",
                "",
                null,
                null,
                new String[] {},
                null,
                null,
                "",
                null,
                false,
                null);
        new MovieCodec().fill(movie, jsonMovie);

        MovieAssert.assertThat(movie)
                .hasRequiredMovieDetailFields()
                .hasActors(actors)
                .hasDurationSeconds(duration);
        // TODO: add assertions on more fields

    }
}
