package com.miage.covidair.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.LocationsActivity;
import com.miage.covidair.R;
import com.miage.covidair.model.Detail;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailsViewHolder> {
    private LayoutInflater inflater;
    private Activity context;
    private List<Detail> mDetails;

    public DetailAdapter(Activity context, List<Detail> details) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mDetails = details;
    }

    @Override
    public DetailAdapter.DetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.detail_item, parent, false);
        DetailAdapter.DetailsViewHolder holder = new DetailAdapter.DetailsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DetailAdapter.DetailsViewHolder holder, int position) {
        // Adapt the ViewHolder state to the new element
        final Detail detail = mDetails.get(position);
        holder.mDetailParameter.setText(detail.getParameter());
        holder.mDetailValue.setText(detail.getValue());
        holder.mDetailUnit.setText(detail.getUnit());
        holder.mDetailAveragingPeriod.setText(detail.getAveragingPeriod());
        holder.mDetailValueLabel.setText("Value :");
        holder.mDetailUnitLabel.setText("Unit :");
        holder.mDetailAveragingPeriodLabel.setText("Averaging period :");

    }

    @Override
    public int getItemCount() {
        return mDetails.size();
    }

    public void setDetails(List<Detail> details) {
        this.mDetails = details;
    }

    // Pattern ViewHolder
    class DetailsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.detail_adapter_parameter)
        TextView mDetailParameter;

        @BindView(R.id.detail_adapter_value)
        TextView mDetailValue;

        @BindView(R.id.detail_adapter_unit)
        TextView mDetailUnit;

        @BindView(R.id.detail_adapter_averagingPeriod)
        TextView mDetailAveragingPeriod;

        @BindView(R.id.detail_adapter_valueLabel)
        TextView mDetailValueLabel;

        @BindView(R.id.detail_adapter_unitLabel)
        TextView mDetailUnitLabel;

        @BindView(R.id.detail_adapter_averagingPeriodLabel)
        TextView mDetailAveragingPeriodLabel;


        public DetailsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}