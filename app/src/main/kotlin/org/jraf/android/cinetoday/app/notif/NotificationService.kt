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
package org.jraf.android.cinetoday.app.notif

class NotificationService//    private void showNotification() {
//        Log.d();
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification = createNotification();
//        notificationManager.notify(NOTIFICATION_ID, notification);
//    }
//
//    private Notification createNotification() {
//        Notification.Builder mainNotifBuilder = new Notification.Builder(this);
//
//        // Small icon
//        mainNotifBuilder.setSmallIcon(R.drawable.ic_notif);
//
//        // Title
//        String title = getString(R.string.notif_title);
//        mainNotifBuilder.setContentTitle(title);
//
//        // Text
//        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
//        for (String movie : mNewMovies) {
//            inboxStyle.addLine(movie);
//        }
//        mainNotifBuilder.setStyle(inboxStyle);
//
//        // Content intent
//        Intent mainActivityIntent = new Intent(this, MainActivity.class);
//        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, 0);
//        mainNotifBuilder.setContentIntent(mainActivityPendingIntent);
//
//        // Wear specifics
//        Notification.WearableExtender wearableExtender = new Notification.WearableExtender();
//        // TODO
////        wearableExtender.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notif_logo));
////        wearableExtender.addAction(new Notification.Action.Builder(R.mipmap.ic_launcher, "Open", mainActivityPendingIntent).build());
////        wearableExtender.setContentAction(0);
//
//
//        Notification.Builder wearableNotifBuilder = wearableExtender.extend(mainNotifBuilder);
//        Notification res = wearableNotifBuilder.build();
//        return res;
//    }
