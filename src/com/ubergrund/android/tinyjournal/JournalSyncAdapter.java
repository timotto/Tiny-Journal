package com.ubergrund.android.tinyjournal;

import android.accounts.Account;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Tim
 * Date: 10/28/13
 * Time: 7:36 AM
 */
public class JournalSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "TinyJournal/SyncAdapter";
    private static final String KEY_LATEST_CALLLOG_ID = "last_calllog_id";
    private static final String KEY_LATEST_SMS_ID = "last_sms_id";
    private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
//    SMS columns: _id,thread_id,address,person,date,date_sent,protocol,read,status,type,reply_path_present,subject,body,service_center,locked,error_code,seen

    private final ContentResolver resolver;
    private final ContactCache contactCache;

    public JournalSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        resolver = context.getContentResolver();
        contactCache = new ContactCache(resolver);
    }

    public JournalSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        resolver = context.getContentResolver();
        contactCache = new ContactCache(resolver);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        syncCallLog();
        syncSMSHistory();

    }

    private void syncCallLog() {

        long latestId = getLatestId(KEY_LATEST_CALLLOG_ID);
        final long lastLatestId = latestId;

        final Cursor callLogQuery = resolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (callLogQuery == null) {
            Log.w(TAG, "CallLog query returned null");
            return;
        }

        try {
            if (!callLogQuery.moveToFirst()) {
                Log.w(TAG, "CallLog query returned 0 results");
                return;
            }

            final int colId = callLogQuery.getColumnIndexOrThrow(CallLog.Calls._ID);
            final int colNumber = callLogQuery.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
            final int colDate = callLogQuery.getColumnIndexOrThrow(CallLog.Calls.DATE);
            final int colDuration = callLogQuery.getColumnIndexOrThrow(CallLog.Calls.DURATION);
            final int colType = callLogQuery.getColumnIndexOrThrow(CallLog.Calls.TYPE);

            while (!callLogQuery.isAfterLast()) {
                final long id = callLogQuery.getLong(colId);
                final String number = callLogQuery.getString(colNumber);
                final long date = callLogQuery.getLong(colDate);
                final long duration = callLogQuery.getLong(colDuration);
                final int type = callLogQuery.getInt(colType);
                callLogQuery.moveToNext();

                latestId = id;

                final long contactId = contactCache.getContactId(number);
                Log.d(TAG, id + ": " + date+" ("+type+") " + number + " ["+contactId+"]"  + " ("+duration+")");

            }
        } finally {
            callLogQuery.close();
            if (latestId != lastLatestId) {
                saveLatestId(KEY_LATEST_CALLLOG_ID, latestId);
            }
        }
    }

    private void syncSMSHistory() {
        long latestId = getLatestId(KEY_LATEST_SMS_ID);
        final long lastLatestId = latestId;

        final Cursor smsQuery = resolver.query(SMS_CONTENT_URI, null, null, null, null);

        if (smsQuery == null) {
            Log.w(TAG, "SMS query returned null");
            return;
        }

        try {
            if (!smsQuery.moveToFirst()) {
                Log.w(TAG, "SMS query returned 0 results");
                return;
            }

            final int colId = smsQuery.getColumnIndexOrThrow("_id");
            final int colAddress = smsQuery.getColumnIndexOrThrow("address");
            final int colDate = smsQuery.getColumnIndexOrThrow("date");
            final int colType = smsQuery.getColumnIndexOrThrow("type");
            final int colSubject = smsQuery.getColumnIndexOrThrow("subject");
            final int colBody = smsQuery.getColumnIndexOrThrow("body");
            final String[] columnNames = smsQuery.getColumnNames();
            final String[] row = new String[columnNames.length];
//            Log.d(TAG, "SMS columns: " + TextUtils.join(",", columnNames));
            while (!smsQuery.isAfterLast()) {
//                for(int i=0;i<columnNames.length;i++)
//                    row[i] = smsQuery.getString(i);
//                Log.d(TAG, "SMS row ["+TextUtils.join(",",row)+"]");
                final long id = smsQuery.getLong(colId);
                final String address = smsQuery.getString(colAddress);
                final long date = smsQuery.getLong(colDate);
                final int type = smsQuery.getInt(colType);
                final String subject = smsQuery.getString(colSubject);
                final String body = smsQuery.getString(colBody);

                smsQuery.moveToNext();

                latestId = id;

                final long contactId = contactCache.getContactId(address);
                Log.d(TAG, id + ": " + date+" ("+type+") " + address + " ["+contactId+"]"  + " ("+subject+")");

            }
        } finally {
            smsQuery.close();
            if (latestId != lastLatestId) {
                saveLatestId(KEY_LATEST_SMS_ID, latestId);
            }
        }
    }

    private void saveLatestId(String key, long id) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putLong(key, id);
        edit.commit();
    }

    private long getLatestId(String key) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getLong(key, -1);
    }

    private class ContactCache {

        private final Map<String,Long> phoneNumber2ContactIdCache = new HashMap<String, Long>();
        private final ContentResolver resolver;

        private ContactCache(ContentResolver resolver) {
            this.resolver = resolver;
        }

        public long getContactId(String phoneNumber) {
            if (phoneNumber2ContactIdCache.containsKey(phoneNumber))
                return phoneNumber2ContactIdCache.get(phoneNumber);

            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            final Cursor contactQuery = resolver.query(uri, new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (contactQuery == null) {
                Log.w(TAG, "Unable to resolve number, query returned null");
                return -1;
            }

            try {
                if (!contactQuery.moveToFirst()) {
                    Log.w(TAG, "Empty result");
                    phoneNumber2ContactIdCache.put(phoneNumber, -1l);
                    return -1;
                }
                final long id = contactQuery.getLong(0);
                final String name = contactQuery.getString(1);
                phoneNumber2ContactIdCache.put(phoneNumber, id);
                Log.d(TAG, "Resolved ["+phoneNumber+"] to ["+name+"]");
                return id;
            }
            finally {
                contactQuery.close();
            }
        }
    }

}
