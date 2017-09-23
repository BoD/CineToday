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

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import org.jraf.android.cinetoday.util.uri.HasId

@Entity
data class Theater(
        @PrimaryKey
        override var id: String,
        var name: String,
        var address: String,
        var pictureUri: String?
) : HasId, Parcelable {

    override fun equals(other: Any?) = (other as? Theater)?.id == id

    override fun hashCode() = id.hashCode()


    //--------------------------------------------------------------------------
    // region Parcelable implementation.
    //--------------------------------------------------------------------------

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Theater> = object : Parcelable.Creator<Theater> {
            override fun createFromParcel(source: Parcel): Theater {
                return Theater(source)
            }

            override fun newArray(size: Int): Array<Theater?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(address)
        dest.writeString(pictureUri)
    }

    private constructor(input: Parcel) : this(
            id = input.readString(),
            name = input.readString(),
            address = input.readString(),
            pictureUri = input.readString()
    )

    // endregion
}
