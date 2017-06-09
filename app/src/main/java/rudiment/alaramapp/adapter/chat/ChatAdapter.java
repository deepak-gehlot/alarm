package rudiment.alaramapp.adapter.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import rudiment.alaramapp.R;
import rudiment.alaramapp.bean.ChatResponse;

/**
 * Created by RWS 6 on 6/7/2017.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemHolder> {

    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private Context context;
    private ArrayList<ChatResponse.Message> messages;

    public ChatAdapter(Context context, ArrayList<ChatResponse.Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = null;
        if (i == RIGHT) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_row_right, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_row_left, viewGroup, false);
        }
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        itemHolder.messageTxt.setText(messages.get(i).message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).self.equals("1")) {
            return RIGHT;
        } else {
            return LEFT;
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        private View view;
        private TextView messageTxt;

        public ItemHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            messageTxt = (TextView) itemView.findViewById(R.id.messageTxt);
        }
    }
}
