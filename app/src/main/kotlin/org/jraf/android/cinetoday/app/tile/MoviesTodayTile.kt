/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.app.tile

import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.wear.tiles.ActionBuilders.AndroidActivity
import androidx.wear.tiles.ActionBuilders.LaunchAction
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.LayoutElementBuilders.Column
import androidx.wear.tiles.LayoutElementBuilders.FontStyles
import androidx.wear.tiles.LayoutElementBuilders.Layout
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement.Builder
import androidx.wear.tiles.LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END
import androidx.wear.tiles.LayoutElementBuilders.Text
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.ModifiersBuilders.Modifiers
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders.Resources
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileProviderService
import androidx.wear.tiles.TimelineBuilders.Timeline
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import com.google.common.util.concurrent.ListenableFuture
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.app.main.MainActivity
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.util.async.doAsync
import javax.inject.Inject

private const val RESOURCES_VERSION = "1"

class MoviesTodayTile : TileProviderService() {
    @Inject
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        Components.application.inject(this)
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<Resources> {
        return CallbackToFutureAdapter.getFuture { callback ->
            callback.set(
                Resources.builder()
                    .setVersion(RESOURCES_VERSION)
                    .build()
            )
        }
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<Tile> {
        val deviceParameters = requestParams.deviceParameters!!
        return CallbackToFutureAdapter.getFuture { callback ->
            doAsync {
                val movies = getTodaysMovies()

                callback.set(
                    Tile.builder()
                        .setResourcesVersion(RESOURCES_VERSION)
                        .setTimeline(
                            Timeline.builder()
                                .addTimelineEntry(
                                    TimelineEntry.builder()
                                        .setLayout(
                                            Layout.builder()
                                                .setRoot(createLayout(movies, deviceParameters))
                                        )
                                )
                        ).build()
                )
            }
        }
    }

    private fun getTodaysMovies(): List<Movie> {
        return database.movieDao.allMovies().take(6)
    }

    private fun createLayout(movies: List<Movie>, deviceParameters: DeviceParameters): Builder {
        val box = LayoutElementBuilders.Box.builder()
        box.setHeight(DimensionBuilders.expand())
        box.setWidth(DimensionBuilders.expand())

        val column = Column.builder()
        if (movies.isEmpty()) {
            column
                .addContent(
                    Text.builder()
                        .setText(getString(R.string.tile_pleaseSetup))
                        .setFontStyle(FontStyles.caption1(deviceParameters))
                        .setMaxLines(2)
                )
        } else {
            for (movie in movies) {
                column
                    .addContent(
                        Text.builder()
                            .setText("· ${movie.localTitle} ·")
                            .setFontStyle(FontStyles.caption1(deviceParameters).apply {
                                movie.colorLight?.let { setColor(ColorBuilders.argb(it)) }
                            })
                            .setMaxLines(2)
                            .setOverflow(TEXT_OVERFLOW_ELLIPSIZE_END)
                    )
            }
        }
        box.setModifiers(
            Modifiers.builder()
                .setClickable(
                    Clickable.builder()
                        .setOnClick(
                            LaunchAction.builder()
                                .setAndroidActivity(
                                    AndroidActivity.builder()
                                        .setClassName(MainActivity::class.java.name)
                                        .setPackageName(packageName)
                                )
                        )
                )
        )

        box.addContent(column)
        return box
    }

}
