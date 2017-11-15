package com.opendashcam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.opendashcam.models.Recording;
import com.opendashcam.models.RecordingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to view video recordings produced by this dash cam application
 * Displays all videos from paths matching %OpenDashCam%
 */

public class ViewRecordingsActivity extends AppCompatActivity implements ViewRecordingsRecyclerViewAdapter.RecordingClickListener,
        RecordingsManager.OnRecordingsListener {


    private RecyclerView recyclerView;
    private ViewRecordingsRecyclerViewAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static String LOG_TAG = "CardViewActivity";

    private Context context;
    private BroadcastReceiver mReceiver;

    /**
     * Sets RecyclerView for gallery
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_view_recordings);

        // set RecyclerView for gallery
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ViewRecordingsRecyclerViewAdapter(context);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                            adapter.notifyDataSetChanged();
                        } else {
                            recyclerView.postDelayed(this, 1000);
                        }
                    }
                });
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mReceiver);
        RecordingsManager.sharedInstance().removeOnRecordingsListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver = null;
    }

    /**
     * Adds onClick listener to play the recording
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, new IntentFilter(Recording.ACTION_DATA_LOADED));
        adapter.updateRecordings(RecordingsManager.sharedInstance().getRecordings());
        RecordingsManager.sharedInstance().addOnRecordingsListener(this);
        updateEmptyView();
    }

    @Override
    public void onItemClick(int position, View v) {
        Log.i(LOG_TAG, " Clicked on Item " + position);

        Recording recording = adapter.getItem(position);
        // Play recording on position
        Util.showToast(context, recording.getDateSaved() + " - " + recording.getTimeSaved());
        Util.openFile(context, Uri.fromFile(new File(recording.getFilePath())), "video/mp4");
    }

    @Override
    public void onRecordingsLoaded(List<Recording> recordings) {
        adapter.updateRecordings(RecordingsManager.sharedInstance().getRecordings());
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() == 0) {
            findViewById(R.id.emptyview).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.emptyview).setVisibility(View.GONE);
        }
    }
}
