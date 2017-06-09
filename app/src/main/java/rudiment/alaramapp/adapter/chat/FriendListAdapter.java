package rudiment.alaramapp.adapter.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rudiment.alaramapp.R;
import rudiment.alaramapp.bean.FriendListResponse;
import rudiment.alaramapp.util.OnItemClickListener;

/**
 * Created by RWS 6 on 6/9/2017.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ItemHolder> {

    private Context context;
    private ArrayList<FriendListResponse.User> friendList;
    private OnItemClickListener onItemClickListener;

    public FriendListAdapter(Context context, ArrayList<FriendListResponse.User> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_list_row, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, final int position) {
        final FriendListResponse.User user = friendList.get(position);
        itemHolder.nameTxt.setText(user.displayName);

        itemHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position, friendList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    class ItemHolder extends RecyclerView.ViewHolder {

        private ImageView profileImage;
        private TextView nameTxt;
        private View view;

        public ItemHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            nameTxt = (TextView) itemView.findViewById(R.id.nameTxt);
            profileImage = (ImageView) itemView.findViewById(R.id.profileImg);
        }
    }
}