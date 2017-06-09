package rudiment.alaramapp.fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.CameraActivity;
import rudiment.alaramapp.activity.HomeActivity;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

import static rudiment.alaramapp.activity.HomeActivity.currentPos;

public class SetReminderFragment extends Fragment implements View.OnClickListener {

    private TextView mDateTxt, mTimeTxt;
    private EditText mTitleEdt;
    private RelativeLayout mParentLayout;
    private ProgressDialog progressDialog;


    public static SetReminderFragment newInstance() {
        SetReminderFragment fragment = new SetReminderFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_set_reminder, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        /*setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Set Reminder");*/
        toolbar.setTitle("Set Reminder");
        toolbar.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        mDateTxt = (TextView) view.findViewById(R.id.dateTxt);
        mTimeTxt = (TextView) view.findViewById(R.id.timeTxt);
        mTitleEdt = (EditText) view.findViewById(R.id.titleEdt);
        mParentLayout = (RelativeLayout) view.findViewById(R.id.activity_set_reminder);

        mDateTxt.setOnClickListener(this);
        mTimeTxt.setOnClickListener(this);
        view.findViewById(R.id.setReminderBtn).setOnClickListener(this);
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

   /* @Override
    public void onBackPressed() {
        startActivity(new Intent(SetReminderFragment.this, CameraActivity.class));
        finish();
    }*/

    private void setReminder() {

        final String date = mDateTxt.getText().toString().trim();
        final String time = mTimeTxt.getText().toString().trim();
        final String title = mTitleEdt.getText().toString().trim();
        if (date.isEmpty() || time.isEmpty()/* || title.isEmpty()*/
                || date.equalsIgnoreCase("Select Date") || time.equalsIgnoreCase("Select Time")) {
            Utility.showMessage(mParentLayout, "All field required.");
        } else {
            try {
                progressDialog = ProgressDialog.show(getActivity(), "Wait", "Saving...", false, false);
                JSONStringer jsonStringer = new JSONStringer().object()
                        .key("user_id").value(PreferenceConnector.readString(getActivity(), PreferenceConnector.USER_Id, ""))
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
                        handleResult("", "", "");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responceStr = response.body().string();
                        //Log.e("set reminder ", responceStr);
                        handleResult(responceStr, date + " " + time, title);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleResult(final String resultStr, final String dateTime, final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(resultStr);
                    if (jsonObject.getString("responseCode").equals("200")) {
                        String reminder_id = jsonObject.getString("reminder_id");
                        setAllReminder(getActivity(), dateTime, reminder_id);
                        Toast.makeText(getActivity(), "" + jsonObject.getString("responseMessage"), Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                        currentPos = 2;
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                    } else {
                        Toast.makeText(getActivity(), "" + jsonObject.getString("responseMessage"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setAllReminder(Context context, String dateTime, String reminder_id) {
        try {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date1 = originalFormat.parse(dateTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date1);
            long mills = cal.getTimeInMillis();
            Utility.setAlarm(context, mills, reminder_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDatePicker() {
        int year = 2017, month = 02, day = 07;
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                mTimeTxt.setText(i + ":" + i1);
            }
        }, hour, minutes, true);
        TextView textView = new TextView(getActivity());
        textView.setText("Select Time");
        textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(20);
        timePickerDialog.setCustomTitle(textView);
        timePickerDialog.show();
    }
}
