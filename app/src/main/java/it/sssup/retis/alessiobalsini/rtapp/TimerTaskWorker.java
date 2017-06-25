package it.sssup.retis.alessiobalsini.rtapp;

import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

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
    private boolean global_parameters;

    private void busyWait(long time_ms)
    {
        long wakeup;

        wakeup = SystemClock.currentThreadTimeMillis() + time_ms;
        while (SystemClock.currentThreadTimeMillis() - wakeup < 0) ;
    }

    public TimerTaskWorker(String name,
                           double first_activation,
                           double period_ms,
                           double deadline_ms,
                           double computation_ms)
    {
        TAG = name;
        global_parameters = false;
        T = period_ms;
        d = deadline_ms;
        C = computation_ms;
        a_0 = first_activation;
        job_id = 0;
    }

    public TimerTaskWorker(String name,
                           double first_activation)
    {
        TAG = name;
        global_parameters = true;
        a_0 = first_activation;
        job_id = 0;
    }

    private void prettyStats(double a_i, double s_i, double f_i, double D_i)
    {
        String v;
        double x_max = 60.0;
        double period = global_parameters ? GlobalTaskParameters.T : T;
        double scale = period / x_max;

        v = "[";

        double scaled_jitter = (s_i - a_i) / scale;

        for (int i=0; i < scaled_jitter; i++) {
            v = v + "_";
        }

        double scaled_computation = (f_i - s_i) / scale;

        for (int i=0; i < scaled_computation; i++) {
            v = v + "#";
        }

        double scaled_time_until_dl = (D_i - f_i) / scale;

        for (int i=0; i < scaled_time_until_dl; i++) {
            v = v + "_";
        }

        v = v + "|";

        double scaled_time_until_next_activation = (D_i - f_i - period) / scale;

        for (int i=0; i < scaled_time_until_next_activation; i++) {
            v = v + "_";
        }

        v = v + "]";

        Log.d(TAG, v);
    }

    private String threadGroupIdToString(int id)
    {
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
    public void run()
    {
        double s_i = System.currentTimeMillis();
        double a_i;
        double D_i;
        double next_activation;
        double f_i;
        double lateness;
        double delay;

        if (global_parameters) {
            a_i =  a_0 + (GlobalTaskParameters.T * job_id);
            D_i = a_i + GlobalTaskParameters.d;
            next_activation = a_0 + (GlobalTaskParameters.T * (job_id + 1));
        } else {
            a_i =  a_0 + (T * job_id);
            D_i = a_i + d;
            next_activation = a_0 + (T * (job_id + 1));
        }

        delay = s_i - a_i;

        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

        job_id++;

        if (global_parameters) {
            busyWait((long)GlobalTaskParameters.C);
        } else {
            busyWait((long)C);
        }

        /*
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
        */
    }

    private native String getSchedulingInfo();
}
