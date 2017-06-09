package rudiment.alaramapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.jksiezni.permissive.PermissionsGrantedListener;
import com.github.jksiezni.permissive.PermissionsRefusedListener;
import com.github.jksiezni.permissive.Permissive;
import com.github.jksiezni.permissive.PermissiveMessenger;
import com.github.jksiezni.permissive.Rationale;
import com.google.gson.Gson;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import rudiment.alaramapp.R;
import rudiment.alaramapp.activity.ChatScreenActivity;
import rudiment.alaramapp.adapter.ContactAdapter;
import rudiment.alaramapp.bean.ContactBean;
import rudiment.alaramapp.bean.ContactResponceBean;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Callback;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.OnItemClickListener;
import rudiment.alaramapp.util.Utility;

import static rudiment.alaramapp.util.Constant.URL_CHAT;

public class ContactListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mMyToDoRecyclerView;
    private RelativeLayout mParentLayout;
    private TextView mMsgTxt;
    private ProgressBar mProgressBar;

    private static final String PROGRESS = "progress";
    private static final String MESSAGE = "message";
    private static final String LIST = "list";

    private ArrayList<ContactBean> contactList;
    Cursor cursor;
    int counter = 0;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;
    CometChat cometchat;

    public ContactListFragment() {
        // Required empty public constructor
    }

    public static ContactListFragment newInstance() {
        ContactListFragment fragment = new ContactListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_to_do_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cometchat = CometChat.getInstance(getActivity(), getString(R.string.chat_api_key));
        cometchat.setCometChatUrl(URL_CHAT);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        askContactPermission();

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void init(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mMyToDoRecyclerView = (RecyclerView) view.findViewById(R.id.myToDoRecyclerView);
        mParentLayout = (RelativeLayout) view.findViewById(R.id.to_do_list_parent);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mMsgTxt = (TextView) view.findViewById(R.id.msgTxt);
    }

    /**
     * set list to recycler view
     *
     * @param list
     */
    private void setContactList(final ArrayList<ContactBean> list) {
        show(LIST);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mMyToDoRecyclerView.setLayoutManager(linearLayoutManager);
        ContactAdapter contactAdapter = new ContactAdapter(getActivity(), list);
        mMyToDoRecyclerView.setAdapter(contactAdapter);
        contactAdapter.setOnItemClick(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object obj) {
                startActivity(new Intent(getActivity(), ChatScreenActivity.class)
                        .putExtra("user_id", list.get(pos).numbers.get(0).userId)
                        .putExtra("user_name", list.get(pos).name));
            }
        });

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

    @Override
    public void onRefresh() {
        askContactPermission();
    }

    /**
     * ask contact permission to read device contact
     */
    private void askContactPermission() {
        new Permissive.Request(Manifest.permission.READ_CONTACTS)
                .withRationale(new Rationale() {
                    @Override
                    public void onShowRationale(Activity activity, String[] allowablePermissions, final PermissiveMessenger messenger) {
                        new AlertDialog.Builder(activity)
                                .setTitle("Alert !")
                                .setMessage("Contact permission required to read contact.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        messenger.repeatRequest();
                                    }
                                })
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog1) {
                                        messenger.repeatRequest();
                                    }
                                })
                                .show();
                    }
                })
                .whenPermissionsGranted(new PermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(String[] permissions) throws SecurityException {
                        // given permissions are granted
                        pDialog = new ProgressDialog(getActivity());
                        pDialog.setMessage("Reading contacts...");
                        pDialog.setCancelable(false);
                        pDialog.show();

                        updateBarHandler = new Handler();

                        // Since reading contacts takes more time, let's run it on a separate thread.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String json = getContacts();
                                if (json != null && !json.isEmpty()) {
                                    checkIsRegisterOnServer(json);
                                } else {
                                    Utility.showToast(getActivity(), getString(R.string.wrong));
                                }
                            }
                        }).start();
                    }
                })
                .whenPermissionsRefused(new PermissionsRefusedListener() {
                    @Override
                    public void onPermissionsRefused(String[] permissions) {
                        // given permissions are refused
                        Utility.showToast(getActivity(), getString(R.string.contact_permission_rejected_msg));
                    }
                })
                .execute(getActivity());
    }

    /**
     * fetch device contact and write in json formate
     *
     * @return json string of all contact
     */
    public String getContacts() {

        if (contactList != null) {
            contactList.clear();
        } else {
            contactList = new ArrayList<>();
        }

        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        ContactBean contactBean;

        ContentResolver contentResolver = getActivity().getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {

            counter = 0;
            while (cursor.moveToNext()) {
                contactBean = new ContactBean();

                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
                    }
                });

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    contactBean.name = name;

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    ArrayList<ContactBean.Number> numbersItem = new ArrayList<>();
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        ContactBean.Number number = new ContactBean.Number();
                        number.isRegistered = "0";
                        number.phone = phoneNumber.replaceAll(" ", "");
                        numbersItem.add(number);
                    }
                    contactBean.numbers = numbersItem;

                    phoneCursor.close();

                    // Read every email id associated with the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);

                    ArrayList<String> emailsItem = new ArrayList<>();
                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        emailsItem.add(email);
                    }
                    //   contactBean.emails = emailsItem;
                    emailCursor.close();
                }

                // Add the contact to the ArrayList
                contactList.add(contactBean);
            }
        }

        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < contactList.size(); i++) {
                ContactBean bean = contactList.get(i);
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("name", bean.name);

                JSONArray numberArr = new JSONArray();
                if (bean.numbers != null && bean.numbers.size() != 0) {
                    for (int j = 0; j < bean.numbers.size(); j++) {
                        JSONObject n = new JSONObject();
                        n.put("phone", bean.numbers.get(j).phone);
                        numberArr.put(n);
                    }
                } else {
                    JSONObject n = new JSONObject();
                    n.put("phone", "");
                    numberArr.put(n);
                }
                jsonObject1.put("numbers", numberArr);
                //   jsonObject1.put("emails", emailArr);
                jsonArray.put(jsonObject1);
            }

            jsonObject.put("data", jsonArray);
            Log.e("result list", jsonObject.toString());
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * send all device contact to server and check which one is registered.
     *
     * @param contactJson device contact json
     */
    private void checkIsRegisterOnServer(String contactJson) {
        QueryManager.getInstance().postRequest(Constant.URL + "check_registered_contact.php", contactJson, new Callback() {
            @Override
            public void onResult(String result) {
                pDialog.cancel();
                if (result != null && !result.isEmpty()) {
                    ContactResponceBean contactResponceBean = new Gson().fromJson(result, ContactResponceBean.class);
                    if (contactResponceBean.responseCode.equals("200")) {
                        setContactList(contactResponceBean.data);
                        addFriends(contactResponceBean);
                    } else {
                        Utility.showToast(getActivity(), contactResponceBean.responseMessage);
                    }
                } else {
                    Utility.showToast(getActivity(), getString(R.string.wrong));
                }
            }
        });
    }

    private void addFriends(ContactResponceBean responceBean) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < responceBean.data.size(); i++) {
            ContactBean item = responceBean.data.get(i);
            for (int j = 0; j < item.numbers.size(); j++) {
                if (item.numbers.get(j).isRegistered.equals("1")) {
                    jsonArray.put(item.numbers.get(j).userId);
                }
            }
        }
        if (jsonArray.length() != 0) {
            cometchat.addFriends(jsonArray, new Callbacks() {
                @Override
                public void successCallback(JSONObject jsonObject) {
                  //  Toast.makeText(getActivity(), jsonObject.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failCallback(JSONObject jsonObject) {
                //    Toast.makeText(getActivity(), jsonObject.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void sendMessage() {

    }
}