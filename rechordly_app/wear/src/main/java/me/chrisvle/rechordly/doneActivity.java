package me.chrisvle.rechordly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class doneActivity extends Activity {

    private ImageButton mImageButton;
    private GestureDetector mDetector;
    private GestureDetector tapDetector;
    private static final String DEBUG_TAG = "Gestures";
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        Intent i = getIntent();
        time = i.getStringExtra("time");

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mImageButton = (ImageButton) stub.findViewById(R.id.done_btn);
                mImageButton.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent e) {
                        if (tapDetector.onTouchEvent(e)) {
                            // single tap
                            return true;
                        } else {
                            return mDetector.onTouchEvent(e);
                        }
                    }
                });
            }
        });

        //Configure single tap detector
        tapDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                return true;
            }
        });

        // Configure a gesture detector
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent event) {
//                mDismissOverlay.show();
                Log.d(DEBUG_TAG, " onLongPress: " + event.toString());
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
//                Log.d("Event: ", "onSingleTapEvent Fired!");
//                Intent intent = new Intent(getBaseContext(), SaveRetryActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {
                Log.d(DEBUG_TAG, "onScroll: Distance: " + String.valueOf(distanceX) + ", " + String.valueOf(distanceY));
                if (distanceX > 5.0) {
                    Log.d("Event: ", "onScrollEvent Fired!");
                    Intent intent = new Intent(getBaseContext(), CropActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("swipe", "right");
                    intent.putExtra("time", time);
                    startActivity(intent);
                }
                if (distanceX < -5.0) {
                    Log.d("Event: ", "onScrollEvent Fired!");
                    Intent intent = new Intent(getBaseContext(), Main2Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("swipe", "left");
                    intent.putExtra("time", time);
                    startActivity(intent);
                    return true;
                }
                Log.d(DEBUG_TAG, " onScroll: " + e1.toString()+e2.toString());
                return false;
            }
        });
    }

    // Capture long presses
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    public void onClick(View v) {
        Intent intent = new Intent(getBaseContext(), SaveRetryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }


}
