package ink.interfaces;

import com.sinch.android.rtc.calling.Call;

/**
 * Created by USER on 2016-07-24.
 */
public interface CallCallback {
    void onCallProgress(Call call);

    void onCallEstablished(Call call);

    void onCallEnded(Call call);
}
