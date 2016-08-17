package ink;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import ink.utils.RealmHelper;

/**
 * Created by USER on 2016-06-26.
 */
public class StartupApplication extends MultiDexApplication implements IAdobeAuthClientCredentials {
    private static final String CREATIVE_SDK_CLIENT_ID = "2b5c43dc4f6d4f79a7d353433a972a4b";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "11b233bb-98d2-45d3-8c80-2be66726f10b";

    @Override
    public void onCreate() {
        RealmHelper.getInstance().initRealm(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext());
        super.onCreate();
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    
    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }
}
