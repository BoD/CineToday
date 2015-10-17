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
package org.jraf.android.moviestoday.mobile.model.movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import org.jraf.android.moviestoday.mobile.api.Api;
import org.jraf.android.moviestoday.mobile.model.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Movie implements Parcelable {
    public String id;
    public String localTitle;
    public String directors;
    public String actors;
    public Date releaseDate;
    public int durationMinutes;
    public String[] genres;
    public String posterUri;
    public String trailerUri;
    public String webUri;

    public Movie() {}

    public static Movie fromJson(JSONObject jsonMovie) throws ParseException {
        Movie res = new Movie();
        try {
            res.id = jsonMovie.getString("code");
            res.localTitle = jsonMovie.getString("title");

            JSONObject jsonCastingShort = jsonMovie.getJSONObject("castingShort");
            res.directors = jsonCastingShort.getString("directors");
            res.actors = jsonCastingShort.getString("actors");

            JSONObject jsonRelease = jsonMovie.getJSONObject("release");
            String releaseDateStr = jsonRelease.getString("releaseDate");
            try {
                res.releaseDate = Api.SIMPLE_DATE_FORMAT.parse(releaseDateStr);
            } catch (java.text.ParseException e) {
                throw new ParseException(e);
            }

            res.durationMinutes = jsonMovie.getInt("runtime");

            JSONArray jsonGenreArray = jsonMovie.getJSONArray("genre");
            int len = jsonGenreArray.length();
            ArrayList<String> genres = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                JSONObject jsonGenre = jsonGenreArray.getJSONObject(i);
                genres.add(jsonGenre.getString("$"));
            }
            res.genres = genres.toArray(new String[len]);

            JSONObject jsonPoster = jsonMovie.getJSONObject("poster");
            res.posterUri = jsonPoster.getString("href");

            JSONObject jsonTrailer = jsonMovie.getJSONObject("trailer");
            res.trailerUri = jsonTrailer.getString("href");

            JSONArray jsonLinkArray = jsonMovie.getJSONArray("link");
            len = jsonLinkArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonLink = jsonLinkArray.getJSONObject(i);
                res.webUri = jsonLink.getString("href");
                break;
            }

            return res;
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", localTitle='" + localTitle + '\'' +
                ", directors='" + directors + '\'' +
                ", actors='" + actors + '\'' +
                ", releaseDate=" + releaseDate +
                ", durationMinutes=" + durationMinutes +
                ", genres=" + Arrays.toString(genres) +
                ", posterUri='" + posterUri + '\'' +
                ", trailerUri='" + trailerUri + '\'' +
                ", webUri='" + webUri + '\'' +
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
        dest.writeString(this.localTitle);
        dest.writeString(this.directors);
        dest.writeString(this.actors);
        dest.writeLong(releaseDate != null ? releaseDate.getTime() : -1);
        dest.writeInt(this.durationMinutes);
        dest.writeStringArray(this.genres);
        dest.writeString(this.posterUri);
        dest.writeString(this.trailerUri);
        dest.writeString(this.webUri);
    }

    protected Movie(Parcel in) {
        this.id = in.readString();
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
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {return new Movie(source);}

        public Movie[] newArray(int size) {return new Movie[size];}
    };

    // endregion
}
