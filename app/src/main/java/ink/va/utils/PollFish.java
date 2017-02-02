package ink.va.utils;

import android.app.Activity;

import ink.va.callbacks.GeneralCallback;

/**
 * Created by PC-Comp on 2/2/2017.
 */

public class PollFish {

    public static PollFish instance = new PollFish();
    private Activity activity;

    public static PollFish get() {
        return instance;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private Thread mWorkerThread;

    public void initPollFish(final GeneralCallback onInitListener) {
        if (mWorkerThread != null) {
            mWorkerThread = null;
        }
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                com.pollfish.main.PollFish.ParamsBuilder paramsBuilder = new com.pollfish.main.PollFish.ParamsBuilder("a0c8426f-32be-417b-9c26-c26424225e11").build()
                        .indicatorPadding(5)
                        .releaseMode(false)
                        .customMode(true)
                        .build();
                com.pollfish.main.PollFish.initWith(activity, paramsBuilder);
                onInitListener.onSuccess(null);
                mWorkerThread = null;
            }
        });
        mWorkerThread.start();
    }

    public void hidePollFish() {
        com.pollfish.main.PollFish.hide();
    }

    public void showPolFish() {
        com.pollfish.main.PollFish.show();
    }

}
