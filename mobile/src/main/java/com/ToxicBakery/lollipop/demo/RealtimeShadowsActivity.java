package com.ToxicBakery.lollipop.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ToxicBakery.lollipop.R;

public class RealtimeShadowsActivity extends Activity implements View.OnClickListener {

    private static final String KEY_ELEVATION = RealtimeShadowsActivity.class.getSimpleName() + ".KEY_ELEVATION";

    private Button buttonPumpUp;
    private int elevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_realtime_shadow);

        buttonPumpUp = (Button) findViewById(R.id.activity_demo_realtime_shadow_button);
        buttonPumpUp.setOnClickListener(this);

        if (savedInstanceState != null) {
            elevation = savedInstanceState.getInt(KEY_ELEVATION);
            updateElevation();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_ELEVATION, elevation);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_demo_realtime_shadow_button:
                elevation += 2;
                if (elevation > 10)
                    elevation = 0;

                updateElevation();
                break;
        }
    }

    private void updateElevation() {
        buttonPumpUp.setElevation(elevation);
        buttonPumpUp.setTranslationZ(elevation);
    }
}
