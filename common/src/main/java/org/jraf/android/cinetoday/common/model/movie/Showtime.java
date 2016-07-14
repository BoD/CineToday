/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import android.os.Parcel;
import android.os.Parcelable;

public class Showtime implements Parcelable, Comparable<Showtime> {
    public String time;
    public boolean is3d;

    @Override
    public String toString() {
        return "Showtime{" +
                "time='" + time + '\'' +
                ", is3d=" + is3d +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Showtime showtime = (Showtime) o;

        if (is3d != showtime.is3d) return false;
        return time != null ? time.equals(showtime.time) : showtime.time == null;

    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (is3d ? 1 : 0);
        return result;
    }

    //--------------------------------------------------------------------------
    // region Parcelable implementation.
    //--------------------------------------------------------------------------

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(time);
        dest.writeByte(is3d ? (byte) 1 : (byte) 0);
    }

    public Showtime() {}

    protected Showtime(Parcel in) {
        time = in.readString();
        is3d = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Showtime> CREATOR = new Parcelable.Creator<Showtime>() {
        @Override
        public Showtime createFromParcel(Parcel source) {return new Showtime(source);}

        @Override
        public Showtime[] newArray(int size) {return new Showtime[size];}
    };

    // endregion

    @Override
    public int compareTo(Showtime another) {
        int res = time.compareTo(another.time);
        if (res != 0) return res;
        if (is3d) {
            if (another.is3d) return 0;
            return 1;
        } else {
            if (!another.is3d) return 0;
            return -1;
        }
    }
}
