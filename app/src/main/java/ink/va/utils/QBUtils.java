package ink.va.utils;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import ink.va.service.CallService;

/**
 * Created by PC-Comp on 12/28/2016.
 */

public class QBUtils {

    private QBResRequestExecutor requestExecutor;
    private Context context;
    private SharedHelper sharedHelper;
    private QBEventListener onQbEventListener;

    public QBUtils(QBResRequestExecutor requestExecutor, Context context, SharedHelper sharedHelper) {
        this.requestExecutor = requestExecutor;
        this.context = context;
        this.sharedHelper = sharedHelper;
    }

    public void silentQbLogin() {
        startSignUpNewUser(createQBUserWithCurrentData(this.sharedHelper.getFirstName() + " " +
                this.sharedHelper.getLastName(), this.sharedHelper.getLogin()));
    }


    private QBUser createQBUserWithCurrentData(String fullName, String userName) {
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(fullName)) {
            qbUser = new QBUser();
            qbUser.setFullName(fullName);
            qbUser.setLogin(userName);
            qbUser.setPassword(sharedHelper.getUserPassword().length() < 8 ? sharedHelper.getUserPassword() + "minorPas" : sharedHelper.getUserPassword());
        }

        return qbUser;
    }


    private void startSignUpNewUser(final QBUser newUser) {
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        signInCreatedUser(newUser);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser);
                        } else {
                            triggerQBEvent(false, e.toString());
                        }
                    }
                }
        );
    }

    private void signInCreatedUser(final QBUser user) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                loginToChat(user);
            }

            @Override
            public void onError(QBResponseException responseException) {
                triggerQBEvent(false, responseException.toString());
            }
        });
    }

    public void setOnQbEventListener(QBEventListener onQbEventListener) {
        this.onQbEventListener = onQbEventListener;
    }

    private void loginToChat(final QBUser qbUser) {
        saveUserData(qbUser);
        CallService.start(context, qbUser);
        triggerQBEvent(true, qbUser);
    }


    private void triggerQBEvent(boolean success, Object argument) {
        if (onQbEventListener != null) {
            if (success) {
                onQbEventListener.onLoginSuccess((QBUser) argument);
            } else {
                onQbEventListener.onLoginFailed((String) argument);
            }
        }
    }

    private void saveUserData(QBUser qbUser) {
        sharedHelper.saveQbUser(qbUser);
    }

    public interface QBEventListener {
        void onLoginSuccess(QBUser qbUser);

        void onLoginFailed(String reason);
    }
}
