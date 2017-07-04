package it.sssup.retis.alessiobalsini.rtapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;

import static java.lang.System.getProperty;

public class MainActivity extends AppCompatActivity {
    private LinkedList<Timer> timers;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((EditText) findViewById(R.id.utilizationValue)).addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count)
            {
                if (s.length() != 0) {
                    double value = Double.parseDouble(s.toString());

                    ((SeekBar) findViewById(R.id.utilizationSeekBar)).setProgress((int) (value * 100.0));
                }
            }
        });

        ((SeekBar) findViewById(R.id.utilizationSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar s)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar s)
            {
            }

            @Override
            public void onProgressChanged(SeekBar s, int v, boolean b)
            {
                EditText textField = ((EditText) findViewById(R.id.utilizationValue));
                double valueInField = Double.parseDouble(textField.getText().toString());

                if ((int)(valueInField * 100.0) != v) {
                    String newTextValue = Double.toString((double)(v) / 100.0);

                    GlobalTaskParameters.C = GlobalTaskParameters.T * v / 100.0;

                    textField.setText(newTextValue);
                }
            }
        });

        String tasks = getIntent().getStringExtra("tasks");
        if (tasks == null)
            tasks = "4";

        String period = getIntent().getStringExtra("period");
        if (period == null)
            period = "200";

        String utilization = getIntent().getStringExtra("utilization");
        if (utilization == null)
            utilization = "0.3";

        ((EditText) findViewById(R.id.utilizationValue)).setText(utilization);
        ((EditText) findViewById(R.id.periodValue)).setText(period);

        timers = new LinkedList<Timer>();
        timers.clear();

        for (int i=0; i<Integer.parseInt(tasks); i++) {
            increase_threads();
        }

        showThreadsNumber();

        appendDbgText("Desired Utilization: " + utilization + " Desired Period: " + period + "\n");

        appendDbgText("Property debug.sys.noschedgroups: " + getProperty("debug.sys.noschedgroups") + "\n");
    }

    private void appendDbgText(String txt)
    {
        TextView tv = (TextView) findViewById(R.id.dbg_txt);
        tv.append(txt);

        ((ScrollView) findViewById(R.id.dbg_scroll)).post(new Runnable() {
            public void run()
            {
                ((ScrollView) findViewById(R.id.dbg_scroll)).fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private synchronized void showThreadsNumber()
    {
        ((TextView) findViewById(R.id.threads_over_cores)).setText("Threads: " + timers.size()
                + " Cores: " + Runtime.getRuntime().availableProcessors());
    }

    private synchronized void increase_threads()
    {
        TimerTaskWorker task;
        Date first_activation;
        Timer timer;
        double period = Double.parseDouble(((EditText) findViewById(R.id.periodValue)).getText().toString());
        double computation = Double.parseDouble(((EditText) findViewById(R.id.utilizationValue)).getText().toString()) * period;
        double deadline = period;
        double phase = 2000;
        double first_activation_ms;
        boolean global = true;

        appendDbgText(getSchedulingInfo() + "\n");

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
        task.setFilesDir(getFilesDir());

        timer.scheduleAtFixedRate(task, first_activation, (long)period);

        showThreadsNumber();
    }

    private synchronized void decrease_threads()
    {
        if (timers.size() > 0) {
            timers.get(timers.size() - 1).cancel();
            timers.remove(timers.size() - 1);
        }
        showThreadsNumber();
    }

    public void increase_threads_callback(View v)
    {
        increase_threads();
    }

    public synchronized void decrease_threads_callback(View v)
    {
        decrease_threads();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    private native String getSchedulingInfo();
}
