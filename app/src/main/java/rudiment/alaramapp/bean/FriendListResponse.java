package rudiment.alaramapp.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 6/9/2017.
 */

public class FriendListResponse {

    public String responseCode;
    public String responseMessage;

    @SerializedName("all_user_friend")
    public ArrayList<User> friendList;

    public class User {
        /* "userid": "1",
      "username": "rudiment.deepak@gmail.com",
      "displayname": "Deepak Gehlot",
      "avatar": "",
      "user_phone": "8827987615",
      "register_date": "2017-06-07 08:03:09"*/
        @SerializedName("userid")
        public String userId;
        @SerializedName("username")
        public String userName;
        @SerializedName("displayname")
        public String displayName;
        public String avatar;
        @SerializedName("user_phone")
        public String number;

    }
}
