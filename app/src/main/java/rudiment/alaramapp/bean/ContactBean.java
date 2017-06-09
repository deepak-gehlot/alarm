package rudiment.alaramapp.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 4/12/2017.
 */

public class ContactBean {

    public String name;
    public ArrayList<Number> numbers;
    //public ArrayList<String> emails;

    public static class Number {
        @SerializedName("userid")
        public String userId;
        public String phone;
        @SerializedName("isRegisterd")
        public String isRegistered = "";
    }
}
