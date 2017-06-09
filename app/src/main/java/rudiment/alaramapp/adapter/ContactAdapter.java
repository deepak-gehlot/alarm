package rudiment.alaramapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import rudiment.alaramapp.R;
import rudiment.alaramapp.bean.ContactBean;
import rudiment.alaramapp.util.OnItemClickListener;

/**
 * Created by RWS 6 on 4/12/2017.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ItemHolder> {

    private Context context;
    private ArrayList<ContactBean> list;
    private OnItemClickListener onItemClick;

    public ContactAdapter(Context context, ArrayList<ContactBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, final int position) {
        ContactBean bean = list.get(position);

        holder.nameTxt.setText(bean.name);
        final UserStatus userStatus = checkNumber(bean.numbers);
        holder.numberTxt.setText(userStatus.phone.trim());
        holder.inviteBtn.setVisibility(userStatus.isRegistered.equals("0") ? View.VISIBLE : View.GONE);

        holder.inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(context, context.getString(R.string.wrong), userStatus.phone.trim());
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onItemClick(position, list.get(position));
            }
        });

    }


    public void setOnItemClick(OnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    /**
     * check is user registered or not
     *
     * @param numbers List of contact numbers of one user
     * @return UserStatus (isRegistered & number)
     */
    private UserStatus checkNumber(ArrayList<ContactBean.Number> numbers) {
        UserStatus userStatus = new UserStatus();
        for (ContactBean.Number number : numbers) {
            if (number.isRegistered.equals("1")) {
                userStatus.isRegistered = "1";
                userStatus.phone = number.phone;
                break;
            } else {
                userStatus.isRegistered = "0";
                userStatus.phone = numbers.get(0).phone;
            }
        }
        return userStatus;
    }

    public class UserStatus {
        public String isRegistered;
        public String phone;
    }

    /**
     * send message
     *
     * @param context
     * @param message
     * @param phoneNumber
     */
    private void sendMessage(Context context, String message, String phoneNumber) {
        Intent intentsms = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
        intentsms.putExtra("sms_body", message);
        try {
            context.startActivity(intentsms);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "No messaging app installed in device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        ImageView profileImg;
        TextView nameTxt, numberTxt;
        Button inviteBtn;
        View view;

        public ItemHolder(View view) {
            super(view);
            this.view = view;
            profileImg = (ImageView) view.findViewById(R.id.profileImg);
            nameTxt = (TextView) view.findViewById(R.id.nameTxt);
            numberTxt = (TextView) view.findViewById(R.id.numberTxt);
            inviteBtn = (Button) view.findViewById(R.id.inviteBtn);
        }
    }
}
