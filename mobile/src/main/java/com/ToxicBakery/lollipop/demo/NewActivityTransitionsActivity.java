package com.ToxicBakery.lollipop.demo;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ToxicBakery.lollipop.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NewActivityTransitionsActivity extends ListActivity implements AdapterView.OnItemClickListener {


    private TransitionDemo[] mTransitionDemos = {
            new TransitionDemo("Explode", TransitionActivity.TRAN_TYPE_EXPLODE),
            new TransitionDemo("Slide",TransitionActivity.TRAN_TYPE_SLIDE),
            new TransitionDemo("Fade", TransitionActivity.TRAN_TYPE_FADE),
            new TransitionDemo("ViewSharing", TransitionActivity.TRAN_TYPE_VIEW_SHARE)};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<TransitionDemo>(this, android.R.layout.simple_selectable_list_item, mTransitionDemos));
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(NewActivityTransitionsActivity.this, TransitionActivity.class);
        Bundle options = ActivityOptions.makeSceneTransitionAnimation(NewActivityTransitionsActivity.this).toBundle();

        TransitionDemo demo = (TransitionDemo) parent.getItemAtPosition(position);
        intent.putExtra(TransitionActivity.EXTRA_TRANSITION_TYPE, demo.mTranType);

        startActivity(intent, options);

    }

    public static class TransitionActivity extends Activity implements AdapterView.OnItemClickListener {


        public static final String EXTRA_TRANSITION_TYPE = "com.ToxicBakery.lollipop.EXTRA_TRANSITION_TYPE";

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({TRAN_TYPE_VIEW_SHARE, TRAN_TYPE_EXPLODE, TRAN_TYPE_SLIDE, TRAN_TYPE_FADE})
        public @interface TRANSITION_TYPE{};

        public static final int TRAN_TYPE_EXPLODE = 0;

        public static final int TRAN_TYPE_SLIDE = 1;
        public static final int TRAN_TYPE_FADE = 2;
        public static final int TRAN_TYPE_VIEW_SHARE = 3;
        private @DrawableRes int[] mDrawables = {R.drawable.whirlpool, R.drawable.zwicky,
                                                R.drawable.carina, R.drawable.jet,
                                                R.drawable.ngc, R.drawable.butterfly};


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setupTransition();

            setContentView(R.layout.activity_demo_new_transitions);
            GridView gridView = (GridView) findViewById(R.id.gridView);
            gridView.setAdapter(new ImageViewAdapter(this, mDrawables));
            gridView.setOnItemClickListener(this);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(this, SharedViewDetailActivity.class);
            Integer resId = (Integer) parent.getItemAtPosition(position);
            intent.putExtra(SharedViewDetailActivity.EXTRA_IMAGE_RES_ID, resId);
            Bundle options
                    = ActivityOptions.makeSceneTransitionAnimation(this, view, "sharedImage").toBundle();
            startActivity(intent, options);
        }

        private void setupTransition() {
            Window window = getWindow();
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            window.setAllowEnterTransitionOverlap(true);

            int transitionType = getIntent().getIntExtra(EXTRA_TRANSITION_TYPE, -1);

            switch (transitionType) {
                case TRAN_TYPE_EXPLODE:
                    window.setEnterTransition(new Explode());
                    window.setExitTransition(new Explode());
                    break;
                case TRAN_TYPE_SLIDE:
                    window.setEnterTransition(new Slide(Gravity.LEFT));
                    window.setExitTransition(new Slide(Gravity.RIGHT));
                    break;
                case TRAN_TYPE_FADE:
                    window.setEnterTransition(new Fade());
                    window.setExitTransition(new Fade());
                    break;
                case TRAN_TYPE_VIEW_SHARE:
                    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
                    window.setSharedElementEnterTransition(new ChangeImageTransform());
                    window.setSharedElementExitTransition(new ChangeImageTransform());
                    break;
            }

        }
    }












    private static class ImageViewAdapter extends BaseAdapter {

        private Context mContext;
        private @DrawableRes int[] mDrawables;

        private ImageViewAdapter(Context context, @DrawableRes int[] drawables) {
            mContext = context;
            mDrawables = drawables;
        }

        @Override
        public int getCount() {
            return mDrawables.length;
        }

        @Override
        public Integer getItem(int position) {
            return mDrawables[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(mContext);
            }

            ((ImageView)convertView).setImageResource(mDrawables[position]);

            return convertView;
        }
    }

    private static class TransitionDemo {
        private String mName;
        private @TransitionActivity.TRANSITION_TYPE int mTranType;

        private TransitionDemo(String mName, @TransitionActivity.TRANSITION_TYPE int mTranType) {
            this.mName = mName;
            this.mTranType = mTranType;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    public static class SharedViewDetailActivity extends Activity {

        public static final String EXTRA_IMAGE_RES_ID = "com.ToxicBakery.lollipop";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_demo_shared_view_detail);
            ImageView image = (ImageView) findViewById(R.id.imageView);
            int imageResourceId = getIntent().getIntExtra(EXTRA_IMAGE_RES_ID, -1);
            image.setImageResource(imageResourceId);
        }
    }
}
