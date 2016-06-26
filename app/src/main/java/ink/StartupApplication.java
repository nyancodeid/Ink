package ink;

import android.app.Application;

import ink.utils.RealmHelper;

/**
 * Created by USER on 2016-06-26.
 */
public class StartupApplication extends Application {
    @Override
    public void onCreate() {
        RealmHelper.getInstance().initRealm(getApplicationContext());
        super.onCreate();
    }
}
