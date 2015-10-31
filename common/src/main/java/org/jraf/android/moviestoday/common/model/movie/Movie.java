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
package org.jraf.android.moviestoday.common.model.movie;

import java.util.Arrays;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    public String id;
    public String originalTitle;
    public String localTitle;
    public String directors;
    public String actors;
    public Date releaseDate;
    public int durationMinutes;
    public String[] genres;
    public String posterUri;
    public String trailerUri;
    public String webUri;
    public String synopsis;
    public String[] todayShowtimes;

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
                ", durationMinutes=" + durationMinutes +
                ", genres=" + Arrays.toString(genres) +
                ", posterUri='" + posterUri + '\'' +
                ", trailerUri='" + trailerUri + '\'' +
                ", webUri='" + webUri + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", todayShowtimes=" + Arrays.toString(todayShowtimes) +
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

    /*
     * Parcelable implementation.
     */
    // region

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.originalTitle);
        dest.writeString(this.localTitle);
        dest.writeString(this.directors);
        dest.writeString(this.actors);
        dest.writeLong(releaseDate != null ? releaseDate.getTime() : -1);
        dest.writeInt(this.durationMinutes);
        dest.writeStringArray(this.genres);
        dest.writeString(this.posterUri);
        dest.writeString(this.trailerUri);
        dest.writeString(this.webUri);
        dest.writeString(this.synopsis);
        dest.writeStringArray(this.todayShowtimes);
    }

    protected Movie(Parcel in) {
        this.id = in.readString();
        this.originalTitle = in.readString();
        this.localTitle = in.readString();
        this.directors = in.readString();
        this.actors = in.readString();
        long tmpReleaseDate = in.readLong();
        this.releaseDate = tmpReleaseDate == -1 ? null : new Date(tmpReleaseDate);
        this.durationMinutes = in.readInt();
        this.genres = in.createStringArray();
        this.posterUri = in.readString();
        this.trailerUri = in.readString();
        this.webUri = in.readString();
        this.synopsis = in.readString();
        this.todayShowtimes = in.createStringArray();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {return new Movie(source);}

        public Movie[] newArray(int size) {return new Movie[size];}
    };

    // endregion
}
