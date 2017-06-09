package rudiment.alaramapp.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rudiment.alaramapp.R;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

public class SetReminderActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mDateTxt, mTimeTxt;
    private EditText mTitleEdt;
    private RelativeLayout mParentLayout;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_reminder);

        init();
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Set Reminder");
        toolbar.setTitleTextColor(ContextCompat.getColor(SetReminderActivity.this, R.color.white));
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mDateTxt = (TextView) findViewById(R.id.dateTxt);
        mTimeTxt = (TextView) findViewById(R.id.timeTxt);
        mTitleEdt = (EditText) findViewById(R.id.titleEdt);
        mParentLayout = (RelativeLayout) findViewById(R.id.activity_set_reminder);

        mDateTxt.setOnClickListener(this);
        mTimeTxt.setOnClickListener(this);
        findViewById(R.id.setReminderBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dateTxt:
                setDatePicker();
                break;
            case R.id.timeTxt:
                setTimePicker();
                break;
            case R.id.setReminderBtn:
                setReminder();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SetReminderActivity.this, CameraActivity.class));
        finish();
    }

    private void setReminder() {
        progressDialog = ProgressDialog.show(SetReminderActivity.this, "Wait", "Saving...", false, false);
        String date = mDateTxt.getText().toString().trim();
        String time = mTimeTxt.getText().toString().trim();
        String title = mTitleEdt.getText().toString().trim();
        if (date.isEmpty() || time.isEmpty() || title.isEmpty()) {
            Utility.showMessage(mParentLayout, "All field required.");
        } else {
            try {
                JSONStringer jsonStringer = new JSONStringer().object()
                        .key("user_id").value(PreferenceConnector.readString(SetReminderActivity.this, PreferenceConnector.USER_Id, ""))
                        .key("reminder_date").value(date)
                        .key("reminder_time").value(time)
                        .key("reminder_title").value(title)
                        .key("reminder_description").value(title)
                        .key("reminder_text").value(CameraActivity.mRemindeStr)
                        .key("img1").value(CameraActivity.mImageBase64Str)
                        .key("ext1").value("jpeg")
                        .endObject();
                QueryManager.getInstance().postRequest(Constant.URL + "set_reminder.php", jsonStringer.toString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handleResult("");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responceStr = response.body().string();
                        Log.e("set reminder ", responceStr);
                        handleResult(responceStr);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleResult(final String resultStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(resultStr);
                    if (jsonObject.getString("responseCode").equals("200")) {
                        Toast.makeText(SetReminderActivity.this, "" + jsonObject.getString("responseMessage"), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SetReminderActivity.this, "" + jsonObject.getString("responseMessage"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setDatePicker() {
        int year = 2017, month = 02, day = 07;
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                i1 = i1 + 1;
                mDateTxt.setText(i + "-" + i1 + "-" + i2);
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void setTimePicker() {
        int hour = 10, minutes = 10;
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                mTimeTxt.setText(i + ":" + i1);
            }
        }, hour, minutes, true);
        TextView textView = new TextView(SetReminderActivity.this);
        textView.setText("Select Time");
        textView.setTextColor(ContextCompat.getColor(SetReminderActivity.this, R.color.black));
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(20);
        timePickerDialog.setCustomTitle(textView);
        timePickerDialog.show();
    }
}
