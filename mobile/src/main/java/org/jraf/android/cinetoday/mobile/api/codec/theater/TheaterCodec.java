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
package org.jraf.android.cinetoday.mobile.api.codec.theater;

import org.jraf.android.cinetoday.common.model.ParseException;
import org.jraf.android.cinetoday.common.model.theater.Theater;
import org.jraf.android.cinetoday.mobile.api.codec.Codec;
import org.json.JSONException;
import org.json.JSONObject;

public class TheaterCodec implements Codec<Theater> {
    private static final TheaterCodec INSTANCE = new TheaterCodec();

    private TheaterCodec() {}

    public static TheaterCodec get() {
        return INSTANCE;
    }

    public void fill(Theater theater, JSONObject jsonTheater) throws ParseException {
        try {
            theater.id = jsonTheater.getString("code");
            theater.name = jsonTheater.getString("name");
            theater.address = jsonTheater.getString("address") + "\n" + jsonTheater.getString("postalCode") + " " + jsonTheater.getString("city");

            JSONObject jsonPoster = jsonTheater.optJSONObject("poster");
            if (jsonPoster != null) {
                theater.pictureUri = jsonPoster.getString("href");
            }
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }
}
