package com.github.apls777.photofeed;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.apls777.photofeed.R;

public class MainActivity extends ActionBarActivity {

    private ImageLoader imageLoader;
    private ArrayList<String> imageUrls;
    private ArrayList<String> previewImageUrls;
    private ArrayList<String> usersNames;
    private ImageAdapter imageAdapter;
    private DisplayImageOptions imageOptions;
    private DownloadPreviewsTask mDownloadPreviewsTask;

    private int page = 1;
    private boolean hasNextPhotos = true;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.main_action_bar);

        TextView mainTitle = (TextView)findViewById(R.id.main_title);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/lobster.ttf");
        mainTitle.setTypeface(customFont);

        ConnectionDetector cd = new ConnectionDetector(this);

        if (cd.hasConnection()) {
            imageLoader = ImageLoader.getInstance();
            imageUrls = new ArrayList<String>();
            previewImageUrls = new ArrayList<String>();
            usersNames = new ArrayList<String>();

            GridView gridView = (GridView) findViewById(R.id.gridview);
            imageAdapter = new ImageAdapter();

            gridView.setAdapter(imageAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startImageDetailsActivity(position);
                }
            });
            gridView.setOnScrollListener(new EndlessGridListener());

            initDownloadPreviewsTask(R.id.preview_img_loading, PhotoFeed.getGalleryUrl(page));
            if (mDownloadPreviewsTask != null && mDownloadPreviewsTask.getStatus() == AsyncTask.Status.RUNNING) {
                mDownloadPreviewsTask.attachActivity(this);
                mDownloadPreviewsTask.setSpinnerVisible();
                page++;
            } else {
                Log.d("downloadPreviewImagesTask", "downloadPreviewImagesTask has executed already");
            }

            imageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        } else {
            showAlertDialog(MainActivity.this, "No internet connection", "You don't have internet connection!", false);
        }
    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.show();
    }

    public void updateImagesUrls(ArrayList<HashMap<String, String>> images) {
        for (int i = 0; i <= images.size() - 1; i++) {
            imageUrls.add(images.get(i).get(PhotoFeed.ORIGINAL_IMAGES));
            previewImageUrls.add(images.get(i).get(PhotoFeed.PREVIEW_IMAGES));
            usersNames.add(images.get(i).get(PhotoFeed.USERS_NAMES));
        }
    }

    private void initDownloadPreviewsTask(int spinnerId, String url) {
        mDownloadPreviewsTask = new DownloadPreviewsTask();
        mDownloadPreviewsTask.setSpinner(spinnerId);
        mDownloadPreviewsTask.attachActivity(this);
        mDownloadPreviewsTask.execute(url); // show spinner
    }

    public ImageAdapter getImageAdapter() {
        return imageAdapter;
    }

    public void setHasNext(boolean hasNext) {
        hasNextPhotos = hasNext;
    }

    private void startImageDetailsActivity(int position) {
        Intent intent = new Intent(this, ImageDetailsActivity.class);
        intent.putExtra(PhotoFeed.EXTRA_IMAGES, imageUrls);
        intent.putExtra(PhotoFeed.EXTRA_NAMES, usersNames);
        intent.putExtra(PhotoFeed.EXTRA_IMAGE_POS, position);
        intent.putExtra(PhotoFeed.EXTRA_NEXT_PAGE, (hasNextPhotos ? page : 0));
        startActivity(intent);
    }

    public class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            imageLoader.displayImage(previewImageUrls.get(position), holder.imageView, imageOptions, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    holder.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.progressBar.setVisibility(View.GONE);
                }
            });

            return view;
        }
    }

    private static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }

    private class EndlessGridListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 9;
        private int previousTotal = 0;
        private boolean loading = true;

        public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {

            if (loading && totalCount > previousTotal) {
                loading = false;
                previousTotal = totalCount;
            }

            //Log.i("check", String.valueOf(totalCount) + " " + String.valueOf(visibleCount) + " " + String.valueOf(firstVisible));
            if (!loading && hasNextPhotos && ((totalCount - visibleCount - firstVisible) <= visibleThreshold)) {
                String url = PhotoFeed.getGalleryUrl(page);
                initDownloadPreviewsTask(R.id.new_photos_loading, url);
                page++;
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }
    }
}
