package ink.callbacks;

/**
 * Created by USER on 2016-06-30.
 */
public interface GeneralCallback<ObjectType> {
    void onSuccess(ObjectType objectType);

    void onFailure(ObjectType objectType);
}
