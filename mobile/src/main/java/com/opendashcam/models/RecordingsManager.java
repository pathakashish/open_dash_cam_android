package com.opendashcam.models;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.opendashcam.BackgroundVideoRecorder;
import com.opendashcam.Util;
import com.opendashcam.utils.IoThread;
import com.opendashcam.utils.MainThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads {@link Recording} objects and reloads them when new ones are added.
 * <p>
 * Created by ashish on 11/14/17.
 */

public class RecordingsManager {

    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String _ID = MediaStore.Video.Media._ID;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;
    private final static String ORDER_BY = MediaStore.Video.Media._ID + " DESC";
    private static final String LOG_TAG = RecordingsManager.class.getSimpleName();

    private final List<Recording> mRecodings = new ArrayList<>();
    private Context mApplicationContext;
    private final List<OnRecordingsListener> mRecordingsListeners = new ArrayList<>();

    public void addOnRecordingsListener(OnRecordingsListener listener) {
        synchronized (mRecordingsListeners) {
            mRecordingsListeners.add(listener);
        }
    }

    public void removeOnRecordingsListener(OnRecordingsListener listener) {
        synchronized (mRecordingsListeners) {
            mRecordingsListeners.remove(listener);
        }
    }

    private static class Holder {
        private static RecordingsManager INSTANCE = new RecordingsManager();
    }

    public static RecordingsManager sharedInstance() {
        return Holder.INSTANCE;
    }

    public void init(Context applicationContext) {
        mApplicationContext = applicationContext.getApplicationContext();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Util.ACTION_CLEAR_RECORDINGS.equals(intent.getAction())) {
                    synchronized (mRecodings) {
                        mRecodings.clear();
                    }
                    notifyRecordingsLoaded();
                } else if (BackgroundVideoRecorder.ACTION_NEW_RECORDING_AVAILABLE.equals(intent.getAction())) {
                    Recording recording = (Recording) intent.getSerializableExtra(BackgroundVideoRecorder.EXTRA_RECORDING);
                    synchronized (mRecodings) {
                        mRecodings.add(recording);
                    }
                    notifyRecordingsLoaded();
                }
//                else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) { // Do we need this??? Probably not since we have ACTION_NEW_RECORDING_AVAILABLE & ACTION_CLEAR_RECORDINGS now.
//                    startLoadInBackground();
//                }
            }
        };
//        mApplicationContext.registerReceiver(receiver, new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED));
        LocalBroadcastManager.getInstance(mApplicationContext).registerReceiver(receiver, new IntentFilter(BackgroundVideoRecorder.ACTION_NEW_RECORDING_AVAILABLE));
        LocalBroadcastManager.getInstance(mApplicationContext).registerReceiver(receiver, new IntentFilter(Util.ACTION_CLEAR_RECORDINGS));
    }

    public void startLoadInBackground() {
        if (ContextCompat.checkSelfPermission(mApplicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mApplicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        IoThread.post(new Runnable() {
            @Override
            public void run() {

                ArrayList<Recording> results = new ArrayList<>();

                //Here we set up a string array of the thumbnail ID column we want to get back
                String[] columns = {_ID, MEDIA_DATA};
                // Now we create the cursor pointing to the external thumbnail store
                Cursor cursor = mApplicationContext.getContentResolver().query(
                        MEDIA_EXTERNAL_CONTENT_URI,
                        columns, // Which columns to return
                        MEDIA_DATA + " like ? ",       // WHERE clause; which rows to return (all rows)
                        new String[]{"%OpenDashCam%"},       // WHERE clause selection arguments (none)
                        ORDER_BY // Order-by clause (descending by date added)
                );
                if (null == cursor) {
                    Log.w(LOG_TAG, "query returned null cursor. Recordings not loaded!");
                    return;
                }
                int count = cursor.getCount();
                // We now get the column index of the thumbnail id
                final int columnIndex = cursor.getColumnIndex(_ID);
                // Meta data
                final int columnMetaIndex = cursor.getColumnIndex(MEDIA_DATA);
                //move position to first element
                cursor.moveToFirst();

                for (int i = 0; i < count; i++) {
                    // Get id
                    int id = cursor.getInt(columnIndex);

                    // Get filePath
                    String filePath = cursor.getString(columnMetaIndex);

                    // Add recording object to the arraylist
                    Recording recording = new Recording(mApplicationContext, id, filePath);
                    results.add(recording);

                    cursor.moveToNext();
                }
                cursor.close();

                synchronized (mRecodings) {
                    mRecodings.clear();
                    mRecodings.addAll(results);
                }
                notifyRecordingsLoaded();
            }
        });
    }

    private void notifyRecordingsLoaded() {
        MainThread.post(new Runnable() {
            @Override
            public void run() {
                List<Recording> results = new ArrayList<>();
                synchronized (mRecodings) {
                    results.addAll(mRecodings);
                }
                for (OnRecordingsListener listener : mRecordingsListeners) {
                    if (null != listener) {
                        listener.onRecordingsLoaded(results);
                    }
                }
            }
        });
    }

    public List<Recording> getRecordings() {
        List<Recording> results = new ArrayList<>();
        synchronized (mRecodings) {
            results.addAll(mRecodings);
        }
        return results;
    }

    public interface OnRecordingsListener {
        void onRecordingsLoaded(List<Recording> recordings);
    }
}
