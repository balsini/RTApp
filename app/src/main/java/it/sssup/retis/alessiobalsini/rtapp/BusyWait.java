package it.sssup.retis.alessiobalsini.rtapp;

import android.os.SystemClock;
import android.util.Log;

import java.util.TimerTask;

/**
 * Created by alessio on 05/06/17.
 */

public class BusyWait extends TimerTask {
    private String TAG;

    private long T; // Period (ms)
    private long d; // Relative Deadline (ms)
    private long C; // Computation time (ms)
    private long a_0; // Computation time (ms)
    private long job_id;

    private void busyWait() {
        long wakeup;

        wakeup = SystemClock.currentThreadTimeMillis() + C;
        while (SystemClock.currentThreadTimeMillis() - wakeup < 0) ;
    }

    public BusyWait(String name, long first_activation, long period_ms, long deadline_ms, long computation_ms) {
        TAG = name;
        T = period_ms;
        d = deadline_ms;
        C = computation_ms;
        a_0 = first_activation;

        job_id = 0;
    }

    private void prettyStats(long a_i, long s_i, long f_i, long D_i) {
        String v;
        int x_max = 60;
        double scale = (double) T / (double) x_max;

        v = "[";

        for (int i=0; i < (double)(s_i - a_i) / scale; i++) {
            v = v + "_";
        }

        for (int i=0; i < (double)(f_i - s_i) / scale; i++) {
            v = v + "#";
        }

        for (int i=0; i < (double)(D_i - f_i) / scale; i++) {
            v = v + "_";
        }

        v = v + "|";

        for (int i=0; i < (double)(D_i - f_i  - T) / scale; i++) {
            v = v + "_";
        }

        v = v + "]";

        Log.d(TAG, v);
    }

    @Override
    public void run() {
        long s_i = System.currentTimeMillis();
        long a_i = a_0 + (T * job_id);
        long D_i = a_i + d;
        long delay = s_i - a_i;
        long next_activation = a_0 + (T * (job_id + 1));
        long f_i;
        long lateness;

        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        job_id++;
        busyWait();

        f_i = System.currentTimeMillis();

        lateness = f_i - D_i;

        prettyStats(a_i, s_i, f_i, D_i);

        if (lateness > 0) {
            Log.d(TAG, "!!! DEADLINE MISS !!!");
        } else {
            Log.d(TAG, "job: " + job_id
                    + "\ta_i: " + a_i
                    + "\tjitter: " + delay
                    + "\tC: " + (f_i - s_i));
        }

        Log.d(TAG, getSchedulingInfo());
    }

    private native String getSchedulingInfo();
}
