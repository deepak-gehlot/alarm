package rudiment.alaramapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;

import rudiment.alaramapp.R;
import rudiment.alaramapp.bean.ReminderBean;
import rudiment.alaramapp.util.OnItemClickListener;

/**
 * Created by RWS 6 on 2/6/2017.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ReminderBean.ReminderItem> reminderItems;
    private AQuery aQuery;
    private Context context;
    OnItemClickListener itemClickListener;
    ArrayList<String> imageList = new ArrayList<>();

    public HistoryAdapter(Context context, ArrayList<ReminderBean.ReminderItem> reminderItems) {
        this.reminderItems = reminderItems;
        aQuery = new AQuery(context);
        this.context = context;
        getAllImages(reminderItems);
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView mReminderImg;
        final TextView mDescriptionTxt;

        public ItemHolder(View view) {
            super(view);
            this.view = view;
            mReminderImg = (ImageView) view.findViewById(R.id.remiderImg);
            mDescriptionTxt = (TextView) view.findViewById(R.id.descriptionTxt);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder1, final int position) {
        ItemHolder holder = (ItemHolder) holder1;
        final ReminderBean.ReminderItem reminderItem = reminderItems.get(position);
        aQuery.id(holder.mReminderImg).image(reminderItem.reminder_image, true, true, 70, R.drawable.no_image_available);
        holder.mDescriptionTxt.setText(reminderItem.reminder_description);

        holder.mReminderImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDialog(context, reminderItem.reminder_image, position);
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   itemClickListener.onItemClick(position, reminderItem);
            }
        });
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return reminderItems.size();
    }

    private void getAllImages(ArrayList<ReminderBean.ReminderItem> reminderItems) {
        if (reminderItems != null && reminderItems.size() > 0) {
            for (int i = 0; i < reminderItems.size(); i++) {
                final ReminderBean.ReminderItem reminderItem = reminderItems.get(i);
                String s = reminderItem.reminder_image;
                imageList.add(s);
            }
        }
    }

    private void setDialog(Context context, String url, int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_full_image_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView closeBtn = (ImageView) dialog.findViewById(R.id.closeBtn);
        ViewPager viewPager = (ViewPager) dialog.findViewById(R.id.view_pager);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

        ViewPagerAdapter adapterView = new ViewPagerAdapter(context, imageList);
        viewPager.setAdapter(adapterView);
        viewPager.setCurrentItem(position);
    }

    public class ViewPagerAdapter extends PagerAdapter {
        Context mContext;
        ArrayList<String> mList = new ArrayList<>();

        ViewPagerAdapter(Context context, ArrayList<String> list) {
            this.mContext = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View v, Object obj) {
            return v == ((ImageView) obj);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int i) {
            ImageView mImageView = new ImageView(mContext);
            //mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //mImageView.setImageResource(mList.get(i));
            AQuery aQuery = new AQuery(context);
            aQuery.id(mImageView).image(mList.get(i), true, true, 0, R.drawable.no_image_available);
            ((ViewPager) container).addView(mImageView, 0);
            return mImageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int i, Object obj) {
            ((ViewPager) container).removeView((ImageView) obj);
        }
    }
}
