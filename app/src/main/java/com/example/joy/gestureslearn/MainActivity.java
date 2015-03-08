package com.example.joy.gestureslearn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;


public class MainActivity extends ActionBarActivity {
    public static final String DEBUG_TAG = "debug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout frame = (FrameLayout) findViewById(R.id.graphics_holder);
        PlayAreaView image = new PlayAreaView(this);
        frame.addView(image);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class PlayAreaView extends View {

        GestureDetector gestures;
        private Matrix translate;
        private Bitmap droid;
        private Matrix animateStart;
        private Interpolator animateInterpolator;
        private long startTime;
        private long endTime;
        private float totalAnimDx;
        private float totalAnimDy;

        public PlayAreaView(Context context) {
            super(context);
            translate = new Matrix();
            gestures = new GestureDetector(MainActivity.this,
                    new GestureListener(this));
            droid = BitmapFactory.decodeResource(getResources(),
                    R.drawable.basketball);
        }

        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(droid, translate, null);
            Matrix m = canvas.getMatrix();
            Log.d(DEBUG_TAG, "Matrix: " + translate.toShortString());
            Log.d(DEBUG_TAG, "Canvas: " + m.toShortString());
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return gestures.onTouchEvent(event);
        }

        public void onMove(float dx, float dy) {
            translate.postTranslate(dx, dy);
            invalidate();
        }

        public void onResetLocation() {
            translate.reset();
            invalidate();
        }

        public void onAnimateMove(float dx, float dy, long duration) {
            animateStart = new Matrix(translate);
            animateInterpolator = new OvershootInterpolator();
            startTime = System.currentTimeMillis();
            endTime = startTime + duration;
            totalAnimDx = dx;
            totalAnimDy = dy;
            post(new Runnable() {
                @Override
                public void run() {
                    onAnimateStep();
                }
            });
        }

        private void onAnimateStep() {
            long curTime = System.currentTimeMillis();
            float percentTime = (float) (curTime - startTime)
                    / (float) (endTime - startTime);
            float percentDistance = animateInterpolator
                    .getInterpolation(percentTime);
            float curDx = percentDistance * totalAnimDx;
            float curDy = percentDistance * totalAnimDy;
            translate.set(animateStart);
            onMove(curDx, curDy);

            //Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
            if (percentTime < 1.0f) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        onAnimateStep();
                    }
                });
            }
        }

        public void onSetLocation(float dx, float dy) {
            translate.postTranslate(dx, dy);
        }

    }

    private class GestureListener implements GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener {
        PlayAreaView view;

        public GestureListener(PlayAreaView view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.v(DEBUG_TAG, "onSingleTapConfirmed");
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.v(DEBUG_TAG, "onDoubleTap");
            view.onResetLocation();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.v(DEBUG_TAG, "onDoubleTapEvent");
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.v(DEBUG_TAG, "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.v(DEBUG_TAG, "onScroll");

            view.onMove(-distanceX, -distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.v(DEBUG_TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.v(DEBUG_TAG, "onFling");
            final float distanceTimeFactor = 0.4f;
            final float totalDx = (distanceTimeFactor * velocityX / 2);
            final float totalDy = (distanceTimeFactor * velocityY / 2);

            view.onAnimateMove(totalDx, totalDy,
                    (long) (1000 * distanceTimeFactor));
            return true;
        }
    }
}
