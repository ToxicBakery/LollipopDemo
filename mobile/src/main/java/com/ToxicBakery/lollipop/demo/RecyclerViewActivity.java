package com.ToxicBakery.lollipop.demo;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ToxicBakery.lollipop.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample implementation of a RecyclerView.
 * <p/>
 * The major difference between a ListView and a RecyclerView is that a ListView tightly binds data to a view while the
 * RecyclerView only cares about recycling the views.
 * <p/>
 * The key differences can be seen in the helper classes: LayoutManager, Adapter, ViewHolder, ItemDecoration,
 * ItemAnimator. <br /> In these class, RecyclerView can be manipulated on the fly to fill a screen in a variety of
 * ways.
 */
public class RecyclerViewActivity extends Activity implements View.OnClickListener, RecyclerView.OnItemTouchListener {

    private static final String KEY_DATA_ELEMENTS = RecyclerViewActivity.class.getName() + ".KEY_DATA_ELEMENTS";
    private static final String KEY_COUNTER = RecyclerViewActivity.class.getName() + ".KEY_COUNTER";

    private ImageButton buttonAddItem;
    private RecyclerView recyclerView;
    private GestureDetectorCompat gestureDetector;
    private GestureDetector.SimpleOnGestureListener onGestureListener;
    private RecyclerViewAdapter recyclerViewAdapter;

    private ArrayList<String> dataElements;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_recyclerview);

        buttonAddItem = (ImageButton) findViewById(R.id.activity_demo_recyclerview_imagebutton_add);
        recyclerView = (RecyclerView) findViewById(R.id.activity_demo_recyclerview_recyclerview);

        int fabSize = getResources().getDimensionPixelSize(R.dimen.fab_size);
        Outline outline = new Outline();
        outline.setOval(0, 0, fabSize, fabSize);
        buttonAddItem.setOnClickListener(this);
        buttonAddItem.setOutline(outline);

        if (savedInstanceState == null) {
            dataElements = new ArrayList<>();
        } else {
            dataElements = savedInstanceState.getStringArrayList(KEY_DATA_ELEMENTS);
            counter = savedInstanceState.getInt(KEY_COUNTER);
        }

        /*
         * Configure the RecyclerView
         */

        // Gesture detector to help listen for item selection
        onGestureListener = new GestureDetector.SimpleOnGestureListener();
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewDemoOnGestureListener());

        // Use a layout manager to emulate a stacked representation of views in a horizontal or vertical orientation.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // Use an adapter to drive the Recycler
        recyclerViewAdapter = new RecyclerViewAdapter(dataElements);

        // Use an decorator to create item dividers
        RecyclerView.ItemDecoration itemDecoration = new HorizontalLineDecorator(getApplicationContext());

        // Put it all together
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.addOnItemTouchListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(KEY_DATA_ELEMENTS, dataElements);
        outState.putInt(KEY_COUNTER, counter);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
    }

    @Override
    public void onClick(View view) {
        if (view == null)
            return;

        switch (view.getId()) {
            case android.R.id.text1: {
                int position = recyclerView.getChildPosition(view);
                dataElements.remove(position);
                recyclerViewAdapter.notifyItemRemoved(position);
                break;
            }
            case R.id.activity_demo_recyclerview_imagebutton_add: {
                int size = dataElements.size();
                int position = size / 2;
                dataElements.add(position, getString(R.string.demo_recycler_touch_to_remove) + " " + ++counter);
                recyclerViewAdapter.notifyItemInserted(position);
                break;
            }
        }
    }

    /**
     * ViewHolder pattern similar in fashion to the ListView performance approach.
     */
    private static final class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewItem;

        private ViewHolder(View itemView) {
            super(itemView);

            textViewItem = (TextView) itemView.findViewById(android.R.id.text1);
        }

        private void updateView(String data) {
            textViewItem.setText(data);
        }

    }

    /**
     * Adapter connection the data to the views.
     */
    private static final class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        final List<String> data;

        private RecyclerViewAdapter(List<String> data) {
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout
                    .simple_list_item_1, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.updateView(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    /**
     * Simple divider implementation derived from the v7 samples.
     */
    private static final class HorizontalLineDecorator extends RecyclerView.ItemDecoration {

        private static final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        private Drawable divider;

        private HorizontalLineDecorator(Context context) {
            final TypedArray typedArray = context.obtainStyledAttributes(ATTRS);
            divider = typedArray.getDrawable(0);
            typedArray.recycle();
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int childCount = parent.getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            outRect.set(0, 0, 0, divider.getIntrinsicHeight());
        }
    }

    /**
     * Replication of OnItemClickListener using a gesture detector and the RecyclerView touch listener interface.
     */
    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return true;
        }

    }

}
