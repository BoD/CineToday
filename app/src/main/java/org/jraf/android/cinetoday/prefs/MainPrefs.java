package org.jraf.android.cinetoday.prefs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.jraf.android.prefs.SharedPreferencesWrapper;

public class MainPrefs extends SharedPreferencesWrapper {
    private static MainPrefs sInstance;

    public static MainPrefs get(Context context) {
        if (sInstance == null) {
            SharedPreferences wrapped = getWrapped(context);
            sInstance = new MainPrefs(wrapped);
        }
        return sInstance;
    }

    protected static SharedPreferences getWrapped(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public MainPrefs(SharedPreferences wrapped) {
        super(wrapped);
    }

    @SuppressLint("CommitPrefEdits")
    public MainEditorWrapper edit() {
        return new MainEditorWrapper(super.edit());
    }


    //================================================================================
    // region LastUpdateDate
    //================================================================================


    /**
     * Last time an update was successfully called.
     */
    @Nullable
    public Long getLastUpdateDate() {
        if (!contains("lastUpdateDate")) return null;
        return getLong("lastUpdateDate", 0L);
    }

    /**
     * Last time an update was successfully called.
     */
    public boolean containsLastUpdateDate() {
        return contains("lastUpdateDate");
    }

    /**
     * Last time an update was successfully called.
     */
    public MainPrefs putLastUpdateDate(Long lastUpdateDate) {
        edit().putLastUpdateDate(lastUpdateDate).apply();
        return this;
    }

    /**
     * Last time an update was successfully called.
     */
    public MainPrefs removeLastUpdateDate() {
        edit().remove("lastUpdateDate").apply();
        return this;
    }

    // endregion


    //================================================================================
    // region ShowNewReleasesNotification
    //================================================================================

    /**
     * Show a notification on new movie release day.
     */
    public Boolean getShowNewReleasesNotification() {
        return isShowNewReleasesNotification();
    }

    /**
     * Show a notification on new movie release day.
     */
    public Boolean isShowNewReleasesNotification() {
        if (!contains("showNewReleasesNotification")) return true;
        return getBoolean("showNewReleasesNotification", false);
    }

    /**
     * Show a notification on new movie release day.
     */
    public boolean containsShowNewReleasesNotification() {
        return contains("showNewReleasesNotification");
    }

    /**
     * Show a notification on new movie release day.
     */
    public MainPrefs putShowNewReleasesNotification(Boolean showNewReleasesNotification) {
        edit().putShowNewReleasesNotification(showNewReleasesNotification).apply();
        return this;
    }

    /**
     * Show a notification on new movie release day.
     */
    public MainPrefs removeShowNewReleasesNotification() {
        edit().remove("showNewReleasesNotification").apply();
        return this;
    }

    // endregion


    //================================================================================
    // region NotifiedMovieIds
    //================================================================================


    /**
     * List of movies that have been used in 'new release' notifications.
     */
    public Set<String> getNotifiedMovieIds() {
        if (!contains("notifiedMovieIds")) return new HashSet<String>(Arrays.asList(""));
        return getStringSet("notifiedMovieIds", null);
    }

    /**
     * List of movies that have been used in 'new release' notifications.
     */
    public boolean containsNotifiedMovieIds() {
        return contains("notifiedMovieIds");
    }

    /**
     * List of movies that have been used in 'new release' notifications.
     */
    public MainPrefs putNotifiedMovieIds(Set<String> notifiedMovieIds) {
        edit().putNotifiedMovieIds(notifiedMovieIds).apply();
        return this;
    }

    /**
     * List of movies that have been used in 'new release' notifications.
     */
    public MainPrefs removeNotifiedMovieIds() {
        edit().remove("notifiedMovieIds").apply();
        return this;
    }

    // endregion
}