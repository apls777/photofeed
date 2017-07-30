package com.github.apls777.photofeed;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class PagerImagesTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

    private ImageDetailsActivity detailsActivity = null;

    void attachActivity(ImageDetailsActivity detailsActivity) {
        this.detailsActivity = detailsActivity;
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
    protected void onPostExecute(ArrayList<HashMap<String, String>> images) {
        super.onPostExecute(images);
        if (images != null) {
            boolean hasNext = (images.size() >= PhotoFeed.ITEMS_PER_PAGE);
            detailsActivity.setHasNext(hasNext);
            detailsActivity.updateImages(images);
        }
    }
}
