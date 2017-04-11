package ink.va.utils;

import java.util.LinkedList;
import java.util.List;

import ink.va.models.UserModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by USER on 2016-07-20.
 */
public class User {
    private static User user = new User();
    private int coins;
    private int bombCount;
    private String facebookUserId;
    private String userName;
    private String userId;
    private boolean isCoinsLoaded;
    @Setter
    @Getter
    private List<String> friendIds;

    public User() {
        friendIds = new LinkedList<>();
    }

    public int getCoins() {
        return coins;
    }


    public static User get() {
        return user;
    }

    public int getBombCount() {
        return bombCount;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setBombCount(int bombCount) {
        this.bombCount = bombCount;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }


    public boolean isCoinsLoaded() {
        return isCoinsLoaded;
    }

    public void setCoinsLoaded(boolean coinsLoaded) {
        isCoinsLoaded = coinsLoaded;
    }

    public void setFacebookUserId(String facebookUserId) {
        this.facebookUserId = facebookUserId;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserModel buildUser(SharedHelper sharedHelper) {
        UserModel userModel = new UserModel();
        userModel.setUserId(sharedHelper.getUserId());
        userModel.setAddress(sharedHelper.getUserAddress());
        userModel.setFacebookName(sharedHelper.getUserFacebookName());
        userModel.setFacebookProfile(sharedHelper.getUserFacebookLink());
        userModel.setFirstName(sharedHelper.getFirstName());
        userModel.setLastName(sharedHelper.getLastName());
        userModel.setGender(sharedHelper.getUserGender());
        userModel.setStatus(sharedHelper.getUserStatus());
        userModel.setSocialAccount(sharedHelper.isSocialAccount());
        userModel.setImageUrl(sharedHelper.getImageLink());
        userModel.setPhoneNumber(sharedHelper.getUserPhoneNumber());
        userModel.setRelationship(sharedHelper.getUserRelationship());
        userModel.setSkype(sharedHelper.getUserSkype());
        userModel.setLogin(sharedHelper.getLogin());
        return userModel;
    }

}
