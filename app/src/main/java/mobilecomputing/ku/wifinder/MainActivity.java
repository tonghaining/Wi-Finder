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
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int NUMBER_OF_ITERATIONS = 20;

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
            collectData();
            iterationsDataTextView.setText(getIterationsDataString());
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

    private void collectData() {
        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            collectedData.add(getWiFiStrength());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getIterationsDataString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int data : collectedData) {
            stringBuilder.append(data).append("\n");
        }

        return stringBuilder.toString();
    }
}
