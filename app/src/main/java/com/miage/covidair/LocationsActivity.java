package com.miage.covidair;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.service.LocationSearchService;
import com.miage.covidair.adapter.LocationAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LocationsActivity extends AppCompatActivity {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private LocationAdapter mLocationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Instanciate a CityAdpater with empty content
        String city = getIntent().getStringExtra("city");
        mLocationAdapter = new LocationAdapter(this, new ArrayList<>(), city);
        mRecyclerView.setAdapter(mLocationAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

        // Register to Event bus : now each time an event is posted, the activity will receive it if it is @Subscribed to this event
        EventBusManager.BUS.register(this);
        String city = getIntent().getStringExtra("city");
        LocationSearchService.INSTANCE.searchFromAPI("locations?city="+city+"&country=FR&limit=10000");
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void searchResult(final SearchLocationResultEvent event) {
        // Here someone has posted a SearchCityResultEvent
        // Update adapter's model
        mLocationAdapter.setLocations(event.getLocations());
        runOnUiThread(() -> mLocationAdapter.notifyDataSetChanged());
    }

}