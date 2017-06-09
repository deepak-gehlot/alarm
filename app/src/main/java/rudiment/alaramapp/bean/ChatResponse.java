package rudiment.alaramapp.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 6/7/2017.
 */

public class ChatResponse {

    @SerializedName("history")
    public ArrayList<Message> messages;

    public static class Message {
        public String id = "";
        public String from = "";
        public String message = "";
        public String self = "";
        public String old = "";
        public String sent = "";
        public String direction = "";
        public String message_type = "";
        public String to = "";
    }
}
