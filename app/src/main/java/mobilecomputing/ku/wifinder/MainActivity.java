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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.lang.Math;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int NUMBER_OF_ITERATIONS = 8;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TextView currentAngleTextView;

    private TextView bestAngleTextView;

    private TextView signalStrengthTextView;

    private Button runButton;

    private List<Integer> collectedData = new ArrayList<>();

    private Compass compass;

    private int iterCount;

    private int lastIterationResult;

    private List<Integer> angleList = new ArrayList<>();

    private List<Integer> signalList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signalStrengthTextView = findViewById(R.id.signal_strength);
        runButton = findViewById(R.id.runButton);
        runButton.setOnClickListener(this);

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


    private Integer collectData() throws ExecutionException, InterruptedException {

        Future<Integer> futureData = executorService.submit(() -> {
            List<Integer> collectedData = new ArrayList<>();

            for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
                int wifiStrength = getWiFiStrength();

                collectedData.add(wifiStrength);
//                iterationsDataTextView.post(() -> {
//                    iterationsDataTextView.append(wifiStrength + "\n");
//                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return calculateAverage(collectedData);
        });

        return futureData.get();
    }

    private int calculateAverage(List <Integer> list) {
        Integer sum = 0;
        if(!list.isEmpty()) {
            for (Integer item : list) {
                sum += item;
            }
            return (int) (sum.doubleValue() / list.size());
        }
        return sum;
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
        bestAngleTextView = findViewById(R.id.current_best_angle_textview);
        bestAngleTextView.setText(getString(R.string.best_angle_placeholder, 3 - iterCount));
        currentAngleTextView = findViewById(R.id.current_angle_textview);
    }

    public int iteration(int signal, Integer angle) {
        this.iterCount++;
        this.signalList.add(signal);

        if (angle == null) {
            return -1;
        }
        this.angleList.add(angle);

        if (this.iterCount >= 3) {
            Integer[] angles = new Integer[this.angleList.size()];
            angles = this.angleList.toArray(angles);

            Integer[] signals = new Integer[this.signalList.size()];
            signals = this.signalList.toArray(signals);

            int firstRssi = signals[signals.length - 3];
            int secondRssi = signals[signals.length - 2];
            int thirdRssi = signals[signals.length - 1];
            int firstAngle = angles[angles.length - 2];
            int secondAngle = angles[angles.length - 1];
            int opt = this.getOptimalAngle(firstRssi, secondRssi, thirdRssi, firstAngle, secondAngle);

            return opt;
        } else {
            return -1;
        }
    }

    public int getOptimalAngle(int firstRssi, int secondRssi, int thirdRssi,
                               int firstAngle, int secondAngle) {

        int angle;
        boolean isRightTurn = true;
        int angleOffset = (540 - firstAngle) % 360;

        //map to 180
        int firstAngleMapped = (firstAngle + angleOffset) % 360; // = 180
        int secondAngleMapped = (secondAngle + angleOffset) % 360;

        if (secondAngleMapped > firstAngleMapped) {
            // User turned right
            angle = 360 - secondAngleMapped;
        } else {
            // User turned left
            angle = secondAngleMapped;
            isRightTurn = false;
        }

        double sinAngle = Math.sin(Math.toRadians(angle));
        double cosAngle = Math.cos(Math.toRadians(angle));

        int AD = secondRssi - firstRssi;
        int AB = thirdRssi - secondRssi;

        int transposeAngle = getTransposeAngle(AD, AB, isRightTurn);

        AD = Math.abs(AD);
        AB = Math.abs(AB);

        int optimalAngle = (int) ((90 - angle) + Math.atan(Math.toRadians((AD - AB * cosAngle) / (AB * sinAngle))));
        optimalAngle = Math.abs(optimalAngle);

        if (isRightTurn) {
            optimalAngle = -optimalAngle;
        }

        // map back the secondAngleMapped to the initial position
        int compensateAngleOffset = 360 - angleOffset;

        //map to compass angle
        return (secondAngleMapped + transposeAngle + optimalAngle + compensateAngleOffset) % 360;
        //return (secondAngle + transposeAngle + optimalAngle) % 360;
    }

    private int getTransposeAngle(int AD, int AB, boolean isRightTurn) {
        if (isRightTurn) {
            return getRightTransposeAngle(AD, AB);
        }
        return getLeftTransposeAngle(AD, AB);
    }

    private int getLeftTransposeAngle(double AD, double AB) {
        int transposeAngle = getRightTransposeAngle(AD, AB);
        if (transposeAngle == 180) {
            return transposeAngle;
        }
        return -transposeAngle;
    }

    private int getRightTransposeAngle(double AD, double AB) {
        int transposeAngle = 0;

        if (AD >= 0) {
            if (AB < 0) {
                transposeAngle = -90;
            }
        } else {
            transposeAngle = AB >= 0 ? 90 : 180;
        }
        return transposeAngle;
    }

    @Override
    public void onClick(View v) {
        int wifiStrength = 0;
        try {
            wifiStrength = collectData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (iterCount == 0) {
            iteration(wifiStrength, (int) compass.getAzimuth());

        } else if (iterCount == 1) {
            iteration(wifiStrength, null);

        } else if (iterCount == 2) {
            lastIterationResult = iteration(wifiStrength, (int) compass.getAzimuth());

        } else if (iterCount > 2) {

            lastIterationResult = iteration(wifiStrength, lastIterationResult);

        }

        signalStrengthTextView.setText(getString(R.string.signal_strength) + wifiStrength);
        bestAngleTextView.setText("Optimal angle: " + lastIterationResult);
    }
}
