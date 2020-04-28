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

import com.miage.covidair.adapter.CityAdapter;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.service.CitySearchService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private CityAdapter mCityAdapter;
    @BindView(R.id.activity_main_loader)
    ProgressBar mProgressBar;
    @BindView(R.id.activity_main_search_adress_edittext)
    EditText mSearchEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Instanciate a CityAdpater with empty content
        mCityAdapter = new CityAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mCityAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        //CitySearchService.INSTANCE.searchCities();
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void searchResult(final SearchCityResultEvent event) {
        // Here someone has posted a SearchCityResultEvent
        // Update adapter's model
        runOnUiThread(() -> {
            mCityAdapter.setCities(event.getCities());
            mCityAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
        });
        mCityAdapter.setCities(event.getCities());
        runOnUiThread(() -> mCityAdapter.notifyDataSetChanged());
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
}
