package it.sssup.retis.alessiobalsini.rtapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private LinkedList<Thread> my_threads;
    double instructions_per_ns;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instructions_per_ns = 0;

        my_threads = new LinkedList<Thread>();
        my_threads.clear();

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
        Calibration c = new Calibration(20, 100000000);
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
        appendDbgText(stringFromJNI() + ": " + my_threads.size() + "/" + Runtime.getRuntime().availableProcessors() + "\n");
    }

    public synchronized void increase_threads(View view) {
        BusyWait t = new BusyWait(16, 16, 10);
        t.start();
        my_threads.add(t);

        showThreadsNumber();
    }

    public synchronized void decrease_threads(View view) {
        if (my_threads.size() > 0) {
            my_threads.get(0).interrupt();

            my_threads.remove(0);
        }
        showThreadsNumber();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
