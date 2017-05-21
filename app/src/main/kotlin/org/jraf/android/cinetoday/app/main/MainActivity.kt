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
package org.jraf.android.cinetoday.app.main

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.wearable.view.ConfirmationOverlay
import android.support.wearable.view.drawer.WearableActionDrawer
import android.support.wearable.view.drawer.WearableDrawerLayout
import android.support.wearable.view.drawer.WearableDrawerView
import android.support.wearable.view.drawer.WearableNavigationDrawer
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import com.google.android.wearable.intent.RemoteIntent
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesHelper
import org.jraf.android.cinetoday.app.movie.details.MovieDetailsActivity
import org.jraf.android.cinetoday.app.movie.list.MovieListCallbacks
import org.jraf.android.cinetoday.app.movie.list.MovieListFragment
import org.jraf.android.cinetoday.app.preferences.PreferencesFragment
import org.jraf.android.cinetoday.app.theater.favorites.TheaterFavoritesCallbacks
import org.jraf.android.cinetoday.app.theater.favorites.TheaterFavoritesFragment
import org.jraf.android.cinetoday.app.theater.search.TheaterSearchActivity
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.databinding.MainBinding
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.provider.movie.MovieColumns
import org.jraf.android.cinetoday.provider.movie.MovieSelection
import org.jraf.android.cinetoday.provider.showtime.ShowtimeColumns
import org.jraf.android.cinetoday.provider.theater.TheaterContentValues
import org.jraf.android.cinetoday.provider.theater.TheaterSelection
import org.jraf.android.util.dialog.AlertDialogListener
import org.jraf.android.util.dialog.FrameworkAlertDialogFragment
import org.jraf.android.util.handler.HandlerUtil
import org.jraf.android.util.log.Log
import org.jraf.android.util.ui.screenshape.ScreenShapeHelper
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import javax.inject.Inject

class MainActivity : Activity(), MovieListCallbacks, TheaterFavoritesCallbacks, WearableActionDrawer.OnMenuItemClickListener, AlertDialogListener {

    private var mBinding: MainBinding? = null
    private var mAtLeastOneFavorite: Boolean = false
    private var mShouldClosePeekingActionDrawer: Boolean = false

    @Inject lateinit var mLoadMoviesHelper: LoadMoviesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Components.application.inject(this)

        mBinding = DataBindingUtil.setContentView<MainBinding>(this, R.layout.main)
        mBinding!!.navigationDrawer.setAdapter(NavigationDrawerAdapter())
        mBinding!!.actionDrawer.setOnMenuItemClickListener(this)
        mBinding!!.actionDrawer.setShouldPeekOnScrollDown(true)

        // Workaround for http://stackoverflow.com/questions/42141631
        // XXX If the screen is round, we consider the height *must* be the same as the width
        if (ScreenShapeHelper.get(this).isRound) mBinding!!.conFragment.layoutParams.height = ScreenShapeHelper.get(this).width

        showMovieListFragment()
        ensureFavoriteTheaters()

        menuInflater.inflate(R.menu.main, mBinding!!.actionDrawer.menu)

