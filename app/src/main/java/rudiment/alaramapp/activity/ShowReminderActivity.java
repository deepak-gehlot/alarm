package rudiment.alaramapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.Timer;
import java.util.TimerTask;

import rudiment.alaramapp.R;
import rudiment.alaramapp.bean.ReminderBean;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Callback;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

import static rudiment.alaramapp.util.PreferenceConnector.USER_Id;

public class ShowReminderActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView titleTxt, timerTxt;
    int timeCount = 20;
    private AQuery aQuery;
    String reminder_id;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_reminder);
        timeCount = 20;
        imageView = (ImageView) findViewById(R.id.imageView);
        titleTxt = (TextView) findViewById(R.id.titleTxt);
        timerTxt = (TextView) findViewById(R.id.timerTxt);

        aQuery = new AQuery(ShowReminderActivity.this);

        reminder_id = getIntent().getExtras().getString("reminder_id");
        getReminderDetails(reminder_id);

        playSound();
    }

    /**
     * @param reminder_id
     */
    private void getReminderDetails(String reminder_id) {
        final ProgressDialog progressDialog = ProgressDialog.show(ShowReminderActivity.this, "", "wait...", false, false);
        try {
            JSONStringer jsonStringer = new JSONStringer().object()
                    .key("reminder_id").value(reminder_id)
                    .endObject();
            QueryManager.getInstance().postRequest(Constant.URL + "get_reminder_single.php", jsonStringer.toString(), new Callback() {
                @Override
                public void onResult(String result) {
                    progressDialog.dismiss();
                    if (result != null && !result.isEmpty()) {
                        handleResponse(result);
                    } else {
                        Utility.showToast(ShowReminderActivity.this, getString(R.string.wrong));
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Utility.showToast(ShowReminderActivity.this, getString(R.string.wrong));
        }
    }

    private void handleResponse(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            ReminderBean.ReminderItem item = new ReminderBean.ReminderItem();
            if (jsonObject.getString("responseCode").equals("200")) {
                JSONObject reminderDetailObj = jsonObject.getJSONObject("reminder_detail");
                item.reminder_id = reminderDetailObj.getString("reminder_id");
                item.user_id = reminderDetailObj.getString("user_id");
                item.reminder_date = reminderDetailObj.getString("reminder_date");
                item.reminder_time = reminderDetailObj.getString("reminder_time");
                item.reminder_description = reminderDetailObj.getString("reminder_description");
                item.reminder_image = reminderDetailObj.getString("reminder_image");
                item.reminder_text = reminderDetailObj.getString("reminder_text");
                setData(item);
            } else {
                Utility.showToast(ShowReminderActivity.this, jsonObject.getString("responseMessage"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utility.showToast(ShowReminderActivity.this, getString(R.string.wrong));
        }
    }

    private void setData(ReminderBean.ReminderItem reminderItem) {
        aQuery.id(imageView).image(reminderItem.reminder_image, true, true, 0, R.drawable.profile);
        titleTxt.setText(reminderItem.reminder_description);


        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeCount = timeCount - 1;
                        timerTxt.setText("" + timeCount);
                        if (timeCount == 0) {
                            timer.cancel();
                            timer.purge();
                            finish();
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    private void playSound() {
        Uri alert = RingtoneManager.getActualDefaultRingtoneUri(ShowReminderActivity.this, RingtoneManager.TYPE_ALARM);

        if (alert == null) {
            // alert is null, using backup
            alert = RingtoneManager.getActualDefaultRingtoneUri(ShowReminderActivity.this, RingtoneManager.TYPE_NOTIFICATION);

            // I can't see this ever being null (as always have a default notification)
            // but just incase
            if (alert == null) {
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getActualDefaultRingtoneUri(ShowReminderActivity.this, RingtoneManager.TYPE_RINGTONE);
            }
        }
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alert);
        r.play();
    }

    public void onCrossItOutBtnClick(View view) {
        timer.cancel();
        timer.purge();
        moveReminderToHistory(ShowReminderActivity.this, reminder_id);
    }

    public void onCheckedOutBtnClick(View view) {
        finish();
    }


    /**
     * move reminder to history list (Cross It Out)
     *
     * @param reminder_id
     */
    private void moveReminderToHistory(final Context context, String reminder_id) {
        final ProgressDialog progressDialog = ProgressDialog.show(context, "", "wait...", false, false);
        try {
            JSONStringer jsonStringer = new JSONStringer().object()
                    .key("user_id").value(PreferenceConnector.readString(context, USER_Id, ""))
                    .key("reminder_id").value(reminder_id)
                    .endObject();
            QueryManager.getInstance().postRequest(Constant.URL + "add_reminder_history.php", jsonStringer.toString(), new Callback() {
                @Override
                public void onResult(String result) {
                    progressDialog.dismiss();
                    if (result != null && !result.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getString("responseCode").equals("200")) {
                                finish();
                            } else {
                                Utility.showToast(context, jsonObject.getString("responseMessage"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Utility.showToast(context, context.getString(R.string.wrong));
                        }
                    } else {
                        Utility.showToast(context, context.getString(R.string.wrong));
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Utility.showToast(context, context.getString(R.string.wrong));
        }
    }
}
