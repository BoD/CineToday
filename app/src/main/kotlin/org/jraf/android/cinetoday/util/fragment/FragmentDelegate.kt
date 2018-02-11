/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.util.fragment

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import kotlin.reflect.KProperty

class FragmentDelegate<out F : Fragment>(
    @IdRes private val containerResId: Int,
    private val tag: String,
    private val provideFragmentInstance: () -> F
) {
    private var cached: F? = null

    operator fun getValue(thisRef: FragmentActivity, property: KProperty<*>): F {
        if (cached == null) {
            @Suppress("UNCHECKED_CAST")
            cached = thisRef.supportFragmentManager.findFragmentByTag(tag) as F?
            if (cached == null) {
                cached = provideFragmentInstance()
                thisRef.supportFragmentManager.beginTransaction()
                    .add(containerResId, cached, tag)
                    .commit()
            }
        }
        return cached!!
    }
}
