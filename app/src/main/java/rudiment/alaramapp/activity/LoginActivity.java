package rudiment.alaramapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.desai.vatsal.mydynamictoast.MyDynamicToast;
import com.google.gson.Gson;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;

import org.json.JSONException;
import org.json.JSONObject;
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
import rudiment.alaramapp.R;
import rudiment.alaramapp.bean.ReminderBean;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.services.SetReminderService;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

import static rudiment.alaramapp.util.Constant.URL_CHAT;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmailEdt, mPasswordEdt;
    private ProgressDialog progressDialog;
    CometChat cometchat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cometchat = CometChat.getInstance(LoginActivity.this, getString(R.string.chat_api_key));
        cometchat.setCometChatUrl(URL_CHAT);

        /*if already login then continue....*/
        if (PreferenceConnector.readBoolean(LoginActivity.this, PreferenceConnector.IS_LOGIN, false)) {
            startActivity(new Intent(LoginActivity.this, CameraActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);
        setImageAnim();
        visibleBackground();
        initView();
    }

    private void initView() {
        mEmailEdt = (EditText) findViewById(R.id.emailEdt);
        mPasswordEdt = (EditText) findViewById(R.id.passwordEdt);
        setClickListener();

        mPasswordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    private void setClickListener() {
        findViewById(R.id.registerNowTxt).setOnClickListener(this);
        findViewById(R.id.forgotPassTxt).setOnClickListener(this);
        findViewById(R.id.loginBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registerNowTxt:
                onRegisterNowClick();
                break;
            case R.id.forgotPassTxt:
                break;
            case R.id.loginBtn:
                attemptLogin();
                break;
        }
    }

    private void attemptLogin() {
        String emailStr = mEmailEdt.getText().toString().trim();
        String passwordStr = mPasswordEdt.getText().toString().trim();
        /*{"email":"manish@gmail.com","password":"234234","gcm_id":"345345235235354"}*/
        if (!emailStr.isEmpty() && !passwordStr.isEmpty()) {
            progressDialog = ProgressDialog.show(LoginActivity.this, "Wait", "Login...", false, false);
            try {
                JSONStringer jsonStringer = new JSONStringer().object()
                        .key("email").value(emailStr)
                        .key("password").value(passwordStr)
                        .key("gcm_id").value("")
                        .endObject();
                QueryManager.getInstance().postRequest(Constant.URL + "login.php", jsonStringer.toString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handleResult(null);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        handleResult(response);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (emailStr.isEmpty()) {
            MyDynamicToast.errorMessage(LoginActivity.this, "Enter your Email/Mobile Number.");
        } else if (passwordStr.isEmpty()) {
            MyDynamicToast.errorMessage(LoginActivity.this, "Enter your password.");
        }
    }

    private void handleResult(final Response response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
/*{"responseMessage":"Login successfully","userdetail":{"user_id":"1","user_name":"manish","user_email":"manish@gmail.com",
"user_phone":"5345345","register_date":"2017-02-09 07:58:44"},"responseCode":200}*/
                if (response != null) {
                    try {
                        String responce = response.body().string();
                        Log.e("login", "responce " + responce);

                        JSONObject responceObject = new JSONObject(responce);

                        if (responceObject.getString("responseCode").equals("200")) {
                            JSONObject detail = responceObject.getJSONObject("userdetail");
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.USER_Id, detail.getString("user_id"));
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.NAME, detail.getString("user_name"));
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.EMAIL, detail.getString("user_email"));
                            PreferenceConnector.writeString(LoginActivity.this, PreferenceConnector.NUMBER, detail.getString("user_phone"));
                            loginToCometChat(mEmailEdt.getText().toString(), mPasswordEdt.getText().toString().trim(), detail.getString("user_id"));
                        } else {
                            MyDynamicToast.errorMessage(LoginActivity.this, responceObject.getString("responseMessage"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyDynamicToast.errorMessage(LoginActivity.this, getString(R.string.wrong));
                    }
                } else {
                    Log.e("Login", "Responce null");
                }
            }
        });
    }

    private void loginToCometChat(String email, String password, final String userId) {
        String emailStr = mEmailEdt.getText().toString().trim();
        String passwordStr = mPasswordEdt.getText().toString().trim();
        cometchat.login(URL_CHAT, emailStr, passwordStr, new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                PreferenceConnector.writeBoolean(LoginActivity.this, PreferenceConnector.IS_LOGIN, true);
                startService(new Intent(LoginActivity.this, SetReminderService.class).putExtra("user_id", userId));
                startActivity(new Intent(LoginActivity.this, CameraActivity.class));
                finish();
                MyDynamicToast.successMessage(LoginActivity.this, "Login successfully.");
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                PreferenceConnector.writeBoolean(LoginActivity.this, PreferenceConnector.IS_LOGIN, false);
                MyDynamicToast.errorMessage(LoginActivity.this, jsonObject.toString());
            }
        });
    }

    private void getMyToDoList() {

        /*{"user_id":"1"} */
        try {
            JSONStringer jsonStringer = new JSONStringer().object()
                    .key("user_id").value(PreferenceConnector.readString(LoginActivity.this, PreferenceConnector.USER_Id, ""))
                    .endObject();
            QueryManager.getInstance().postRequest(Constant.URL + "get_user_reminder.php", jsonStringer.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleServerResult("");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resultStr = response.body().string();
                    //Log.e("To DO list ", resultStr);
                    handleServerResult(resultStr);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleServerResult(final String resultStr) {
        if (!isDestroyed()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isDestroyed()) {
                        if (resultStr != null && !resultStr.isEmpty()) {
                            try {
                                ReminderBean reminderBean = new Gson().fromJson(resultStr, ReminderBean.class);
                                if (reminderBean.responseCode.equals("200")) {


                                    if (reminderBean.all_user_reminder != null && reminderBean.all_user_reminder.size() != 0) {
                                        Collections.reverse(reminderBean.all_user_reminder);
                                        for (int i = 0; i < reminderBean.all_user_reminder.size(); i++) {
                                            try {
                                                ReminderBean.ReminderItem reminderItem = reminderBean.all_user_reminder.get(i);
                                                DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                                Date date1 = originalFormat.parse(reminderItem.reminder_date + " " + reminderItem.reminder_time);
                                                Calendar cal = Calendar.getInstance();
                                                cal.setTime(date1);
                                                long mills = cal.getTimeInMillis();
                                                long currentDateTime = new Date().getTime();
                                                if (currentDateTime < mills) {
                                                    Utility.setAlarm(LoginActivity.this, mills, reminderItem.reminder_title);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {

                                    }
                                } else {

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {

                        }
                    }
                }
            });
        }
    }

    private void onRegisterNowClick() {
        startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        finish();
    }

    private void setImageAnim() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View view = findViewById(R.id.logo);
            view.setTransitionName(getString(R.string.activity_image_trans));
        }*/
    }

    private void visibleBackground() {
       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.bottomPanel).setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.white));
            }
        }, 800);*/
    }
}