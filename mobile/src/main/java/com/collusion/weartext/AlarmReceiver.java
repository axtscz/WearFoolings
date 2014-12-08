package com.collusion.weartext;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Antonio on 12/8/2014.
 */
public class AlarmReceiver extends BroadcastReceiver {


    PendingIntent pendingIntentrep;
    Intent intentrep;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("INFO", "Called!");
        Toast.makeText(context, "Hey", Toast.LENGTH_SHORT).show();
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        NotificationMethods notificationMethods = new NotificationMethods();
        notificationMethods.getTextandNotify(context);
    }

    public void repeatingTimer(Context context, int hour, int minute){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        intentrep = new Intent(context, AlarmReceiver.class);
        pendingIntentrep = PendingIntent.getBroadcast(context, 1561, intentrep, 0);
        alarmManager.cancel(pendingIntentrep);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        alarmManager.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntentrep);
        Log.i("INFO", "Alarm set!");
    }

    public void setOnce(Context context){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        intentrep = new Intent(context, AlarmReceiver.class);
        pendingIntentrep = PendingIntent.getBroadcast(context, 1562, intentrep, 0);
        alarmManager.setExact(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis() + 5000,pendingIntentrep);
    }


}
