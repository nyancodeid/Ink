package ink.va.interfaces;

/**
 * Created by PC-Comp on 2/17/2017.
 */

public interface BackUpManagerCallback {
    void onBackUpFinished();

    void onBackUpError(String friendlyErrorMessage);

    void onRestoreFinished();

    void onRestoreError(String friendlyErrorMessage);

    void onBackUpProgress(double percentCompleted);

    void onRestoreProgress(double percentCompleted);
}
