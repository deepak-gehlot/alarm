package rudiment.alaramapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;
import com.inscripts.interfaces.SubscribeCallbacks;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import rudiment.alaramapp.R;
import rudiment.alaramapp.adapter.chat.ChatAdapter;
import rudiment.alaramapp.bean.ChatResponse;

import static rudiment.alaramapp.util.Constant.TYPING;
import static rudiment.alaramapp.util.Constant.URL_CHAT;

public class ChatScreenActivity extends AppCompatActivity {

    private CometChat cometchat;
    private String otherUserId = "", userNameStr = "";
    private EditText msgEdt;
    RecyclerView recyclerView;
    private ArrayList<ChatResponse.Message> messages;
    TextView userNameTxt, typingTxt;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        init();
        handleIntent();
        getAllMessages();
        setTitle();


        flag = true;
      /*  msgEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (flag) {
                    flag = false;
                    try {
                        cometchat.sendMessage(otherUserId, TYPING, new Callbacks() {
                            @Override
                            public void successCallback(JSONObject jsonObject) {

                            }

                            @Override
                            public void failCallback(JSONObject jsonObject) {

                            }
                        });
                        Timer timer = new Timer();

						*//* Send stop typing message after 5 seconds *//*
                        timer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                flag = true;
                            }
                        }, 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });*/
    }

    private void setTitle() {
        userNameTxt.setText(userNameStr);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * show hide typing indicator
     *
     * @param flag if true show typing else hide
     */
    private void showHideTyping(final boolean flag) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flag) {
                    typingTxt.setVisibility(View.VISIBLE);
                    typingTxt.animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                        }
                    });
                } else {
                    typingTxt.animate().alpha(0.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            typingTxt.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        cometchat.subscribe(false, new SubscribeCallbacks() {
            @Override
            public void gotOnlineList(JSONObject jsonObject) {

            }

            @Override
            public void gotBotList(JSONObject jsonObject) {

            }

            @Override
            public void onError(JSONObject jsonObject) {

            }

            @Override
            public void onMessageReceived(JSONObject jsonObject) { //self = 0 means message is by other user
                ChatResponse.Message message = new Gson().fromJson(jsonObject.toString(), ChatResponse.Message.class);
                if (message.self.equals("0")) {
                    if (message.message.equals(TYPING)) {
                        showHideTyping(true);
                        Timer timer = new Timer();
                        /* Send stop typing message after 5 seconds */
                        timer.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                showHideTyping(false);
                            }
                        }, 1500);
                    } else {
                        typingTxt.setVisibility(View.GONE);
                        messages.add(message);
                        recyclerView.getAdapter().notifyItemInserted(messages.size());
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                }
            }

            @Override
            public void gotProfileInfo(JSONObject jsonObject) {
                Toast.makeText(ChatScreenActivity.this, jsonObject.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void gotAnnouncement(JSONObject jsonObject) {

            }

            @Override
            public void onActionMessageReceived(JSONObject jsonObject) {

            }
        });
    }

    private void init() {
        cometchat = CometChat.getInstance(ChatScreenActivity.this, getString(R.string.chat_api_key));
        cometchat.setCometChatUrl(URL_CHAT);

        userNameTxt = (TextView) findViewById(R.id.userName);
        typingTxt = (TextView) findViewById(R.id.typingTxt);
        msgEdt = (EditText) findViewById(R.id.msgEdt);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatScreenActivity.this));
    }

    private void handleIntent() {
        Bundle bundle = getIntent().getExtras();
        otherUserId = bundle.getString("user_id");
        userNameStr = bundle.getString("user_name");
    }

    private void getAllMessages() {
        final ProgressDialog progressDialog = ProgressDialog.show(ChatScreenActivity.this, "", "loading message...", false, false);
        long userId = Long.parseLong(otherUserId);
        long msgId = -1;
        cometchat.getChatHistory(userId, msgId, new Callbacks() {
            @Override
            public void successCallback(final JSONObject jsonObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        ChatResponse chatResponse = new Gson().fromJson(jsonObject.toString(), ChatResponse.class);
                        setList(chatResponse.messages);
                    }
                });
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                Log.e("", jsonObject.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }


    public void sendMessage(View view) {
        String msg = msgEdt.getText().toString().trim();
        msgEdt.setText("");
        if (msg.isEmpty()) {
            return;
        }
        cometchat.sendMessage(otherUserId, msg, new Callbacks() {
            @Override
            public void successCallback(final JSONObject jsonObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ChatResponse.Message message = new ChatResponse.Message();
                            message.id = jsonObject.getString("id");
                            message.message = jsonObject.getString("m");
                            message.from = jsonObject.getString("from");
                            message.self = "1";
                            messages.add(message);
                            recyclerView.getAdapter().notifyItemInserted(messages.size());
                            recyclerView.scrollToPosition(messages.size() - 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                Toast.makeText(ChatScreenActivity.this, jsonObject.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setList(ArrayList<ChatResponse.Message> messages) {
        this.messages = messages;
        ChatAdapter chatAdapter = new ChatAdapter(ChatScreenActivity.this, this.messages);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.scrollToPosition(messages.size() - 1);
    }
}