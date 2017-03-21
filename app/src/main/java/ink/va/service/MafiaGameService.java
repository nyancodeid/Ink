package ink.va.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
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
    private CountDownTimer countDownTimer;

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

        startTimer();
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(this);
        }
        scheduleTask();
        return START_STICKY;
    }

    private void startTimer() {
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    checkMafiaGame();
                }
            };
            countDownTimer.start();
        }
    }

    private void scheduleTask() {
        checkMafiaGame();
    }


    private void checkMafiaGame() {
        Retrofit.getInstance().getInkService().checkMafiaRoom(sharedHelper.getMafiaRoomId(), sharedHelper.getUserId()).enqueue(new Callback<MafiaRoomsModel>() {
            @Override
            public void onResponse(Call<MafiaRoomsModel> call, Response<MafiaRoomsModel> response) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer.start();
                } else {
                    startTimer();
                }
                MafiaRoomsModel mafiaRoomsModel = response.body();
                if (mafiaRoomsModel.isGameEnded()) {
                }
            }

            @Override
            public void onFailure(Call<MafiaRoomsModel> call, Throwable t) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer.start();
                } else {
                    startTimer();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = null;
    }

}
