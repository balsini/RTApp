package it.sssup.retis.alessiobalsini.rtapp;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by alessio on 05/06/17.
 */

public class BusyWait extends Thread {

    private long T; // Period (ms)
    private long D; // Deadline (ms)
    private long C; // Computation time (ms)

    private String TAG = "BusyWait";

    private void javaBusyWait(long s, long ms) {
        long now;
        long wakeup = SystemClock.currentThreadTimeMillis();

        wakeup += (s * 1000) + ms;

        do {
            now = SystemClock.currentThreadTimeMillis();
        } while (now < wakeup);
    }

    public BusyWait(long period_ms, long deadline_ms, long computation_ms) {
        T = period_ms;
        D = deadline_ms;
        C = computation_ms;
    }

    @Override
    public void run() {
        int i=0;
        long activation = 0;
        long starting = 0;
        long finishing = 0;

        /* Initialization */

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        activation = System.currentTimeMillis();

        /* Loop */
        //try {
            while (!isInterrupted()) {

                starting = System.currentTimeMillis();
                javaBusyWait(0, C);
                finishing = System.currentTimeMillis();

                Log.d(TAG, "run: job " + i + " busyWaiting (ms): jitter: " + (starting - activation) + " C: " + (finishing - starting));

                //Thread.sleep(1000);

                //Timer.scheduleAtFixedRate

                activation = activation + T;
                waitAbsolute(activation);

                i++;
            }
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
    }

    public native void waitAbsolute(long absTime_ms);
}
