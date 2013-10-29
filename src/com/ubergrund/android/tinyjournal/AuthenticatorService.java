package com.ubergrund.android.tinyjournal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created with IntelliJ IDEA.
 * User: Tim
 * Date: 10/28/13
 * Time: 7:46 AM
 */
public class AuthenticatorService extends Service {

    private JournalAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new JournalAuthenticator(this);
    }

    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
