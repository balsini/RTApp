package it.sssup.retis.alessiobalsini.rtapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * Use with
 * $ adb shell am broadcast -a "it.sssup.retis.alessiobalsini.RTAPP_INTENT" -e period 200 -e utilization 0.3
 */

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String period = intent.getStringExtra("period");
        String utilization = intent.getStringExtra("utilization");

        //String outputText = "RTApp intent detected. Period: " + period + " Utilization: " + utilization;
        //Toast.makeText(context, outputText, Toast.LENGTH_LONG).show();

        GlobalTaskParameters.T = Double.parseDouble(period);
        GlobalTaskParameters.d = GlobalTaskParameters.T;
        GlobalTaskParameters.C = Double.parseDouble(utilization) * GlobalTaskParameters.T;
    }
}
