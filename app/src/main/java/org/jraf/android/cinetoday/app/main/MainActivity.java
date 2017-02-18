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
package org.jraf.android.cinetoday.app.main;

import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableNavigationDrawer;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesHelper;
import org.jraf.android.cinetoday.app.movie.details.MovieDetailsActivity;
import org.jraf.android.cinetoday.app.movie.list.MovieListCallbacks;
import org.jraf.android.cinetoday.app.movie.list.MovieListFragment;
import org.jraf.android.cinetoday.app.theater.favorites.TheaterFavoritesCallbacks;
import org.jraf.android.cinetoday.app.theater.favorites.TheaterFavoritesFragment;
import org.jraf.android.cinetoday.app.theater.search.TheaterSearchActivity;
import org.jraf.android.cinetoday.databinding.MainBinding;
import org.jraf.android.cinetoday.model.theater.Theater;
import org.jraf.android.cinetoday.provider.movie.MovieColumns;
import org.jraf.android.cinetoday.provider.theater.TheaterContentValues;
import org.jraf.android.cinetoday.provider.theater.TheaterSelection;
import org.jraf.android.cinetoday.util.ui.ScreenShapeHelper;
import org.jraf.android.util.log.Log;

public class MainActivity extends FragmentActivity implements MovieListCallbacks, TheaterFavoritesCallbacks, WearableActionDrawer.OnMenuItemClickListener {
    private static final int REQUEST_ADD_THEATER = 0;

    private MainBinding mBinding;
    private TheaterFavoritesFragment mTheaterFavoritesFragment;
    private MovieListFragment mMovieListFragment;
    private boolean mAtLeastOneFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main);
        mBinding.navigationDrawer.setAdapter(new NavigationDrawerAdapter());
        mBinding.actionDrawer.setOnMenuItemClickListener(this);

        ScreenShapeHelper.get().init(this, new ScreenShapeHelper.Callbacks() {
            @Override
            public void onScreenShapeAvailable(boolean isRound, int chinHeight, float safeMargin) {
                // XXX If the screen is round, we consider the height *must* be the same as the width
                // This is a workaround for http://stackoverflow.com/questions/42141631
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                mBinding.conFragment.getLayoutParams().height = metrics.widthPixels;

                showMovieListFragment();
                ensureFavoriteTheaters();
            }
        });
    }

    private class NavigationDrawerAdapter extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {
        private String[] mTexts = getResources().getStringArray(R.array.main_navigationDrawer_text);
        private TypedArray mDrawables = getResources().obtainTypedArray(R.array.main_navigationDrawer_drawable);

        @Override
        public String getItemText(int position) {
            return mTexts[position];
        }

        @Override
        public Drawable getItemDrawable(int position) {
            return mDrawables.getDrawable(position);
        }

        @Override
        public void onItemSelected(int position) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            switch (position) {
                case 0:
                    showMovieListFragment();
                    break;

                case 1:
                    showTheaterFavoritesFragment();
                    break;
            }
            transaction.commit();
        }

        @Override
        public int getCount() {
            return mTexts.length;
        }
    }


    //--------------------------------------------------------------------------
    // region WearableActionDrawer.OnMenuItemClickListener.
    //--------------------------------------------------------------------------

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.main_action_add_favorite:
                Log.d();
                startTheaterSearchActivity();
                break;
        }
        return false;
    }

    // endregion


    //--------------------------------------------------------------------------
    // region Fragments.
    //--------------------------------------------------------------------------

    private MovieListFragment getMovieListFragment() {
        if (mMovieListFragment == null) {
            mMovieListFragment =
                    (MovieListFragment) getSupportFragmentManager().findFragmentByTag(MovieListFragment.class.getName());
            if (mMovieListFragment == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.conFragment, mMovieListFragment = MovieListFragment.newInstance(), MovieListFragment.class.getName())
                        .commit();
            }
        }
        return mMovieListFragment;
    }

    private TheaterFavoritesFragment getTheaterFavoritesFragment() {
        if (mTheaterFavoritesFragment == null) {
            mTheaterFavoritesFragment = (TheaterFavoritesFragment) getSupportFragmentManager().findFragmentByTag(TheaterFavoritesFragment.class.getName());
            if (mTheaterFavoritesFragment == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.conFragment, mTheaterFavoritesFragment = TheaterFavoritesFragment.newInstance(), TheaterFavoritesFragment.class.getName())
                        .commit();
            }
        }
        return mTheaterFavoritesFragment;
    }

    private void showMovieListFragment() {
        getSupportFragmentManager().beginTransaction()
                .hide(getTheaterFavoritesFragment())
                .show(getMovieListFragment())
                .commit();
    }

    private void showTheaterFavoritesFragment() {
        getSupportFragmentManager().beginTransaction()
                .hide(getMovieListFragment())
                .show(getTheaterFavoritesFragment())
                .commit();
    }

    // endregion

    private void startTheaterSearchActivity() {startActivityForResult(new Intent(this, TheaterSearchActivity.class), REQUEST_ADD_THEATER);}


    //--------------------------------------------------------------------------
    // region Callbacks.
    //--------------------------------------------------------------------------

    @Override
    public void onAddTheaterClick() {
        Log.d();
        startTheaterSearchActivity();
    }

    @Override
    public void onTheaterClick(long theaterId) {
        Log.d();
    }

    @Override
    public void onNavigateClick() {
        Log.d();
    }

    @Override
    public void onWebSiteClick() {
        Log.d();
    }

    @Override
    public void onDeleteClick() {
        Log.d();
    }

    @Override
    public void onMovieClick(long movieId) {
        Log.d();
        Intent intent = new Intent(this, MovieDetailsActivity.class)
                .setData(Uri.withAppendedPath(MovieColumns.CONTENT_URI, String.valueOf(movieId)));
        startActivity(intent);
    }

    // endregion


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ADD_THEATER:
                if (resultCode != RESULT_OK) {
                    if (!mAtLeastOneFavorite) {
                        // There are no favorites and the user canceled? Exit the app!
                        finish();
                    }
                    break;
                }
                Theater theater = data.getExtras().getParcelable(TheaterSearchActivity.EXTRA_RESULT);
                addToFavorites(theater);
                break;
        }
    }

    private void addToFavorites(final Theater theater) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                new TheaterContentValues()
                        .putPublicId(theater.id)
                        .putName(theater.name)
                        .putAddress(theater.address)
                        .putPictureUri(theater.pictureUri).insert(MainActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAtLeastOneFavorite = true;
                LoadMoviesHelper.get().startLoadMoviesIntentService(MainActivity.this);
            }
        }.execute();
    }

    private void ensureFavoriteTheaters() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return new TheaterSelection().count(MainActivity.this) > 0;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mAtLeastOneFavorite = result;
                if (!mAtLeastOneFavorite) startTheaterSearchActivity();
            }
        }.execute();
    }
}