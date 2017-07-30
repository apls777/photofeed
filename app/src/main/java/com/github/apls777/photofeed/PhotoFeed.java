package com.github.apls777.photofeed;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class PhotoFeed {

    private static final String API_URL = "https://example.com/api";

    static final int ITEMS_PER_PAGE = 30;
    static final String PREVIEW_IMAGES = "preview_images";
    static final String ORIGINAL_IMAGES = "original_images";
    static final String USERS_NAMES = "users_names";

    static final String EXTRA_IMAGES = "images";
    static final String EXTRA_NAMES = "names";
    static final String EXTRA_IMAGE_POS = "image_position";
    static final String EXTRA_NEXT_PAGE = "next_page";

    private String url;

	private String getUrl() {
		return url;
	}

	private void setUrl(String url) {
		this.url = url;
	}
	
	PhotoFeed(String url) {
		setUrl(url);
	}

    static String getGalleryUrl(int page) {
        int offset = ITEMS_PER_PAGE * (page - 1);
        return API_URL + "/images?limit=" + ITEMS_PER_PAGE + "&offset=" + offset;
    }

	ArrayList<HashMap<String, String>> getImages() throws IOException {
		String previewJPGURL;
		String viewJPGURL;
		String userName;
		String response;

        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
			
		try {
		    response = CustomHttpClient.executeHttpGet(getUrl());
            JSONArray mArray = new JSONArray(response);

            Log.d("feedUrl", getUrl());

			try {
                if (mArray.length() > 0) {
                    for (int i = 0; i < mArray.length(); i++) {
                        JSONObject json_data = mArray.getJSONObject(i);

                        viewJPGURL = json_data.getString("image_big");
                        previewJPGURL = json_data.getString("image_small");
                        userName = json_data.getString("username");

                        HashMap<String, String> mHashMap = new HashMap<String, String>();
                        mHashMap.put(PREVIEW_IMAGES, previewJPGURL);
                        mHashMap.put(ORIGINAL_IMAGES, viewJPGURL);
                        mHashMap.put(USERS_NAMES, userName);

                        data.add(mHashMap);
                        Log.i("getImages", "" + data);
                    }
                } else {
                    data = null;
                }
		    } catch(JSONException e) {
                Log.e("parseError", "Parsing error: " + e.toString());
            }
        } catch (Exception e) {
			Log.e("connectionError", "Connection error: " + e.toString());
        }

		return data;
	}
}
