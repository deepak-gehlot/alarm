package rudiment.alaramapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.cometchat.sdk.MessageSDK;
import com.inscripts.interfaces.Callbacks;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestChatActivity extends AppCompatActivity {

    Button btn, loginBtn;
    File file;
    CometChat cometchat;
    Bitmap bitmap;
    public static final String URL = "http://88.99.188.221/cometchat/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_chat);

        cometchat = CometChat.getInstance(TestChatActivity.this, getString(R.string.chat_api_key));
        cometchat.setCometChatUrl(URL);

        Drawable drawable = getResources().getDrawable(R.drawable.profile);
        bitmap = ((BitmapDrawable) drawable).getBitmap();
        file = bitmapToFile(bitmap);
        btn = (Button) findViewById(R.id.btn);
        loginBtn = (Button) findViewById(R.id.loginBtn);




        loginBtn.setEnabled(true);
        btn.setEnabled(true);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        EditText email = (EditText) findViewById(R.id.email);
        EditText pass = (EditText) findViewById(R.id.pass);
        loginTOCometChat(email.getText().toString().trim(), pass.getText().toString().trim());
    }

    private void register() {
        EditText email = (EditText) findViewById(R.id.email);
        EditText pass = (EditText) findViewById(R.id.pass);
        createUser(email.getText().toString().trim(), pass.getText().toString().trim());
    }

    private void createUser(String email, String pass) {
        final ProgressDialog progressDialog = ProgressDialog.show(TestChatActivity.this, "", "", false, false);
        cometchat.createUser(email, pass, email, "", file, new Callbacks() {
            @Override
            public void successCallback(JSONObject response) {
                progressDialog.dismiss();
                Log.e("test", response.toString());
                Toast.makeText(TestChatActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failCallback(JSONObject response) {
                progressDialog.dismiss();
                Log.e("test", response.toString());
                Toast.makeText(TestChatActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startChat() {
        MessageSDK.launchCometChat(TestChatActivity.this, new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {

            }

            @Override
            public void failCallback(JSONObject jsonObject) {

            }
        });
    }

    private void loginTOCometChat(String email, String pass) {
        final ProgressDialog progressDialog = ProgressDialog.show(TestChatActivity.this, "", "", false, false);
        cometchat.login(URL, email, pass, new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                progressDialog.dismiss();
                Toast.makeText(TestChatActivity.this, jsonObject.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                progressDialog.dismiss();
                Log.e("test", jsonObject.toString());
                Toast.makeText(TestChatActivity.this, jsonObject.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static File bitmapToFile(Bitmap bitmap) {
        File outFile = getImageFilePNG();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outFile;
    }

    public static File getImageFilePNG() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/any name folder");
        dir.mkdirs();

        String fileName = String.format("%d.png", System.currentTimeMillis());
        return new File(dir, fileName);
    }
}
