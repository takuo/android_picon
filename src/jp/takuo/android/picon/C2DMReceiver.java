/*
 * Copyright 2010 Takuo Kitame.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.takuo.android.picon;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {
    private static final String LOG_TAG = "C2DMReceiver";
    public C2DMReceiver() {
        super(DeviceRegistrar.SENDER_ID);
    }

    @Override
    public void onRegistered(Context context, String registration) {
        Log.d(LOG_TAG, "onRegistered");
        DeviceRegistrar.registerWithServer(context, registration);
    }

    @Override
    public void onUnregistered(Context context) {
        Log.d(LOG_TAG, "onUnregistered");
        SharedPreferences prefs = Prefs.get(context);
        String deviceRegistrationID = prefs.getString("deviceRegistrationID", null);
        DeviceRegistrar.unregisterWithServer(context, deviceRegistrationID);
    }

    @Override
    public void onError(Context context, String errorId) {
        //context.sendBroadcast(new Intent("com.google.ctp.UPDATE_UI"));
    }

    @Override
    public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String url = (String) extras.get("url");
            String title = (String) extras.get("title");
            String sel = (String) extras.get("sel");
            Log.d(LOG_TAG, "onMessage: " + url + ", " + title + ", " + sel );
/*
            if (title != null && url != null && url.startsWith("http")) {
                SharedPreferences settings = Prefs.get(context);
                Intent launchIntent = LauncherUtils.getLaunchIntent(context, title, url, sel);

                // Notify and optionally start activity
                if (settings.getBoolean("launchBrowserOrMaps", true) && launchIntent != null) {
                    LauncherUtils.playNotificationSound(context);
                    context.startActivity(launchIntent);
                } else {
                    if (sel != null && sel.length() > 0) {  // have selection
                        LauncherUtils.generateNotification(context, sel,
                                context.getString(R.string.copied_desktop_clipboard), launchIntent);
                    } else {
                        LauncherUtils.generateNotification(context, url, title, launchIntent);
                    }
                }

                // Record history (for link/maps only)
                if (launchIntent != null && launchIntent.getAction().equals(Intent.ACTION_VIEW)) {
                    HistoryDatabase.get(context).insertHistory(title, url);
                }
            }
            */
        }
    }
}
