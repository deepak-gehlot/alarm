package rudiment.alaramapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import rudiment.alaramapp.R;
import rudiment.alaramapp.services.ContactObserverService;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

public class SplashActivity extends AppCompatActivity {
    Handler handler;
    public static int SPLASHTIME = 3000;
    Message msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        PreferenceConnector.writeInteger(SplashActivity.this, PreferenceConnector.DEVICE_HEIGHT, height);
        PreferenceConnector.writeInteger(SplashActivity.this, PreferenceConnector.DEVICE_WIDTH, width);

        addHandler();
        msg = new Message();
        msg.what = 1;
        handler.sendMessageDelayed(msg, SPLASHTIME);


        if (!Utility.isMyServiceRunning(SplashActivity.this, ContactObserverService.class)) {
            startService(new Intent(SplashActivity.this, ContactObserverService.class));
        }
    }

    private void switchActivityToLogin() {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            handler.removeMessages(msg.what);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switchActivityToLogin();
                        }
                    });
                }
                return false;
            }
        });
    }
}
