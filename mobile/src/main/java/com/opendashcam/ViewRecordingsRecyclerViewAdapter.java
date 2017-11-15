package com.opendashcam;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.opendashcam.models.Recording;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to display video recordings in ViewRecordingsActivity.
 * Supplies data to the RecyclerView for display.
 */

public class ViewRecordingsRecyclerViewAdapter extends RecyclerView
        .Adapter<ViewRecordingsRecyclerViewAdapter
        .RecordingHolder> {

    private static String LOG_TAG = "ViewRecordingsRecycl...";
    private final List<Recording> recordings = new ArrayList<>();
    private static RecordingClickListener recordingClickListener;
    private Context context;
    private int width, height;

    public ViewRecordingsRecyclerViewAdapter(Context appContext) {
        context = appContext;
        width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics());
    }

    public void updateRecordings(List<Recording> recordings) {
        this.recordings.clear();
        this.recordings.addAll(recordings);
        notifyDataSetChanged();
    }

    public Recording getItem(int position) {
        return recordings.get(position);
    }

    public static class RecordingHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        ImageView thumbnail;
        TextView label;
        TextView dateTime;
        CheckBox starred;

        public RecordingHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            label = (TextView) itemView.findViewById(R.id.recordingDate);
            dateTime = (TextView) itemView.findViewById(R.id.recordingTime);
            starred = (CheckBox) itemView.findViewById(R.id.starred);
            Log.i(LOG_TAG, "Add Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recordingClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(RecordingClickListener recordingClickListener) {
        this.recordingClickListener = recordingClickListener;
    }

    @Override
    public RecordingHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_recordings_row, parent, false);

        RecordingHolder recordingHolder = new RecordingHolder(view);
        return recordingHolder;
    }

    @Override
    public void onBindViewHolder(RecordingHolder holder, int position) {
        Recording rec = recordings.get(position);
//        holder.thumbnail.setImageBitmap(rec.getThumbnail());
        String sArtworkUri = Uri.withAppendedPath(VideoRequestHandler.THUMBNAIL_IDENTIFIER_URI, String.valueOf(rec.getId())).toString();
        Picasso.with(holder.itemView.getContext())
                .load(sArtworkUri)
                .resize(width, height)
                .noFade().centerCrop()
                .error(R.drawable.quit_widget)
                .placeholder(R.drawable.ic_videocam_red_128dp)
                .into(holder.thumbnail);

        holder.label.setText(recordings.get(position).getDateSaved());
        holder.dateTime.setText(recordings.get(position).getTimeSaved());
        holder.starred.setChecked(recordings.get(position).getStarredStatus());
        holder.starred.setTag(position);
        holder.starred.setOnCheckedChangeListener(starredListener);
    }

    /**
     * Action to happen when starred/unstarred
     */
    CompoundButton.OnCheckedChangeListener starredListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.isPressed()) {
                Recording recording = recordings.get((Integer) buttonView.getTag());
                recording.toggleStar(context, isChecked);
            }
        }
    };

    public void addItem(Recording recording, int index) {
        recordings.add(index, recording);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        recordings.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public interface RecordingClickListener {
        void onItemClick(int position, View v);
    }
}
