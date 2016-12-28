package ink.va.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ink.databases.QbUsersDbManager;
import ink.va.interfaces.IncomeCallFragmentCallbackListener;
import ink.va.utils.CollectionsUtils;
import ink.va.utils.RingtonePlayer;
import ink.va.utils.UsersUtils;
import ink.va.utils.WebRtcSessionManager;

/**
 * QuickBlox team
 */
public class IncomeCallFragment extends Fragment implements Serializable, View.OnClickListener {

    private static final String TAG = IncomeCallFragment.class.getSimpleName();
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);
    private TextView callTypeTextView;
    private ImageButton rejectButton;
    private ImageButton takeButton;

    private List<Integer> opponentsIds;
    private Vibrator vibrator;
    private QBRTCTypes.QBConferenceType conferenceType;
    private long lastClickTime = 0l;
    private RingtonePlayer ringtonePlayer;
    private IncomeCallFragmentCallbackListener incomeCallFragmentCallbackListener;
    private QBRTCSession currentSession;
    private QbUsersDbManager qbUserDbManager;
    private TextView alsoOnCallText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            incomeCallFragmentCallbackListener = (IncomeCallFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCallEventsController");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);

        Log.d(TAG, "onCreate() from IncomeCallFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_call, container, false);

        initFields();

        if (currentSession != null) {
            initUI(view);
            setDisplayedTypeCall(conferenceType);
            initButtonsListener();
        }

        ringtonePlayer = new RingtonePlayer(getActivity());
        return view;
    }

    private void initFields() {
        currentSession = WebRtcSessionManager.getInstance(getActivity()).getCurrentSession();
        qbUserDbManager = QbUsersDbManager.getInstance(getActivity().getApplicationContext());

        if (currentSession != null) {
            opponentsIds = currentSession.getOpponents();
            conferenceType = currentSession.getConferenceType();
            Log.d(TAG, conferenceType.toString() + "From onCreateView()");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    private void initButtonsListener() {
        rejectButton.setOnClickListener(this);
        takeButton.setOnClickListener(this);
    }

    private void initUI(View view) {
        callTypeTextView = (TextView) view.findViewById(R.id.call_type);

        ImageView callerAvatarImageView = (ImageView) view.findViewById(R.id.image_caller_avatar);
        callerAvatarImageView.setImageResource(R.drawable.no_background_image);

        TextView callerNameTextView = (TextView) view.findViewById(R.id.text_caller_name);

        QBUser callerUser = qbUserDbManager.getUserById(currentSession.getCallerID());
        callerNameTextView.setText(UsersUtils.getUserNameOrId(callerUser, currentSession.getCallerID()));

        TextView otherIncUsersTextView = (TextView) view.findViewById(R.id.text_other_inc_users);
        otherIncUsersTextView.setText(getOtherIncUsersNames());

        alsoOnCallText = (TextView) view.findViewById(R.id.text_also_on_call);
        setVisibilityAlsoOnCallTextView();

        rejectButton = (ImageButton) view.findViewById(R.id.image_button_reject_call);
        takeButton = (ImageButton) view.findViewById(R.id.image_button_accept_call);
    }

    private void setVisibilityAlsoOnCallTextView() {
        if (opponentsIds.size() < 2) {
            alsoOnCallText.setVisibility(View.INVISIBLE);
        }
    }

    private Drawable getBackgroundForCallerAvatar() {
        return ContextCompat.getDrawable(getActivity(), R.drawable.no_background_image);
    }

    public void startCallNotification() {
        Log.d(TAG, "startCallNotification()");

        ringtonePlayer.play(false);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }

    }

    private void stopCallNotification() {
        Log.d(TAG, "stopCallNotification()");

        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private String getOtherIncUsersNames() {
        ArrayList<QBUser> usersFromDb = qbUserDbManager.getUsersByIds(opponentsIds);
        ArrayList<QBUser> opponents = new ArrayList<>();
        opponents.addAll(UsersUtils.getListAllUsersFromIds(usersFromDb, opponentsIds));

        opponents.remove(QBChatService.getInstance().getUser());
        Log.d(TAG, "opponentsIds = " + opponentsIds);
        return CollectionsUtils.makeStringFromUsersFullNames(opponents);
    }

    private void setDisplayedTypeCall(QBRTCTypes.QBConferenceType conferenceType) {
        boolean isVideoCall = conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;

        callTypeTextView.setText(isVideoCall ? "its vide" : "its not vide");
        takeButton.setImageResource(isVideoCall ? R.drawable.audi_call_icon : R.drawable.audi_call_icon);
    }

    @Override
    public void onStop() {
        stopCallNotification();
        super.onStop();
        Log.d(TAG, "onStop() from IncomeCallFragment");
    }

    @Override
    public void onClick(View v) {

        if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
            return;
        }
        lastClickTime = SystemClock.uptimeMillis();

        switch (v.getId()) {
            case R.id.image_button_reject_call:
                reject();
                break;

            case R.id.image_button_accept_call:
                accept();
                break;

            default:
                break;
        }
    }

    private void accept() {
        enableButtons(false);
        stopCallNotification();

        incomeCallFragmentCallbackListener.onAcceptCurrentSession();
        Log.d(TAG, "Call is started");
    }

    private void reject() {
        enableButtons(false);
        stopCallNotification();

        incomeCallFragmentCallbackListener.onRejectCurrentSession();
        Log.d(TAG, "Call is rejected");
    }

    private void enableButtons(boolean enable) {
        takeButton.setEnabled(enable);
        rejectButton.setEnabled(enable);
    }
}