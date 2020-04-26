package com.miage.covidair.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class LoadImage extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;

    public LoadImage(ImageView imageView){
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String search = strings[0];
        URL url = null;
        Bitmap bmp = null;
        try {
            url = new URL("https://maps.googleapis.com/maps/api/streetview?size=1500x1500&location=" +
                    search +
                    "&fov=80&heading=70&pitch=0&key=AIzaSyDY7Jss-oxBTtQJ-yDP9xLseurySYX3l7E");

            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }
}
