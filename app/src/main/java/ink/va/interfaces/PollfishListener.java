package ink.va.interfaces;

/**
 * Created by PC-Comp on 2/2/2017.
 */

public interface PollFishListener {
    void onPollFishClosed();
    void onPollFishOpened();
    void onPollFishSurveyNotAvailable();
    void onPollFishSurveyReceived(boolean playfulSurvey, int surveyPrice);
    void onPollFishSurveyCompleted(boolean playfulSurvey, int surveyPrice);
    void onUserNotEligible();
}
