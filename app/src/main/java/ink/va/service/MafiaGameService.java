package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
    private LocalBinder mBinder = new LocalBinder();
    private SharedHelper sharedHelper;
    private Handler handler;
    private boolean stopHandler;

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
        if(sharedHelper==null){
            sharedHelper = new SharedHelper(this);
        }
        stopHandler = false;
        scheduleTask();
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(this);
        }
        return START_STICKY;
    }

    private void scheduleTask() {
        handler = new Handler();
        checkMafiaGame();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkMafiaGame();
        }
    };

    private void checkMafiaGame() {
        Retrofit.getInstance().getInkService().checkMafiaRoom(sharedHelper.getMafiaRoomId(), sharedHelper.getUserId()).enqueue(new Callback<MafiaRoomsModel>() {
            @Override
            public void onResponse(Call<MafiaRoomsModel> call, Response<MafiaRoomsModel> response) {
                if (!stopHandler) {
                    handler.postDelayed(runnable, 5000);
                }
                MafiaRoomsModel mafiaRoomsModel = response.body();
                if (mafiaRoomsModel.isGameEnded()) {
                }
            }

            @Override
            public void onFailure(Call<MafiaRoomsModel> call, Throwable t) {
                if (!stopHandler) {
                    handler.postDelayed(runnable, 5000);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopHandler = true;
        handler = null;
        runnable = null;
    }

}
