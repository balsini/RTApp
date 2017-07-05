package it.sssup.retis.alessiobalsini.rtapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private int desired_tasks;
    private int current_tasks;

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
            tasks = Integer.toString(desired_tasks = 2);

        String period = getIntent().getStringExtra("period");
        if (period == null)
            period = "200";

        String utilization = getIntent().getStringExtra("utilization");
        if (utilization == null)
            utilization = "0.3";

        ((EditText) findViewById(R.id.utilizationValue)).setText(utilization);
        ((EditText) findViewById(R.id.periodValue)).setText(period);

        showThreadsNumber();

        GlobalTaskParameters.filesDir = getFilesDir();

        current_tasks = 0;

        //appendDbgText("Desired Utilization: " + utilization + " Desired Period: " + period + "\n");
        //appendDbgText("Property debug.sys.noschedgroups: " + getProperty("debug.sys.noschedgroups") + "\n");
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
        ((TextView) findViewById(R.id.desired_threads_number)).setText("#Desired: " + Integer.toString(desired_tasks));
        ((TextView) findViewById(R.id.threads_number)).setText("#Task: " + current_tasks);
        ((TextView) findViewById(R.id.cores_number)).setText("#Core: " + Runtime.getRuntime().availableProcessors());
    }

    private synchronized void go()
    {
        double period = Double.parseDouble(((EditText) findViewById(R.id.periodValue)).getText().toString());
        double utilization = Double.parseDouble(((EditText) findViewById(R.id.utilizationValue)).getText().toString());

        current_tasks = desired_tasks;

        showThreadsNumber();

        //appendDbgText(getSchedulingInfo() + "\n");

        Intent intent = new Intent();
        intent.setAction("it.sssup.retis.alessiobalsini.RTAPP_INTENT");
        intent.putExtra("tasks", Integer.toString(current_tasks));
        intent.putExtra("period", Double.toString(period));
        intent.putExtra("utilization", Double.toString(utilization));
        sendBroadcast(intent);
    }

    public void go_callback(View v)
    {
        go();
    }

    public void increase_threads_callback(View v)
    {
        desired_tasks++;
        showThreadsNumber();
    }

    public synchronized void decrease_threads_callback(View v)
    {
        if (desired_tasks > 0) {
            desired_tasks--;
            showThreadsNumber();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    private native String getSchedulingInfo();
}
