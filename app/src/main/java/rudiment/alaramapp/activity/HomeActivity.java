package rudiment.alaramapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import rudiment.alaramapp.R;
import rudiment.alaramapp.fragment.ContactListFragment;
import rudiment.alaramapp.fragment.HistoryListFragment;
import rudiment.alaramapp.fragment.MyToDoListFragment;
import rudiment.alaramapp.fragment.inbox.InboxFragment;

import static rudiment.alaramapp.util.Constant.CONTACT_LIST_FRAGMENT;
import static rudiment.alaramapp.util.Constant.HISTORY_LIST_FRAGMENT;
import static rudiment.alaramapp.util.Constant.INBOX_FRAGMENT;
import static rudiment.alaramapp.util.Constant.MY_TO_DO_LIST_FRAGMENT;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    public static int currentPos = 2;

    RelativeLayout topPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
    }

    private void init() {
        findViewById(R.id.setReminderImgLayout).setOnClickListener(this);
        findViewById(R.id.groupLayout).setOnClickListener(this);
        findViewById(R.id.settingLayout).setOnClickListener(this);
        findViewById(R.id.myToDoLayout).setOnClickListener(this);
        findViewById(R.id.contactLayout).setOnClickListener(this);
        findViewById(R.id.historyLayout).setOnClickListener(this);
        topPanel = (RelativeLayout) findViewById(R.id.topPanel);
        addCurrentView(currentPos);
    }

    private void changeSelector(View view) {
        findViewById(R.id.groupLayout).setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimary));
        findViewById(R.id.settingLayout).setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimary));
        findViewById(R.id.myToDoLayout).setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimary));
        findViewById(R.id.contactLayout).setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimary));
        findViewById(R.id.historyLayout).setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimary));

        view.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimaryDark));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(HomeActivity.this, CameraActivity.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setReminderImgLayout:
                onSetReminderClick();
                break;
            case R.id.groupLayout:
                addCurrentView(INBOX_FRAGMENT);
                break;
            case R.id.settingLayout:
                break;
            case R.id.contactLayout:
                addCurrentView(CONTACT_LIST_FRAGMENT);
                break;
            case R.id.myToDoLayout:
                addCurrentView(MY_TO_DO_LIST_FRAGMENT);
                break;
            case R.id.historyLayout:
                addCurrentView(HISTORY_LIST_FRAGMENT);
                break;
        }
    }

    private void addCurrentView(int pos) {
        topPanel.setVisibility(View.VISIBLE);
        switch (pos) {
            case INBOX_FRAGMENT:
                topPanel.setVisibility(View.GONE);
                changeSelector(findViewById(R.id.groupLayout));
                addFragment(InboxFragment.newInstance(), "InboxFragment", HomeActivity.this);
                break;
            case 1:

                break;
            case MY_TO_DO_LIST_FRAGMENT:
                changeSelector(findViewById(R.id.myToDoLayout));
                addFragment(MyToDoListFragment.newInstance(), "MyToDoListFragment", HomeActivity.this);
                break;
            case HISTORY_LIST_FRAGMENT:
                changeSelector(findViewById(R.id.historyLayout));
                addFragment(HistoryListFragment.newInstance(), "HistoryListFragment", HomeActivity.this);
                break;
            case CONTACT_LIST_FRAGMENT:
                changeSelector(findViewById(R.id.contactLayout));
                addFragment(ContactListFragment.newInstance(), "ContactListFragment", HomeActivity.this);
                break;
        }
    }

    private void onSetReminderClick() {
        startActivity(new Intent(HomeActivity.this, CameraActivity.class));
    }

    public static void addFragment(Fragment fragment, String tag, AppCompatActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root_frame, fragment, tag);
        //  fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }
}
