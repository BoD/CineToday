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
package org.jraf.android.cinetoday.model.theater;

import android.os.Parcel;
import android.os.Parcelable;

public class Theater implements Parcelable {
    public String id;
    public String name;
    public String address;
    public String pictureUri;

    public Theater() {}

    @Override
    public String toString() {
        return "Theater{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", pictureUri='" + pictureUri + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theater theater = (Theater) o;
        return id.equals(theater.id);

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
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(pictureUri);
    }

    protected Theater(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        pictureUri = in.readString();
    }

    public static final Creator<Theater> CREATOR = new Creator<Theater>() {
        @Override
        public Theater createFromParcel(Parcel source) {return new Theater(source);}

        @Override
        public Theater[] newArray(int size) {return new Theater[size];}
    };

    // endregion
}
