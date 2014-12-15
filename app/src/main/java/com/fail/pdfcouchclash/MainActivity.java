package com.fail.pdfcouchclash;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Revision;
import com.couchbase.lite.replicator.Replication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private WebView mWebView;
    private final static String DBNAME = "public";
    private final static String URL = "http://54.75.156.177:5984";
    private final static String DOCUMENT_ID = "38efd6c0d96855be655fb3f3fa000a09";
    private final static String TAG = MainActivity.class.getSimpleName();
    private static final String MIME_TYPE_TEXT_HTML = "text/html";
    private static final String UTF_8 = "utf-8";

    private boolean mHasServiceBeenCalled;
    private Replication mStructDbPull;

    private Replication mStructDbPush;

    private Replication mUserDbPull;

    private Replication mUserDbPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webView1);



        try {

            ClashApplication.createStructuralViews(DBNAME);
            if(ClashApplication.getDatabase(DBNAME) != null) renderWebView();
            replicateStructuralDB();
        } catch (CouchbaseLiteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderWebView(){
        AsyncTask<Void, Void, Object> task = new AsyncTask<Void, Void, Object>() {
            //private ProgressDialog progress;

            private static final int TYPE_WEB = 1;
            private static final int TYPE_IMAGE = 2;
            private static final int TYPE_IMAGE_SCALEABLE = 3;

            private int mType;

            private Matrix imageMatrix;

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Object doInBackground(Void... arg0) {

                String attachmentID = "33";

                InputStream in = getAttachment(DBNAME, DOCUMENT_ID);
                mType = TYPE_WEB;
                if (in != null) {
                    if (mType == TYPE_WEB) {
                        String result = "";
                        try {
                            result = inputStreamAsString(in);
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return result;
                    } else if (mType == TYPE_IMAGE || mType == TYPE_IMAGE_SCALEABLE) {
                        Bitmap b = BitmapFactory.decodeStream(in);
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return b;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                Log.d(TAG, "LOADED " + (result != null) + " " + mType);


                if (result != null) {

                    try {
                        String encoding = UTF_8;
                        String data = URLEncoder.encode((String) result, encoding).replaceAll("\\+", " ");
                        Log.d(TAG,  "Before "+encoding+" encoding: "+result+"\nAfter "+encoding+" encoding: "+data);
                        ((WebView) mWebView).loadData(data, MIME_TYPE_TEXT_HTML, UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                } else {
                    Log.d(TAG,  "Attachment is null " + DOCUMENT_ID);

                }
            }
        };
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.HONEYCOMB) {
            task.execute();
        } else {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void replicateStructuralDB() throws MalformedURLException, CouchbaseLiteException{
        Database structuralDB = ClashApplication.getDatabase(DBNAME);

        //final String masterUrl = schema.concat(username).concat(":").concat(password).concat("@").concat(dbLocation).concat("/").concat(dbName);

        java.net.URL url = new URL(URL+"/"+DBNAME);

        final Replication pull = structuralDB.createPullReplication(url);
        pull.setContinuous(false);
        // TODO add BasicAuthenticator object


        pull.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                // will be called back when the pull replication status changes

                boolean active = (pull.getStatus() == Replication.ReplicationStatus.REPLICATION_ACTIVE);
		        /*if (!active) {
		            DebugHandler.log(this, dbName+" Pull replication not active: "+event.getSource().getStatus());
		        } else {
		            double total = push.getCompletedChangesCount() + pull.getCompletedChangesCount();
		            //progressDialog.setMax(total);
		            //progressDialog.setProgress(push.getChangesCount() + pull.getChangesCount());

		        }*/
                if (pull.getLocalDatabase() != null) Log.d(TAG,  DBNAME+" Pull "+pull.getStatus()+" doc count: "+pull.getLocalDatabase().getDocumentCount()+
                        " changes count/Completed changes count: "+pull.getChangesCount()+"/"+pull.getCompletedChangesCount()+
                        " running: "+pull.isRunning());

                // Start processing of locally held data by service. Only call this method once
                if(pull.isContinuous() &&
                        //!mHasServiceBeenCalled &&
                        pull.getStatus() == Replication.ReplicationStatus.REPLICATION_IDLE &&
                        pull.getChangesCount() == pull.getCompletedChangesCount()){
                    //Query menuQuery;

                    // Check for primary menu items in local db
                    //menuQuery = SevenCityApplication.getDatabase(DBNAME).getView(getString(R.string.view_name_menus)).createQuery();
                    //QueryEnumerator queryEnumerator = menuQuery.run();
                    // Only start service parsing if local data exists or data received from server
                    if(pull.getLocalDatabase().getDocumentCount() > 0) {
                        Log.d("ExitReplication", DBNAME+" Pull "+pull.getStatus()+" doc count: "+pull.getLocalDatabase().getDocumentCount()+
                                " changes count/Completed changes count: "+pull.getChangesCount()+"/"+pull.getCompletedChangesCount()+
                                " running: "+pull.isRunning());
                        renderWebView();
                        mHasServiceBeenCalled = true;
                    }




                } else if(!pull.isContinuous() &&
                        //!mHasServiceBeenCalled &&
                        pull.getStatus() == Replication.ReplicationStatus.REPLICATION_STOPPED &&
                        pull.getChangesCount() == pull.getCompletedChangesCount()) {

                    // Check for primary menu items in local db

                    // Only start service parsing if local data exists or data received from server
                    if(pull.getCompletedChangesCount() > 0) {
                        if (pull.getLocalDatabase() != null) Log.d("ExitReplication", DBNAME+" Pull "+pull.getStatus()+" doc count: "+pull.getLocalDatabase().getDocumentCount()+" changes count: "+pull.getChangesCount()+"/"+pull.getCompletedChangesCount());
                        renderWebView();
                        mHasServiceBeenCalled = true;
                    }

                }
            }
        });
        pull.start();
        // It's important to keep a reference to a running replication,
        // or it is likely to be gc'd!
        mStructDbPull = pull;



    }

    /**
     * Get the name of the first attachement of the {@link com.couchbase.lite.Document} supplied
     *
     * @param doc
     * @return
     */
    public static String getFirstAttachmentName(Document doc) {
        if(doc.getCurrentRevision() != null) {
            List<String> attachmentNames = doc.getCurrentRevision().getAttachmentNames();
            for(String attachmentName : attachmentNames) {
                return attachmentName;
            }
        }


        return null;
    }

    /**
     * Read an input stream into a String
     *
     * @param stream
     * @return
     * @throws IOException
     */
    public static String inputStreamAsString(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
            stream.close();
        }

        return sb.toString();
    }

    /**
     * Get the attachment from the DB dbName
     *
     * @param dbName
     * @param id
     * @return
     */
    public InputStream getAttachment(String dbName, String id) {
        InputStream ret = null;
        if (id == null) {
            return null;
        } else {
            Database structuralDB;
            try {
                structuralDB = ClashApplication.getDatabase(dbName);
                Document doc = structuralDB.getDocument(id);
                String attachmentId = getFirstAttachmentName(doc);
                Revision rev = doc.getCurrentRevision();
                //Map<String, Object> map = rev.getProperties();
                if(rev != null) {
                    Attachment att = rev.getAttachment(attachmentId);
                    if(att != null) {
                        ret = att.getContent();
                    }
                }
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }

            return ret;
        }
    }
}
