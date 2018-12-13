package com.example.bang.mapuse;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class title extends AppCompatActivity {
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                finish();
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 3000);
    }
    @Override
    protected void onDestroy() {
        Log.i("test", "onDstory()");
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
