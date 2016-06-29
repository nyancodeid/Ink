package ink;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import ink.utils.RealmHelper;

/**
 * Created by USER on 2016-06-26.
 */
public class StartupApplication extends Application {
    @Override
    public void onCreate() {
        RealmHelper.getInstance().initRealm(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        super.onCreate();
    }
}
