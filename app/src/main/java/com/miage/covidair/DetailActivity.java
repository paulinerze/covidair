package com.miage.covidair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.adapter.DetailAdapter;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchMeasurementResultEvent;
import com.miage.covidair.service.DetailSearchService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity{

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private DetailAdapter mDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Instanciate a CityAdpater with empty content
        mDetailAdapter = new DetailAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mDetailAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

        // Register to Event bus : now each time an event is posted, the activity will receive it if it is @Subscribed to this event
        EventBusManager.BUS.register(this);
        String location = getIntent().getStringExtra("location");
        String longitude = getIntent().getStringExtra("longitude");
        String latitude = getIntent().getStringExtra("latitude");
        String city = getIntent().getStringExtra("city");
        DetailSearchService.INSTANCE.searchDetails(city,location,longitude,latitude);
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void searchMeasurement(final SearchMeasurementResultEvent event) {
        // Here someone has posted a SearchCityResultEvent
        // Update adapter's model
        mDetailAdapter.setMeasurements(event.getDetails());
        runOnUiThread(() -> mDetailAdapter.notifyDataSetChanged());
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                return true;
            case R.id.action_favorite:
                /* DO ADD */
                return true;
            case R.id.action_map:
                /* DO DELETE */
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
