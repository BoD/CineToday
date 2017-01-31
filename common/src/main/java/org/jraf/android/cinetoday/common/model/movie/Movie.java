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
package org.jraf.android.cinetoday.common.model.movie;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class Movie implements Parcelable {
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
     * Keys: name of the theater, prefixed by its index, e.g. "0/MK2 Bibliothèque" ({@code String}).<br/>
     * Values: showtimes for today at a given theater ({@code ArrayList<Showtime>}).
     */
    public Bundle todayShowtimes;

    public Movie() {}

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


    //--------------------------------------------------------------------------
    // region Parcelable implementation.
    //--------------------------------------------------------------------------

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(originalTitle);
        dest.writeString(localTitle);
        dest.writeString(directors);
        dest.writeString(actors);
        dest.writeLong(releaseDate != null ? releaseDate.getTime() : -1);
        dest.writeInt(durationSeconds != null ? durationSeconds : -1);
        dest.writeStringArray(genres);
        dest.writeString(posterUri);
        dest.writeString(trailerUri);
        dest.writeString(webUri);
        dest.writeString(synopsis);
        dest.writeBundle(todayShowtimes);
    }

    protected Movie(Parcel in) {
        id = in.readString();
        originalTitle = in.readString();
        localTitle = in.readString();
        directors = in.readString();
        actors = in.readString();
        long tmpReleaseDate = in.readLong();
        releaseDate = tmpReleaseDate == -1 ? null : new Date(tmpReleaseDate);
        int tmpDurationSeconds = in.readInt();
        durationSeconds = tmpDurationSeconds == -1 ? null : tmpDurationSeconds;
        genres = in.createStringArray();
        posterUri = in.readString();
        trailerUri = in.readString();
        webUri = in.readString();
        synopsis = in.readString();
        todayShowtimes = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {return new Movie(source);}

        @Override
        public Movie[] newArray(int size) {return new Movie[size];}
    };

    // endregion


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
