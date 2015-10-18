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
package org.jraf.android.moviestoday.common.wear;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.jraf.android.moviestoday.common.model.movie.Movie;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.parcelable.ParcelableUtil;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Helper singleton class to deal with the wear APIs.<br/>
 * Note: {@link #connect(Context)} must be called prior to calling all the other methods.
 */
public class WearHelper {
    private static final WearHelper INSTANCE = new WearHelper();

    private static final String PATH_MOVIES = "/movies";
    private static final String KEY_VALUE = "KEY_VALUE";

    private GoogleApiClient mGoogleApiClient;

    private WearHelper() {}

    public static WearHelper get() {
        return INSTANCE;
    }

    @WorkerThread
    public synchronized void connect(Context context) {
        Log.d();
        if (mGoogleApiClient != null) {
            Log.d("Already connected");
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        // Blocking
        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
        if (!connectionResult.isSuccess()) {
            // TODO handle failures
        }
    }

    public synchronized void disconnect() {
        Log.d();
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    /*
     * Data.
     */
    // region

    @WorkerThread
    public void putMovies(Collection<Movie> movies) {
        Log.d();
        // First remove any old value
        Wearable.DataApi.deleteDataItems(mGoogleApiClient, createUri(PATH_MOVIES)).await();

        // Create new value
        Bundle moviesBundle = new Bundle();
        ArrayList<Movie> moviesArrayList = new ArrayList<>(movies);
        moviesBundle.putParcelableArrayList(KEY_VALUE, moviesArrayList);

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_MOVIES);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putAsset(KEY_VALUE, createAssetFromBundle(moviesBundle));

        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();

        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).await();
    }

    @WorkerThread
    @Nullable
    public List<Movie> getMovies() {
        DataItemBuffer dataItemBuffer = Wearable.DataApi.getDataItems(mGoogleApiClient, createUri(PATH_MOVIES)).await();
        try {
            if (!dataItemBuffer.getStatus().isSuccess()) return null;
            if (dataItemBuffer.getCount() == 0) return null;
            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItemBuffer.get(0));
            DataMap dataMap = dataMapItem.getDataMap();
            Asset moviesAsset = dataMap.getAsset(KEY_VALUE);
            Bundle moviesBundle = loadBundleFromAsset(moviesAsset);
            moviesBundle.setClassLoader(Movie.class.getClassLoader()); // Not sure why this is needed
            return moviesBundle.getParcelableArrayList(KEY_VALUE);
        } finally {
            dataItemBuffer.release();
        }
    }

    // endregion


    /*
     * Messaging.
     */
    // region

    @WorkerThread
    private void sendMessage(String path, @Nullable final byte[] payload) {
        Log.d("path=" + path);
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodesResult.getNodes()) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, payload).await();
        }
    }

    // endregion


    /*
     * Misc.
     */
    // region

    private static Uri createUri(String path) {
        return new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(path).build();
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @WorkerThread
    public Bitmap loadBitmapFromAsset(Asset asset) {
        DataApi.GetFdForAssetResult fd = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await();
        InputStream inputStream = fd.getInputStream();
        return BitmapFactory.decodeStream(inputStream);
    }

    private static Asset createAssetFromBundle(@NonNull Bundle bundle) {
        byte[] data = ParcelableUtil.parcel(bundle);
        assert data != null;
        return Asset.createFromBytes(data);
    }

    @WorkerThread
    @Nullable
    public Bundle loadBundleFromAsset(Asset asset) {
        DataApi.GetFdForAssetResult fd = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await();
        InputStream inputStream = fd.getInputStream();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            IoUtil.copy(inputStream, byteStream);
        } catch (IOException e) {
            Log.w("Could not read from asset", e);
            return null;
        }
        byte[] byteArray = byteStream.toByteArray();
        return ParcelableUtil.unparcel(byteArray, Bundle.CREATOR);
    }

    // endregion
}
