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
package org.jraf.android.cinetoday.mobile.api.codec.showtime;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import org.jraf.android.cinetoday.common.model.ParseException;
import org.jraf.android.cinetoday.common.model.movie.Movie;
import org.jraf.android.cinetoday.mobile.api.codec.Codec;
import org.jraf.android.util.log.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class ShowtimeCodec implements Codec<Movie> {
    private static final ShowtimeCodec INSTANCE = new ShowtimeCodec();

    public static final SimpleDateFormat DAY_DATE_FORMAT = new SimpleDateFormat("d MMMM", Locale.FRENCH);

    private ShowtimeCodec() {}

    public static ShowtimeCodec get() {
        return INSTANCE;
    }

    public void fill(Movie movie, JSONObject jsonMovieShowtime) throws ParseException {
        // Example input:
        // Séances du dimanche 1 novembre 2015 : 10:00 (film à 10:15), 14:00 (film à 14:15), 16:00 (film à 16:15), 20:00 (film à 20:15), 21:45 (film à 22:00)
        try {
            String display = jsonMovieShowtime.getString("display");

            // Split per day
            String[] showtimesDays = display.split("\r\n");

            // Find today's date in the list
            String todayFormatted = DAY_DATE_FORMAT.format(new Date());
            String todayShowtimesStr = null;
            for (String showtimesDay : showtimesDays) {
                if (showtimesDay.contains(todayFormatted)) {
                    // Found
                    todayShowtimesStr = showtimesDay;
                    break;
                }
            }
            if (todayShowtimesStr == null) {
                Log.w("Could not find today in showtime days");
                return;
            }

            // Remove first part
            todayShowtimesStr = todayShowtimesStr.substring(todayShowtimesStr.indexOf(':') + 2);

            // Split times
            String[] todayShowtimes = todayShowtimesStr.split(", ");

            // Clean up
            for (int i = 0; i < todayShowtimes.length; i++) {
                String todayShowtime = todayShowtimes[i];
                int spaceIdx = todayShowtime.indexOf(' ');
                if (spaceIdx != -1) {
                    todayShowtime = todayShowtime.substring(0, spaceIdx);
                }
                todayShowtimes[i] = todayShowtime;
            }

            // Make a set and merge it with the previous one if any
            TreeSet<String> todayShowtimesSet = new TreeSet<>(Arrays.asList(todayShowtimes));
            if (movie.todayShowtimes != null) {
                // The movie already has showtimes: merge them with the new ones
                Collections.addAll(todayShowtimesSet, movie.todayShowtimes);
            }
            movie.todayShowtimes = todayShowtimesSet.toArray(new String[todayShowtimesSet.size()]);

        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }
}
