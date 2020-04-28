package com.miage.covidair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.model.City.City;
import com.miage.covidair.service.CitySearchService;
import com.miage.covidair.service.LocationSearchService;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mActiveGoogleMap;
    @BindView(R.id.activity_main_loader)
    ProgressBar mProgressBar;
    @BindView(R.id.activity_main_search_adress_edittext)
    EditText mSearchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Get map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CitySearchService.INSTANCE.searchCities(null);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing to do when texte is about to change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // While text is changing, hide list and show loader
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Once text has changed
                // Show a loader
                mProgressBar.setVisibility(View.VISIBLE);

                // Launch a search through the PlaceSearchService
                CitySearchService.INSTANCE.searchCities(editable.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

        // Register to Event bus : now each time an event is posted, the activity will receive it if it is @Subscribed to this event
        EventBusManager.BUS.register(this);
        //CitySearchService.INSTANCE.searchCities("");
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        // Do NOT forget to call super.onPause()
        super.onPause();
    }

    @Subscribe
    public void searchResult(final SearchCityResultEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Here someone has posted a SearchResultEvent
                // Check that map is ready
                if (mActiveGoogleMap != null) {
                    // Update map's markers
                    mActiveGoogleMap.clear();
                    for (City city : event.getCities()) {
                        // Step 1: create marker icon (and resize drawable so that marker is not too big)
                        if (city.latitude != null && city.longitude != null){
                            int markerIconResource;
                            markerIconResource = R.drawable.baseline_location_on_black_18dp;
                            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), markerIconResource);
                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 50, 50, false);

                            // Step 2: define marker options
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(city.getLatitude()), Double.valueOf(city.getLongitude())))
                                    .title(city.name)
                                    .snippet("Nombre de mesures : " +city.count)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

                            // Step 3: add marker
                            mActiveGoogleMap.addMarker(markerOptions);
                        }
                    }
                }
            }
        });

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
            case R.id.action_map:
                Intent mapIntent = new Intent(this, MapActivity.class);
                startActivity(mapIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mActiveGoogleMap = googleMap;
    }
}
