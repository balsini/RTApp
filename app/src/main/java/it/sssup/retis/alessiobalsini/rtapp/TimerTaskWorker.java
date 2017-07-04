package it.sssup.retis.alessiobalsini.rtapp;

import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    private int id;
    private File files_dir;
    private FileWriter my_file;

    // Response times for each job
    private int RT_max;
    private double RT[];
    private int RT_c;

    public enum ReportDestination {
        DEST_FILE,
        DEST_UDP
    }

    private void busyWait(long time_ms)
    {
        long wakeup;

        wakeup = SystemClock.currentThreadTimeMillis() + time_ms;
        while (SystemClock.currentThreadTimeMillis() - wakeup < 0) ;
    }

    private void init()
    {
        TAG = "Task_" + String.valueOf(id);
        job_id = 0;
        RT_c = 0;
        //RT_max = 254;
        RT_max = 1024;
        RT = new double[RT_max];
        my_file = null;
    }

    public TimerTaskWorker(int id,
                           double first_activation,
                           double period_ms,
                           double deadline_ms,
                           double computation_ms)
    {
        global_parameters = false;
        T = period_ms;
        d = deadline_ms;
        C = computation_ms;
        a_0 = first_activation;
        this.id = id;
        init();
    }

    public TimerTaskWorker(int id,
                           double first_activation)
    {
        global_parameters = true;
        a_0 = first_activation;
        this.id = id;
        init();
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

    public void setFilesDir(File filesDir)
    {
        files_dir = filesDir;
        Log.d(TAG, "Files directory: " + files_dir.toString());

        try {
            File tmp_file = new File(files_dir, TAG + "_RT.txt");

            if (tmp_file.exists()) {
                tmp_file.delete();
            }
            tmp_file.createNewFile();

            my_file = new FileWriter(tmp_file.toString(), true);
            Log.d(TAG, "File created: " + tmp_file.toString());
        } catch (IOException e) {
            my_file = null;
            Log.d(TAG, "File NOT created: " + e.getStackTrace().toString());
        }
    }

    private void send_stats(ReportDestination dest)
    {
        switch (dest) {
            case DEST_FILE:
                try {

                    for (int i = 0; i < RT_max; i++) {
                        my_file.write(Double.toString(RT[i]) + ",");
                    }
                    my_file.write("\n");

                    my_file.flush();
                    //my_file.close();

                    //byte[] bytes = bos.toByteArray();

                    Log.d(TAG, "File: written");
                } catch (Exception e) {
                    Log.d(TAG, "File: ERROR in writing: " + e.toString());
                }

                break;
            case DEST_UDP:
                try {
                    int server_port = 5505;
                    InetAddress server_addr = InetAddress.getByName("10.30.3.57");

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(bos);

                    int protocolVersion = 1;
                    int dataSize = 8;
                    int taskID = id;
                    int dataCount = RT_c;

                    ///////////////////////////
                    //    Packet format
                    //
                    //  -------------------
                    // | Protocol Version
                    //  -------------------
                    // | Task ID
                    //  -------------------
                    // | Data Size
                    //  -------------------
                    // | Length
                    //  -------------------
                    // | Data[0]
                    // | Data[1]
                    // | Data[2]
                    // | ...
                    //  -------------------
                    //
                    ///////////////////////////

                    dos.writeInt(protocolVersion);
                    dos.writeInt(taskID);
                    dos.writeInt(dataSize);
                    dos.writeInt(dataCount);

                    for (int i = 0; i < RT_max; i++) {
                        dos.writeDouble(RT[i]);
                    }

                    dos.close();

                    DatagramSocket s = new DatagramSocket();
                    byte[] bytes = bos.toByteArray();
                    DatagramPacket p = new DatagramPacket(bytes, bytes.length, server_addr, server_port);
                    s.send(p);

                    Log.d(TAG, "UDP: message sent");
                } catch (Exception e) {
                    Log.d(TAG, "UDP: Exception in sending: " + e.toString());
                }
                break;
            default:
                break;
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

        f_i = System.currentTimeMillis();

        lateness = f_i - D_i;

        if (RT_c < RT_max) {
            RT[RT_c] = f_i - a_i;
            RT_c++;
        } else {
            send_stats(ReportDestination.DEST_FILE);
            RT_c = 0;
        }
/*
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
