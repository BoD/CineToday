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
package org.jraf.android.cinetoday.app.preferences

import android.os.Bundle
import android.text.format.DateUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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

class PreferencesFragment : PreferenceFragmentCompat() {
    companion object {
        fun newInstance(): PreferencesFragment {
            return PreferencesFragment()
        }
    }

    @Inject
    lateinit var loadMoviesHelper: LoadMoviesHelper
    @Inject
    lateinit var mainPrefs: MainPrefs
    @Inject
    lateinit var loadMoviesListenerHelper: LoadMoviesListenerHelper

    private var loadMoviesStarted: Boolean = false
    private lateinit var progressSubscription: Disposable

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Components.application.inject(this)
        addPreferencesFromResource(R.xml.preferences)

        // Refresh
        findPreference<Preference>("refresh")!!.setOnPreferenceClickListener {
            if (!loadMoviesStarted) loadMoviesHelper.startLoadMoviesIntentService()
            true
        }
        setLastUpdateDateSummary()

        // About
        findPreference<Preference>("about")!!.setOnPreferenceClickListener {
            val builder = AboutActivityIntentBuilder()
                .setAppName(getString(R.string.app_name))
                .setBuildDate(BuildConfig.BUILD_DATE)
                .setGitSha1(BuildConfig.GIT_SHA1)
                .setAuthorCopyright(getString(R.string.about_authorCopyright))
                .setLicense(getString(R.string.about_License))
                .setShareTextSubject(getString(R.string.about_shareText_subject))
                .setShareTextBody(getString(R.string.about_shareText_body))
                .setBackgroundResId(R.drawable.about_bg)
                .addLink(getString(R.string.about_email_uri), getString(R.string.about_email_text))
                .addLink(getString(R.string.about_web_uri), getString(R.string.about_web_text))
                .addLink(getString(R.string.about_artwork_uri), getString(R.string.about_artwork_text))
                .addLink(getString(R.string.about_sources_uri), getString(R.string.about_sources_text))
            startActivity(builder.build(context))
            true
        }

        progressSubscription = loadMoviesListenerHelper.progressInfo
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it) {
                    is LoadMoviesListenerHelper.ProgressInfo.Idle -> {
                        loadMoviesStarted = false
                        setLastUpdateDateSummary()
                    }

                    is LoadMoviesListenerHelper.ProgressInfo.PreLoading -> {
                        loadMoviesStarted = true
                        findPreference<Preference>("refresh")!!.isEnabled = false
                    }

                    is LoadMoviesListenerHelper.ProgressInfo.Loading -> {
                        loadMoviesStarted = true
                        with(findPreference<Preference>("refresh")!!) {
                            isEnabled = false
                            summary = getString(
                                R.string.preference_refresh_summary_ongoing,
                                it.currentMovieIndex,
                                it.totalMovies
                            )
                        }
                    }
                }
            }
    }

    override fun onDestroy() {
        progressSubscription.dispose()
        super.onDestroy()
    }

    private fun setLastUpdateDateSummary() {
        val lastUpdateDate = mainPrefs.lastUpdateDate
        val refreshPref = findPreference<Preference>("refresh")!!
        if (lastUpdateDate == null) {
            refreshPref.setSummary(R.string.preference_refresh_summary_none)
        } else {
            val lastUpdateDateStr = DateUtils.formatDateTime(
                context,
                lastUpdateDate,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
            )
            refreshPref.summary = getString(R.string.preference_refresh_summary_date, lastUpdateDateStr)
        }
        refreshPref.isEnabled = true
    }
}
