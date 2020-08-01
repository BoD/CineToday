/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.widget

import android.os.Handler
import android.support.wearable.input.RotaryEncoder
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class RotaryPagerSnapHelper : PagerSnapHelper() {
    private var snapHandler = Handler()
    private var snapRunnable: Runnable? = null

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)

        // Handle snap with the rotary input
        recyclerView?.requestFocus()
        recyclerView?.setOnGenericMotionListener(View.OnGenericMotionListener { v, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_SCROLL && RotaryEncoder.isFromRotaryEncoder(motionEvent)) {
                val delta = -RotaryEncoder.getRotaryAxisValue(motionEvent) * RotaryEncoder.getScaledScrollFactor(v.context) * 1.5f
                v.scrollBy(0, delta.roundToInt())

                // Snap
                snapRunnable?.let { snapHandler.removeCallbacks(it) }
                snapRunnable = Runnable {
                    val snapView = findSnapView(recyclerView.layoutManager)
                    if (snapView != null) {
                        val snapDistance = calculateDistanceToFinalSnap(recyclerView.layoutManager!!, snapView)!!
                        if (snapDistance[1] != 0) {
                            recyclerView.smoothScrollBy(0, snapDistance[1])
                        }
                    }
                }
                snapRunnable?.let { snapHandler.postDelayed(it, 100) }

                return@OnGenericMotionListener true
            }

            false
        })
    }
}