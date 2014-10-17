package com.ToxicBakery.lollipop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ToxicBakery.lollipop.demo.CardViewActivity;
import com.ToxicBakery.lollipop.demo.RealtimeShadowsActivity;
import com.ToxicBakery.lollipop.demo.RecyclerViewActivity;
import com.ToxicBakery.lollipop.demo.RevealActivity;
import com.ToxicBakery.lollipop.demo.RippleActivity;
import com.ToxicBakery.lollipop.demo.SharedViewActivity;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final Demonstration[] DEMOS = {
            new Demonstration(CardViewActivity.class, R.string.demo_cardview)
            , new Demonstration(RealtimeShadowsActivity.class, R.string.demo_realtime_shadows)
            , new Demonstration(RecyclerViewActivity.class, R.string.demo_recycler_view)
            , new Demonstration(RevealActivity.class, R.string.demo_reveal)
            , new Demonstration(RippleActivity.class, R.string.demo_ripple)
            , new Demonstration(SharedViewActivity.class, R.string.demo_shared_view)
    };

    private ListView listViewDemos;
    private DemonstrationAdapter demonstrationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        demonstrationAdapter = new DemonstrationAdapter();

        listViewDemos = (ListView) findViewById(R.id.activity_main_list_view_demos);
        listViewDemos.setAdapter(demonstrationAdapter);
        listViewDemos.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == listViewDemos) {
            Demonstration demonstration = demonstrationAdapter.getItem(position);
            Intent intent = new Intent(this, demonstration.activityClass);
            startActivity(intent);
        }
    }

    private static final class Demonstration {

        private final Class<? extends Activity> activityClass;
        @StringRes
        private final int title;

        private Demonstration(Class<? extends Activity> activityClass, int title) {
            this.activityClass = activityClass;
            this.title = title;
        }
    }

    private static final class DemonstrationAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return DEMOS.length;
        }

        @Override
        public Demonstration getItem(int position) {
            return DEMOS[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout
                        .simple_selectable_list_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Demonstration demonstration = getItem(position);
            viewHolder.updateView(demonstration);

            return convertView;
        }

        private static final class ViewHolder {

            private final TextView textViewTitle;

            private ViewHolder(View convertView) {
                textViewTitle = (TextView) convertView.findViewById(android.R.id.text1);
            }

            private void updateView(Demonstration demonstration) {
                textViewTitle.setText(demonstration.title);
            }

        }

    }

}
