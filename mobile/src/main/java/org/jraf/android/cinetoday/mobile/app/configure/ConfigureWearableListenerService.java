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
package org.jraf.android.cinetoday.mobile.app.configure;

import android.content.Intent;

import org.jraf.android.cinetoday.common.wear.WearHelper;
import org.jraf.android.cinetoday.mobile.app.main.MainActivity;
import org.jraf.android.util.log.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class ConfigureWearableListenerService extends WearableListenerService {
    public ConfigureWearableListenerService() {}

    @Override
    public void onPeerConnected(Node peer) {}

    @Override
    public void onPeerDisconnected(Node peer) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(messageEvent.toString());
        byte[] payload = messageEvent.getData();
        switch (messageEvent.getPath()) {
            case WearHelper.PATH_ACTION_OPEN_CONFIGURE_ACTIVITY:
                openConfigureActivity();
                break;
        }
    }

    private void openConfigureActivity() {
        if (MainActivity.isRunning()) {
            Log.d("Main activity already running, don't do anything");
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
