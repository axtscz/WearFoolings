package com.collusion.weartext;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Notifier extends Service {
    public Notifier() {
        Log.i("INFO", "Hry");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
