package com.github.apls777.photofeed;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class DownloadPreviewsTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

    private ProgressBar spinner;
    private MainActivity imageGridActivity = null;
    private int spinnerId;

    AsyncTask<String, Void, ArrayList<HashMap<String, String>>> setSpinner(int spinnerId) {
        this.spinnerId = spinnerId;
        return this;
    }

    void attachActivity(MainActivity imageGridActivity) {
        this.imageGridActivity = imageGridActivity;
    }

    @Override
    protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
        String url = "";
        if (params.length > 0) {
            url = params[0];
        }

        PhotoFeed mPhotoFeed = new PhotoFeed(url);
        ArrayList<HashMap<String, String>> images = null;
        try {
            images = mPhotoFeed.getImages();
        } catch (IOException e) {
            Log.e("Error getting images", e.getMessage(), e);
        }
        return images;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        setSpinnerVisible();
    }

    void setSpinnerVisible() {
        spinner = (ProgressBar) imageGridActivity.findViewById(spinnerId);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String, String>> images) {
        super.onPostExecute(images);
        spinner.setVisibility(View.GONE);
        if (images != null) {
            boolean hasNext = (images.size() >= PhotoFeed.ITEMS_PER_PAGE);
            imageGridActivity.setHasNext(hasNext);
            imageGridActivity.updateImagesUrls(images);
            imageGridActivity.getImageAdapter().notifyDataSetChanged();
        }
    }
}
