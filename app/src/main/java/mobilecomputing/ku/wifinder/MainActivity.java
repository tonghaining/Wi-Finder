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

import java.lang.Math;

public class MainActivity extends Activity {

    private static final int NUMBER_OF_ITERATIONS = 3;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TextView signalStrengthTextView;

    private TextView signalLevelTextView;

    private Button getSignalStrengthBtn;

    private Button startIterationsBtn;

    private TextView iterationsDataTextView;

    private List<Integer> collectedData = new ArrayList<>();

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

    public int getOptimalAngle(int firstRssi, int secondRssi, int thirdRssi,
                               int firstAngle, int secondAngle) {

        int angle = 180 - (secondAngle - firstAngle);

        double sinAngle = Math.sin(Math.toRadians(angle));
        double cosAngle = Math.cos(Math.toRadians(angle));

        int AD = secondRssi - firstRssi;
        int AB = thirdRssi - secondRssi;

        int transposeAngle = getTransposeAngle(AD, AB);

        AD = Math.abs(AD);
        AB = Math.abs(AB);

        int optimalAngle = (int) ((90 - angle) + Math.atan(Math.toRadians((AD - AB * cosAngle) / (AB * sinAngle))));

        int transposedOptimalAngle = optimalAngle + transposeAngle;

        int mapToCompassAngle = secondAngle - transposedOptimalAngle;

        return mapToCompassAngle;
    }

    private int getTransposeAngle(double AD, double AB) {
        int transposeAngle = 0;

        if (AD >= 0) {
            if (AB < 0) {
                transposeAngle = 90;
            }
        } else {
            transposeAngle = AB >= 0 ? -90 : -180;
        }
        return transposeAngle;
    }
}
