package com.miage.covidair.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.DetailActivity;
import com.miage.covidair.R;
import com.miage.covidair.model.Location;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationsViewHolder>{

    private LayoutInflater inflater;
    private Activity context;
    private List<Location> mLocations;
    private String mCity;

    public LocationAdapter(Activity context, List<Location> locations, String city) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mLocations = locations;
        this.mCity = city;
    }

    @Override
    public LocationAdapter.LocationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.locations_item, parent, false);
        LocationAdapter.LocationsViewHolder holder = new LocationAdapter.LocationsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(LocationAdapter.LocationsViewHolder holder, int position) {
        // Adapt the ViewHolder state to the new element
        final Location location = mLocations.get(position);
        holder.mLocation.setText(location.getLocation());
        holder.mCount.setText(location.getCount());
        holder.mLastUpdated.setText(location.getLastUpdated());
        holder.mCountLabel.setText("Count :");
        holder.mLastUpdatedLabel.setText("Last updated :");
        holder.mLocationsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Play mp3
                AssetFileDescriptor afd = null;
                try {
                    afd = context.getAssets().openFd("house.mp3");

                    MediaPlayer player = new MediaPlayer();
                    player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                    player.prepare();
                    player.start();

                } catch (IOException e) {
                    // Silent catch : sound will not be played
                    e.printStackTrace();
                }

                // Open locations details activity
                Intent seeCityDetailIntent = new Intent(context, DetailActivity.class);
                seeCityDetailIntent.putExtra("location", location.getLocation());
                seeCityDetailIntent.putExtra("city", mCity);
                context.startActivity(seeCityDetailIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }

    public void setLocations(List<Location> locations) {
        this.mLocations = locations;
    }

    // Pattern ViewHolder
    class LocationsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.locations_adapter_icon)
        ImageView mLocationsIcon;

        @BindView(R.id.location_adapter_location)
        TextView mLocation;

        @BindView(R.id.location_adapter_countLabel)
        TextView mCountLabel;

        @BindView(R.id.location_adapter_count)
        TextView mCount;

        @BindView(R.id.location_adapter_lastUpdatedLabel)
        TextView mLastUpdatedLabel;

        @BindView(R.id.location_adapter_lastUpdated)
        TextView mLastUpdated;


        public LocationsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
