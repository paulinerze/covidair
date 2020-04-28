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
import com.miage.covidair.model.Favorite;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoritesViewHolder>{

    private LayoutInflater inflater;
    private Activity context;
    private List<Favorite> mFavorites;

    public FavoriteAdapter(Activity context, List<Favorite> favorites) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mFavorites = favorites;
    }

    @Override
    public FavoriteAdapter.FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.favorite_item, parent, false);
        FavoriteAdapter.FavoritesViewHolder holder = new FavoriteAdapter.FavoritesViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(FavoriteAdapter.FavoritesViewHolder holder, int position) {
        // Adapt the ViewHolder state to the new element
        final Favorite favorite = mFavorites.get(position);
        holder.mLocation.setText(favorite.getLocation());
        holder.mCount.setText(favorite.getCount());
        holder.mLastUpdated.setText(favorite.getLastUpdated());

        if (favorite.getSol() != null){
            holder.mTemperatureLayout.setVisibility(View.VISIBLE);
            String.valueOf(favorite.getSol());
            holder.mTemperatureValue.setText(String.valueOf(favorite.getSol()));
        } else{
            holder.mTemperatureLayout.setVisibility(View.GONE);
        }


        if (favorite.getLatestMeasurements() != null && !favorite.getLatestMeasurements().isEmpty()){
            if (favorite.getLatestMeasurements().containsKey("bc")){
                holder.mBCLayout.setVisibility(View.VISIBLE);
                holder.mLatestBC.setText(favorite.getLatestMeasurements().get("bc").parameter);
                holder.mLatestBCValue.setText(favorite.getLatestMeasurements().get("bc").value);
                holder.mLatestBCUnit.setText(favorite.getLatestMeasurements().get("bc").unit);
            } else {
                holder.mBCLayout.setVisibility(View.GONE);
            }
            if (favorite.getLatestMeasurements().containsKey("co")){
                holder.mCOLayout.setVisibility(View.VISIBLE);
                holder.mLatestCO.setText(favorite.getLatestMeasurements().get("co").parameter);
                holder.mLatestCOValue.setText(favorite.getLatestMeasurements().get("co").value);
                holder.mLatestCOUnit.setText(favorite.getLatestMeasurements().get("co").unit);
            } else {
                holder.mCOLayout.setVisibility(View.GONE);
            }
            if (favorite.getLatestMeasurements().containsKey("no2")){
                holder.mNO2Layout.setVisibility(View.VISIBLE);
                holder.mLatestNO2.setText(favorite.getLatestMeasurements().get("no2").parameter);
                holder.mLatestNO2Value.setText(favorite.getLatestMeasurements().get("no2").value);
                holder.mLatestNO2Unit.setText(favorite.getLatestMeasurements().get("no2").unit);
            } else {
                holder.mNO2Layout.setVisibility(View.GONE);
            }
            if (favorite.getLatestMeasurements().containsKey("o3")){
                holder.mO3Layout.setVisibility(View.VISIBLE);
                holder.mLatestO3.setText(favorite.getLatestMeasurements().get("o3").parameter);
                holder.mLatestO3Value.setText(favorite.getLatestMeasurements().get("o3").value);
                holder.mLatestO3Unit.setText(favorite.getLatestMeasurements().get("o3").unit);
            } else {
                holder.mO3Layout.setVisibility(View.GONE);
            }
            if (favorite.getLatestMeasurements().containsKey("pm10")){
                holder.mPM10Layout.setVisibility(View.VISIBLE);
                holder.mLatestPM10.setText(favorite.getLatestMeasurements().get("pm10").parameter);
                holder.mLatestPM10Value.setText(favorite.getLatestMeasurements().get("pm10").value);
                holder.mLatestPM10Unit.setText(favorite.getLatestMeasurements().get("pm10").unit);
            } else {
                holder.mPM10Layout.setVisibility(View.GONE);
            }
            if (favorite.getLatestMeasurements().containsKey("pm25")){
                holder.mPM25Layout.setVisibility(View.VISIBLE);
                holder.mLatestPM25.setText(favorite.getLatestMeasurements().get("pm25").parameter);
                holder.mLatestPM25Value.setText(favorite.getLatestMeasurements().get("pm25").value);
                holder.mLatestPM25Unit.setText(favorite.getLatestMeasurements().get("pm25").unit);
            } else {
                holder.mPM25Layout.setVisibility(View.GONE);
            }
            if (favorite.getLatestMeasurements().containsKey("so2")){
                holder.mSO2Layout.setVisibility(View.VISIBLE);
                holder.mLatestSO2.setText(favorite.getLatestMeasurements().get("so2").parameter);
                holder.mLatestSO2Value.setText(favorite.getLatestMeasurements().get("so2").value);
                holder.mLatestSO2Unit.setText(favorite.getLatestMeasurements().get("so2").unit);
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

    }

    @Override
    public int getItemCount() {
        return mFavorites.size();
    }

    public void setFavorites(List<Favorite> favorites) {
        this.mFavorites = favorites;
    }

    // Pattern ViewHolder
    class FavoritesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.favorite_adapter_icon)
        ImageView mLocationsIcon;

        @BindView(R.id.favorite_adapter_location)
        TextView mLocation;

        @BindView(R.id.favorite_adapter_count)
        TextView mCount;

        @BindView(R.id.favorite_adapter_lastUpdated)
        TextView mLastUpdated;

        @BindView(R.id.temperature_layout)
        LinearLayout mTemperatureLayout;
        @BindView(R.id.favorite_adapter_temperature_value)
        TextView mTemperatureValue;
        //
        @BindView(R.id.favorite_bc)
        LinearLayout mBCLayout;
        @BindView(R.id.favorite_adapter_latest_bc)
        TextView mLatestBC;
        @BindView(R.id.favorite_adapter_latest_bc_value)
        TextView mLatestBCValue;
        @BindView(R.id.favorite_adapter_latest_bc_unit)
        TextView mLatestBCUnit;
        //
        @BindView(R.id.favorite_co)
        LinearLayout mCOLayout;
        @BindView(R.id.favorite_adapter_latest_co)
        TextView mLatestCO;
        @BindView(R.id.favorite_adapter_latest_co_value)
        TextView mLatestCOValue;
        @BindView(R.id.favorite_adapter_latest_co_unit)
        TextView mLatestCOUnit;
        //
        @BindView(R.id.favorite_no2)
        LinearLayout mNO2Layout;
        @BindView(R.id.favorite_adapter_latest_no2)
        TextView mLatestNO2;
        @BindView(R.id.favorite_adapter_latest_no2_value)
        TextView mLatestNO2Value;
        @BindView(R.id.favorite_adapter_latest_no2_unit)
        TextView mLatestNO2Unit;
        //
        @BindView(R.id.favorite_o3)
        LinearLayout mO3Layout;
        @BindView(R.id.favorite_adapter_latest_o3)
        TextView mLatestO3;
        @BindView(R.id.favorite_adapter_latest_o3_value)
        TextView mLatestO3Value;
        @BindView(R.id.favorite_adapter_latest_o3_unit)
        TextView mLatestO3Unit;
        //
        @BindView(R.id.favorite_pm10)
        LinearLayout mPM10Layout;
        @BindView(R.id.favorite_adapter_latest_pm10)
        TextView mLatestPM10;
        @BindView(R.id.favorite_adapter_latest_pm10_value)
        TextView mLatestPM10Value;
        @BindView(R.id.favorite_adapter_latest_pm10_unit)
        TextView mLatestPM10Unit;
        //
        @BindView(R.id.favorite_pm25)
        LinearLayout mPM25Layout;
        @BindView(R.id.favorite_adapter_latest_pm25)
        TextView mLatestPM25;
        @BindView(R.id.favorite_adapter_latest_pm25_value)
        TextView mLatestPM25Value;
        @BindView(R.id.favorite_adapter_latest_pm25_unit)
        TextView mLatestPM25Unit;
        //
        @BindView(R.id.favorite_so2)
        LinearLayout mSO2Layout;
        @BindView(R.id.favorite_adapter_latest_so2)
        TextView mLatestSO2;
        @BindView(R.id.favorite_adapter_latest_so2_value)
        TextView mLatestSO2Value;
        @BindView(R.id.favorite_adapter_latest_so2_unit)
        TextView mLatestSO2Unit;

        public FavoritesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
