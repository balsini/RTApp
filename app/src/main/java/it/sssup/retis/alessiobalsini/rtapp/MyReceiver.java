package it.sssup.retis.alessiobalsini.rtapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;

/*
 * Use with
 * $ adb shell am broadcast -a "it.sssup.retis.alessiobalsini.RTAPP_INTENT" -e tasks 3 -e period 200 -e utilization 0.3
 */

public class MyReceiver extends BroadcastReceiver {
    private static LinkedList<Timer> timers = null;

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

        String outputText = "RTApp Intent detected:"
                + "\n- Tasks: " + tasks
                + "\n- Period: " + period
                + "\n- Utilization: " + utilization;
        Toast.makeText(context, outputText, Toast.LENGTH_LONG).show();

        if (period == null || tasks == null || utilization == null)
            return;

        GlobalTaskParameters.T = Double.parseDouble(period);
        GlobalTaskParameters.d = GlobalTaskParameters.T;
        GlobalTaskParameters.C = Double.parseDouble(utilization) * GlobalTaskParameters.T;

        while (timers.size() > 0)
            decrease_threads();
        while (timers.size() < Integer.parseInt(tasks))
            increase_threads(GlobalTaskParameters.T, GlobalTaskParameters.C);
    }

    private void increase_threads(double period, double computation)
    {
        TimerTaskWorker task;
        Date first_activation;
        Timer timer;
        double deadline = period;
        double phase = 2000;
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

    private void decrease_threads()
    {
        if (timers.size() > 0) {
            timers.get(timers.size() - 1).cancel();
            timers.remove(timers.size() - 1);
        }
    }
}
