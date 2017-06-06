package it.sssup.retis.alessiobalsini.rtapp;

import android.os.SystemClock;
import android.util.Log;

import java.util.TimerTask;

/**
 * Created by alessio on 05/06/17.
 */

public class BusyWait extends TimerTask {

    private long T; // Period (ms)
    private long D; // Deadline (ms)
    private long C; // Computation time (ms)
    private long first_activation; // Computation time (ms)
    private long job_id;

    private String TAG;

    private void javaBusyWait(long s, long ms) {
        long now;
        long wakeup = SystemClock.currentThreadTimeMillis();

        wakeup += (s * 1000) + ms;

        do {
            now = SystemClock.currentThreadTimeMillis();
        } while (now < wakeup);
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
        long activation = first_activation + (T * job_id);
        long starting = 0;
        long finishing = 0;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        starting = System.currentTimeMillis();
        javaBusyWait(0, C);
        finishing = System.currentTimeMillis();

        Log.d(TAG, "run: job " + job_id
                + " a_i: " + activation
                + " jitter: " + (starting - activation)
                + " C: " + (finishing - starting));

        job_id++;
    }

    public native void waitAbsolute(long absTime_ms);
}
