package it.sssup.retis.alessiobalsini.rtapp;

import android.util.Log;

/**
 * Created by alessio on 06/06/17.
 */

public class Calibration extends Thread {

    private String TAG = "Calibration";

    private int measures;
    private long max;
    private long time[];

    public Calibration(int measures, long max) {
        this.measures = measures;
        time = new long[this.measures];
        this.max = max;
    }

    @Override
    public void run() {
        long start_time;
        long end_time;

        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        for (int i = 0; i < measures; i++) {
            long counter = 0;

            start_time = System.nanoTime();

            while (counter < max) {
                counter++;
            }

            end_time = System.nanoTime();

            time[i] = end_time - start_time;

            Log.d(TAG, "time: " + time[i]);
        }
    }

    public double instructions_over_ns() {
        double sum = 0;

        for (int i = 0; i < measures; i++) {
            sum = sum + time[i];
        }

        return (double) max / (double) sum * (double) measures;
    }
}
