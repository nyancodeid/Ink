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
    private QBLoginListener onQbLoginListener;

    public QBUtils(QBResRequestExecutor requestExecutor, Context context, SharedHelper sharedHelper) {
        this.requestExecutor = requestExecutor;
        this.context = context;
        this.sharedHelper = sharedHelper;
    }

    public void silentQbLogin() {
        startSignUpNewUser(createQBUserWithCurrentData(this.sharedHelper.getFirstName() + " " + this.sharedHelper.getLastName(), this.sharedHelper.getLogin()));

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
                        }else{
                            triggerQBEvenet(false, e.toString());
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
                triggerQBEvenet(false, responseException.toString());
            }
        });
    }

    public void setOnQbLoginListener(QBLoginListener onQbLoginListener) {
        this.onQbLoginListener = onQbLoginListener;
    }

    private void loginToChat(final QBUser qbUser) {
        saveUserData(qbUser);
        CallService.start(context, qbUser);
        triggerQBEvenet(true, qbUser);
    }


    private void triggerQBEvenet(boolean success, Object argument) {
        if (onQbLoginListener != null) {
            if (success) {
                onQbLoginListener.onLoginSuccess((QBUser) argument);
            } else {
                onQbLoginListener.onLoginFailed((String) argument);
            }
        }
    }

    private void saveUserData(QBUser qbUser) {
        sharedHelper.saveQbUser(qbUser);
    }

    public interface QBLoginListener {
        void onLoginSuccess(QBUser qbUser);

        void onLoginFailed(String reason);
    }
}
