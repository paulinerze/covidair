package com.miage.covidair;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchCityResultEvent;
import com.miage.covidair.service.CitySearchService;
import com.miage.covidair.adapter.CityAdapter;
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
    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

        // Register to Event bus : now each time an event is posted, the activity will receive it if it is @Subscribed to this event
        EventBusManager.BUS.register(this);

        CitySearchService.INSTANCE.searchCities();
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
}
