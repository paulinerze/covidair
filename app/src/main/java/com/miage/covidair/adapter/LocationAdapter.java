package com.miage.covidair.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.DetailActivity;
import com.miage.covidair.R;
import com.miage.covidair.model.Location.Loca;
import com.miage.covidair.model.Weather.Weather;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationsViewHolder>{

    private LayoutInflater inflater;
    private Activity context;
    private List<Loca> mLocations;
    private String mCity;

    public LocationAdapter(Activity context, List<Loca> locations, String city) {
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
        final Loca location = mLocations.get(position);
        holder.mLocation.setText(location.getLocation());
        holder.mCount.setText(location.getCount());
        holder.mLastUpdated.setText(location.getLastUpdated());

        if (location.getSol() != null){
            holder.mTemperatureLayout.setVisibility(View.VISIBLE);
            String.valueOf(location.getSol());
            holder.mTemperatureValue.setText(String.valueOf(location.getSol()));
        } else{
            holder.mTemperatureLayout.setVisibility(View.GONE);
        }


        if (location.getLatestMeasurements() != null && !location.getLatestMeasurements().isEmpty()){
            if (location.getLatestMeasurements().containsKey("bc")){
                holder.mBCLayout.setVisibility(View.VISIBLE);
                holder.mLatestBC.setText(location.getLatestMeasurements().get("bc").parameter);
                holder.mLatestBCValue.setText(location.getLatestMeasurements().get("bc").value);
                holder.mLatestBCUnit.setText(location.getLatestMeasurements().get("bc").unit);
            } else {
                holder.mBCLayout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("co")){
                holder.mCOLayout.setVisibility(View.VISIBLE);
                holder.mLatestCO.setText(location.getLatestMeasurements().get("co").parameter);
                holder.mLatestCOValue.setText(location.getLatestMeasurements().get("co").value);
                holder.mLatestCOUnit.setText(location.getLatestMeasurements().get("co").unit);
            } else {
                holder.mCOLayout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("no2")){
                holder.mNO2Layout.setVisibility(View.VISIBLE);
                holder.mLatestNO2.setText(location.getLatestMeasurements().get("no2").parameter);
                holder.mLatestNO2Value.setText(location.getLatestMeasurements().get("no2").value);
                holder.mLatestNO2Unit.setText(location.getLatestMeasurements().get("no2").unit);
            } else {
                holder.mNO2Layout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("o3")){
                holder.mO3Layout.setVisibility(View.VISIBLE);
                holder.mLatestO3.setText(location.getLatestMeasurements().get("o3").parameter);
                holder.mLatestO3Value.setText(location.getLatestMeasurements().get("o3").value);
                holder.mLatestO3Unit.setText(location.getLatestMeasurements().get("o3").unit);
            } else {
                holder.mO3Layout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("pm10")){
                holder.mPM10Layout.setVisibility(View.VISIBLE);
                holder.mLatestPM10.setText(location.getLatestMeasurements().get("pm10").parameter);
                holder.mLatestPM10Value.setText(location.getLatestMeasurements().get("pm10").value);
                holder.mLatestPM10Unit.setText(location.getLatestMeasurements().get("pm10").unit);
            } else {
                holder.mPM10Layout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("pm25")){
                holder.mPM25Layout.setVisibility(View.VISIBLE);
                holder.mLatestPM25.setText(location.getLatestMeasurements().get("pm25").parameter);
                holder.mLatestPM25Value.setText(location.getLatestMeasurements().get("pm25").value);
                holder.mLatestPM25Unit.setText(location.getLatestMeasurements().get("pm25").unit);
            } else {
                holder.mPM25Layout.setVisibility(View.GONE);
             }
            if (location.getLatestMeasurements().containsKey("so2")){
                holder.mSO2Layout.setVisibility(View.VISIBLE);
                holder.mLatestSO2.setText(location.getLatestMeasurements().get("so2").parameter);
                holder.mLatestSO2Value.setText(location.getLatestMeasurements().get("so2").value);
                holder.mLatestSO2Unit.setText(location.getLatestMeasurements().get("so2").unit);
            } else {
                holder.mSO2Layout.setVisibility(View.GONE);
            }
        } else {
            holder.mBCLayout.setVisibility(View.GONE);
            holder.mCOLayout.setVisibility(View.GONE);
            holder.mNO2Layout.setVisibility(View.GONE);
            holder.mO3Layout.setVisibility(View.GONE);
            holder.mPM10Layout.setVisibility(View.GONE);
            holder.mPM25Layout.setVisibility(View.GONE);
            holder.mSO2Layout.setVisibility(View.GONE);
            //TODO : faire un layout pour indiquer N/A
        }


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
                seeCityDetailIntent.putExtra("longitude", location.longitude);
                seeCityDetailIntent.putExtra("latitude", location.latitude);
                seeCityDetailIntent.putExtra("city", mCity);
                context.startActivity(seeCityDetailIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }

    public void setLocations(List<Loca> locations) {
        this.mLocations = locations;
    }

    // Pattern ViewHolder
    class LocationsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.locations_adapter_icon)
        ImageView mLocationsIcon;

        @BindView(R.id.location_adapter_location)
        TextView mLocation;

        @BindView(R.id.location_adapter_count)
        TextView mCount;

        @BindView(R.id.location_adapter_lastUpdated)
        TextView mLastUpdated;

        @BindView(R.id.temperature_layout)
        LinearLayout mTemperatureLayout;
        @BindView(R.id.location_adapter_temperature_value)
        TextView mTemperatureValue;
        //
        @BindView(R.id.bc)
        LinearLayout mBCLayout;
        @BindView(R.id.location_adapter_latest_bc)
        TextView mLatestBC;
        @BindView(R.id.location_adapter_latest_bc_value)
        TextView mLatestBCValue;
        @BindView(R.id.location_adapter_latest_bc_unit)
        TextView mLatestBCUnit;
        //
        @BindView(R.id.co)
        LinearLayout mCOLayout;
        @BindView(R.id.location_adapter_latest_co)
        TextView mLatestCO;
        @BindView(R.id.location_adapter_latest_co_value)
        TextView mLatestCOValue;
        @BindView(R.id.location_adapter_latest_co_unit)
        TextView mLatestCOUnit;
        //
        @BindView(R.id.no2)
        LinearLayout mNO2Layout;
        @BindView(R.id.location_adapter_latest_no2)
        TextView mLatestNO2;
        @BindView(R.id.location_adapter_latest_no2_value)
        TextView mLatestNO2Value;
        @BindView(R.id.location_adapter_latest_no2_unit)
        TextView mLatestNO2Unit;
        //
        @BindView(R.id.o3)
        LinearLayout mO3Layout;
        @BindView(R.id.location_adapter_latest_o3)
        TextView mLatestO3;
        @BindView(R.id.location_adapter_latest_o3_value)
        TextView mLatestO3Value;
        @BindView(R.id.location_adapter_latest_o3_unit)
        TextView mLatestO3Unit;
        //
        @BindView(R.id.pm10)
        LinearLayout mPM10Layout;
        @BindView(R.id.location_adapter_latest_pm10)
        TextView mLatestPM10;
        @BindView(R.id.location_adapter_latest_pm10_value)
        TextView mLatestPM10Value;
        @BindView(R.id.location_adapter_latest_pm10_unit)
        TextView mLatestPM10Unit;
        //
        @BindView(R.id.pm25)
        LinearLayout mPM25Layout;
        @BindView(R.id.location_adapter_latest_pm25)
        TextView mLatestPM25;
        @BindView(R.id.location_adapter_latest_pm25_value)
        TextView mLatestPM25Value;
        @BindView(R.id.location_adapter_latest_pm25_unit)
        TextView mLatestPM25Unit;
        //
        @BindView(R.id.so2)
        LinearLayout mSO2Layout;
        @BindView(R.id.location_adapter_latest_so2)
        TextView mLatestSO2;
        @BindView(R.id.location_adapter_latest_so2_value)
        TextView mLatestSO2Value;
        @BindView(R.id.location_adapter_latest_so2_unit)
        TextView mLatestSO2Unit;

        public LocationsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
