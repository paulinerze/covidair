package com.miage.covidair.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.R;
import com.miage.covidair.model.Measurement.Measurement;
import com.miage.covidair.service.LoadImage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.MeasurementsViewHolder> {
    private LayoutInflater inflater;
    private Activity context;
    private List<Measurement> mMeasurement;

    public DetailAdapter(Activity context, List<Measurement> measurements) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mMeasurement = measurements;
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
        final Measurement measurement = mMeasurement.get(position);
        holder.mMeasurementParameter.setText(measurement.getParameter());
        holder.mMeasurementValue.setText(measurement.getValue());
        holder.mMeasurementUnit.setText(measurement.getUnit());
        holder.mMeasurementDate.setText(measurement.getDisplayDate());

        LoadImage loadImage = new LoadImage(holder.mPhoto);
        loadImage.execute(measurement.latitude + "," + measurement.getLongitude());

    }

    @Override
    public int getItemCount() {
        return mMeasurement.size();
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.mMeasurement = measurements;
    }

    // Pattern ViewHolder
    class MeasurementsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.measurement_adapter_parameter)
        TextView mMeasurementParameter;

        @BindView(R.id.measurement_adapter_value)
        TextView mMeasurementValue;

        @BindView(R.id.measurement_adapter_unit)
        TextView mMeasurementUnit;

        @BindView(R.id.measurement_adapter_date)
        TextView mMeasurementDate;

        @BindView(R.id.detail_item_photo)
        ImageView mPhoto;


        public MeasurementsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}