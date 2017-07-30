package com.github.apls777.photofeed;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.apls777.photofeed.R;

public class ImageDetailsActivity extends ActionBarActivity {

    private DisplayImageOptions imageOptions;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private PagerImagesTask mPagerImagesTask = null;
    private List<String> imageUrls;
    private List<String> userNames;

    private int nextPage;
    private ImagePagerAdapter mImagePagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_pager);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
		int pagerPosition = bundle.getInt(PhotoFeed.EXTRA_IMAGE_POS, 0);
        imageUrls = bundle.getStringArrayList(PhotoFeed.EXTRA_IMAGES);
        userNames = bundle.getStringArrayList(PhotoFeed.EXTRA_NAMES);
        nextPage = bundle.getInt(PhotoFeed.EXTRA_NEXT_PAGE);

        imageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_stub)
            .showImageForEmptyUri(R.drawable.ic_empty)
            .showImageOnFail(R.drawable.ic_error)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        mImagePagerAdapter = new ImagePagerAdapter(imageUrls, userNames);
		pager.setAdapter(mImagePagerAdapter);
		pager.setCurrentItem(pagerPosition);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateImages(ArrayList<HashMap<String, String>> images) {
        for (int i = 0; i <= images.size() - 1; i++) {
            imageUrls.add(images.get(i).get(PhotoFeed.ORIGINAL_IMAGES));
            userNames.add(images.get(i).get(PhotoFeed.USERS_NAMES));
        }
        mImagePagerAdapter.notifyDataSetChanged();
    }

    private void initPagerImagesTask(String url) {
        if (mPagerImagesTask == null || mPagerImagesTask.getStatus() == AsyncTask.Status.FINISHED) {
            mPagerImagesTask = new PagerImagesTask();
            mPagerImagesTask.attachActivity(this);
            mPagerImagesTask.execute(url);
        }
    }

    public void setHasNext(boolean hasNext)
    {
        if (hasNext) {
            nextPage++;
        } else {
            nextPage = 0;
        }
    }

    @Override
	protected void onStop() {
		super.onStop();
	}

	private class ImagePagerAdapter extends PagerAdapter {
		
		private List<String> images;
		private List<String> names;
		private LayoutInflater inflater;

		ImagePagerAdapter(List<String> images, List<String> names) {
			this.images = images;
			this.names = names;
			inflater = LayoutInflater.from(ImageDetailsActivity.this);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			final View imageLayout = inflater.inflate(R.layout.pager_image_item, view, false);
		    final TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

            setTitle(names.get(position));

            if (nextPage > 0 && position + 5 > this.getCount()) {
                initPagerImagesTask(PhotoFeed.getGalleryUrl(nextPage));
            }

			imageLoader.displayImage(images.get(position), imageView, imageOptions, new SimpleImageLoadingListener() {

                @Override
				public void onLoadingStarted(String imageUri, View view) {
    				spinner.setVisibility(View.VISIBLE);
				}

                @Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
	    			String message = null;
		    		switch (failReason.getType()) {
			        	case IO_ERROR:
				            message = "Input/Output error";
            				break;
			        	case DECODING_ERROR:
			            	message = "Image can't be decoded";
				            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }

                    Toast.makeText(ImageDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
				    spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					spinner.setVisibility(View.GONE);
				}
            });

            view.addView(imageLayout, 0);
            return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}
