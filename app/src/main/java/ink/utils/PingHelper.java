package ink.utils;

import android.os.Process;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import ink.models.PingResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 8/22/2016.
 */
public class PingHelper {
    private static PingHelper pingHelperInstance = new PingHelper();

    private boolean isPinging;
    private Thread mPingThread;
    private static final long PING_TIME = 50000;
    private Timer timer = new Timer();
    private Gson gson;

    public static PingHelper get() {
        return pingHelperInstance;
    }

    private PingHelper() {
        gson = new Gson();
    }

    public void startPinging(final String userId) {
        if (mPingThread != null) {
            mPingThread = null;
        }
        mPingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                if (mPingThread.getState() != Thread.State.TERMINATED) {
                    pingTime(userId);
                    isPinging = true;
                } else {
                    isPinging = true;
                }
            }
        });
        mPingThread.start();
    }

    public boolean isPinging() {
        return isPinging;
    }


    private void pingTime(final String userId) {
        Call<ResponseBody> pingTimeCall = Retrofit.getInstance().getInkService().pingTime(userId);
        pingTimeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    pingTime(userId);
                    return;
                }
                if (response.body() == null) {
                    pingTime(userId);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    try {
                        PingResponse pingResponse = gson.fromJson(responseBody, PingResponse.class);
                        if (pingResponse.success) {
                            if (timer == null) {
                                timer = new Timer();
                            }
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    pingTime(userId);
                                }
                            }, PING_TIME);
                        } else {
                            pingTime(userId);
                        }
                    } catch (Exception e) {
                        destroyPinging();
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pingTime(userId);
            }
        });
    }

    public void destroyPinging() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (mPingThread != null) {
            mPingThread.interrupt();
            mPingThread = null;
        }
    }

}
