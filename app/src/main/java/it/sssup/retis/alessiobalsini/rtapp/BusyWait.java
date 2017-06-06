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
    private long D; // Deadline (ms)
    private long C; // Computation time (ms)
    private long first_activation; // Computation time (ms)
    private long job_id;

    private void javaBusyWait(long s, long ms) {
        long wakeup = SystemClock.currentThreadTimeMillis() + (s * 1000) + ms;

        while (SystemClock.currentThreadTimeMillis() < wakeup) ;
    }

    public BusyWait(String name, long first_activation, long period_ms, long deadline_ms, long computation_ms) {
        TAG = name;
        T = period_ms;
        D = deadline_ms;
        C = computation_ms;
        this.first_activation = first_activation;

        job_id = 0;
    }

    @Override
    public void run() {
        long s_i = System.currentTimeMillis();
        long f_i;
        long a_i = first_activation + (T * job_id);
        long d = a_i + D;

        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        javaBusyWait(0, C);
        job_id++;

        f_i = System.currentTimeMillis();

        if (f_i > d) {
            Log.d(TAG, "!!! DEADLINE MISS !!!");
            while (first_activation + (T * (job_id + 1)) < System.currentTimeMillis()) {
                Log.d(TAG, "Skipping job " + job_id);
                job_id++;
            }
        } else {
            Log.d(TAG, "job: " + job_id
                    + "\ta_i: " + a_i
                    + "\tjitter: " + (s_i - a_i)
                    + "\tC: " + (f_i - s_i));
        }
    }
}
