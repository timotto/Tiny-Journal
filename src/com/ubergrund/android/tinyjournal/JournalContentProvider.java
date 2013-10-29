package com.ubergrund.android.tinyjournal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 *
 * Map ContactIDs retrieved via ContactsContract.PhoneLookup.CONTENT_FILTER_URI to own RawContactIDs
 *
 * Call Log / SMS / Calendar entry tables
 * _id, other_id, contact_id
 *
 * User: Tim
 * Date: 10/28/13
 * Time: 7:33 AM
 */
public class JournalContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.ubergrund.android.tinyjournal.provider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
