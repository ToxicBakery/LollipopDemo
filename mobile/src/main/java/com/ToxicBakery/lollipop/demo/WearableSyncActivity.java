package com.ToxicBakery.lollipop.demo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.IntentSender;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ToxicBakery.lollipop.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class WearableSyncActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>,
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ViewPager.OnPageChangeListener {

    private static final LruCache<String, Bitmap> BITMAP_LRU_CACHE;
    private static final int WEAR_SCREEN_SIZE_PX = 320;

    // Wear information
    private static final int REQUEST_RESOLVE_ERROR = 1000;
    private static final String START_ACTIVITY_PATH = "/start-wearableSyncActivity";
    private static final String IMAGE_PATH = "/image";
    private static final String IMAGE_KEY = "photo";

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        BITMAP_LRU_CACHE = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getAllocationByteCount() / 1024;
            }
        };
    }

    private static final String[] GALLERY_PROJECTION = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN
    };

    private ViewPager viewPager;
    private GalleryAdapter galleryAdapter;
    private GoogleApiClient googleApiClient;
    private boolean isResolvingError = false;
    private int pageSelected;

    /**
     * Builds an {@link com.google.android.gms.wearable.Asset} from a bitmap. The image that we get back from the camera
     * in "data" is a thumbnail size. Typically, your image should not exceed 320x320 and if you want to have zoom and
     * parallax effect in your app, limit the size of your image to 640x400. Resize your image before transferring to
     * your wearable device.
     */
    private static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable_sync);

        galleryAdapter = new GalleryAdapter(getFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.demo_wearable_sync_viewpager);
        viewPager.setAdapter(galleryAdapter);
        viewPager.setOnPageChangeListener(this);

        getLoaderManager().initLoader(0, null, this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new StartWearableActivityTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isResolvingError) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (!isResolvingError) {
            Wearable.DataApi.removeListener(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        galleryAdapter.setCursor(cursor);
        galleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        galleryAdapter.setCursor(null);
        galleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        pageSelected = i;
        System.out.println("onpage selected: " + i);
        ((FragmentGalleryImage) galleryAdapter.instantiateItem(viewPager, i)).sendPhoto();
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        isResolvingError = false;
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        dataEvents.close();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (isResolvingError) {
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                isResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                googleApiClient.connect();
            }
        } else {
            isResolvingError = false;
            Wearable.DataApi.removeListener(googleApiClient, this);
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }

    private void sendStartActivityMessage(String node) {
        Wearable.MessageApi.sendMessage(
                googleApiClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    }
                }
        );
    }

    /**
     * Sends the asset that was created form the photo we took by adding it to the Data Item store.
     */
    private void sendPhoto(Asset asset) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(IMAGE_PATH);
        dataMap.getDataMap().putAsset(IMAGE_KEY, asset);
        dataMap.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                    }
                });

    }

    public static final class FragmentGalleryImage extends Fragment {

        private static final String EXTRA_GALLERY_IMAGE = FragmentGalleryImage.class.getSimpleName() +
                ".EXTRA_GALLERY_IMAGE";

        private static int lastSent;

        private GalleryImage galleryImage;
        private ImageView imageViewImage;
        private TextView textViewDateTaken;
        private ImageLoader imageLoader;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            galleryImage = getArguments().getParcelable(EXTRA_GALLERY_IMAGE);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_gallery_image, container, false);

            imageViewImage = (ImageView) view.findViewById(R.id.fragment_gallery_image_imageview_image);
            textViewDateTaken = (TextView) view.findViewById(R.id.fragment_gallery_image_textview_date_taken);

            imageLoader = new ImageLoader(imageViewImage, new ImageLoader.ICallback() {
                @Override
                public void onFinished() {
                    sendPhoto();
                }
            });

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();

            Date date = new Date(galleryImage.dateTaken);

            imageViewImage.setVisibility(View.INVISIBLE);
            textViewDateTaken.setText(SimpleDateFormat.getInstance().format(date));

            imageLoader.execute(galleryImage);
        }

        @Override
        public void onPause() {
            super.onPause();

            imageLoader.cancel(false);
        }

        private void sendPhoto() {
            System.out.println("1");
            WearableSyncActivity activity = (WearableSyncActivity) getActivity();
            if (activity != null && activity.galleryAdapter.instantiateItem(activity.viewPager,
                    activity.pageSelected) == this) {
                System.out.println("2");
                Bitmap bitmap = BITMAP_LRU_CACHE.get(ImageLoader.getWearImageCacheName(galleryImage));
                if (bitmap != null) {
                    System.out.println("3");
                    lastSent = activity.pageSelected;
                    activity.sendPhoto(activity.toAsset(bitmap));
                }
            }
        }

        private static final class ImageLoader extends AsyncTask<GalleryImage, Void, Bitmap> {

            private final ImageView imageView;
            private final Context context;

            private Bitmap wearBitmap;
            private ICallback callback;

            private ImageLoader(ImageView imageView, ICallback callback) {
                this.imageView = imageView;
                this.callback = callback;
                context = imageView.getContext().getApplicationContext();
            }

            public static final String getWearImageCacheName(GalleryImage galleryImage) {
                return getImageUri(galleryImage).toString().replaceAll("[^a-zA-Z0-9.]", "") + "small";
            }

            public static final Uri getImageUri(GalleryImage galleryImage) {
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        Integer.toString(galleryImage._id));
            }

            private static void calculateInSampleSize(
                    BitmapFactory.Options options, int reqWidth, int reqHeight) {
                // Raw height and width of image
                final int height = options.outHeight;
                final int width = options.outWidth;
                options.inSampleSize = 1;

                if (height > reqHeight || width > reqWidth) {

                    final int halfHeight = height / 2;
                    final int halfWidth = width / 2;

                    // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                    // height and width larger than the requested height and width.
                    while ((halfHeight / options.inSampleSize) > reqHeight
                            && (halfWidth / options.inSampleSize) > reqWidth) {
                        options.inSampleSize *= 2;
                    }

                    options.outHeight = height / options.inSampleSize;
                    options.outWidth = width / options.inSampleSize;
                }
            }

            @Override
            protected Bitmap doInBackground(GalleryImage... galleryImages) {
                Uri uri = getImageUri(galleryImages[0]);
                String uriString = uri.toString();

                Bitmap bitmap = BITMAP_LRU_CACHE.get(uriString);

                if (bitmap != null)
                    return bitmap;

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                BITMAP_LRU_CACHE.put(uriString, bitmap);

                // Downscale for wearBitmap
                String wearCacheName = getWearImageCacheName(galleryImages[0]);
                File wearFile = new File(context.getCacheDir(), wearCacheName);

                if (wearFile.exists()) {
                    wearBitmap = BitmapFactory.decodeFile(wearFile.getAbsolutePath());
                    BITMAP_LRU_CACHE.put(wearCacheName, wearBitmap);
                } else {
                    try {
                        FileDescriptor imageDescriptor = context.getContentResolver().openFileDescriptor
                                (uri, "r").getFileDescriptor();

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFileDescriptor(imageDescriptor, null, options);
                        calculateInSampleSize(options, WEAR_SCREEN_SIZE_PX, WEAR_SCREEN_SIZE_PX);
                        options.inJustDecodeBounds = false;

                        wearBitmap = BitmapFactory.decodeFileDescriptor(imageDescriptor, null, options);

                        BITMAP_LRU_CACHE.put(wearCacheName, wearBitmap);

                        // cache to disk
                        OutputStream os = new FileOutputStream(wearFile);
                        wearBitmap.compress(Bitmap.CompressFormat.JPEG, 70, os);
                        os.flush();
                        os.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.setVisibility(View.VISIBLE);

                if (bitmap == null) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    imageView.setImageResource(R.drawable.ic_launcher);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(bitmap);
                }

                callback.onFinished();
                callback = null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();

                callback = null;
            }

            private static interface ICallback {

                void onFinished();
            }

        }

    }

    private static final class GalleryImage implements Parcelable {

        public static final Creator CREATOR = new Creator() {
            @Override
            public Object createFromParcel(Parcel source) {
                return new GalleryImage(source);
            }

            @Override
            public GalleryImage[] newArray(int size) {
                return new GalleryImage[0];
            }
        };

        private final int _id;
        private final long dateTaken;

        private GalleryImage(Cursor cursor) {
            int indexId = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int indexDateTaken = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

            _id = cursor.getInt(indexId);
            dateTaken = Long.parseLong(cursor.getString(indexDateTaken));
        }

        private GalleryImage(Parcel parcel) {
            _id = parcel.readInt();
            dateTaken = parcel.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(_id);
            dest.writeLong(dateTaken);
        }
    }

    private static final class GalleryAdapter extends FragmentStatePagerAdapter {

        private Cursor cursor;

        private GalleryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            cursor.moveToPosition(i);

            Bundle bundle = new Bundle();
            bundle.putParcelable(FragmentGalleryImage.EXTRA_GALLERY_IMAGE, new GalleryImage(cursor));

            FragmentGalleryImage fragmentGalleryImage = new FragmentGalleryImage();
            fragmentGalleryImage.setArguments(bundle);

            return fragmentGalleryImage;
        }

        @Override
        public int getCount() {
            return cursor == null ? 0 : cursor.getCount();
        }

        public void setCursor(Cursor cursor) {
            this.cursor = cursor;
        }
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }
}
