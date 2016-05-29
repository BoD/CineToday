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
package org.jraf.android.cinetoday.mobile.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.jraf.android.cinetoday.mobile.api.http.HttpUtil;
import org.jraf.android.util.bitmap.BitmapUtil;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.util.log.Log;

import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class ImageCache {
    private static final ImageCache INSTANCE = new ImageCache();
    private Context mContext;

    public static ImageCache get(Context context) {
        INSTANCE.mContext = context.getApplicationContext();
        return INSTANCE;
    }

    private ImageCache() {}

    @WorkerThread
    @Nullable
    public Bitmap getBitmap(String uri, int maxWidth, int maxHeight) {
        Log.d("uri=%s", uri);
        if (uri == null) return null;
        File cachedFile = getCachedFile(uri, maxWidth, maxHeight);
        if (cachedFile.exists()) {
            Log.d("Cache hit for %s", uri);
        } else {
            Log.d("Cache miss for %s", uri);
            try {
                downloadAndResize(uri, maxWidth, maxHeight);
            } catch (IOException e) {
                Log.w(e, "Could not download image uri=%s", uri);
                return null;
            }
        }
        return BitmapFactory.decodeFile(cachedFile.getPath());
    }

    @WorkerThread
    private void downloadAndResize(String uri, int maxWidth, int maxHeight) throws IOException {
        Log.d("uri=%s", uri);
        // Download the full sized image to a temporary file
        Request request = new Request.Builder().url(uri).build();
        Response response = HttpUtil.getOkHttpClient(mContext).newCall(request).execute();
        File temporaryFile = getTemporaryDownloadFile(uri, maxWidth, maxHeight);
        BufferedSink sink = Okio.buffer(Okio.sink(temporaryFile));
        sink.writeAll(response.body().source());
        sink.close();

        // Get a resized version of the image
        Bitmap resizedBitmap = BitmapUtil.createThumbnail(temporaryFile, maxWidth, maxHeight);

        // Delete the temporary file
        temporaryFile.delete();

        // Dumps the resized version to the temporary file
        OutputStream output = new BufferedOutputStream(new FileOutputStream(temporaryFile));
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output);
        output.flush();
        output.close();

        // Rename the temporary file to the final file name
        temporaryFile.renameTo(getCachedFile(uri, maxWidth, maxHeight));
    }

    private File getCachedFile(String uri, int maxWidth, int maxHeight) {
        String fileName = getCachedFileName(uri, maxWidth, maxHeight);
        return new File(mContext.getExternalCacheDir(), fileName);
    }

    private File getTemporaryDownloadFile(String uri, int maxWidth, int maxHeight) {
        String fileName = getCachedFileName(uri, maxWidth, maxHeight) + ".tmp";
        return new File(mContext.getExternalCacheDir(), fileName);
    }

    private String getCachedFileName(String uri, int maxWidth, int maxHeight) {
        return FileUtil.getValidFileName(uri + "_" + maxWidth + "_" + maxHeight);
    }
}
