package it.sssup.retis.alessiobalsini.rtapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Use with
 * $ adb shell am broadcast -a "it.sssup.retis.alessiobalsini.RTAPP_INTENT" -e tasks 3 -e period 200 -e utilization 0.3
 */

public class MyReceiver extends BroadcastReceiver {
    private static LinkedList<Timer> timers = null;
    private static Timer experiment_expired_timer = null;
    private final static double phase = 2000;
    private final static String TAG = "MyReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (timers == null) {
            timers = new LinkedList<Timer>();
            timers.clear();
        }

        String period = intent.getStringExtra("period");
        String utilization = intent.getStringExtra("utilization");
        String tasks = intent.getStringExtra("tasks");
        String duration = intent.getStringExtra("duration");

        String outputText = "RTApp Intent detected:"
                + "\n- Tasks: " + tasks
                + "\n- Period: " + period
                + "\n- Utilization: " + utilization
                + "\n- Duration: " + duration;
        Toast.makeText(context, outputText, Toast.LENGTH_LONG).show();

        if (period == null || tasks == null || utilization == null || duration == null)
            return;

        GlobalTaskParameters.T = Double.parseDouble(period);
        GlobalTaskParameters.d = GlobalTaskParameters.T;
        GlobalTaskParameters.C = Double.parseDouble(utilization) * GlobalTaskParameters.T;

        while (timers.size() > 0)
            decrease_threads();
        while (timers.size() < Integer.parseInt(tasks))
            increase_threads(GlobalTaskParameters.T, GlobalTaskParameters.C);

        if (experiment_expired_timer != null) {
            try {
                experiment_expired_timer.cancel();
                experiment_expired_timer.purge();
            } catch (Exception e) {}
        }

        experiment_expired_timer = new Timer();

        experiment_expired_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish_experiment();
            }
        }, (long)(Double.parseDouble(duration) * 1000.0 + phase)); // Timer delay expressed in ms

        TimerTaskWorker.experiment_running = true;
    }

    public static void finish_experiment() {
        TimerTaskWorker.experiment_running = false;

        while (timers.size() > 0)
            decrease_threads();

        Log.d(TAG, "---- EXPERIMENT COMPLETED ----");
    }

    private void increase_threads(double period, double computation)
    {
        TimerTaskWorker task;
        Date first_activation;
        Timer timer;
        double deadline = period;
        double first_activation_ms;
        boolean global = true;

        timer = new Timer();
        timers.add(timer);

        first_activation_ms = System.currentTimeMillis() + phase;
        first_activation = new Date((long)first_activation_ms);

        if (global) {
            GlobalTaskParameters.T = period;
            GlobalTaskParameters.d = deadline;
            GlobalTaskParameters.C = computation;

            task = new TimerTaskWorker(timers.size() - 1,
                    first_activation_ms);
        } else {
            task = new TimerTaskWorker(timers.size() - 1,
                    first_activation_ms,
                    period,
                    deadline,
                    computation);
        }

        timer.scheduleAtFixedRate(task, first_activation, (long)period);
    }

    private static void decrease_threads()
    {
        if (timers.size() > 0) {
            timers.get(timers.size() - 1).cancel();
            timers.get(timers.size() - 1).purge();
            timers.remove(timers.size() - 1);
        }
    }
}
