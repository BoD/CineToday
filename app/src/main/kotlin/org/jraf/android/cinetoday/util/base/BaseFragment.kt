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
package org.jraf.android.cinetoday.util.base

import android.app.Fragment
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.content.Context

abstract class BaseFragment<C> : Fragment(), LifecycleRegistryOwner {

    private var mCallbacks: C? = null

    private var lifecycleRegistry = LifecycleRegistry(this)

    protected val callbacks get() = mCallbacks!!

    @Suppress("UNCHECKED_CAST")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null) {
            // This Fragment is nested in another Fragment
            mCallbacks = parentFragment as C
        } else {
            // This Fragment is attached to an Activity
            mCallbacks = context as C
        }
    }

    override fun onDetach() {
        mCallbacks = null
        super.onDetach()
    }

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }
}