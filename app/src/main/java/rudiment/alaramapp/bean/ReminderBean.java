package rudiment.alaramapp.bean;

import java.util.ArrayList;

/**
 * Created by RWS 6 on 2/9/2017.
 */

public class ReminderBean {

    /* "responseMessage": "get all reminder successfully",
    "all_user_reminder": [
        {
            "reminder_id": "1",
            "user_id": "1",
            "reminder_date": "10-02-2017",
            "reminder_time": "10:00 PM",
            "reminder_title": "test",
            "reminder_description": "test description",
            "reminder_image": "http://24web7.in/imentoz/uploads/reminder_image/4345.png",
            "reminder_text": "tests dsfsdf"
        }
    ],
    "responseCode": 200*/
    public String responseCode;
    public String responseMessage;
    public ArrayList<ReminderItem> all_user_reminder;
    public ArrayList<ReminderItem> all_user_reminder_history;

    public static class ReminderItem {
        public String reminder_id;
        public String user_id;
        public String reminder_date;
        public String reminder_time;
        public String reminder_title;
        public String reminder_description;
        public String reminder_image;
        public String reminder_text;
    }
}
