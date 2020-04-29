package com.miage.covidair;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.model.City.City;
import com.miage.covidair.model.Location.Location;
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
                        if (city.latitude != null && city.longitude != null) {
                            // Step 2: define marker options
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(city.getLatitude()), Double.valueOf(city.getLongitude())))
                                    .title(city.name);

                            // Step 3: add marker
                            mActiveGoogleMap.addMarker(markerOptions);
                        }
                    }
                }
            }
        });

    }

    @Subscribe
    public void searchLocationResult(final SearchLocationResultEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Here someone has posted a SearchResultEvent
                // Check that map is ready
                if (mActiveGoogleMap != null) {
                    // Update map's markers
                    mActiveGoogleMap.clear();
                    for (Location location : event.getLocations()) {
                        // Step 1: create marker icon (and resize drawable so that marker is not too big)
                        if (location.latitude != null && location.longitude != null) {
                            // Step 2: define marker options

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude())))
                                    .title(location.location);
                            //.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

                            // Step 3: add marker
                            mActiveGoogleMap.addMarker(markerOptions);

                            mActiveGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                @Override
                                public View getInfoWindow(Marker arg0) {
                                    return null;
                                }

                                @Override
                                public View getInfoContents(Marker marker) {

                                    String measurements = "";
                                    if (location.getLatestMeasurements() != null && !location.getLatestMeasurements().isEmpty()) {
                                        String bc = "";
                                        String co = "";
                                        String no2 = "";
                                        String o3 = "";
                                        String pm10 = "";
                                        String pm25 = "";
                                        String so2 = "";

                                        if (location.getLatestMeasurements().containsKey("bc")) {
                                            bc = "BC : " + location.getLatestMeasurements().get("bc").value + " " + location.getLatestMeasurements().get("bc").unit + "\n";
                                        }
                                        if (location.getLatestMeasurements().containsKey("co")) {
                                            co = "CO : " + location.getLatestMeasurements().get("co").value + " " + location.getLatestMeasurements().get("co").unit + "\n";
                                        }
                                        if (location.getLatestMeasurements().containsKey("no2")) {
                                            no2 = "NO2 : " + location.getLatestMeasurements().get("no2").value + " " + location.getLatestMeasurements().get("no2").unit + "\n";
                                        }
                                        if (location.getLatestMeasurements().containsKey("o3")) {
                                            o3 = "O3 : " + location.getLatestMeasurements().get("o3").value + " " + location.getLatestMeasurements().get("o3").unit + "\n";
                                        }
                                        if (location.getLatestMeasurements().containsKey("pm10")) {
                                            pm10 = "PM10 : " + location.getLatestMeasurements().get("pm10").value + " " + location.getLatestMeasurements().get("pm10").unit + "\n";
                                        }
                                        if (location.getLatestMeasurements().containsKey("pm25")) {
                                            pm25 = "PM25 : " + location.getLatestMeasurements().get("pm25").value + " " + location.getLatestMeasurements().get("pm25").unit + "\n";
                                        }
                                        if (location.getLatestMeasurements().containsKey("so2")) {
                                            so2 = "SO2 : " + location.getLatestMeasurements().get("so2").value + " " + location.getLatestMeasurements().get("so2").unit + "\n";
                                        }

                                        measurements = bc + co + no2 + o3 + pm10 + pm25 + so2;
                                    }

                                    String temperature = "";
                                    if (location.sol != null) {
                                        temperature = "Température actuelle : " + location.sol + "°C \n";
                                    }

                                    LinearLayout info = new LinearLayout(MapActivity.this);
                                    info.setOrientation(LinearLayout.VERTICAL);

                                    TextView title = new TextView(MapActivity.this);
                                    title.setTextColor(Color.BLACK);
                                    title.setGravity(Gravity.CENTER);
                                    title.setTypeface(null, Typeface.BOLD);
                                    title.setText(marker.getTitle());

                                    TextView snippet = new TextView(MapActivity.this);
                                    snippet.setTextColor(Color.GRAY);
                                    snippet.setText(temperature + measurements);

                                    info.addView(title);
                                    info.addView(snippet);

                                    return info;
                                }
                            });
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
        mActiveGoogleMap.setOnInfoWindowClickListener(WhenInfoWindowClick);
    }

    GoogleMap.OnInfoWindowClickListener WhenInfoWindowClick = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            mActiveGoogleMap.clear();
            if (CitySearchService.INSTANCE.isCity(marker.getTitle())){
                LocationSearchService.INSTANCE.searchLocations(marker.getTitle());
            } else {
                Intent seeCityDetailIntent = new Intent(MapActivity.this, DetailActivity.class);
                seeCityDetailIntent.putExtra("longitude", String.valueOf(marker.getPosition().longitude));
                seeCityDetailIntent.putExtra("latitude", String.valueOf(marker.getPosition().latitude));
                MapActivity.this.startActivity(seeCityDetailIntent);
            }

        }
    };
}
