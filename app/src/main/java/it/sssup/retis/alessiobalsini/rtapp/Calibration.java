package it.sssup.retis.alessiobalsini.rtapp;

import android.util.Log;

/**
 * Created by alessio on 06/06/17.
 */

public class Calibration extends Thread {

    private String TAG = "Calibration";
    private long counter;
    private long max;
    private long time;

    public Calibration(long max) {
        this.max = max;
    }

    @Override
    public void run() {
        counter = 0;
        long start_time;
        long end_time;

        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        start_time = System.currentTimeMillis();

        while (counter < max) {
            counter++;
        }

        end_time = System.currentTimeMillis();

        time = end_time - start_time;

        Log.d(TAG, "time: " + time);
    }

    public double result() {
        return (double) counter / (double)time;
    }
}
