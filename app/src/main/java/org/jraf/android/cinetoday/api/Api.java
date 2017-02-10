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
package org.jraf.android.cinetoday.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import org.jraf.android.cinetoday.api.codec.movie.MovieCodec;
import org.jraf.android.cinetoday.api.codec.showtime.ShowtimeCodec;
import org.jraf.android.cinetoday.api.codec.theater.TheaterCodec;
import org.jraf.android.cinetoday.api.http.HttpUtil;
import org.jraf.android.cinetoday.model.ParseException;
import org.jraf.android.cinetoday.model.movie.Movie;
import org.jraf.android.cinetoday.model.theater.Theater;
import org.jraf.android.util.log.Log;

public class Api {
    private static final Api INSTANCE = new Api();

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private static final String SCHEME = "http";
    private static final String HOST = "api.allocine.fr";
    private static final String PATH_REST = "rest";
    private static final String PATH_V3 = "v3";
    private static final String QUERY_PARTNER_KEY = "partner";
    private static final String QUERY_PARTNER_VALUE = "YW5kcm9pZC12M3M";
    private static final String QUERY_FORMAT_KEY = "format";
    private static final String QUERY_FORMAT_VALUE = "json";
    private static final String PATH_SHOWTIMELIST = "showtimelist";
    private static final String QUERY_THEATERS_KEY = "theaters";
    private static final String QUERY_DATE_KEY = "date";
    private static final String PATH_MOVIE = "movie";
    private static final String QUERY_CODE_KEY = "code";
    private static final String QUERY_STRIPTAGS_KEY = "striptags";
    private static final String QUERY_STRIPTAGS_VALUE = "true";
    private static final String PATH_SEARCH = "search";
    private static final String QUERY_COUNT_KEY = "count";
    private static final String QUERY_COUNT_VALUE = "15";
    private static final String QUERY_QUERY_KEY = "q";
    private static final String QUERY_FILTER_KEY = "filter";
    private static final String QUERY_FILTER_VALUE = "theater";

    private Context mContext;

    private Api() {}

    public static Api get(Context context) {
        if (INSTANCE.mContext == null) INSTANCE.mContext = context.getApplicationContext();
        return INSTANCE;
    }

    @WorkerThread
    public void getMovieList(@NonNull SortedSet<Movie> movies, String theaterId, String theaterName, int position, Date date)
            throws IOException, ParseException {
        HttpUrl url = getBaseBuilder(PATH_SHOWTIMELIST)
                .addQueryParameter(QUERY_THEATERS_KEY, theaterId)
                .addQueryParameter(QUERY_DATE_KEY, SIMPLE_DATE_FORMAT.format(date))
                .build();
        String jsonStr = call(url, false);
        parseMovieList(movies, jsonStr, theaterName, position, date);
    }

    @VisibleForTesting
    static void parseMovieList(@NonNull SortedSet<Movie> movies, String jsonStr, String theaterName, int position, Date date) throws ParseException {
        try {
            JSONObject jsonRoot = new JSONObject(jsonStr);
            JSONObject jsonFeed = jsonRoot.getJSONObject("feed");
            JSONArray jsonTheaterShowtimes = jsonFeed.getJSONArray("theaterShowtimes");
            JSONObject jsonTheaterShowtime = jsonTheaterShowtimes.getJSONObject(0);
            JSONArray jsonMovieShowtimes = jsonTheaterShowtime.optJSONArray("movieShowtimes");
            if (jsonMovieShowtimes == null) return;
            int len = jsonMovieShowtimes.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonMovieShowtime = jsonMovieShowtimes.getJSONObject(i);
                JSONObject jsonOnShow = jsonMovieShowtime.getJSONObject("onShow");
                JSONObject jsonMovie = jsonOnShow.getJSONObject("movie");
                Movie movie = new Movie();

                // Movie (does not include showtimes, only the movie details)
                MovieCodec.get().fill(movie, jsonMovie);
                // See if the movie was already in the set, if yes use this one, so the showtimes are merged
                if (movies.contains(movie)) {
                    // Already in the set: find it
                    for (Movie m : movies) {
                        if (m.equals(movie)) {
                            // Found it: discard the new one, use the old one instead
                            movie = m;
                            break;
                        }
                    }
                }

                // Showtimes
                ShowtimeCodec.get().fill(movie, jsonMovieShowtime, theaterName, position, date);

                // If there is no showtimes for today, skip the movie
                if (movie.todayShowtimes == null || movie.todayShowtimes.size() == 0) {
                    Log.w("Movie %s has no showtimes: skip it", movie.id);
                } else {
                    movies.add(movie);
                }
            }
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }

    @WorkerThread
    public void getMovieInfo(Movie movie) throws IOException, ParseException {
        HttpUrl url = getBaseBuilder(PATH_MOVIE)
                .addQueryParameter(QUERY_STRIPTAGS_KEY, QUERY_STRIPTAGS_VALUE)
                .addQueryParameter(QUERY_CODE_KEY, movie.id)
                .build();
        String jsonStr = call(url, true);
        try {
            JSONObject jsonRoot = new JSONObject(jsonStr);
            JSONObject jsonMovie = jsonRoot.getJSONObject("movie");
            MovieCodec.get().fill(movie, jsonMovie);
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }

    @WorkerThread
    public List<Theater> searchTheaters(String query) throws IOException, ParseException {
        if (query.length() < 3) return new ArrayList<>();

        HttpUrl url = getBaseBuilder(PATH_SEARCH)
                .addQueryParameter(QUERY_COUNT_KEY, QUERY_COUNT_VALUE)
                .addQueryParameter(QUERY_FILTER_KEY, QUERY_FILTER_VALUE)
                .addQueryParameter(QUERY_QUERY_KEY, query)
                .build();
        String jsonStr = call(url, true);
        try {
            JSONObject jsonRoot = new JSONObject(jsonStr);
            JSONObject jsonFeed = jsonRoot.getJSONObject("feed");
            int totalResults = jsonFeed.getInt("totalResults");
            if (totalResults == 0) return new ArrayList<>();

            JSONArray jsonTheaters = jsonFeed.getJSONArray("theater");
            int len = jsonTheaters.length();
            List<Theater> res = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                JSONObject jsonTheater = jsonTheaters.getJSONObject(i);
                Theater theater = new Theater();

                // Theater
                TheaterCodec.get().fill(theater, jsonTheater);
                res.add(theater);
            }
            return res;
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }

    @WorkerThread
    @NonNull
    private String call(HttpUrl url, boolean useCache) throws IOException {
        Log.d("url=%s", url);
        Request.Builder urlBuilder = new Request.Builder().url(url);
        if (!useCache) urlBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        Request request = urlBuilder.build();
        Response response = HttpUtil.getOkHttpClient(mContext).newCall(request).execute();
        return response.body().string();
    }

    @NonNull
    private HttpUrl.Builder getBaseBuilder(String path) {
        return new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(HOST)
                .addPathSegment(PATH_REST)
                .addPathSegment(PATH_V3)
                .addPathSegment(path)
                .addQueryParameter(QUERY_PARTNER_KEY, QUERY_PARTNER_VALUE)
                .addQueryParameter(QUERY_FORMAT_KEY, QUERY_FORMAT_VALUE);
    }
}
