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
package org.jraf.android.moviestoday.mobile.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.jraf.android.moviestoday.common.async.ResultCallback;
import org.jraf.android.moviestoday.common.async.ResultOrError;
import org.jraf.android.moviestoday.common.model.ParseException;
import org.jraf.android.moviestoday.common.model.movie.Movie;
import org.jraf.android.moviestoday.mobile.api.codec.movie.MovieCodec;
import org.jraf.android.util.log.wrapper.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Api {
    private static final Api INSTANCE = new Api();

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    static {
        OK_HTTP_CLIENT.setConnectTimeout(30, TimeUnit.SECONDS);
        OK_HTTP_CLIENT.setReadTimeout(30, TimeUnit.SECONDS);
    }

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


    private Api() {}

    public static Api get() {
        return INSTANCE;
    }

    public Set<Movie> getMovieList(String theaterId, Date date) throws IOException, ParseException {
        HttpUrl url = getBaseBuilder(PATH_SHOWTIMELIST)
                .addQueryParameter(QUERY_THEATERS_KEY, theaterId)
                .addQueryParameter(QUERY_DATE_KEY, SIMPLE_DATE_FORMAT.format(date))
                .build();
        String jsonStr = call(url);
        try {
            JSONObject jsonRoot = new JSONObject(jsonStr);
            JSONObject jsonFeed = jsonRoot.getJSONObject("feed");
            JSONArray jsonTheaterShowtimes = jsonFeed.getJSONArray("theaterShowtimes");
            JSONObject jsonTheaterShowtime = jsonTheaterShowtimes.getJSONObject(0);
            JSONArray jsonMovieShowtimes = jsonTheaterShowtime.getJSONArray("movieShowtimes");
            int len = jsonMovieShowtimes.length();
            LinkedHashSet<Movie> res = new LinkedHashSet<>(len);
            for (int i = 0; i < len; i++) {
                JSONObject jsonMovieShowtime = jsonMovieShowtimes.getJSONObject(i);
                JSONObject jsonOnShow = jsonMovieShowtime.getJSONObject("onShow");
                JSONObject jsonMovie = jsonOnShow.getJSONObject("movie");
                Movie movie = new Movie();
                MovieCodec.get().fill(movie, jsonMovie);
                res.add(movie);
            }
            return res;
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }

    public void getMovieList(final String theaterId, final Date date, final ResultCallback<Set<Movie>> callResult) {
        new AsyncTask<Void, Void, ResultOrError<Set<Movie>>>() {
            @Override
            protected ResultOrError<Set<Movie>> doInBackground(Void... params) {
                try {
                    return new ResultOrError<>(getMovieList(theaterId, date));
                } catch (Throwable t) {
                    return new ResultOrError<>(t);
                }
            }

            @Override
            protected void onPostExecute(ResultOrError<Set<Movie>> resultOrError) {
                if (resultOrError.isError()) {
                    callResult.onError(resultOrError.error);
                } else {
                    callResult.onResult(resultOrError.result);
                }
            }
        }.execute();
    }

    public void getMovieInfo(Movie movie) throws IOException, ParseException {
        HttpUrl url = getBaseBuilder(PATH_MOVIE)
                .addQueryParameter(QUERY_STRIPTAGS_KEY, QUERY_STRIPTAGS_VALUE)
                .addQueryParameter(QUERY_CODE_KEY, movie.id)
                .build();
        String jsonStr = call(url);
        try {
            JSONObject jsonRoot = new JSONObject(jsonStr);
            JSONObject jsonMovie = jsonRoot.getJSONObject("movie");
            MovieCodec.get().fill(movie, jsonMovie);
        } catch (JSONException e) {
            throw new ParseException(e);
        }
    }

    @NonNull
    private String call(HttpUrl url) throws IOException {
        Log.d("url=" + url);
        Request request = new Request.Builder().url(url).build();
        Response response = OK_HTTP_CLIENT.newCall(request).execute();
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
