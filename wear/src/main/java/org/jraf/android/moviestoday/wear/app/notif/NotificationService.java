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
package org.jraf.android.moviestoday.wear.app.notif;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.wear.WearHelper;
import org.jraf.android.moviestoday.wear.app.main.MainActivity;
import org.jraf.android.util.log.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class NotificationService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 0;
    private ArrayList<String> mNewMovies;

    public NotificationService() {}

    @Override
    public void onPeerConnected(Node peer) {}

    @Override
    public void onPeerDisconnected(Node peer) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) { }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("count=%s", dataEvents.getCount());

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() != DataEvent.TYPE_CHANGED) continue;

            DataItem dataItem = dataEvent.getDataItem();
            Uri uri = dataItem.getUri();
            Log.d("uri=" + uri);
            String path = uri.getPath();
            Log.d("path=" + path);
            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap dataMap = dataMapItem.getDataMap();
            mNewMovies = dataMap.getStringArrayList(WearHelper.KEY_NEW_MOVIES);
            showNotification();
        }
    }

    private void showNotification() {
        Log.d();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification() {
        Notification.Builder mainNotifBuilder = new Notification.Builder(this);

        // Small icon
        mainNotifBuilder.setSmallIcon(R.mipmap.ic_launcher);

        // Title
        String title = getString(R.string.notif_title);
        mainNotifBuilder.setContentTitle(title);

        // Text
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        for (String movie : mNewMovies) {
            inboxStyle.addLine(movie);
        }
        mainNotifBuilder.setStyle(inboxStyle);

        // Content intent
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);
        mainNotifBuilder.setContentIntent(mainActivityPendingIntent);

        // Wear specifics
        Notification.WearableExtender wearableExtender = new Notification.WearableExtender();
        // TODO
//        wearableExtender.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notif_logo));
//        wearableExtender.addAction(new Notification.Action.Builder(R.mipmap.ic_launcher, "Open", mainActivityPendingIntent).build());
//        wearableExtender.setContentAction(0);


        Notification.Builder wearableNotifBuilder = wearableExtender.extend(mainNotifBuilder);
        Notification res = wearableNotifBuilder.build();
        return res;
    }
}
