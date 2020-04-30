package com.miage.covidair;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.adapter.DetailAdapter;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.service.LocationSearchService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private DetailAdapter mDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main);

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
        String longitude = getIntent().getStringExtra("longitude");
        String latitude = getIntent().getStringExtra("latitude");
        LocationSearchService.INSTANCE.searchLocationFromDB(longitude, latitude);
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void searchDetail(final SearchLocationResultEvent event) {
        // Here someone has posted a SearchCityResultEvent
        // Update adapter's model
        mDetailAdapter.setMeasurements(event.getLocations());
        runOnUiThread(() -> mDetailAdapter.notifyDataSetChanged());
    }

    /**
     * Called when the user touches the button
     */
    public void saveToFavorites(View view) {
        String latitude = getIntent().getStringExtra("latitude");
        String longitude = getIntent().getStringExtra("longitude");
        LocationSearchService.INSTANCE.addToFavorites(latitude, longitude);
    }

    /**
     * Called when the user touches the button
     */
    public void removeFromFavorites(View view) {
        String latitude = getIntent().getStringExtra("latitude");
        String longitude = getIntent().getStringExtra("longitude");
        LocationSearchService.INSTANCE.rmFromFavorites(latitude, longitude);
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
