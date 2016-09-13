package ink.models;

import io.realm.RealmObject;

/**
 * Created by PC-Comp on 9/13/2016.
 */
public class CountBadgeModel extends RealmObject {
    String count;
    String userId;


    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
