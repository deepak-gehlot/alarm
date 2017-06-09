package rudiment.alaramapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rudiment.alaramapp.bean.ReminderBean;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

/**
 * Created by RWS 6 on 4/12/2017.
 */
public class SetReminderService extends IntentService {

    public SetReminderService() {
        super("SetReminderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String user_id = intent.getExtras().getString("user_id");
        getMyToDoList(user_id);
    }

    private void getMyToDoList(String user_id) {
        try {
            JSONStringer jsonStringer = new JSONStringer().object()
                    .key("user_id").value(user_id)
                    .endObject();
            QueryManager.getInstance().postRequest(Constant.URL + "get_user_reminder.php", jsonStringer.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleServerResult("");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resultStr = response.body().string();
                    Log.e("To DO list ", resultStr);
                    handleServerResult(resultStr);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleServerResult(final String resultStr) {
        if (resultStr != null && !resultStr.isEmpty()) {
            try {
                ReminderBean reminderBean = new Gson().fromJson(resultStr, ReminderBean.class);
                if (reminderBean.responseCode.equals("200")) {
                    if (reminderBean.all_user_reminder != null && reminderBean.all_user_reminder.size() != 0) {
                        PreferenceConnector.writeBoolean(SetReminderService.this, PreferenceConnector.IS_REMINDER_UPTODATE, true);
                        Collections.reverse(reminderBean.all_user_reminder);
                        for (ReminderBean.ReminderItem item : reminderBean.all_user_reminder) {
                            setAllReminder(SetReminderService.this, item.reminder_date + " " + item.reminder_time, item.reminder_title);
                        }
                    } else {
                    }
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setAllReminder(Context context, String dateTime, String text) {

        try {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date1 = originalFormat.parse(dateTime);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date1);
            long mills = cal.getTimeInMillis();
            long currentDateTime = new Date().getTime();
            if (currentDateTime < mills) {
                Utility.setAlarm(context, mills, text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
