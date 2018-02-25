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
import android.os.Bundle
import android.support.wear.widget.drawer.WearableDrawerLayout
import android.support.wear.widget.drawer.WearableDrawerView
import android.support.wear.widget.drawer.WearableNavigationDrawerView
import android.support.wearable.view.ConfirmationOverlay
import android.view.MenuItem
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
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.databinding.MainBinding
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.util.async.doAsync
import org.jraf.android.cinetoday.util.base.BaseActivity
import org.jraf.android.cinetoday.util.fragment.FragmentDelegate
import org.jraf.android.cinetoday.util.uri.setData
import org.jraf.android.util.dialog.AlertDialogListener
import org.jraf.android.util.dialog.FrameworkAlertDialogFragment
import org.jraf.android.util.handler.HandlerUtil
import org.jraf.android.util.log.Log
import org.jraf.android.util.ui.screenshape.ScreenShapeHelper
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity(), MovieListCallbacks, TheaterFavoritesCallbacks, MenuItem.OnMenuItemClickListener,
    AlertDialogListener {

    companion object {
        private const val REQUEST_ADD_THEATER = 0
        private const val DIALOG_THEATER_DELETE_CONFIRM = 0
        private const val DELAY_HIDE_ACTION_DRAWER_MS = 1000L
    }

    @Inject
    lateinit var database: AppDatabase
    @Inject
    lateinit var loadMoviesHelper: LoadMoviesHelper

    private lateinit var binding: MainBinding
    private var atLeastOneFavorite: Boolean = false
    private var peekAndHideActionDrawer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Components.application.inject(this)

        // Navigation drawer
        binding = DataBindingUtil.setContentView(this, R.layout.main)!!
        binding.navigationDrawer.setAdapter(NavigationDrawerAdapter())
        binding.navigationDrawer.addOnItemSelectedListener { position ->
            when (position) {
                0 -> showMovieListFragment()
                1 -> showTheaterFavoritesFragment()
                2 -> showPreferencesFragment()
            }
        }

        // Action drawer
        binding.actionDrawer.setOnMenuItemClickListener(this)
        binding.actionDrawer.isPeekOnScrollDownEnabled = true
        menuInflater.inflate(R.menu.main, binding.actionDrawer.menu)

        // Workaround for http://stackoverflow.com/questions/42141631
        // XXX If the screen is round, we consider the height *must* be the same as the width
        if (ScreenShapeHelper.get(this).isRound) binding.conFragment.layoutParams.height =
                ScreenShapeHelper.get(this).width

        showMovieListFragment()
        ensureFavoriteTheaters()

        // Peek navigation drawer when app starts
        binding.drawerLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.navigationDrawer.controller.peekDrawer()
                binding.drawerLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        // Peek action drawer when on the theaters section
        binding.drawerLayout.setDrawerStateCallback(object : WearableDrawerLayout.DrawerStateCallback() {
            override fun onDrawerClosed(layout: WearableDrawerLayout, drawerView: WearableDrawerView) {
                if (drawerView === binding.navigationDrawer && peekAndHideActionDrawer) {
                    binding.actionDrawer.controller.peekDrawer()
                    scheduleHideActionDrawer()
                    peekAndHideActionDrawer = false
                }
            }
        })

        // Load movies now if last update is too old
        loadingMoviesIfLastUpdateTooOld()
    }

    private fun loadingMoviesIfLastUpdateTooOld() {
        val lastUpdateDate = Components.application.mainPrefs.lastUpdateDate
        if (lastUpdateDate != null && System.currentTimeMillis() - lastUpdateDate > TimeUnit.DAYS.toMillis(1)) {
            loadMoviesHelper.startLoadMoviesIntentService()
        }
    }


    private fun scheduleHideActionDrawer() {
        HandlerUtil.getMainHandler().removeCallbacks(mHideActionDrawerRunnable)
        HandlerUtil.getMainHandler().postDelayed(mHideActionDrawerRunnable, DELAY_HIDE_ACTION_DRAWER_MS)
    }

    private val mHideActionDrawerRunnable = Runnable {
        if (binding.actionDrawer.isPeeking) {
            binding.actionDrawer.controller.closeDrawer()
        }
    }

    private inner class NavigationDrawerAdapter : WearableNavigationDrawerView.WearableNavigationDrawerAdapter() {
        private val mTexts = resources.getStringArray(R.array.main_navigationDrawer_text)
        private val mDrawables = resources.obtainTypedArray(R.array.main_navigationDrawer_drawable)

        override fun getItemText(position: Int): String {
            return mTexts[position]
        }

        override fun getItemDrawable(position: Int): Drawable {
            return mDrawables.getDrawable(position)
        }

        override fun getCount(): Int {
            return mTexts.size
        }
    }


    //--------------------------------------------------------------------------
    // region MenuItem.OnMenuItemClickListener.
    //--------------------------------------------------------------------------

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        Log.d()
        when (menuItem.itemId) {
            R.id.main_action_add_favorite -> startTheaterSearchActivity()

            R.id.main_action_directions -> openDirectionsToTheater(mTheaterFavoritesFragment.currentVisibleTheater!!.address)

            R.id.main_action_web -> openTheaterWebsite(mTheaterFavoritesFragment.currentVisibleTheater!!.name)

            R.id.main_action_delete -> confirmDeleteTheater(mTheaterFavoritesFragment.currentVisibleTheater!!.id)
        }
        binding.actionDrawer.controller.closeDrawer()
        return false
    }

    private fun startTheaterSearchActivity() {
        startActivityForResult(Intent(this, TheaterSearchActivity::class.java), REQUEST_ADD_THEATER)
    }

    private fun openDirectionsToTheater(theaterAddress: String) {
        Log.d()
        val theaterAddressEncoded = try {
            URLEncoder.encode(theaterAddress, "utf-8")
        } catch (ignored: UnsupportedEncodingException) {
        }

        val uri = Uri.parse("http://maps.google.com/maps?f=d&daddr=" + theaterAddressEncoded)
        openOnPhone(uri)
    }

    private fun openTheaterWebsite(theaterName: String) {
        Log.d()
        val theaterNameEncoded = try {
            URLEncoder.encode("cinema $theaterName", "utf-8")
        } catch (ignored: UnsupportedEncodingException) {
        }

        val uri = Uri.parse("https://www.google.com/search?sourceid=navclient&btnI=I&q=" + theaterNameEncoded)
        openOnPhone(uri)
    }

    private fun confirmDeleteTheater(theaterId: String) {
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

    private val mMovieListFragment: MovieListFragment by FragmentDelegate(R.id.conFragment, "MovieList") {
        MovieListFragment.newInstance()
    }

    private val mTheaterFavoritesFragment: TheaterFavoritesFragment by FragmentDelegate(
        R.id.conFragment,
        "TheaterFavorites"
    ) {
        TheaterFavoritesFragment.newInstance()
    }

    private val mPreferencesFragment: PreferencesFragment by FragmentDelegate(R.id.conFragment, "Preferences") {
        PreferencesFragment.newInstance()
    }

    private fun showMovieListFragment() {
        supportFragmentManager.beginTransaction()
            .hide(mTheaterFavoritesFragment)
            .hide(mPreferencesFragment)
            .show(mMovieListFragment)
            .commit()

        binding.actionDrawer.controller.closeDrawer()
        binding.actionDrawer.setIsLocked(true)
    }

    private fun showTheaterFavoritesFragment() {
        supportFragmentManager.beginTransaction()
            .hide(mMovieListFragment)
            .hide(mPreferencesFragment)
            .show(mTheaterFavoritesFragment)
            .commit()

        binding.actionDrawer.setIsLocked(false)
        peekAndHideActionDrawer = true
    }

    private fun showPreferencesFragment() {
        supportFragmentManager.beginTransaction()
            .hide(mMovieListFragment)
            .hide(mTheaterFavoritesFragment)
            .show(mPreferencesFragment)
            .commit()

        binding.actionDrawer.controller.closeDrawer()
        binding.actionDrawer.setIsLocked(true)
    }

    // endregion


    //--------------------------------------------------------------------------
    // region Callbacks.
    //--------------------------------------------------------------------------

    override fun onTheaterListScrolled() {
        scheduleHideActionDrawer()
    }

    override fun onMovieClick(movie: Movie) {
        Log.d()
        startActivity(
            Intent(this, MovieDetailsActivity::class.java)
                .setData(movie)
        )
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        when (requestCode) {
            REQUEST_ADD_THEATER -> {
                if (resultCode != Activity.RESULT_OK) {
                    if (!atLeastOneFavorite) {
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
        doAsync {
            database.theaterDao.insert(theater)
            atLeastOneFavorite = true
            loadMoviesHelper.startLoadMoviesIntentService()
        }
    }

    private fun ensureFavoriteTheaters() {
        doAsync({
            database.theaterDao.countTheaters() > 0
        }, { result ->
            atLeastOneFavorite = result
            if (!atLeastOneFavorite) startTheaterSearchActivity()
        })
    }


    //--------------------------------------------------------------------------
    // region AlertDialogListener.
    //--------------------------------------------------------------------------

    override fun onDialogClickPositive(dialogId: Int, payload: Any) {
        when (dialogId) {
            DIALOG_THEATER_DELETE_CONFIRM -> {
                val theaterId = payload as String
                doAsync {
                    // Delete theater
                    database.theaterDao.delete(theaterId)

                    // Delete movies that have no show times
                    database.movieDao.deleteWithNoShowtimes()

                    ensureFavoriteTheaters()
                }
            }
        }
    }

    override fun onDialogClickNegative(dialogId: Int, payload: Any) {}

    override fun onDialogClickListItem(dialogId: Int, index: Int, payload: Any) {}

    // endregion
}