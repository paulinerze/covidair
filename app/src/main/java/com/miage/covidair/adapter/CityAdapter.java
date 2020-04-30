package com.miage.covidair.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.LocationsActivity;
import com.miage.covidair.R;
import com.miage.covidair.model.City.City;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private LayoutInflater inflater;
    private Activity context;
    private List<City> mCities;

    public CityAdapter(Activity context, List<City> cities) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mCities = cities;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.city_item, parent, false);
        CityViewHolder holder = new CityViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {
        // Adapt the ViewHolder state to the new element
        final City city = mCities.get(position);
        holder.mCityName.setText(city.getName());
        holder.mCityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open city details activity
                Intent seeCityDetailIntent = new Intent(context, LocationsActivity.class);
                seeCityDetailIntent.putExtra("city", city.getName());
                context.startActivity(seeCityDetailIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCities.size();
    }

    public void setCities(List<City> cities) {
        this.mCities = cities;
    }

    // Pattern ViewHolder
    class CityViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.city_adapter_name)
        TextView mCityName;

        @BindView(R.id.city_adapter_icon)
        ImageView mCityIcon;

        @BindView(R.id.cityLayout)
        LinearLayout mCityLayout;

        public CityViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}