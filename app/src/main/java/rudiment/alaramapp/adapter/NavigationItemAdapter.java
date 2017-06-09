package rudiment.alaramapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rudiment.alaramapp.R;


/**
 * Created by RWS 6 on 3/15/2017.
 */

public class NavigationItemAdapter extends RecyclerView.Adapter<NavigationItemAdapter.ItemHolder> {

    public ArrayList<String> items;
    public ArrayList<Integer> itemsIcon;
    public OnNavigationClick onNavigationClick;

    public NavigationItemAdapter(ArrayList<String> items, ArrayList<Integer> itemsIcon) {
        this.items = items;
        this.itemsIcon = itemsIcon;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, final int position) {
        holder.textView.setText(items.get(position));
        holder.icon.setImageResource(itemsIcon.get(position));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigationClick.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnNavigationClickListener(OnNavigationClick onNavigationClick) {
        this.onNavigationClick = onNavigationClick;
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        TextView textView;
        ImageView icon;
        View view;

        public ItemHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.textView);
            icon = (ImageView) view.findViewById(R.id.icon);
            this.view = view;
        }
    }

    public interface OnNavigationClick {
        public void onClick(int position);
    }
}
