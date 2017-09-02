package kashmirr.social.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by PC-Comp on 12/22/2016.
 */

public class MyCollectionResponseModel {
    @SerializedName("result")
    List<MyCollectionModel> myCollectionModels;

    public List<MyCollectionModel> getMyCollectionModels() {
        return myCollectionModels;
    }
    
}
