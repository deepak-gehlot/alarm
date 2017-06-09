package rudiment.alaramapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rudiment.alaramapp.R;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.Extension;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;
import rudiment.alaramapp.util.ValidationTemplate;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mNameEdt, mEmailEdt, mPhoneEdt, mPasswordEdt, mConfirmPasswordEdt;
    private RelativeLayout mParentLayout;
    private ProgressDialog progressDialog;

    private Extension extension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        init();
    }

    private void init() {
        mNameEdt = (EditText) findViewById(R.id.nameEdt);
        mEmailEdt = (EditText) findViewById(R.id.emailEdt);
        mPhoneEdt = (EditText) findViewById(R.id.phoneEdt);
        mPasswordEdt = (EditText) findViewById(R.id.passwordEdt);
        mConfirmPasswordEdt = (EditText) findViewById(R.id.confirmPasswordEdt);
        mParentLayout = (RelativeLayout) findViewById(R.id.activity_registration);

        extension = new Extension();
        findViewById(R.id.loginBtn).setOnClickListener(this);
        findViewById(R.id.registrationBtn).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //need to manage this one
        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginBtn:
                onBackPressed();
                break;
            case R.id.registrationBtn:
                attemptSubmit();
                break;
        }
    }

    private void attemptSubmit() {
        String name = mNameEdt.getText().toString().trim();
        String email = mEmailEdt.getText().toString().trim();
        String phone = mPhoneEdt.getText().toString().trim();
        String password = mPasswordEdt.getText().toString().trim();
        String confirmPasswordEdt = mConfirmPasswordEdt.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPasswordEdt.isEmpty()) {
            Utility.showMessage(mParentLayout, getString(R.string.all_required));
        } else if (!extension.executeStrategy(RegistrationActivity.this, email, ValidationTemplate.EMAIL)) {
            Utility.showMessage(mParentLayout, getString(R.string.invalid_email));
        } else if (!extension.executeStrategy(RegistrationActivity.this, phone, ValidationTemplate.isnumber)) {
            Utility.showMessage(mParentLayout, getString(R.string.invalid_number));
        } else if (!extension.executeStrategy(RegistrationActivity.this, "", ValidationTemplate.INTERNET)) {
            Utility.showMessage(mParentLayout, getString(R.string.all_required));
        } else if (!password.equals(confirmPasswordEdt)) {
            Utility.showMessage(mParentLayout, getString(R.string.password_mismatch));
        } else {
            sendToServer(name, email, phone, password);
        }
    }

    private void sendToServer(String name, String email, String phone, String password) {
        /*{"user_name":"manish","user_email":"manish@gmail.com","user_phone":"5345345","user_password":"234234"}*/
        progressDialog = ProgressDialog.show(RegistrationActivity.this, "Wait", "Registering...", false, false);
        try {
            JSONStringer jsonStringer = new JSONStringer().object()
                    .key("user_name").value(name)
                    .key("user_email").value(email)
                    .key("user_phone").value(phone)
                    .key("user_password").value(password)
                    .endObject();
            QueryManager.getInstance().postRequest(Constant.URL + "regisration.php", jsonStringer.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleResult("");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleResult(response.body().string());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }
    }

    private void handleResult(final String resultStr) {
        /*

{"responseMessage":"Registration successfully.","userdetail":{"user_id":"1","user_email":"manish@gmail.com",
     "user_phone":"5345345","register_date":"2017-02-09 07:58:44"},"responseCode":200}*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                if (resultStr != null && !resultStr.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(resultStr);
                        if (jsonObject.getString("responseCode").equals("200")) {
                            JSONObject userDataObj = jsonObject.getJSONObject("userdetail");
                            PreferenceConnector.writeString(RegistrationActivity.this, PreferenceConnector.USER_Id, userDataObj.getString("user_id"));
                            PreferenceConnector.writeString(RegistrationActivity.this, PreferenceConnector.EMAIL, userDataObj.getString("user_email"));
                            PreferenceConnector.writeString(RegistrationActivity.this, PreferenceConnector.NUMBER, userDataObj.getString("user_phone"));
                            PreferenceConnector.writeString(RegistrationActivity.this, PreferenceConnector.NAME, userDataObj.getString("user_name"));
                            PreferenceConnector.writeBoolean(RegistrationActivity.this, PreferenceConnector.IS_LOGIN, true);
                            startActivity(new Intent(RegistrationActivity.this, HomeActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utility.showMessage(mParentLayout, getString(R.string.wrong));
                    }
                } else {
                    Utility.showMessage(mParentLayout, getString(R.string.wrong));
                }
            }
        });
    }
}
