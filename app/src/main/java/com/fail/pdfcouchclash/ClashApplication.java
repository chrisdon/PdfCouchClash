package com.fail.pdfcouchclash;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;

/**
 * Created by cdonnelly on 15/12/2014.
 */
public class ClashApplication extends Application {

    private final static String TAG = ClashApplication.class.getSimpleName();

    private final static String DB_NAME = "FitchMobilePortal";

    /** If true then strict mode is enabled */
    private static final boolean STRICT = false;



    /** Instance of the CouchbaseLite {@link com.couchbase.lite.Manager} */
    private static Manager mDbManager;

    //private static Database mStructuralDatabase;

    //private static Database mUserDatabase;

    private static Context mContext;



    @Override
    public void onCreate() {
        try {
            mContext = getApplicationContext();
            mDbManager = new Manager(new AndroidContext(mContext), Manager.DEFAULT_OPTIONS);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Cannot create database");
            return;
        }
    }



    public static Database getDatabase(String dbName) throws CouchbaseLiteException {
        if (!Manager.isValidDatabaseName(dbName)) {
            return null;
        }

        return mDbManager.getDatabase(dbName);
    }

    public static void createStructuralViews(String dbName) throws CouchbaseLiteException {
        Database database = mDbManager.getDatabase(dbName);

        DBUtils.createHtmlView(database, mContext);

    }


}
