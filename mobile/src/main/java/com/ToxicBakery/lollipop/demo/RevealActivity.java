package com.ToxicBakery.lollipop.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.ToxicBakery.lollipop.R;

public class RevealActivity extends Activity {


    private View imageToReveal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_reveal);
        imageToReveal = findViewById(R.id.imageView);
    }

    public void onRevealTapped(View view) {

        if (imageToReveal.getVisibility() == View.INVISIBLE) {
            playRevealAnimationForView(imageToReveal);
        } else {
            playHideAnimatinoForView(imageToReveal);
        }

    }

    private void playHideAnimatinoForView(final View viewToHide) {
        Animator anim = getHideAnimation(viewToHide);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                viewToHide.setVisibility(View.INVISIBLE);
            }
        });
        anim.start();
    }

    private Animator getHideAnimation(View viewToHide) {
        int cx = (viewToHide.getLeft() + viewToHide.getRight()) / 2;
        int cy = (viewToHide.getTop() + viewToHide.getBottom()) / 2;


        int initialRadius = viewToHide.getWidth();
        int finalRadius = 0;

        return ViewAnimationUtils.createCircularReveal(viewToHide,
                                                        cx,
                                                        cy,
                                                        initialRadius,
                                                        finalRadius);
    }

    private void playRevealAnimationForView(View revealView) {
        revealView.setVisibility(View.VISIBLE);
        // get the center for the clipping circle
        Animator anim = getRevealAnimation(revealView);
        anim.start();
    }

    private Animator getRevealAnimation(View revealView) {
        int cx = (revealView.getLeft() + revealView.getRight()) / 2;
        int cy = (revealView.getTop() + revealView.getBottom()) / 2;

        // get the final radius for the clipping circle
        int finalRadius = revealView.getWidth();
        int initialRadius = 0;

        // create and start the animator for this view
        // (the start radius is zero)
        return ViewAnimationUtils.createCircularReveal(revealView,
                                                        cx,
                                                        cy,
                                                        initialRadius,
                                                        finalRadius);
    }
}
