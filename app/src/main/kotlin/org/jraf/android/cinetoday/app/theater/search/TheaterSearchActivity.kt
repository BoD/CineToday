/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.app.theater.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.TheaterSearchBinding
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.util.base.BaseActivity
import org.jraf.android.util.log.Log

class TheaterSearchActivity : BaseActivity(), TheaterSearchCallbacks {

    companion object {
        val EXTRA_RESULT = "${TheaterSearchActivity::class.java.name}.EXTRA_RESULT"
    }

    private var binding: TheaterSearchBinding? = null
    private val handler = Handler()
    private var query: String? = null
    private val theaterSearchFragment: TheaterSearchFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fraTheaterSearch) as TheaterSearchFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.theater_search)
    }

    override fun onTheaterClicked(theater: Theater) {
        Log.d("theater=%s", theater)
        val result = Intent()
        result.putExtra(EXTRA_RESULT, theater)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onSearch(query: String) {
        this.query = query
        handler.removeCallbacks(queryRunnable)
        handler.postDelayed(queryRunnable, 500)
    }

    private val queryRunnable = Runnable {
        val query = query?.trim()
        if (query != null) theaterSearchFragment.search(query)
    }

    override fun onDestroy() {
        handler.removeCallbacks(queryRunnable)
        super.onDestroy()
    }
}