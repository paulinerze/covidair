package com.miage.covidair.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.R;
import com.miage.covidair.model.Location.Location;
import com.miage.covidair.service.LoadImage;
import com.miage.covidair.service.LocationSearchService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.MeasurementsViewHolder> {
    private LayoutInflater inflater;
    private Activity context;
    private List<Location> mLocations;

    public DetailAdapter(Activity context, List<Location> locations) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mLocations = locations;
    }

    @Override
    public MeasurementsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.detail_item, parent, false);
        MeasurementsViewHolder holder = new MeasurementsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MeasurementsViewHolder holder, int position) {
        // Adapt the ViewHolder state to the new element
        final Location location = mLocations.get(position);
        holder.mLocation.setText(location.location);
        LoadImage loadImage = new LoadImage(holder.mPhoto);
        loadImage.execute(location.latitude + "," + location.getLongitude());
        String.valueOf(location.getSol());
        holder.mTemperatureValue.setText(String.valueOf(location.getSol()));
        holder.mPluieValue.setText(String.valueOf(location.pluie));
        holder.mVentValue.setText(String.valueOf(location.vent));
        holder.mLastUpdated.setText(location.lastUpdated);


        if (!LocationSearchService.INSTANCE.isFavorite(location.location)) {
            holder.mAddButton.setVisibility(View.VISIBLE);
            holder.mRemoveButton.setVisibility(View.GONE);
        } else {
            holder.mAddButton.setVisibility(View.GONE);
            holder.mRemoveButton.setVisibility(View.VISIBLE);
        }


        if (location.getLatestMeasurements() != null && !location.getLatestMeasurements().isEmpty()) {
            if (location.getLatestMeasurements().containsKey("bc")) {
                holder.mBCLayout.setVisibility(View.VISIBLE);
                holder.mLatestBC.setText(location.getLatestMeasurements().get("bc").parameter);
                holder.mLatestBCValue.setText(location.getLatestMeasurements().get("bc").value);
                holder.mLatestBCUnit.setText(location.getLatestMeasurements().get("bc").unit);
            } else {
                holder.mBCLayout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("co")) {
                holder.mCOLayout.setVisibility(View.VISIBLE);
                holder.mLatestCO.setText(location.getLatestMeasurements().get("co").parameter);
                holder.mLatestCOValue.setText(location.getLatestMeasurements().get("co").value);
                holder.mLatestCOUnit.setText(location.getLatestMeasurements().get("co").unit);
            } else {
                holder.mCOLayout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("no2")) {
                holder.mNO2Layout.setVisibility(View.VISIBLE);
                holder.mLatestNO2.setText(location.getLatestMeasurements().get("no2").parameter);
                holder.mLatestNO2Value.setText(location.getLatestMeasurements().get("no2").value);
                holder.mLatestNO2Unit.setText(location.getLatestMeasurements().get("no2").unit);
            } else {
                holder.mNO2Layout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("o3")) {
                holder.mO3Layout.setVisibility(View.VISIBLE);
                holder.mLatestO3.setText(location.getLatestMeasurements().get("o3").parameter);
                holder.mLatestO3Value.setText(location.getLatestMeasurements().get("o3").value);
                holder.mLatestO3Unit.setText(location.getLatestMeasurements().get("o3").unit);
            } else {
                holder.mO3Layout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("pm10")) {
                holder.mPM10Layout.setVisibility(View.VISIBLE);
                holder.mLatestPM10.setText(location.getLatestMeasurements().get("pm10").parameter);
                holder.mLatestPM10Value.setText(location.getLatestMeasurements().get("pm10").value);
                holder.mLatestPM10Unit.setText(location.getLatestMeasurements().get("pm10").unit);
            } else {
                holder.mPM10Layout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("pm25")) {
                holder.mPM25Layout.setVisibility(View.VISIBLE);
                holder.mLatestPM25.setText(location.getLatestMeasurements().get("pm25").parameter);
                holder.mLatestPM25Value.setText(location.getLatestMeasurements().get("pm25").value);
                holder.mLatestPM25Unit.setText(location.getLatestMeasurements().get("pm25").unit);
            } else {
                holder.mPM25Layout.setVisibility(View.GONE);
            }
            if (location.getLatestMeasurements().containsKey("so2")) {
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
        }
    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }

    public void setMeasurements(List<Location> locations) {
        this.mLocations = locations;
    }

    // Pattern ViewHolder
    class MeasurementsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.detail_adapter_location)
        TextView mLocation;
        @BindView(R.id.detail_item_photo)
        ImageView mPhoto;
        @BindView(R.id.detail_adapter_temperature_value)
        TextView mTemperatureValue;
        @BindView(R.id.detail_adapter_vent_value)
        TextView mVentValue;
        @BindView(R.id.detail_adapter_pluie_value)
        TextView mPluieValue;
        @BindView(R.id.add_button)
        LinearLayout mAddButton;
        @BindView(R.id.remove_button)
        LinearLayout mRemoveButton;
        @BindView(R.id.detail_adapter_lastUpdated)
        TextView mLastUpdated;


        //
        @BindView(R.id.detail_bc)
        LinearLayout mBCLayout;
        @BindView(R.id.detail_adapter_latest_bc)
        TextView mLatestBC;
        @BindView(R.id.detail_adapter_latest_bc_value)
        TextView mLatestBCValue;
        @BindView(R.id.detail_adapter_latest_bc_unit)
        TextView mLatestBCUnit;
        //
        @BindView(R.id.detail_co)
        LinearLayout mCOLayout;
        @BindView(R.id.detail_adapter_latest_co)
        TextView mLatestCO;
        @BindView(R.id.detail_adapter_latest_co_value)
        TextView mLatestCOValue;
        @BindView(R.id.detail_adapter_latest_co_unit)
        TextView mLatestCOUnit;
        //
        @BindView(R.id.detail_no2)
        LinearLayout mNO2Layout;
        @BindView(R.id.detail_adapter_latest_no2)
        TextView mLatestNO2;
        @BindView(R.id.detail_adapter_latest_no2_value)
        TextView mLatestNO2Value;
        @BindView(R.id.detail_adapter_latest_no2_unit)
        TextView mLatestNO2Unit;
        //
        @BindView(R.id.detail_o3)
        LinearLayout mO3Layout;
        @BindView(R.id.detail_adapter_latest_o3)
        TextView mLatestO3;
        @BindView(R.id.detail_adapter_latest_o3_value)
        TextView mLatestO3Value;
        @BindView(R.id.detail_adapter_latest_o3_unit)
        TextView mLatestO3Unit;
        //
        @BindView(R.id.detail_pm10)
        LinearLayout mPM10Layout;
        @BindView(R.id.detail_adapter_latest_pm10)
        TextView mLatestPM10;
        @BindView(R.id.detail_adapter_latest_pm10_value)
        TextView mLatestPM10Value;
        @BindView(R.id.detail_adapter_latest_pm10_unit)
        TextView mLatestPM10Unit;
        //
        @BindView(R.id.detail_pm25)
        LinearLayout mPM25Layout;
        @BindView(R.id.detail_adapter_latest_pm25)
        TextView mLatestPM25;
        @BindView(R.id.detail_adapter_latest_pm25_value)
        TextView mLatestPM25Value;
        @BindView(R.id.detail_adapter_latest_pm25_unit)
        TextView mLatestPM25Unit;
        //
        @BindView(R.id.detail_so2)
        LinearLayout mSO2Layout;
        @BindView(R.id.detail_adapter_latest_so2)
        TextView mLatestSO2;
        @BindView(R.id.detail_adapter_latest_so2_value)
        TextView mLatestSO2Value;
        @BindView(R.id.detail_adapter_latest_so2_unit)
        TextView mLatestSO2Unit;


        public MeasurementsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}