package it.sssup.retis.alessiobalsini.rtapp;

import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.TimerTask;

/**
 * Created by alessio on 05/06/17.
 */

public class TimerTaskWorker extends TimerTask {
    private String TAG;

    private double T; // Period (ms)
    private double d; // Relative Deadline (ms)
    private double C; // Computation time (ms)
    private double a_0; // Computation time (ms)
    private long job_id;

    private void busyWait() {
        long wakeup;

        wakeup = SystemClock.currentThreadTimeMillis() + (long)C;
        while (SystemClock.currentThreadTimeMillis() - wakeup < 0) ;
    }

    public TimerTaskWorker(String name,
                           double first_activation,
                           double period_ms,
                           double deadline_ms,
                           double computation_ms) {
        TAG = name;
        T = period_ms;
        d = deadline_ms;
        C = computation_ms;
        a_0 = first_activation;

        job_id = 0;
    }

    private void prettyStats(double a_i, double s_i, double f_i, double D_i) {
        String v;
        double x_max = 60.0;
        double scale = T / x_max;

        v = "[";

        for (int i=0; i < (s_i - a_i) / scale; i++) {
            v = v + "_";
        }

        for (int i=0; i < (f_i - s_i) / scale; i++) {
            v = v + "#";
        }

        for (int i=0; i < (D_i - f_i) / scale; i++) {
            v = v + "_";
        }

        v = v + "|";

        for (int i=0; i < (D_i - f_i  - T) / scale; i++) {
            v = v + "_";
        }

        v = v + "]";

        Log.d(TAG, v);
    }

    private String threadGroupIdToString(int id) {
        switch (id) {
            case -1:
                return "THREAD_GROUP_DEFAULT";
            case 0:
                return "THREAD_GROUP_BG_NONINTERACTIVE";
            case 1:
                return "THREAD_GROUP_FOREGROUND";
            case 2:
                return "THREAD_GROUP_SYSTEM";
            case 3:
                return "THREAD_GROUP_AUDIO_APP";
            case 4:
                return "THREAD_GROUP_AUDIO_SYS";
            case 5:
                return "THREAD_GROUP_TOP_APP";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public void run() {
        double s_i = System.currentTimeMillis();
        double a_i =  a_0 + (T * job_id);
        double D_i = a_i + d;
        double delay = s_i - a_i;
        double next_activation = a_0 + (T * (job_id + 1));
        double f_i;
        double lateness;

        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

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

        Class c;
        try {
            c = Class.forName("android.os.Process");
            Method m = c.getMethod("getProcessGroup", new Class[]{int.class});
            Object o = m.invoke(null, 0);

            Log.d(TAG, getSchedulingInfo() + " group (" + threadGroupIdToString((int) o) + " (" + o.toString() + "))");
        } catch (Exception e) {
            Log.d(TAG, "---- EXCEPTION ----" + e.getStackTrace());
        }

    }

    private native String getSchedulingInfo();
}
