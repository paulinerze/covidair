package com.miage.covidair;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.adapter.LocationAdapter;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.service.CitySearchService;
import com.miage.covidair.service.LocationSearchService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BigSearchActivity extends AppCompatActivity {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private LocationAdapter mLocationAdapter;
    @BindView(R.id.activity_main_loader)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Instanciate a CityAdpater with empty content
        String city = getIntent().getStringExtra("zone");
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

        String zone = getIntent().getStringExtra("zone");
        String nom = getIntent().getStringExtra("nom");
        String minBC = getIntent().getStringExtra("minBC");
        String maxBC = getIntent().getStringExtra("maxBC");
        String minCO = getIntent().getStringExtra("minCO");
        String maxCO = getIntent().getStringExtra("maxCO");
        String minNO2 = getIntent().getStringExtra("minNO2");
        String maxNO2 = getIntent().getStringExtra("maxNO2");
        String minO3 = getIntent().getStringExtra("minO3");
        String maxO3 = getIntent().getStringExtra("maxO3");
        String minPM10 = getIntent().getStringExtra("minPM10");
        String maxPM10 = getIntent().getStringExtra("maxPM10");
        String minPM25 = getIntent().getStringExtra("minPM25");
        String maxPM25 = getIntent().getStringExtra("maxPM25");
        String minSO2 = getIntent().getStringExtra("minSO2");
        String maxSO2 = getIntent().getStringExtra("maxSO2");

        LocationSearchService.INSTANCE.bigSearch(zone, nom, minBC, maxBC, minCO, maxCO, minNO2, maxNO2, minO3, maxO3, minPM10, maxPM10, minPM25, maxPM25, minSO2, maxSO2);


    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void searchResultLocation(final SearchLocationResultEvent event) {
        // Here someone has posted a SearchCityResultEvent
        // Update adapter's model
        mLocationAdapter.setLocations(event.getLocations());
        runOnUiThread(() -> mLocationAdapter.notifyDataSetChanged());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                Intent listeIntent = new Intent(this, MainActivity.class);
                startActivity(listeIntent);
                return true;
            case R.id.action_favorite:
                Intent favoriteIntent = new Intent(this, FavoriteActivity.class);
                startActivity(favoriteIntent);
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.action_map:
                Intent mapIntent = new Intent(this, MapActivity.class);
                startActivity(mapIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}