package com.miage.covidair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LocationsActivity extends AppCompatActivity {


    @BindView(R.id.activity_detail_city_street)
    TextView mCityStreet;

    @BindView(R.id.activity_detail_city_pic)
    ImageView mCityPic;

    private String mCityStreetValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locations_item);

        ButterKnife.bind(this);
        mCityStreetValue = getIntent().getStringExtra("placeStreet");
        mCityStreet.setText(mCityStreetValue);
    }

    @OnClick(R.id.activity_detail_city_street)
    public void clickedOnCityStreet() {
        finish();
    }

    @OnClick(R.id.activity_detail_button_search)
    public void clickedOnGoogleSearch() {
        // Open browser using an Intent
        Uri url = Uri.parse("https://www.google.fr/search?q=" + mCityStreetValue);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, url);
        startActivity(launchBrowser);
    }

    @OnClick(R.id.activity_detail_button_share)
    public void clickedOnShare() {
        // Open share picker using an Intent
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai découvert " + mCityStreetValue + " grâce à City Searcher !");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @OnClick(R.id.activity_detail_button_galery)
    public void clickedOnPickFromGalery() {
        // Open galery picker using an Intent
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        // If we get a result from the SELECT_PHOTO query
        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    // Get the selected image as bitmap
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);

                        // Set the bitmap to the picture
                        mCityPic.setImageBitmap(selectedImageBitmap);
                    } catch (FileNotFoundException e) {
                        // Silent catch : image will not be displayed
                    }

                }
        }
    }
}