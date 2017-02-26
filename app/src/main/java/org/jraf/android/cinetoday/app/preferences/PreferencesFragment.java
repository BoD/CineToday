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
package org.jraf.android.cinetoday.app.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.jraf.android.cinetoday.BuildConfig;
import org.jraf.android.cinetoday.R;
import org.jraf.android.util.about.AboutActivityIntentBuilder;

public class PreferencesFragment extends PreferenceFragment {

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AboutActivityIntentBuilder builder = new AboutActivityIntentBuilder();
                builder.setAppName(getString(R.string.app_name));
                builder.setBuildDate(BuildConfig.BUILD_DATE);
                builder.setGitSha1(BuildConfig.GIT_SHA1);
                builder.setAuthorCopyright(getString(R.string.about_authorCopyright));
                builder.setLicense(getString(R.string.about_License));
                builder.setShareTextSubject(getString(R.string.about_shareText_subject));
                builder.setShareTextBody(getString(R.string.about_shareText_body));
                builder.setBackgroundResId(R.drawable.about_bg);
                builder.addLink(getString(R.string.about_email_uri), getString(R.string.about_email_text));
                builder.addLink(getString(R.string.about_web_uri), getString(R.string.about_web_text));
                builder.addLink(getString(R.string.about_artwork_uri), getString(R.string.about_artwork_text));
                builder.addLink(getString(R.string.about_sources_uri), getString(R.string.about_sources_text));
                startActivity(builder.build(getContext()));
                return true;
            }
        });
    }
}
