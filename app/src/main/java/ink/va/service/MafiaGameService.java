package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ink.va.models.MafiaRoomsModel;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 3/14/2017.
 */

public class MafiaGameService extends Service {
    private ScheduledExecutorService scheduler;
    private LocalBinder mBinder = new LocalBinder();
    private SharedHelper sharedHelper;

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
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(this);
        }
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
                            Log.d("askhfaskjfa", "run: pinging");
                        }
                    }, 0, 5, TimeUnit.SECONDS);
        }
    }

    private void checkMafiaGame() {
        Retrofit.getInstance().getInkService().checkMafiaRoom(sharedHelper.getMafiaRoomId(), sharedHelper.getUserId()).enqueue(new Callback<MafiaRoomsModel>() {
            @Override
            public void onResponse(Call<MafiaRoomsModel> call, Response<MafiaRoomsModel> response) {
                MafiaRoomsModel mafiaRoomsModel = response.body();
                if (mafiaRoomsModel.isGameEnded()) {

                }
            }

            @Override
            public void onFailure(Call<MafiaRoomsModel> call, Throwable t) {

            }
        });
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
