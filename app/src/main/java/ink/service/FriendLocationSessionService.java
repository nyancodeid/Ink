package ink.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by PC-Comp on 8/17/2016.
 */
public class FriendLocationSessionService extends Service {

    private String opponentId;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if(extras!=null){
            opponentId = extras.getString("opponentId");
        }
        return START_NOT_STICKY;
    }


    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(getApplicationContext(), LocationRequestSessionDestroyer.class);
        intent.putExtra("opponentId", opponentId);
        startService(intent);
        stopSelf();
    }
}
