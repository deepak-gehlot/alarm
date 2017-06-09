package rudiment.alaramapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.ShowReminderActivity;
import rudiment.alaramapp.adapter.ToDoAdapter;
import rudiment.alaramapp.bean.ReminderBean;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.OnItemClickListener;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

public class MyToDoListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mMyToDoRecyclerView;
    private RelativeLayout mParentLayout;
    private TextView mMsgTxt;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String PROGRESS = "progress";
    private static final String MESSAGE = "message";
    private static final String LIST = "list";

    public MyToDoListFragment() {
        // Required empty public constructor
    }

    public static MyToDoListFragment newInstance() {
        MyToDoListFragment fragment = new MyToDoListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_to_do_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
    }

    private void show(String msg) {
        switch (msg) {
            case PROGRESS:
                mProgressBar.setVisibility(View.VISIBLE);
                mMsgTxt.setVisibility(View.GONE);
                mMyToDoRecyclerView.setVisibility(View.GONE);
                break;
            case MESSAGE:
                mProgressBar.setVisibility(View.GONE);
                mMsgTxt.setVisibility(View.VISIBLE);
                mMyToDoRecyclerView.setVisibility(View.GONE);
                break;
            case LIST:
                mProgressBar.setVisibility(View.GONE);
                mMsgTxt.setVisibility(View.GONE);
                mMyToDoRecyclerView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void init(View view) {
        mMyToDoRecyclerView = (RecyclerView) view.findViewById(R.id.myToDoRecyclerView);
        mParentLayout = (RelativeLayout) view.findViewById(R.id.to_do_list_parent);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mMsgTxt = (TextView) view.findViewById(R.id.msgTxt);

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setmMyToDoRecyclerView(ArrayList<ReminderBean.ReminderItem> list) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mMyToDoRecyclerView.setLayoutManager(linearLayoutManager);
        ToDoAdapter toDoAdapter = new ToDoAdapter(getActivity(), list);
        mMyToDoRecyclerView.setAdapter(toDoAdapter);
        toDoAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object obj) {
                startActivity(new Intent(getActivity(), ShowReminderActivity.class).putExtra("reminder_id", ((ReminderBean.ReminderItem) obj).reminder_id));
            }
        });
    }

    private void getMyToDoList() {
        show(PROGRESS);
        /*{"user_id":"1"} */
        try {
            JSONStringer jsonStringer = new JSONStringer().object()
                    .key("user_id").value(PreferenceConnector.readString(getActivity(), PreferenceConnector.USER_Id, ""))
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
        if (getView() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null) {
                        if (resultStr != null && !resultStr.isEmpty()) {
                            try {
                                ReminderBean reminderBean = new Gson().fromJson(resultStr, ReminderBean.class);
                                if (reminderBean.responseCode.equals("200")) {
                                    if (reminderBean.all_user_reminder != null && reminderBean.all_user_reminder.size() != 0) {
                                        Collections.reverse(reminderBean.all_user_reminder);
                                        setmMyToDoRecyclerView(reminderBean.all_user_reminder);
                                      //setAllReminder(getActivity(), reminderBean.all_user_reminder);
                                        show(LIST);
                                    } else {
                                        mMsgTxt.setText(getSetReminderText());
                                        show(MESSAGE);
                                    }
                                } else {
                                    Utility.showMessage(mParentLayout, reminderBean.responseMessage);
                                    mMsgTxt.setText(getSetReminderText());
                                    show(MESSAGE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mMsgTxt.setText(getSetReminderText());
                                show(MESSAGE);
                            }
                        } else {
                            mMsgTxt.setText(getString(R.string.wrong));
                            show(MESSAGE);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        getMyToDoList();
        swipeRefreshLayout.setRefreshing(false);
    }

    /*text with image*/
    private SpannableStringBuilder getSetReminderText() {
        String str = "No Reminder set, you can Set Reminder by clicking  icon.";
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(str);
        ImageSpan playImageSpan = new ImageSpan(getActivity(), R.drawable.set_reminder);
        ssBuilder.setSpan(playImageSpan, // Span to add
                str.indexOf("clicking") + String.valueOf("clicking").length(), // Start of the span (inclusive)
                str.indexOf("clicking") + String.valueOf("clicking").length() + 1, // End of the span (exclusive)
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Do not extend the span when text add later
        );
        return ssBuilder;
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getMyToDoList();
            }
        }, 200);
    }
}