        mBinding!!.drawerLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mBinding!!.drawerLayout.peekDrawer(Gravity.TOP)
                mBinding!!.drawerLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        mBinding!!.drawerLayout.setDrawerStateCallback(object : WearableDrawerLayout.DrawerStateCallback() {
            override fun onDrawerOpened(view: View) {}

            override fun onDrawerClosed(view: View) {
                if (view === mBinding!!.navigationDrawer && mTheaterFavoritesFragment.isVisible && mShouldClosePeekingActionDrawer) {
                    mBinding!!.drawerLayout.peekDrawer(Gravity.BOTTOM)
                    HandlerUtil.getMainHandler().postDelayed(mHideActionDrawerRunnable, DELAY_HIDE_PEEKING_ACTION_DRAWER_MS.toLong())
                    mShouldClosePeekingActionDrawer = false
                }
            }

            override fun onDrawerStateChanged(@WearableDrawerView.DrawerState i: Int) {}
        })
    }

    private val mHideActionDrawerRunnable = Runnable {
        if (mBinding!!.actionDrawer.isPeeking) {
            mBinding!!.drawerLayout.closeDrawer(Gravity.BOTTOM)
        }
    }

    private inner class NavigationDrawerAdapter : WearableNavigationDrawer.WearableNavigationDrawerAdapter() {
        private val mTexts = resources.getStringArray(R.array.main_navigationDrawer_text)
        private val mDrawables = resources.obtainTypedArray(R.array.main_navigationDrawer_drawable)

        override fun getItemText(position: Int): String {
            return mTexts[position]
        }

        override fun getItemDrawable(position: Int): Drawable {
            return mDrawables.getDrawable(position)
        }

        override fun onItemSelected(position: Int) {
            val transaction = fragmentManager.beginTransaction()
            when (position) {
                0 -> showMovieListFragment()

                1 -> showTheaterFavoritesFragment()

                2 -> showPreferencesFragment()
            }
            transaction.commit()
        }

        override fun getCount(): Int {
            return mTexts.size
        }
    }


    //--------------------------------------------------------------------------
    // region WearableActionDrawer.OnMenuItemClickListener.
    //--------------------------------------------------------------------------

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        Log.d()
        when (menuItem.itemId) {
            R.id.main_action_add_favorite -> startTheaterSearchActivity()

            R.id.main_action_directions -> openDirectionsToTheater(mTheaterFavoritesFragment.currentVisibleTheater!!.address)

            R.id.main_action_web -> openTheaterWebsite(mTheaterFavoritesFragment.currentVisibleTheater!!.name)

            R.id.main_action_delete -> confirmDeleteTheater(mTheaterFavoritesFragment.currentVisibleTheater!!.id)
        }
        mBinding!!.actionDrawer.closeDrawer()
        return false
    }

    private fun openDirectionsToTheater(theaterAddress: String) {
        var theaterAddress = theaterAddress
        Log.d()
        try {
            theaterAddress = URLEncoder.encode(theaterAddress, "utf-8")
        } catch (ignored: UnsupportedEncodingException) {
        }

        val uri = Uri.parse("http://maps.google.com/maps?f=d&daddr=" + theaterAddress)
        openOnPhone(uri)
    }

    fun openTheaterWebsite(theaterName: String) {
        var theaterName = theaterName
        Log.d()
        theaterName = "cinema " + theaterName
        try {
            theaterName = URLEncoder.encode(theaterName, "utf-8")
        } catch (ignored: UnsupportedEncodingException) {
        }

        val uri = Uri.parse("https://www.google.com/search?sourceid=navclient&btnI=I&q=" + theaterName)
        openOnPhone(uri)
    }

    fun confirmDeleteTheater(theaterId: Long) {
        Log.d()
        FrameworkAlertDialogFragment.newInstance(DIALOG_THEATER_DELETE_CONFIRM)
                .message(R.string.main_theater_delete_confirm_message)
                .positiveButton(R.string.main_action_delete)
                .negativeButton(R.string.common_cancel)
                .payload(theaterId)
                .show(this)
    }

    // endregion


    //--------------------------------------------------------------------------
    // region Fragments.
    //--------------------------------------------------------------------------

    private val mMovieListFragment: MovieListFragment by lazy {
        val current = fragmentManager.findFragmentByTag(MovieListFragment::class.java.name) as MovieListFragment?
        val res: MovieListFragment
        if (current == null) {
            res = MovieListFragment.newInstance()
            fragmentManager.beginTransaction()
                    .add(R.id.conFragment, res, MovieListFragment::class.java.name)
                    .commit()
        } else {
            res = current
        }
        res
    }

    private val mTheaterFavoritesFragment: TheaterFavoritesFragment by lazy {
        val current = fragmentManager.findFragmentByTag(TheaterFavoritesFragment::class.java.name) as TheaterFavoritesFragment?
        val res: TheaterFavoritesFragment
        if (current == null) {
            res = TheaterFavoritesFragment.newInstance()
            fragmentManager.beginTransaction()
                    .add(R.id.conFragment, res, TheaterFavoritesFragment::class.java.name)
                    .commit()
        } else {
            res = current
        }
        res
    }

    private val mPreferencesFragment: PreferencesFragment by lazy {
        val current = fragmentManager.findFragmentByTag(PreferencesFragment::class.java.name) as PreferencesFragment?
        val res: PreferencesFragment
        if (current == null) {
            res = PreferencesFragment.newInstance()
            fragmentManager.beginTransaction()
                    .add(R.id.conFragment, res, PreferencesFragment::class.java.name)
                    .commit()
        } else {
            res = current
        }
        res
    }

    private fun showMovieListFragment() {
        fragmentManager.beginTransaction()
                .hide(mTheaterFavoritesFragment)
                .hide(mPreferencesFragment)
                .show(mMovieListFragment)
                .commit()

        mBinding!!.actionDrawer.lockDrawerClosed()
    }

    private fun showTheaterFavoritesFragment() {
        fragmentManager.beginTransaction()
                .hide(mMovieListFragment)
                .hide(mPreferencesFragment)
                .show(mTheaterFavoritesFragment)
                .commit()

        mBinding!!.actionDrawer.unlockDrawer()
        mShouldClosePeekingActionDrawer = true
    }

    private fun showPreferencesFragment() {
        fragmentManager.beginTransaction()
                .hide(mMovieListFragment)
                .hide(mTheaterFavoritesFragment)
                .show(mPreferencesFragment)
                .commit()

        mBinding!!.actionDrawer.lockDrawerClosed()
    }

    // endregion

    private fun startTheaterSearchActivity() {
        startActivityForResult(Intent(this, TheaterSearchActivity::class.java), REQUEST_ADD_THEATER)
    }


    //--------------------------------------------------------------------------
    // region Callbacks.
    //--------------------------------------------------------------------------

    override fun onTheaterListScrolled() {
        HandlerUtil.getMainHandler().removeCallbacks(mHideActionDrawerRunnable)
        HandlerUtil.getMainHandler().postDelayed(mHideActionDrawerRunnable, DELAY_HIDE_PEEKING_ACTION_DRAWER_MS.toLong())
    }

    override fun onMovieClick(movieId: Long) {
        Log.d()
        val intent = Intent(this, MovieDetailsActivity::class.java)
                .setData(Uri.withAppendedPath(MovieColumns.CONTENT_URI, movieId.toString()))
        startActivity(intent)
    }

    // endregion

    private fun openOnPhone(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        RemoteIntent.startRemoteActivity(this, intent, null)

        // 'Open on phone' confirmation overlay
        ConfirmationOverlay()
                .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                .setMessage(getString(R.string.main_confirmation_openedOnPhone))
                .showOn(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ADD_THEATER -> {
                if (resultCode != Activity.RESULT_OK) {
                    if (!mAtLeastOneFavorite) {
                        // There are no favorites and the user canceled? Exit the app!
                        finish()
                    }
                    return
                }
                val theater = data.extras.getParcelable<Theater>(TheaterSearchActivity.EXTRA_RESULT)
                addToFavorites(theater)
            }
        }
    }

    private fun addToFavorites(theater: Theater) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                TheaterContentValues()
                        .putPublicId(theater.id!!)
                        .putName(theater.name!!)
                        .putAddress(theater.address!!)
                        .putPictureUri(theater.pictureUri).insert(this@MainActivity)
                return null
            }

            override fun onPostExecute(aVoid: Void) {
                mAtLeastOneFavorite = true
                mLoadMoviesHelper.startLoadMoviesIntentService()
            }
        }.execute()
    }

    private fun ensureFavoriteTheaters() {
        object : AsyncTask<Void, Void, Boolean>() {
            override fun doInBackground(vararg params: Void): Boolean? {
                return TheaterSelection().count(this@MainActivity) > 0
            }

            override fun onPostExecute(result: Boolean?) {
                mAtLeastOneFavorite = result!!
                if (!mAtLeastOneFavorite) startTheaterSearchActivity()
            }
        }.execute()
    }


    //--------------------------------------------------------------------------
    // region AlertDialogListener.
    //--------------------------------------------------------------------------

    override fun onDialogClickPositive(dialogId: Int, payload: Any) {
        when (dialogId) {
            DIALOG_THEATER_DELETE_CONFIRM -> {
                val theaterId = payload as Long
                object : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg params: Void): Void? {
                        TheaterSelection().id(theaterId).delete(this@MainActivity)

                        // Delete movies that have no show times
                        val movieSelection = MovieSelection()
                        movieSelection.addRaw("(select "
                                + " count(" + ShowtimeColumns.TABLE_NAME + "." + ShowtimeColumns._ID + ")"
                                + " from " + ShowtimeColumns.TABLE_NAME
                                + " where " + ShowtimeColumns.TABLE_NAME + "." + ShowtimeColumns.MOVIE_ID
                                + " = " + MovieColumns.TABLE_NAME + "." + MovieColumns._ID
                                + " ) = 0")
                        movieSelection.delete(this@MainActivity)
                        return null
                    }

                    override fun onPostExecute(aVoid: Void) {
                        ensureFavoriteTheaters()
                    }
                }.execute()
            }
        }
    }

    override fun onDialogClickNegative(dialogId: Int, payload: Any) {}

    override fun onDialogClickListItem(dialogId: Int, index: Int, payload: Any) {}

    companion object {
        private val REQUEST_ADD_THEATER = 0
        private val DIALOG_THEATER_DELETE_CONFIRM = 0
        private val DELAY_HIDE_PEEKING_ACTION_DRAWER_MS = 2500
    }


    // endregion
}