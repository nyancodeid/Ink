package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by PC-Comp on 3/14/2017.
 */

public class MafiaGameService extends Service {
    private ScheduledExecutorService scheduler;
    private LocalBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        public MafiaGameService getService() {
            return MafiaGameService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scheduleTask();
        return START_STICKY;
    }

    private void scheduleTask() {
        if (scheduler == null || !scheduler.isTerminated() & !scheduler.isShutdown()) {
            scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            checkMafiaGame();
                        }
                    }, 0, 30, TimeUnit.SECONDS);
        }
    }

    private void checkMafiaGame() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyScheduler();
    }

    public void destroyScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
