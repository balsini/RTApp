package it.sssup.retis.alessiobalsini.rtapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private LinkedList<Thread> my_threads;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        my_threads = new LinkedList<Thread>();
        my_threads.clear();

        showThreadsNumber();
    }

    public void calibrate(View view) {
        TextView tv = (TextView) findViewById(R.id.dbg_txt);
        tv.append("Calibrating load...");


    }

    private synchronized void showThreadsNumber() {
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.dbg_txt);
        tv.append(stringFromJNI() + ": " + my_threads.size() + "/" + Runtime.getRuntime().availableProcessors() + "\n");
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
