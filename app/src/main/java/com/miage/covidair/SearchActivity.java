package com.miage.covidair;

import android.annotation.SuppressLint;
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

import com.miage.covidair.adapter.CityAdapter;
import com.miage.covidair.adapter.LocationAdapter;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.event.SearchLocationResultEvent;
import com.miage.covidair.service.CitySearchService;
import com.miage.covidair.service.LocationSearchService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private LocationAdapter mLocationAdapter;
    @BindView(R.id.activity_main_loader)
    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it

        super.onPause();
    }


    public void search(View view){


        EditText mSearchZone = findViewById(R.id.zone_edittext);
        String zone = mSearchZone.getText().toString();

        EditText mSearchNom = findViewById(R.id.nom_edittext);
        String nom = mSearchNom.getText().toString();

        EditText mSearchMaxBC = findViewById(R.id.seuil_max_bc_edittext);
        EditText mSearchMinBC = findViewById(R.id.seuil_min_bc_edittext);
        String maxBC = mSearchMaxBC.getText().toString();
        String minBC = mSearchMinBC.getText().toString();

        EditText mSearchMaxCO = findViewById(R.id.seuil_max_co_edittext);
        EditText mSearchMinCO = findViewById(R.id.seuil_min_co_edittext);
        String maxCO = mSearchMaxCO.getText().toString();
        String minCO = mSearchMinCO.getText().toString();


        EditText  mSearchMaxNO2 = findViewById(R.id.seuil_max_no2_edittext);
        EditText mSearchMinNO2 = findViewById(R.id.seuil_min_no2_edittext);
        String maxNO2 = mSearchMaxNO2.getText().toString();
        String minNO2 = mSearchMinNO2.getText().toString();

        EditText mSearchMaxO3 = findViewById(R.id.seuil_max_o3_edittext);
        EditText mSearchMinO3 = findViewById(R.id.seuil_min_o3_edittext);
        String maxO3 = mSearchMaxO3.getText().toString();
        String minO3 = mSearchMinO3.getText().toString();

        EditText mSearchMaxPM10 = findViewById(R.id.seuil_max_PM10_edittext);
        EditText mSearchMinPM10 = findViewById(R.id.seuil_min_PM10_edittext);
        String maxPM10 = mSearchMaxPM10.getText().toString();
        String minPM10 = mSearchMinPM10.getText().toString();

        EditText mSearchMaxPM25 = findViewById(R.id.seuil_max_PM25_edittext);
        EditText mSearchMinPM25 = findViewById(R.id.seuil_min_PM25_edittext);
        String maxPM25 = mSearchMaxPM25.getText().toString();
        String minPM25 = mSearchMinPM25.getText().toString();

        EditText mSearchMaxSO2 = findViewById(R.id.seuil_max_SO2_edittext);
        EditText mSearchMinSO2 = findViewById(R.id.seuil_min_SO2_edittext);
        String maxSO2 = mSearchMaxSO2.getText().toString();
        String minSO2 = mSearchMinSO2.getText().toString();

        Intent seeMatchningLocationDetailIntent = new Intent(SearchActivity.this, BigSearchActivity.class);
        seeMatchningLocationDetailIntent.putExtra("zone", zone);
        seeMatchningLocationDetailIntent.putExtra("nom", nom);
        seeMatchningLocationDetailIntent.putExtra("minBC",minBC);
        seeMatchningLocationDetailIntent.putExtra("maxBC",maxBC);
        seeMatchningLocationDetailIntent.putExtra("minCO",minCO);
        seeMatchningLocationDetailIntent.putExtra("maxCO",maxCO);
        seeMatchningLocationDetailIntent.putExtra("minNO2",minNO2);
        seeMatchningLocationDetailIntent.putExtra("maxNO2",maxNO2);
        seeMatchningLocationDetailIntent.putExtra("minO3",minO3);
        seeMatchningLocationDetailIntent.putExtra("maxO3",maxO3);
        seeMatchningLocationDetailIntent.putExtra("minPM10",minPM10);
        seeMatchningLocationDetailIntent.putExtra("maxPM10",maxPM10);
        seeMatchningLocationDetailIntent.putExtra("minPM25",minPM25);
        seeMatchningLocationDetailIntent.putExtra("maxPM25",maxPM25);
        seeMatchningLocationDetailIntent.putExtra("minSO2",minSO2);
        seeMatchningLocationDetailIntent.putExtra("maxSO2",maxSO2);
        SearchActivity.this.startActivity(seeMatchningLocationDetailIntent);



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

