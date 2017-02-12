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
package org.jraf.android.cinetoday.model.movie;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;

import android.support.annotation.Nullable;

public class Movie {
    public String id;
    public String originalTitle;
    public String localTitle;
    public @Nullable String directors;
    public @Nullable String actors;
    public @Nullable Date releaseDate;
    public @Nullable Integer durationSeconds;
    public String[] genres;
    public @Nullable String posterUri;
    public @Nullable String trailerUri;
    public String webUri;
    public String synopsis;

    /**
     * Keys: id of the theater<br/>
     * Values: showtimes for today at a given theater.
     */
    public SortedMap<String, List<Showtime>> todayShowtimes;

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", localTitle='" + localTitle + '\'' +
                ", directors='" + directors + '\'' +
                ", actors='" + actors + '\'' +
                ", releaseDate=" + releaseDate +
                ", durationSeconds=" + durationSeconds +
                ", genres=" + Arrays.toString(genres) +
                ", posterUri='" + posterUri + '\'' +
                ", trailerUri='" + trailerUri + '\'' +
                ", webUri='" + webUri + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", todayShowtimes=" + todayShowtimes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id.equals(movie.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Compares in reverse release date order.
     */
    public static final Comparator<Movie> COMPARATOR = new Comparator<Movie>() {
        @Override
        public int compare(Movie lhs, Movie rhs) {
            if (rhs.releaseDate != null && lhs.releaseDate != null) {
                int res = rhs.releaseDate.compareTo(lhs.releaseDate);
                if (res != 0) return res;
            }
            return lhs.id.compareTo(rhs.id);
        }
    };
}
