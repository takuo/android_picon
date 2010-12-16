/*
 * Copyright (C) 2011 Takuo Kitame
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.takuo.android.picon;

import java.util.ArrayList;

import com.google.android.c2dm.C2DMessaging;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Picon extends Activity implements OnClickListener {
    private static final String LOG_TAG = "Picon";
    public static final String UPDATE_UI_ACTION = "jp.takuo.android.picon.UPDATE_UI";
    public static final String AUTH_PERMISSION_ACTION = "jp.takuo.android.picon.AUTH_PERMISSION";
    private int mAccountSelectedPosition = 0;
    private Boolean mPendingAuth = false;
    private Button mRegisterButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mRegisterButton = (Button) findViewById(R.id.button_register);
        mRegisterButton.setOnClickListener(this);

        // Display accounts
        String accounts[] = getGoogleAccounts();
        if (accounts.length == 0) {
            TextView promptText = (TextView) findViewById(R.id.label_select_account);
            promptText.setText(R.string.no_accounts);
            mRegisterButton.setEnabled(false);
        } else {
            ListView listView = (ListView) findViewById(R.id.list_accounts);
            listView.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.account, accounts));
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setItemChecked(mAccountSelectedPosition, true);
        }
        registerReceiver(mAuthPermissionReceiver, new IntentFilter(AUTH_PERMISSION_ACTION));
        registerReceiver(mUpdateUIReceiver, new IntentFilter(UPDATE_UI_ACTION));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPendingAuth) {
            mPendingAuth = false;
            String regId = C2DMessaging.getRegistrationId(this);
            if (regId != null && !"".equals(regId)) {
                DeviceRegistrar.registerWithServer(this, regId);
            } else {
                C2DMessaging.register(this, DeviceRegistrar.SENDER_ID);
            }
        }
    }

    private String[] getGoogleAccounts() {
        ArrayList<String> accountNames = new ArrayList<String>();
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (account.type.equals("com.google")) {
                accountNames.add(account.name);
            }
        }

        String[] result = new String[accountNames.size()];
        accountNames.toArray(result);
        return result;
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_register:
            Log.d(LOG_TAG, "Start Registering");
            ListView listView = (ListView) findViewById(R.id.list_accounts);
            mAccountSelectedPosition = listView.getCheckedItemPosition();
            TextView account = (TextView) listView.getChildAt(mAccountSelectedPosition);
            mRegisterButton.setEnabled(false);
            register((String) account.getText());
            break;
        }
        
    }

    private void register(String account) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.connecting_text);
        textView.setVisibility(ProgressBar.VISIBLE);

        SharedPreferences prefs = Prefs.get(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("accountName", account);
        editor.commit();
        C2DMessaging.register(this, DeviceRegistrar.SENDER_ID);
    }

    private final BroadcastReceiver mAuthPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getBundleExtra("AccountManagerBundle");
            if (extras != null) {
                Intent authIntent = (Intent) extras.get(AccountManager.KEY_INTENT);
                if (authIntent != null) {
                    mPendingAuth = true;
                    startActivity(authIntent);
                }
            }
        }
    };

    private void handleConnectingUpdate(int status) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        TextView textView = (TextView) findViewById(R.id.connecting_text);
        if (status == DeviceRegistrar.REGISTERED_STATUS) {
//            setScreenContent(R.layout.select_launch_mode);
        } else {
            mRegisterButton.setEnabled(true);
        }
        if (status == DeviceRegistrar.AUTH_ERROR_STATUS) {
            textView.setText(status == DeviceRegistrar.AUTH_ERROR_STATUS ? R.string.auth_error_text :
                    R.string.connect_error_text);
            mRegisterButton.setEnabled(true);
        } else {
            textView.setText("Done.");
        }
    }

    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (mScreenId == R.layout.select_account) {
                handleConnectingUpdate(intent.getIntExtra(
                        DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS));
//            } else if (mScreenId == R.layout.connected) {
//                handleDisconnectingUpdate(intent.getIntExtra(
//                        DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS));
//            }
        }
    };

}
