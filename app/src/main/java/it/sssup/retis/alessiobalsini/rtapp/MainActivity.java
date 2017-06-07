package it.sssup.retis.alessiobalsini.rtapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;

import static java.lang.Math.ceil;

public class MainActivity extends AppCompatActivity {

    private LinkedList<Timer> timers;
    double instructions_per_ns;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private long instructions_to_wait_ns(double time_ns) {
        return (long) ceil(instructions_per_ns * time_ns);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instructions_per_ns = 0;

        timers = new LinkedList<Timer>();
        timers.clear();

        showThreadsNumber();
    }

    private void appendDbgText(String txt) {
        TextView tv = (TextView) findViewById(R.id.dbg_txt);
        tv.append(txt);

        ((ScrollView) findViewById(R.id.dbg_scroll)).post(new Runnable() {
            public void run() {
                ((ScrollView) findViewById(R.id.dbg_scroll)).fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void calibrate(View view) {
        Calibration c = new Calibration(20, 100000);
        instructions_per_ns = 0;

        appendDbgText("Calibrating...\n");

        for (int i=0; i<5; i++) {
            c.start();

            try {
                c.join();
                appendDbgText("- Instr./ns: " + c.instructions_over_ns() + "\n");
                if (instructions_per_ns < c.instructions_over_ns()) {
                    instructions_per_ns = c.instructions_over_ns();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        appendDbgText("-- Chosen : " + instructions_per_ns + "\n");
        appendDbgText("DONE\n");
    }

    private synchronized void showThreadsNumber() {
        appendDbgText(stringFromJNI() + ": " + timers.size() + "/" + Runtime.getRuntime().availableProcessors() + "\n");
    }

    public synchronized void increase_threads(View view) {
        BusyWait task;
        Date first_activation;
        Timer timer;
        long period = 100;
        long deadline = 100;
        long computation = 10;
        long phase = 2000;
        long first_activation_ms;

        timer = new Timer();
        timers.add(timer);

        first_activation_ms = System.currentTimeMillis() + phase;
        first_activation = new Date(first_activation_ms);
        task = new BusyWait("Task_" + (timers.size() - 1),
                first_activation_ms,
                period,
                deadline,
                computation);
        timer.scheduleAtFixedRate(task, first_activation, period);

        showThreadsNumber();
    }

    public synchronized void decrease_threads(View view) {
        if (timers.size() > 0) {
            timers.get(timers.size() - 1).cancel();
            timers.remove(timers.size() - 1);
        }
        showThreadsNumber();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
