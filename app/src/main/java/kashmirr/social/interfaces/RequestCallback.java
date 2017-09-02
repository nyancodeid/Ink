package kashmirr.social.interfaces;

/**
 * Created by PC-Comp on 5/3/2017.
 */

public interface RequestCallback<T> {
    void onRequestSuccess(T result);

    void onRequestFailed(T... result);
}
