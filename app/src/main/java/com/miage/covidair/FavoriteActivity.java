package com.miage.covidair;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miage.covidair.adapter.FavoriteAdapter;
import com.miage.covidair.event.EventBusManager;
import com.miage.covidair.event.SearchFavoriteResultEvent;
import com.miage.covidair.service.FavoriteSearchService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.activity_main_loader)
    ProgressBar mProgressBar;
    private FavoriteAdapter mFavoriteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Instanciate a CityAdpater with empty content
        mFavoriteAdapter = new FavoriteAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mFavoriteAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

        // Register to Event bus : now each time an event is posted, the activity will receive it if it is @Subscribed to this event
        EventBusManager.BUS.register(this);

        FavoriteSearchService.INSTANCE.searchFavorites();
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void searchResult(final SearchFavoriteResultEvent event) {
        // Here someone has posted a SearchCityResultEvent
        // Update adapter's model
        runOnUiThread(() -> {
            mFavoriteAdapter.setFavorites(event.getFavorites());
            mFavoriteAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
        });
        mFavoriteAdapter.setFavorites(event.getFavorites());
        runOnUiThread(() -> mFavoriteAdapter.notifyDataSetChanged());
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
