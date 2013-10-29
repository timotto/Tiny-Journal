package com.ubergrund.android.tinyjournal;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class MyActivity extends Activity {

    private static final String TAG = "TinyJournal/MyActivity";

    private static final String ACCOUNT = "staticaccount";
    private Account account;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        account = createSyncAccount(this);

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, JournalContentProvider.AUTHORITY, settingsBundle);
    }


    public static Account createSyncAccount(Context context) {
        final Account account = new Account(ACCOUNT, JournalAuthenticator.ACCOUNT_TYPE);

        final AccountManager accountManager = (AccountManager)context.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(account, null, null)) {
            Log.d(TAG, "staticaccount created");
        } else {
            Log.d(TAG, "staticaccount NOT created");
        }

        return account;
    }
}
