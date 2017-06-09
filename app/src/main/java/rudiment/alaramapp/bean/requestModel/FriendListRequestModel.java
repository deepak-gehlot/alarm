package rudiment.alaramapp.bean.requestModel;

import com.google.gson.annotations.SerializedName;

/**
 * Created by RWS 6 on 6/9/2017.
 */

public class FriendListRequestModel {
    @SerializedName("user_id")
    public String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
