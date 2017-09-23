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
package org.jraf.android.cinetoday.app.preferences

import android.os.Bundle
import android.preference.PreferenceFragment
import android.text.format.DateUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.jraf.android.cinetoday.BuildConfig
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesHelper
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesListenerHelper
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.prefs.MainPrefs
import org.jraf.android.util.about.AboutActivityIntentBuilder
import javax.inject.Inject

class PreferencesFragment : PreferenceFragment() {
    companion object {
        fun newInstance(): PreferencesFragment {
            return PreferencesFragment()
        }
    }

    @Inject lateinit var mLoadMoviesHelper: LoadMoviesHelper
    @Inject lateinit var mMainPrefs: MainPrefs
    @Inject lateinit var mLoadMoviesListenerHelper: LoadMoviesListenerHelper

    private var mLoadMoviesStarted: Boolean = false
    private lateinit var mProgressSubscription: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Components.application.inject(this)
        addPreferencesFromResource(R.xml.preferences)

        // Refresh
        findPreference("refresh").setOnPreferenceClickListener { _ ->
            if (!mLoadMoviesStarted) mLoadMoviesHelper.startLoadMoviesIntentService()
            true
        }
        setLastUpdateDateSummary()

        // About
        findPreference("about").setOnPreferenceClickListener { _ ->
            val builder = AboutActivityIntentBuilder()
            builder.setAppName(getString(R.string.app_name))
            builder.setBuildDate(BuildConfig.BUILD_DATE)
            builder.setGitSha1(BuildConfig.GIT_SHA1)
            builder.setAuthorCopyright(getString(R.string.about_authorCopyright))
            builder.setLicense(getString(R.string.about_License))
            builder.setShareTextSubject(getString(R.string.about_shareText_subject))
            builder.setShareTextBody(getString(R.string.about_shareText_body))
            builder.setBackgroundResId(R.drawable.about_bg)
            builder.addLink(getString(R.string.about_email_uri), getString(R.string.about_email_text))
            builder.addLink(getString(R.string.about_web_uri), getString(R.string.about_web_text))
            builder.addLink(getString(R.string.about_artwork_uri), getString(R.string.about_artwork_text))
            builder.addLink(getString(R.string.about_sources_uri), getString(R.string.about_sources_text))
            startActivity(builder.build(context))
            true
        }

        mProgressSubscription = mLoadMoviesListenerHelper.progressInfo.observeOn(AndroidSchedulers.mainThread()).subscribe {
            when (it) {
                is LoadMoviesListenerHelper.ProgressInfo.Idle -> {
                    mLoadMoviesStarted = false
                    setLastUpdateDateSummary()
                }

                is LoadMoviesListenerHelper.ProgressInfo.PreLoading -> {
                    mLoadMoviesStarted = true
                    val refreshPref = findPreference("refresh")
                    refreshPref.isEnabled = false
                }

                is LoadMoviesListenerHelper.ProgressInfo.Loading -> {
                    mLoadMoviesStarted = true
                    val refreshPref = findPreference("refresh")
                    refreshPref.isEnabled = false
                    refreshPref.summary = getString(R.string.preference_refresh_summary_ongoing, it.currentMovieIndex, it.totalMovies)
                }
            }
        }
    }

    override fun onDestroy() {
        mProgressSubscription.dispose()
        super.onDestroy()
    }

    private fun setLastUpdateDateSummary() {
        val lastUpdateDate = mMainPrefs.lastUpdateDate
        val refreshPref = findPreference("refresh")
        if (lastUpdateDate == null) {
            refreshPref.setSummary(R.string.preference_refresh_summary_none)
        } else {
            val lastUpdateDateStr = DateUtils.formatDateTime(context, lastUpdateDate, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME)
            refreshPref.summary = getString(R.string.preference_refresh_summary_date, lastUpdateDateStr)
        }
        refreshPref.isEnabled = true
    }
}
