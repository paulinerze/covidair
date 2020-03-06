package com.miage.covidair;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchDetailResultEvent;
import com.miage.covidair.service.DetailSearchService;
import com.miage.covidair.adapter.DetailAdapter;
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
        String city = getIntent().getStringExtra("city");
        DetailSearchService.INSTANCE.searchFromAPI("https://api.openaq.org/v1/measurements?city="+city+"&location="+location+"&order_by=date&sort=desc");
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void searchResult(final SearchDetailResultEvent event) {
        // Here someone has posted a SearchCityResultEvent
        // Update adapter's model
        mDetailAdapter.setDetails(event.getDetails());
        runOnUiThread(() -> mDetailAdapter.notifyDataSetChanged());
    }
}
