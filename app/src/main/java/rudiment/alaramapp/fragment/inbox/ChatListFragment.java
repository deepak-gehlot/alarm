package rudiment.alaramapp.fragment.inbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.ChatScreenActivity;
import rudiment.alaramapp.activity.ContactListActivity;
import rudiment.alaramapp.adapter.chat.FriendListAdapter;
import rudiment.alaramapp.bean.FriendListResponse;
import rudiment.alaramapp.bean.requestModel.FriendListRequestModel;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Callback;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.OnItemClickListener;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView startNewChatTxt;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        getFriendList();
    }

    private void init(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.friendList);
        startNewChatTxt = (TextView) view.findViewById(R.id.startNewChatTxt);


        startNewChatTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.startNewChatTxt:
                startActivity(new Intent(getActivity(), ContactListActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                break;
        }
    }

    /**
     * get chat thread list
     */
    private void getFriendList() {
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Loading...", false, false);
        FriendListRequestModel requestModel = new FriendListRequestModel();
        requestModel.setUserId(PreferenceConnector.readString(getActivity(), PreferenceConnector.USER_Id, ""));
        String requestJson = new Gson().toJson(requestModel, FriendListRequestModel.class);
        QueryManager.getInstance().postRequest(Constant.URL + "get_friendlist.php", requestJson, new Callback() {
            @Override
            public void onResult(String result) {
                progressDialog.dismiss();
                if (result != null && !result.isEmpty()) {
                    FriendListResponse friendListResponse = new Gson().fromJson(result, FriendListResponse.class);
                    if (friendListResponse.responseCode.equals("200")) {
                        if (friendListResponse.friendList != null) {
                            setList(friendListResponse.friendList);
                        } else {
                            Utility.showToast(getActivity(), getString(R.string.wrong));
                        }
                    } else {
                        Utility.showToast(getActivity(), friendListResponse.responseMessage);
                    }
                } else {
                    Utility.showToast(getActivity(), getString(R.string.wrong));
                }
            }
        });
    }

    /**
     * set list to recycler list and handle list item click
     */
    private void setList(ArrayList<FriendListResponse.User> friendList) {
        FriendListAdapter friendListAdapter = new FriendListAdapter(getActivity(), friendList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(friendListAdapter);
        friendListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object obj) {
                FriendListResponse.User user = (FriendListResponse.User) obj;
                startActivity(new Intent(getActivity(), ChatScreenActivity.class)
                        .putExtra("user_id", user.userId)
                        .putExtra("user_name", user.displayName));
            }
        });
    }
}
