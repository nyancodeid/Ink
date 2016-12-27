package ink;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.auth.IAdobeAuthClientCredentials;
import com.danikula.videocache.HttpProxyCacheServer;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;

/**
 * Created by USER on 2016-06-26.
 */
public class StartupApplication extends MultiDexApplication implements IAdobeAuthClientCredentials, VKCallback<Object> {
    private static final String CREATIVE_SDK_CLIENT_ID = "2b5c43dc4f6d4f79a7d353433a972a4b";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "11b233bb-98d2-45d3-8c80-2be66726f10b";
    private SharedHelper sharedHelper;
    private Activity mActivity = null;
    private HttpProxyCacheServer proxy;

    @Override
    public void onCreate() {
        RealmHelper.getInstance().initRealm(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        sharedHelper = new SharedHelper(this);
        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext());
        VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
            @Override
            public void onVKAccessTokenChanged(@Nullable VKAccessToken oldToken, @Nullable VKAccessToken newToken) {
                sharedHelper.putVkAccessToken(newToken.accessToken);
            }
        };
        vkAccessTokenTracker.startTracking();
        VKSdk.customInitialize(this, 5593776, sharedHelper.getVkAccessToken());

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                mActivity = activity;
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mActivity = new Activity();
            }

            /** Unused implementation **/
            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
        });

        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    public Activity getCurrentActivity() {
        return mActivity;
    }

    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }

    @Override
    public void onResult(Object res) {

    }

    @Override
    public void onError(VKError error) {

    }

    public static HttpProxyCacheServer getProxy(Context context) {
        StartupApplication app = (StartupApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024 * 1024 * 1024)
                .cacheDirectory(getCacheDir())
                .build();
    }
}
