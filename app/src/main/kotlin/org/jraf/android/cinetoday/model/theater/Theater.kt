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
package org.jraf.android.cinetoday.model.theater

import android.os.Parcel
import android.os.Parcelable

class Theater() : Parcelable {
    var id: String? = null
    var name: String? = null
    var address: String? = null
    var pictureUri: String? = null

    override fun toString(): String {
        return "Theater{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", pictureUri='" + pictureUri + '\'' +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val theater = o as Theater?
        return id == theater!!.id

    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    //--------------------------------------------------------------------------
    // region Parcelable implementation.
    //--------------------------------------------------------------------------

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(address)
        dest.writeString(pictureUri)
    }

    protected constructor(`in`: Parcel) : this() {
        id = `in`.readString()
        name = `in`.readString()
        address = `in`.readString()
        pictureUri = `in`.readString()
    }

    companion object {

        val CREATOR: Parcelable.Creator<Theater> = object : Parcelable.Creator<Theater> {
            override fun createFromParcel(source: Parcel): Theater {
                return Theater(source)
            }

            override fun newArray(size: Int): Array<Theater?> {
                return arrayOfNulls(size)
            }
        }
    }

    // endregion
}
