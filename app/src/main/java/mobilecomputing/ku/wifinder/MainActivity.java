/*
 *  This file provided by Facebook is for non-commercial testing and evaluation
 *  purposes only.  Facebook reserves all rights not expressly granted.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package mobilecomputing.ku.wifinder;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends Activity {

    private static final int NUMBER_OF_ITERATIONS = 3;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TextView signalStrengthTextView;

    private TextView signalLevelTextView;

    private Button getSignalStrengthBtn;

    private Button startIterationsBtn;

    private TextView iterationsDataTextView;

    private TextView currentAngleTextView;

    private List<Integer> collectedData = new ArrayList<>();

    private Compass compass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signalStrengthTextView = findViewById(R.id.signal_strength);
        signalLevelTextView = findViewById(R.id.signal_level);
        iterationsDataTextView = findViewById(R.id.iterations_data);

        getSignalStrengthBtn = findViewById(R.id.get_signal_strength_btn);
        getSignalStrengthBtn.setOnClickListener((view) -> {
            signalStrengthTextView.setText(getText(R.string.signal_strength) + "" + getWiFiStrength());
            signalLevelTextView.setText(getString(R.string.signal_level, getWifiSignalLevel()));
        });

        startIterationsBtn = findViewById(R.id.start_iterations);
        startIterationsBtn.setOnClickListener((view) -> {
            try {
                collectedData = collectData();
            } catch (Exception e) {
                Log.e(MainActivity.class.toString(), e.toString());
            }

//            iterationsDataTextView.setText(getIterationsDataString());
        });

        setupCompass();

    }

    private int getWiFiStrength() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        return wifiManager.getConnectionInfo().getRssi();
    }

    private int getWifiSignalLevel() {
        int rssi = getWiFiStrength();

        return WifiManager.calculateSignalLevel(rssi, 5);
    }

    private List<Integer> collectData() throws ExecutionException, InterruptedException {

        Future<List<Integer>> futureData = executorService.submit(() -> {
            List<Integer> collectedData = new ArrayList<>();

            for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
                int wifiStrength = getWiFiStrength();

                collectedData.add(wifiStrength);
                iterationsDataTextView.post(() -> {
                    iterationsDataTextView.append(wifiStrength + "\n");
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return collectedData;
        });

        return futureData.get();
    }

    private String getIterationsDataString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int data : collectedData) {
            stringBuilder.append(data).append("\n");
        }

        return stringBuilder.toString();
    }

    private void setupCompass() {
        compass = new Compass(this);
        compass.setListener((float azimuth) ->
            currentAngleTextView.setText(getString(R.string.current_angle, azimuth))
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        compass.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        compass.start();
        currentAngleTextView = findViewById(R.id.current_angle_textview);
    }

}
